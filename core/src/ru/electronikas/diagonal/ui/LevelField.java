package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Dir;
import ru.electronikas.diagonal.model.action.ActType;
import ru.electronikas.diagonal.model.action.DiAction;
import ru.electronikas.diagonal.ui.menu.GameOverMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * LevelField — owns the board grid background + active cell actors + the StaticPanel above.
 *
 * BUGFIX (panel-redesign): the previous code computed `size` as a LOCAL variable in
 * createFields() and only synced it to CellModel.size inside the CellModel constructor.
 * This meant that:
 *   - The grid background was drawn with the correct size for the current FIELD_SIZE.
 *   - But CellModel.size was being recomputed on every new CellModel(pos, value) call,
 *     which is normally the same value — UNLESS the board was rebuilt by undo() without
 *     going through createFields(), in which case CellModel.size could drift if FIELD_SIZE
 *     had changed (e.g. via SelectBox in SettingsMenu) without a full recreate.
 *
 * Fix: centralize the metric recomputation in {@link #recomputeMetrics()} and call it
 *   - once in the constructor (before createFields)
 *   - before every applyUndoActions()
 * DY now derives from StaticPanel.PANEL_HEIGHT + a fixed gap, so the field always
 * sits a consistent distance below the panel regardless of FIELD_SIZE.
 */
public class LevelField {

    /** Vertical offset of the board's bottom edge from the bottom of the screen. */
    public static float DY;

    public static List<CellModel> cells;
    private DiGameModel diGameModel;
    private Stage stage;
    private StaticPanel staticPanel;
    private BottomActionBar bottomActionBar;
    private volatile boolean isPause = false;
    private GameOverMenu gameOverMenu;

    /**
     * P1-fix: public read-accessor for the pause flag. Used by DiGestureListener
     * to short-circuit fling/pan events while the SettingsMenu (or GameOverMenu)
     * overlay is open. Without this check the game keeps advancing underneath
     * the settings panel — the player closes the panel and finds the board
     * has 'teleported' because DiGameModel.onMove() mutated cells/score even
     * though applyActions() early-returned on the visual side.
     */
    public boolean isPaused() {
        return isPause;
    }

    /**
     * P1-fix: external pause control. Called by SettingsMenu.animateOpen() /
     * animateHide() so the gesture listener can be neutralised while the
     * settings overlay is on screen.
     *
     * Note: this does NOT affect the GameOverMenu path — that one toggles
     * isPause directly via the GameOverAction handler in applyActions(), and
     * is unaffected by this method.
     */
    public void setPaused(boolean paused) {
        this.isPause = paused;
    }

    public void hideGameOverMenu() {
        if(gameOverMenu!=null) {
            gameOverMenu.animateHide();
        }
    }

    public LevelField(DiGameModel diGameModel, Stage stage) {
        this.diGameModel = diGameModel;
        this.stage = stage;

        Gdx.app.log("LevelField", "ctor start, FIELD_SIZE=" + DiGameModel.FIELD_SIZE);
        
        // Clear any previous cells list to avoid duplicates on resize
        if (cells != null) {
            for (CellModel cm : cells) {
                if (cm.cell != null && cm.cell.getStage() != null) {
                    cm.cell.remove();
                }
            }
        }
        
        // Recompute CellModel.size + DY FIRST, so both createFields() and the
        // StaticPanel constructor see consistent values.
        recomputeMetrics();
        Gdx.app.log("LevelField", "metrics ready: CellModel.size=" + CellModel.size + " DY=" + DY);

        createFields();
        Gdx.app.log("LevelField", "createFields done");

        try {
            staticPanel = new StaticPanel(stage, diGameModel);
            Gdx.app.log("LevelField", "StaticPanel ready");
        } catch (Throwable t) {
            Gdx.app.error("LevelField", "StaticPanel FAILED", t);
            // Don't crash the whole game — let the board be playable even without
            // the top HUD. The user will see a board but no score/record/undo UI.
            // This is a defensive measure to prevent the 'black screen + immediate
            // close' reported on some devices.
        }

        try {
            // P1-fix: add a BottomActionBar between the board and the ad banner
            // so the empty gap at the bottom is filled with useful controls
            // (New Game + field-size switcher).
            bottomActionBar = new BottomActionBar(stage);
            Gdx.app.log("LevelField", "BottomActionBar ready");
        } catch (Throwable t) {
            Gdx.app.error("LevelField", "BottomActionBar FAILED", t);
            // Don't crash the whole game if the bottom bar fails — just log and continue.
            // The board + top panel are still usable.
        }

        cells = new ArrayList<CellModel>();
        applyActions(diGameModel.onMove(Dir.none, true));
        Gdx.app.log("LevelField", "ctor done");
    }

    /**
     * Centralize all metric calculations that depend on FIELD_SIZE / screen size.
     * Called from the constructor and from applyUndoActions() (defensive: the
     * metrics should not change during undo, but the call is cheap and protects
     * against future refactors).
     *
     *   CellModel.size = screen width / FIELD_SIZE
     *   DY             = board_bottom (anchored above the bottom action bar + ad banner)
     *
     * The board is CENTERED vertically in the available space between the
     * StaticPanel (top) and the BottomActionBar + ad banner (bottom), so the
     * empty gap is split evenly above and below the board instead of all
     * piling up at the bottom.
     */
    public static void recomputeMetrics() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        // Cell size: width-based, so the board spans the full screen width.
        CellModel.size = sw / DiGameModel.FIELD_SIZE;

        // Top constraint: bottom edge of the StaticPanel.
        float panelBottom = sh - sh * StaticPanel.PANEL_TOP_FRACTION;
        float topGap = sh * StaticPanel.FIELD_TOP_GAP_FRACTION;
        float availableTop = panelBottom - topGap;

        // Bottom constraint: top edge of the BottomActionBar (which itself sits
        // on top of the ad banner reserve).
        float bottomReserve = sh * (BottomActionBar.BOTTOM_BAR_HEIGHT_FRACTION
                + BottomActionBar.AD_BANNER_RESERVE_FRACTION);
        float bottomGap = sh * StaticPanel.FIELD_TOP_GAP_FRACTION; // symmetric gap
        float availableBottom = bottomReserve + bottomGap;

        // Vertical space available for the board itself.
        float availableHeight = availableTop - availableBottom;
        float boardHeight = CellModel.size * DiGameModel.FIELD_SIZE;

        // Center the board in the available vertical space. If the board is
        // taller than the available space (very small screens / large FIELD_SIZE),
        // DY can go negative — clamp it so the board never gets pushed below the
        // ad banner reserve.
        DY = availableBottom + Math.max(0f, (availableHeight - boardHeight) / 2f);
        // Defensive: never let the board overlap the ad banner reserve.
        if (DY < bottomReserve) {
            DY = bottomReserve;
        }
    }

    private void createFields() {
        float size = CellModel.size; // already set by recomputeMetrics()
        for(int x=0; x < DiGameModel.FIELD_SIZE; x++) {
            for(int y=0; y < DiGameModel.FIELD_SIZE; y++) {
                Image img = new Image(Di2048Game.game.getUiSkin().getPatch("graypane"));
                img.setPosition(x * size, y * size + DY);
                img.setSize(size,size);
                stage.addActor(img);
            }
        }
    }

    public void applyActions(List<DiAction> stepActions) {
        if(isPause & !stepActions.get(0).type().equals(ActType.gameContinue)) return;

        for(DiAction diAction : stepActions) {

            switch (diAction.type()) {
                case newCell:
                    CellModel cellModel = new CellModel(diAction.newPos(), diAction.getValue());
                    cells.add(cellModel);
                    cellModel.fadeInCell();
                    stage.addActor(cellModel.cell);
                    break;

                case move:
                    CellModel cell = diAction.cellModel();
                    cell.moveToNewPos();
                    break;

                case delCell:
                    CellModel cellModel1 = diAction.cellModel();
                    cellModel1.remove();
                    cells.remove(cellModel1);
                    break;

                case gameContinue:
                    isPause = false;
                    break;

                case gameOver:
                    gameOverMenu = new GameOverMenu(stage);
                    gameOverMenu.animateOpen();
                    isPause = true;
                    break;

                case scoreAnimation:
                    if (staticPanel != null) {
                        staticPanel.animatePlusScore(diAction.getValue(), diAction.newPos());
                    }
                    break;
            }

        }

    }

    /**
     * P1-2: rebuild the visual board from an undo action list.
     *
     * DiGameModel.undo() returns:
     *   1. GameContinueAction (unpause)
     *   2. NewCellAction for every non-zero cell in the restored snapshot
     *
     * Before applying those NewCellActions we must clear the current CellModel
     * actors on the board, otherwise duplicates will be stacked.
     *
     * BUGFIX (panel-redesign): also recompute metrics defensively before rebuilding
     * cells, so CellModel.size and DY are guaranteed in sync with the current
     * FIELD_SIZE even if createFields() was never re-called for this board size.
     */
    public void applyUndoActions(List<DiAction> stepActions) {
        // 0. Defensive: make sure size/DY match the current FIELD_SIZE
        recomputeMetrics();

        // 1. Clear all current cells from stage and list
        for (CellModel cm : new ArrayList<>(cells)) {
            cm.cell.remove();
        }
        cells.clear();

        // 2. Re-apply actions on the empty board
        applyActions(stepActions);

        // 3. Refresh the score panel
        if (staticPanel != null) {
            staticPanel.refresh();
        }
    }

    public void onMove(Dir dir) {
        applyActions(diGameModel.onMove(dir, false));
    }

/*

    public void setOnWinListener(ActListener onWinListener) {
        diGameModel.setOnWinListener(onWinListener);
    }

    public void setOnFailListener(ActListener onFailListener) {
        diGameModel.setOnFailListener(onFailListener);
    }
*/

}
