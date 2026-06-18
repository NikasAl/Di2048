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
    private boolean isPause = false;
    private GameOverMenu gameOverMenu;

    public void hideGameOverMenu() {
        if(gameOverMenu!=null) {
            gameOverMenu.animateHide();
        }
    }

    public LevelField(DiGameModel diGameModel, Stage stage) {
        this.diGameModel = diGameModel;
        this.stage = stage;

        // Recompute CellModel.size + DY FIRST, so both createFields() and the
        // StaticPanel constructor see consistent values.
        recomputeMetrics();

        createFields();

        staticPanel = new StaticPanel(stage, diGameModel);

        cells = new ArrayList<CellModel>();
        applyActions(diGameModel.onMove(Dir.none, true));
    }

    /**
     * Centralize all metric calculations that depend on FIELD_SIZE / screen size.
     * Called from the constructor and from applyUndoActions() (defensive: the
     * metrics should not change during undo, but the call is cheap and protects
     * against future refactors).
     *
     *   CellModel.size = screen width / FIELD_SIZE
     *   DY             = (panel bottom) - FIELD_TOP_GAP - board_height
     *
     * Where 'panel bottom' is computed from {@link StaticPanel#PANEL_TOP_FRACTION}
     * and {@link StaticPanel#PANEL_HEIGHT_FRACTION}. Both StaticPanel and
     * LevelField read from the same source of truth, so the gap between them
     * is always {@link StaticPanel#FIELD_TOP_GAP_FRACTION} of the screen height,
     * regardless of FIELD_SIZE.
     */
    public static void recomputeMetrics() {
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        // Cell size: width-based, so the board spans the full screen width.
        CellModel.size = sw / DiGameModel.FIELD_SIZE;

        // StaticPanel is positioned at y = h - h*PANEL_TOP_FRACTION with height h*PANEL_HEIGHT_FRACTION,
        // so its BOTTOM edge is at y = h - h*PANEL_TOP_FRACTION - h*PANEL_HEIGHT_FRACTION... but
        // careful: libGDX Table.setPosition(x, y) sets the BOTTOM-LEFT corner of the table,
        // and StaticPanel aligns to top via Align.top. We position the table so that its
        // top edge is at h - h*PANEL_TOP_FRACTION. With height h*PANEL_HEIGHT_FRACTION,
        // its bottom edge is at h - h*PANEL_TOP_FRACTION - h*PANEL_HEIGHT_FRACTION.
        // Wait — actually the previous StaticPanel used setPosition(0, h - h/5) with the
        // table's bottom-left at that y, meaning the table occupies [h-h/5, h-h/5+h/8]
        // which goes ABOVE the screen. That worked because Align.top was set and the
        // visible content was anchored at the table's top. To keep the visual position
        // identical to the previous version, we use the SAME anchor: panel bottom-left
        // at y = h - h*PANEL_TOP_FRACTION, panel visually grows UPWARD.
        // For DY we only care about the visual BOTTOM edge of the panel, which is at
        // y = h - h*PANEL_TOP_FRACTION (where the table's bottom-left sits).
        float panelBottom = sh - sh * StaticPanel.PANEL_TOP_FRACTION;
        float boardTop = panelBottom - sh * StaticPanel.FIELD_TOP_GAP_FRACTION;
        float boardHeight = CellModel.size * DiGameModel.FIELD_SIZE;
        DY = boardTop - boardHeight;
        // Defensive: never let the board fall off the bottom of the screen.
        if (DY < sh * 0.02f) {
            DY = sh * 0.02f;
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
                    staticPanel.animatePlusScore(diAction.getValue(), diAction.newPos());
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
        staticPanel.refresh();
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
