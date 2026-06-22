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
import ru.electronikas.diagonal.settings.GameSounds;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.menu.ConfirmDialog;
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

    /**
     * Safety margin subtracted from the row-2 height when computing the action-button
     * size, to account for ImageButton's internal image-padding. Without this margin
     * the buttons visually overflow the panel's bottom edge by a few pixels even though
     * their .size() is mathematically inside the row — because libGDX ImageButton draws
     * its imageUp slightly larger than the assigned cell.
     *
     * Expressed as a FRACTION of row2Height so it scales with screen size and density
     * (the user originally tried an absolute +20 px workaround which would look wrong
     * on QHD phones and tiny on small phones).
     *
     * P1-fix: bumped from 0.15 to 0.25 after user feedback that buttons still
     * protruded past the bottom edge of the blue panel. 25% gives a more
     * comfortable margin so the icon buttons sit fully inside the panel.
     */
    private static final float BUTTON_OVERFLOW_MARGIN_FRACTION = 0.25f;  // 25% of row2Height

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

        // Action button size: bounded by BOTH the row height AND the per-button
        // column width (4 columns inside the nested row2 table: sound | del2s | undo | settings).
        //
        // IMPORTANT: ImageButton draws its imageUp drawable with its OWN internal
        // padding (imageUpalignment + libGDX's default ImageButton padding), so the
        // visible button footprint is slightly larger than the .size() value. If we
        // make actionSize == row2Height - 2*pad, the buttons visually overflow the
        // row by a few pixels at the bottom. The fix is a RELATIVE safety margin
        // (BUTTON_OVERFLOW_MARGIN_FRACTION = 0.15 of row2Height) subtracted from the
        // vertical bound — this scales correctly across all screen sizes/densities,
        // unlike the absolute '+ 20 px' workaround the user tested.
        float columnWidth = (w - 5 * pad) / 4f;
        float verticalBound = row2Height - 2 * pad
                - row2Height * BUTTON_OVERFLOW_MARGIN_FRACTION;
        float horizontalBound = columnWidth - 2 * pad;
        float actionSize = Math.min(verticalBound, horizontalBound);
        if (actionSize < h * 0.04f) actionSize = h * 0.04f;  // floor so it's never invisible

        // ----- Row 1: Score | Record (2 cells, exactly matches the table's column count) -----
        // IMPORTANT: libGDX Table infers the column count from the row with the most
        // cells. If row 1 has 2 cells and row 2 has 3, the table silently grows to 3
        // columns and the 2-cell row becomes misaligned (the second cell lands in the
        // MIDDLE column instead of the right one, leaving the right column empty and
        // shifting everything left). To avoid this we wrap the 3-button row 2 in a
        // NESTED Table that occupies a single cell of the outer table — so the outer
        // table stays at exactly 2 columns and the row-1 cells align correctly.
        table.row().height(row1Height).padTop(pad);
        // P1-fix: calibrate the font scale ONCE using the LONGEST of the two
        // texts ("Счет\n..." vs "Рекорд\n...") so both labels end up at the
        // SAME scale. Without this, textSizeTuning would pick a larger scale
        // for the shorter "Счет" and a smaller one for "Рекорд", making the
        // two panes look mismatched.
        // The scale is also capped by the row height so the text never
        // overflows the pane vertically when the window is very wide.
        float scoreRecordScale = calibrateScoreRecordScale(statWidth, row1Height);
        scoreLabel = createScoreLabel(statWidth, scoreRecordScale);
        table.add(scoreLabel).width(statWidth).padLeft(pad).padRight(pad).top();
        table.add(createRecordLabel(statWidth, scoreRecordScale)).width(statWidth).padLeft(pad).padRight(pad).top();

        // ----- Row 2: nested table with 4 equal columns of action buttons -----
        // Sound | Del 2s | Undo | Settings
        // The nested table's cells use .expandY().center() so each button is
        // vertically centered inside its column even when actionSize < row2Height
        // (which is now the case because of BUTTON_OVERFLOW_MARGIN_FRACTION).
        // Without .expandY(), libGDX defaults the cell's vertical alignment to TOP
        // and the buttons end up glued to the bottom of the row.
        Table buttonRow = new Table(Di2048Game.game.getUiSkin());
        buttonRow.add(createSoundBut()).size(actionSize).uniformX().expandX().expandY().center();
        buttonRow.add(createDel2sBut()).size(actionSize).uniformX().expandX().expandY().center();
        buttonRow.add(createUndoBut()).size(actionSize).uniformX().expandX().expandY().center();
        buttonRow.add(createSettingsBut()).size(actionSize).uniformX().expandX().expandY().center();

        // Add the nested button-row as a SINGLE cell spanning both outer columns.
        // .colspan(2).growX().center() on the OUTER cell makes the nested table
        // fill the row horizontally and center vertically within the outer row.
        // .growY() is NOT used here because row2Height is already fixed via
        // table.row().height(row2Height); the .center() handles vertical centering
        // of the nested table inside that fixed-height row.
        table.row().height(row2Height);
        table.add(buttonRow).colspan(2).growX().center();

        // Do NOT call table.pack() — it would shrink the table to preferred sizes
        // and undo the explicit width/height we just set.
        stage.addActor(table);
    }

    /** Cached undo icon drawable (loaded once, reused across recreations). */
    private static TextureRegionDrawable undoDrawable = null;

    /** Cached del2s icon drawable (loaded once, reused across recreations). */
    private static TextureRegionDrawable del2sDrawable = null;

    /**
     * Lazy-load the 'delete 2s' icon (a tile with '2' + red X badge) as a
     * TextureRegionDrawable. Loaded once and cached statically.
     */
    private static TextureRegionDrawable getDel2sDrawable() {
        if (del2sDrawable == null) {
            try {
                Texture tex = new Texture(Gdx.files.internal("data/skins/del2s128.png"));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                del2sDrawable = new TextureRegionDrawable(new TextureRegion(tex));
            } catch (Throwable e) {
                Gdx.app.error("StaticPanel", "Failed to load del2s128.png", e);
                try {
                    del2sDrawable = new TextureRegionDrawable(
                            Di2048Game.game.getUiSkin().getRegion("settings"));
                } catch (Throwable e2) {
                    Gdx.app.error("StaticPanel", "del2s fallback also failed", e2);
                    del2sDrawable = new TextureRegionDrawable(
                            new TextureRegion(new Texture(
                                    1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)));
                }
            }
        }
        return del2sDrawable;
    }

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
     * P1-2 + v2 + v4: 'Undo last move' as an ImageButton with a dedicated undo icon.
     *
     * v4 change: now opens a ConfirmDialog ("Undo last move? Watch a short ad...")
     * before firing the rewarded video. The dialog gives the user a chance to
     * cancel without accidentally triggering a 30s unskippable ad.
     *
     * Silently no-ops if no undo snapshot is available (diGameModel.canUndo()
     * returns false) — in that case we don't even show the dialog.
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
                    // Nothing to undo — silently ignore, no dialog.
                    return;
                }
                // Show a confirmation dialog. On 'Yes', fire the rewarded video.
                ConfirmDialog dlg = new ConfirmDialog(stage,
                        "undoDialogTitle", "undoDialogMessage",
                        () -> {
                            Di2048Game.game.platformListener.showRewardVideo(() -> Di2048Game.game.undoLastMove());
                            Di2048Game.game.platformListener.trackEvent("UndoMoveOnClBut");
                        });
                dlg.animateOpen();
            }
        });
        return undoBut;
    }

    /**
     * v4: 'Delete 2s' as an ImageButton with a dedicated del2s icon.
     *
     * Same flow as the existing 'Del 2s Continue' button in GameOverMenu, but
     * available mid-game from the top HUD: opens a ConfirmDialog, on 'Yes'
     * fires a rewarded video and on reward calls Di2048Game.del2s() which
     * removes every 2-tile from the board.
     *
     * Useful when the board is filling up with low-value 2-tiles and the
     * player wants to free up space without ending the game.
     */
    private Actor createDel2sBut() {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = getDel2sDrawable();
        style.imageDown = getDel2sDrawable().tint(new Color(1f, 1f, 1f, 0.6f));

        final ImageButton del2sBut = new ImageButton(style);
        final Color baseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        final Color dimColor  = new Color(1.0f, 1.0f, 1.0f, 0.7f);
        del2sBut.addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.color(baseColor, 1.4f),
                                Actions.color(dimColor, 1.4f)
                        )
                )
        );
        del2sBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                ConfirmDialog dlg = new ConfirmDialog(stage,
                        "del2sDialogTitle", "del2sDialogMessage",
                        () -> {
                            Di2048Game.game.platformListener.showRewardVideo(() -> Di2048Game.game.del2s());
                            Di2048Game.game.platformListener.trackEvent("Del2sOnClBut");
                        });
                dlg.animateOpen();
            }
        });
        return del2sBut;
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

    /**
     * Sound toggle button — leftmost in row 2 of the top panel.
     *
     * Mirrors the logic in SettingsMenu.soundButton() (so the two stay in sync):
     *   - Tap when sound is ON  -> set volume to 0 (mute), track 'Sound_Off'
     *   - Tap when sound is OFF -> set volume to DEFAULT_VAL, play a flip sound
     *                              so the user hears the change immediately,
     *                              track 'Sound_On'
     *
     * The icon used depends on the current state: 'ns128.png' when on,
     * 'nsoff128.png' when off. We rebuild the ImageButton style each time
     * the user taps so the icon updates instantly.
     *
     * NOTE: this button is re-created on every game recreate (new StaticPanel
     * is constructed in LevelField.<init>), so the icon always reflects the
     * latest Storage.getSoundVolume() value at the moment of construction.
     * If the user mutes via SettingsMenu while a StaticPanel is alive, the
     * top-panel sound button's icon won't update until the next game recreate
     * — acceptable for now, can be refined later with a shared observer.
     */
    private Actor createSoundBut() {
        final ImageButton soundBut = new ImageButton(buildSoundStyle());
        soundBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (Storage.getSoundVolume() < Storage.DEFAULT_VAL) {
                    // Currently muted -> turn ON
                    Storage.setSoundVolume(Storage.DEFAULT_VAL);
                    GameSounds.flipSoundPlay();
                    Di2048Game.game.platformListener.trackEvent("Sound_On");
                } else {
                    // Currently ON -> mute
                    Storage.setSoundVolume(0);
                    Di2048Game.game.platformListener.trackEvent("Sound_Off");
                }
                // Swap the icon in-place so the user sees the new state immediately.
                soundBut.setStyle(buildSoundStyle());
            }
        });
        return soundBut;
    }

    /**
     * Build a fresh ImageButtonStyle whose imageUp reflects the current sound state.
     * Called at construction time AND after every tap on the sound button.
     */
    private ImageButton.ImageButtonStyle buildSoundStyle() {
        boolean muted = Storage.getSoundVolume() < Storage.DEFAULT_VAL;
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = getSoundDrawable(muted);
        style.imageDown = getSoundDrawable(muted).tint(new Color(1f, 1f, 1f, 0.6f));
        return style;
    }

    /** Cached sound-icon drawables (loaded once, reused across recreations). */
    private static TextureRegionDrawable soundOnDrawable = null;
    private static TextureRegionDrawable soundOffDrawable = null;

    /**
     * Clear all cached drawables. Must be called when the GL context is lost
     * (e.g., resolution change via adb wm size) to avoid black square icons.
     */
    public static void clearCachedDrawables() {
        undoDrawable = null;
        del2sDrawable = null;
        soundOnDrawable = null;
        soundOffDrawable = null;
    }

    private static TextureRegionDrawable getSoundDrawable(boolean muted) {
        return muted ? getSoundOffDrawable() : getSoundOnDrawable();
    }

    private static TextureRegionDrawable getSoundOnDrawable() {
        if (soundOnDrawable == null) {
            soundOnDrawable = loadSoundDrawable("data/skins/ns128.png", "sound_on");
        }
        return soundOnDrawable;
    }

    private static TextureRegionDrawable getSoundOffDrawable() {
        if (soundOffDrawable == null) {
            soundOffDrawable = loadSoundDrawable("data/skins/nsoff128.png", "sound_off");
        }
        return soundOffDrawable;
    }

    private static TextureRegionDrawable loadSoundDrawable(String path, String name) {
        try {
            Texture tex = new Texture(Gdx.files.internal(path));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return new TextureRegionDrawable(new TextureRegion(tex));
        } catch (Throwable e) {
            Gdx.app.error("StaticPanel", "Failed to load " + path, e);
            try {
                return new TextureRegionDrawable(
                        Di2048Game.game.getUiSkin().getRegion("settings"));
            } catch (Throwable e2) {
                Gdx.app.error("StaticPanel", name + " fallback also failed", e2);
                return new TextureRegionDrawable(
                        new TextureRegion(new Texture(
                                1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)));
            }
        }
    }

    /**
     * P1-fix: compute a single font scale to be shared by both the Score and
     * Record labels, so they render at the SAME size.
     *
     * Algorithm:
     *  1. Build a temporary label with the LONGEST of the two texts
     *     ("Рекорд\n..." is always >= "Счет\n..." in width because "Рекорд"
     *     has more characters than "Счет").
     *  2. Iteratively shrink from 0.8 down by 0.05 until prefWidth <= 85% of
     *     paneWidth AND prefHeight <= 90% of rowHeight. The height check
     *     prevents the text from overflowing the pane vertically when the
     *     window is very wide (wide panes -> large width budget -> large
     *     scale -> text taller than the row).
     *  3. Return the scale; both createScoreLabel and createRecordLabel
     *     receive it as a parameter and apply it via setFontScale.
     *
     * P1-fix v2: bumped start scale 0.6 -> 0.8 and targets 70/80 -> 85/90
     * so the labels are bigger and fill more of the green pane (user
     * feedback: 'чуть увеличить шрифты').
     */
    private float calibrateScoreRecordScale(float paneWidth, float rowHeight) {
        // Use the Record text as the reference — it's always >= Score in width.
        String refText = getRecordText();
        Label ref = new Label(refText, Di2048Game.game.getUiSkin()
                .get("score-lbl", Label.LabelStyle.class));
        ref.setAlignment(Align.center);

        float targetWidth = paneWidth * 0.85f;
        float targetHeight = rowHeight * 0.90f;
        float scale = 0.8f;
        while (scale > 0.2f) {
            ref.setFontScale(scale);
            ref.layout();
            if (ref.getPrefWidth() <= targetWidth && ref.getPrefHeight() <= targetHeight) {
                break;
            }
            scale -= 0.05f;
        }
        Gdx.app.log("StaticPanel", "score/record scale=" + scale
                + " prefW=" + ref.getPrefWidth() + "/" + targetWidth
                + " prefH=" + ref.getPrefHeight() + "/" + targetHeight);
        return scale;
    }

    private Label createRecordLabel(float width, float scale) {
        Label label = new Label(getRecordText(), Di2048Game.game.getUiSkin().get("score-lbl", Label.LabelStyle.class));
        label.setAlignment(Align.center);
        label.setFontScale(scale);
        return label;
    }

    private String getRecordText() {
        return Di2048Game.game.bdl().get("record") + "\n" + Storage.getRecord();
    }

    private Label createScoreLabel(float width, float scale) {
        Label label = new Label(getScoreText(), Di2048Game.game.getUiSkin()
                .get("score-lbl", Label.LabelStyle.class));
        label.setAlignment(Align.center);
        label.setFontScale(scale);
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
        // P1-fix: apply BOARD_X_OFFSET so the "+score" animation appears at the
        // correct horizontal position when the board is centered.
        scoreAnimLabel.setPosition(
                LevelField.BOARD_X_OFFSET + pos.x * CellModel.size + CellModel.size / 4,
                pos.y * CellModel.size + CellModel.size);
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
