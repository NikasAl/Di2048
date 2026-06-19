package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
 * DESIGN v2 (panel-fix commit): the previous 2-row redesign still overflowed:
 *  - score/record labels used the 'score-lbl' LabelStyle which has its own
 *    green 'greenpane' background. With pad(w/80) AND width(colWidth=w/2),
 *    each label's green pane visually extended beyond the table's blue
 *    'bluepane' background, producing the reported overhang.
 *  - The undo button was a TextButton with 'UNDO'/'ОТМЕНА' label, which looked
 *    inconsistent next to the icon-based settings button and crowded the row.
 *
 * Fix v2:
 *  - Row 1 (stats): two Labels with the green-pane background, but each gets
 *    width = (w - 3*pad) / 2  so the two panes plus padding sum to exactly w.
 *    Font scale reduced to 0.55 to keep "Счет\n123456" inside the pane.
 *  - Row 2 (actions): undo is now an ImageButton with a dedicated icon
 *    (undo128.png, registered as 'undo-but' ImageButtonStyle in mainatlas.json).
 *    Settings stays an ImageButton. Both are square and centered.
 *
 * Geometry constants are public so LevelField positions the board consistently.
 */
public class StaticPanel {

    /**
     * Public constants so LevelField can position the board relative to the panel.
     *
     * PANEL_HEIGHT_FRACTION was 1/6 — too short: the undo/settings buttons
     * (sized as row2Height*0.85) were sticking out past the bottom edge of
     * the blue background by ~50% of their height, because the table's
     * implicit padding plus the icon's pref size exceeded the assigned row
     * height. Bumped to 1/5 so the second row has room for the buttons
     * AND the padding around them.
     */
    public static final float PANEL_TOP_FRACTION = 1f / 5f;        // panel y anchor
    public static final float PANEL_HEIGHT_FRACTION = 1f / 5f;     // panel height (was 1/6, now 1/5)
    public static final float FIELD_TOP_GAP_FRACTION = 0.02f;      // gap between panel bottom and board top

    /** Horizontal padding inside the table (between cells + edge). */
    private static final float CELL_PAD_FRACTION = 0.025f;  // 2.5% of screen width

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

        float pad = w * CELL_PAD_FRACTION;
        // Row heights are now computed so that:
        //   row1Height + row2Height + 3*pad (top/middle/bottom) <= panelHeight
        // and the action buttons (actionSize = row2Height - 2*pad) fit inside row2.
        float panelHeight = h * PANEL_HEIGHT_FRACTION;
        float row1Height = panelHeight * 0.50f;
        float row2Height = panelHeight * 0.40f;
        // Reserve ~10% of panel height for inter-row padding + edge padding.

        // Each stat pane takes half of (width - 3*pad) so two panes + 3 pads = exactly w.
        float statWidth = (w - 3 * pad) / 2f;
        // Each action button is sized to fit inside row2Height with a 2*pad margin.
        float actionSize = row2Height - 2 * pad;
        if (actionSize > h * 0.10f) actionSize = h * 0.10f;  // cap so it doesn't get huge

        // ----- Row 1: Score | Record -----
        table.row().height(row1Height).padTop(pad);
        scoreLabel = createScoreLabel(statWidth);
        table.add(scoreLabel).width(statWidth).padLeft(pad).padRight(pad).top();
        table.add(createRecordLabel()).width(statWidth).padLeft(pad).padRight(pad).top();

        // ----- Row 2: Undo (icon) | Settings (icon) -----
        table.row().height(row2Height).padBottom(pad).padTop(pad / 2f);
        table.add(createUndoBut()).size(actionSize).top();
        table.add(createSettingsBut()).size(actionSize).top();

        // Do NOT call table.pack() — it would shrink the table to preferred sizes
        // and undo the explicit width/height we just set.
        stage.addActor(table);
    }

    /** Cached undo icon drawable (loaded once, reused across recreations). */
    private static TextureRegionDrawable undoDrawable = null;

    /**
     * Lazy-load the undo icon as a TextureRegionDrawable. Loaded once and cached
     * statically so we don't leak GPU memory across game recreations.
     *
     * We intentionally do NOT register this via the skin JSON because libGDX
     * Skin JSON has unreliable support for standalone Textures (the standard
     * way is to pack icons into an atlas, but we want a non-invasive change).
     * Building the drawable in code is the safest path.
     */
    private static TextureRegionDrawable getUndoDrawable() {
        if (undoDrawable == null) {
            try {
                Texture tex = new Texture(Gdx.files.internal("data/skins/undo128.png"));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                undoDrawable = new TextureRegionDrawable(new TextureRegion(tex));
            } catch (Throwable e) {
                Gdx.app.error("StaticPanel", "Failed to load undo128.png", e);
                // Fall back to a fresh TextureRegionDrawable built from the existing
                // 'settings' region in the skin (use new TextureRegionDrawable(region)
                // rather than a cast — getDrawable() may return a different Drawable subtype).
                try {
                    undoDrawable = new TextureRegionDrawable(
                            Di2048Game.game.getUiSkin().getRegion("settings"));
                } catch (Throwable e2) {
                    Gdx.app.error("StaticPanel", "Fallback also failed", e2);
                    // Last-resort: 1x1 white texture so the button still renders something.
                    undoDrawable = new TextureRegionDrawable(
                            new TextureRegion(new Texture(
                                    1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)));
                }
            }
        }
        return undoDrawable;
    }

    /**
     * P1-2 + v2: 'Undo last move' as an ImageButton with a dedicated undo icon.
     * Watches a rewarded video, then reverts the most recent move. Silently
     * no-ops if no undo snapshot is available.
     */
    private Actor createUndoBut() {
        // Build the ImageButton style in code — safer than declaring a skin style
        // for a standalone-texture icon.
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = getUndoDrawable();
        style.imageDown = getUndoDrawable().tint(new Color(1f, 1f, 1f, 0.6f));

        final ImageButton undoBut = new ImageButton(style);
        // Subtle pulsing animation so the button reads as 'available' even without text.
        final Color baseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        final Color dimColor  = new Color(1.0f, 1.0f, 1.0f, 0.7f);
        undoBut.addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.color(baseColor, 1.4f),
                                Actions.color(dimColor, 1.4f)
                        )
                )
        );
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
        label.setFontScale(0.55f);
        // Do NOT call label.pack() — it would override the table-assigned width.
        return label;
    }

    private String getRecordText() {
        return Di2048Game.game.bdl().get("record") + "\n" + Storage.getRecord();
    }

    private Label createScoreLabel(float width) {
        Label label = new Label(getScoreText(), Di2048Game.game.getUiSkin()
                .get("score-lbl", Label.LabelStyle.class));
        label.setAlignment(Align.center);
        // Conservative fixed scale: "Счет\n123456" must fit inside half the screen.
        // Previous code called Utils.textSizeTuning which over-scaled for the
        // available width because it didn't account for the green-pane background
        // padding built into the 'score-lbl' style.
        label.setFontScale(0.55f);
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
