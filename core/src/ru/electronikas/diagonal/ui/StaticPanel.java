package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Pos;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.menu.SettingsMenu;

/**
 * StaticPanel — fixed top-of-screen HUD showing score, record, undo and settings.
 *
 * REDESIGN (panel-redesign commit): the previous single-row layout
 *   score(30%) | record(30%) | undo(18%) | settings(h/8 px)
 * overflowed on most phone widths because settings used an absolute pixel width
 * and the undo TextButton was wider than its 18% allocation. The settings icon
 * was being pushed off-screen.
 *
 * New layout (2 rows, fits comfortably on any phone width):
 *
 *   +--------------------------------------------------+
 *   |  SCORE        |  RECORD                          |  row 1: ~6% screen height
 *   +--------------------------------------------------+
 *   |  UNDO         |  SETTINGS (icon)                |  row 2: ~6% screen height
 *   +--------------------------------------------------+
 *
 * - Panel total height = h/7 (~14.3% screen height, was h/8 = 12.5%)
 * - Panel is positioned via a single source of truth: PANEL_TOP_FRACTION
 *   (matches the value LevelField.DY derives from)
 * - Undo button is given equal weight to Settings so neither starves
 * - Score/Record label widths are computed from screen width, no hardcoded pixels
 *
 * The score row uses a smaller font scale (0.85 of the calibrated scale) so the
 * 'Count' / 'Счет' header and the numeric value both fit on two lines inside
 * half the screen width.
 */
public class StaticPanel {

    /** Public constants so LevelField can position the board relative to the panel. */
    public static final float PANEL_TOP_FRACTION = 1f / 5f;     // panel y = h - h/5  (top edge)
    public static final float PANEL_HEIGHT_FRACTION = 1f / 7f;  // panel height = h/7
    public static final float FIELD_TOP_GAP_FRACTION = 0.02f;   // gap between panel bottom and board top

    private Stage stage;
    private DiGameModel diGameModel;
    float h;
    float w;

    Label scoreLabel;

    SettingsMenu settingsMenu;

    public StaticPanel(Stage stage, DiGameModel diGameModel) {
        this.stage = stage;
        this.diGameModel = diGameModel;

        h = Gdx.graphics.getHeight();
        w = Gdx.graphics.getWidth();

        Table table = new Table(Di2048Game.game.getUiSkin());
        table.align(Align.top);
        table.setWidth(w);
        table.setHeight(h * PANEL_HEIGHT_FRACTION);
        table.setPosition(0, h - h * PANEL_TOP_FRACTION);
        table.setBackground("bluepane");
        table.defaults().height(h * PANEL_HEIGHT_FRACTION / 2f);

        // ----- Row 1: Score | Record -----
        table.row();
        float colWidth = w / 2f;
        scoreLabel = createScoreLabel(colWidth);
        table.add(scoreLabel).width(colWidth).pad(w/80).top();
        table.add(createRecordLabel()).width(colWidth).pad(w/80).top();

        // ----- Row 2: Undo | Settings -----
        table.defaults().height(h * PANEL_HEIGHT_FRACTION / 2f);
        table.row();
        // Undo takes ~45% of width, Settings takes ~25% (icon is square).
        // Remaining ~30% is split into symmetric padding via pad().
        float undoW = w * 0.42f;
        float settingsW = h * PANEL_HEIGHT_FRACTION / 2f * 0.9f; // ~square, slightly smaller than row height
        table.add(createUndoBut()).width(undoW).pad(w/80).top();
        table.add(createSettingsBut()).width(settingsW).pad(w/80).top();

        table.pack();
        // Re-apply the explicit width/height — pack() can shrink them otherwise
        table.setWidth(w);
        table.setHeight(h * PANEL_HEIGHT_FRACTION);
        table.setPosition(0, h - h * PANEL_TOP_FRACTION);
//        table.setDebug(true);
        stage.addActor(table);
    }

    /**
     * P1-2: 'Undo last move' button. Watches a rewarded video, then reverts the
     * most recent move. Silently no-ops if no undo snapshot is available
     * (Di2048Game.undoLastMove guards on canUndo()).
     *
     * REDESIGN: smaller font scale (0.7) and explicit width allocation so the
     * label never pushes the settings button off-screen.
     */
    private Actor createUndoBut() {
        TextButton undoBut = new TextButton(Di2048Game.game.bdl().get("undo"),
                Di2048Game.game.getUiSkin().get("green-but", TextButton.TextButtonStyle.class));
        undoBut.getLabel().setFontScale(0.7f);
        undoBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!diGameModel.canUndo()) {
                    // Nothing to undo — silently ignore
                    return;
                }
                Di2048Game.game.platformListener.showRewardVideo(() -> Di2048Game.game.undoLastMove());
                Di2048Game.game.platformListener.trackEvent("UndoMoveOnClBut");
            }
        });
        return undoBut;
    }

    private Actor createSettingsBut() {
        final ImageButton settingsBut = new ImageButton(
                Di2048Game.game.getUiSkin().get("settings-but", ImageButton.ImageButtonStyle.class));
        settingsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                settingsMenu = new SettingsMenu(stage);
                settingsMenu.animateOpen();
                Di2048Game.game.platformListener.trackEvent("SettingsOnClBut");
            }
        });
        final Color baseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        final Color alphaColor = new Color(1.0f, 1.0f, 1.0f, 0.5f);
        // Создаем последовательность действий для анимации
        settingsBut.addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.color(baseColor, 1.8f),
                                Actions.color(alphaColor, 1.8f)
                        )
                )
        );
        return settingsBut;
    }

    private Label createRecordLabel() {
        Label label = new Label(getRecordText(), Di2048Game.game.getUiSkin().get("score-lbl", Label.LabelStyle.class));
        label.setAlignment(Align.center);
        label.setFontScale(scale * 0.85f);
        label.pack();
        return label;
    }

    private String getRecordText() {
        return Di2048Game.game.bdl().get("record") + "\n" + Storage.getRecord();
    }

    float scale;
    private Label createScoreLabel(float width) {
        Label label = new Label(getScoreText(), Di2048Game.game.getUiSkin()
                .get("score-lbl", Label.LabelStyle.class));
        label.setAlignment(Align.center);
        scale = Utils.textSizeTuning(label, width, 70);
        label.setFontScale(scale * 0.85f);
        label.pack();
        return label;
    }

    private String getScoreText() {
        return Di2048Game.game.bdl().get("count") + "\n" + diGameModel.score;
    }

    public void refresh() {
        scoreLabel.setText(getScoreText());
    }

    public void animatePlusScore(int value, Pos pos) {
        final Label scoreAnimLabel = new Label("+" + value, Di2048Game.game.getUiSkin());
        scoreAnimLabel.setColor(Utils.getRandomColor());
        scoreAnimLabel.setPosition(pos.x * CellModel.size + CellModel.size / 4, pos.y * CellModel.size + CellModel.size);
        scoreAnimLabel.pack();
        stage.addActor(scoreAnimLabel);
        scoreAnimLabel.addAction(Actions.fadeOut(1f));
        scoreAnimLabel.addAction(Actions.sequence(
                Actions.moveBy(0, 20, 1f),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        scoreAnimLabel.remove();
                        return true;
                    }
                }
        ));
        refresh();
    }
}
