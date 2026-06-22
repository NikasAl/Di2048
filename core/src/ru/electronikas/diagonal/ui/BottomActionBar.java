package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.settings.Storage;

/**
 * BottomActionBar — fills the gap between the bottom of the board and the
 * top of the ad banner with a single centered control: a field-size
 * switcher with left/right arrow icons.
 *
 *   +--------------------------------------------------+
 *   |              [ < ]   Поле: 4x4   [ > ]            |
 *   +--------------------------------------------------+
 *
 * v3 changes (this commit):
 *  - Removed the 'New Game' button. It was too easy to tap by accident and
 *    lose progress; New Game is still reachable via SettingsMenu.
 *  - The field-size switcher is now centered horizontally in the bar
 *    (was split left/right) — fewer elements, more breathing room.
 *  - Prev/next chevrons are now Image actors with dedicated PNG icons
 *    (arrow_left128.png / arrow_right128.png) because the bundled bitmap
 *    font (test.fnt, DejaVu Sans subset, 132 chars) does NOT include
 *    '<', '>', or any arrow glyph. The TextButton labels were rendering
 *    as empty boxes.
 *  - AD_BANNER_RESERVE_FRACTION bumped from 0.10 to 0.16 to clear the
 *    double-height Yandex banner that was overlapping the bar.
 *
 * Geometry: anchored above the ad banner reserve, height = BOTTOM_BAR_HEIGHT_FRACTION
 * of the screen height.
 */
public class BottomActionBar {

    /** Height of the bottom action bar as a fraction of screen height. */
    public static final float BOTTOM_BAR_HEIGHT_FRACTION = 0.08f;
    /**
     * Reserved space at the very bottom of the screen for the ad banner.
     * Bumped from 0.10 to 0.16 because the Yandex banner sometimes serves
     * the double-height (160 dp) format which extends higher than the
     * standard 100 dp banner and was overlapping the bar.
     */
    public static final float AD_BANNER_RESERVE_FRACTION = 0.16f;

    private final Stage stage;
    private Label fieldSizeLabel;

    /** Cached arrow-icon drawables (loaded once, reused across recreations). */
    private static TextureRegionDrawable arrowLeftDrawable = null;
    private static TextureRegionDrawable arrowRightDrawable = null;

    /**
     * Clear cached arrow drawables. Must be called when the GL context is lost
     * (e.g., resolution change via adb wm size) to avoid black square icons.
     */
    public static void clearCachedDrawables() {
        arrowLeftDrawable = null;
        arrowRightDrawable = null;
    }

    public BottomActionBar(Stage stage) {
        this.stage = stage;

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float barHeight = h * BOTTOM_BAR_HEIGHT_FRACTION;
        float bannerReserve = h * AD_BANNER_RESERVE_FRACTION;

        Table table = new Table(Di2048Game.game.getUiSkin());
        table.setWidth(w);
        table.setHeight(barHeight);
        // Anchor: bottom of the bar sits on top of the (now larger) banner reserve.
        table.setPosition(0, bannerReserve);
        table.setBackground("bluepane");
        table.defaults().height(barHeight);

        // ----- Centered field-size switcher: [ < ]  Поле: 4x4  [ > ] -----
        table.row();
        Table switcher = new Table(Di2048Game.game.getUiSkin());
        switcher.defaults().height(barHeight * 0.85f);

        // Left arrow (icon)
        Image leftArrow = new Image(getArrowLeftDrawable());
        leftArrow.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                changeFieldSize(-1);
            }
        });
        switcher.add(leftArrow).size(barHeight * 0.7f).padRight(w * 0.02f);

        // Field-size label
        fieldSizeLabel = createFieldSizeLabel();
        switcher.add(fieldSizeLabel).width(w * 0.30f).pad(w * 0.005f);

        // Right arrow (icon)
        Image rightArrow = new Image(getArrowRightDrawable());
        rightArrow.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                changeFieldSize(+1);
            }
        });
        switcher.add(rightArrow).size(barHeight * 0.7f).padLeft(w * 0.02f);

        // Wrap the switcher in a centered Container so the whole thing
        // (arrows + label) is horizontally centered within the full bar width.
        Container<Table> centered = new Container<>(switcher);
        centered.fill(false);
        centered.align(Align.center);
        table.add(centered).grow().center();

        stage.addActor(table);
    }

    /**
     * Lazy-load the left-arrow icon as a TextureRegionDrawable. Loaded once
     * and cached statically. We use a dedicated PNG (arrow_left128.png)
     * because the bundled bitmap font does not include '<' or arrow glyphs.
     */
    private static TextureRegionDrawable getArrowLeftDrawable() {
        if (arrowLeftDrawable == null) {
            arrowLeftDrawable = loadArrow("data/skins/arrow_left128.png", "arrow_left");
        }
        return arrowLeftDrawable;
    }

    private static TextureRegionDrawable getArrowRightDrawable() {
        if (arrowRightDrawable == null) {
            arrowRightDrawable = loadArrow("data/skins/arrow_right128.png", "arrow_right");
        }
        return arrowRightDrawable;
    }

    private static TextureRegionDrawable loadArrow(String path, String name) {
        try {
            Texture tex = new Texture(Gdx.files.internal(path));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return new TextureRegionDrawable(new TextureRegion(tex));
        } catch (Throwable e) {
            Gdx.app.error("BottomActionBar", "Failed to load " + path, e);
            try {
                return new TextureRegionDrawable(
                        Di2048Game.game.getUiSkin().getRegion("settings"));
            } catch (Throwable e2) {
                Gdx.app.error("BottomActionBar", name + " fallback also failed", e2);
                return new TextureRegionDrawable(
                        new TextureRegion(new Texture(
                                1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)));
            }
        }
    }

    private Label createFieldSizeLabel() {
        Label label = new Label(getFieldSizeText(),
                Di2048Game.game.getUiSkin().get("score-lbl", Label.LabelStyle.class));
        label.setAlignment(Align.center);
        label.setFontScale(0.6f);
        return label;
    }

    private String getFieldSizeText() {
        int fs = Storage.getCurrentFieldType();
        // Note: use plain 'x' instead of '×' (U+00D7) because the bundled
        // test.fnt bitmap font (DejaVu Sans subset) does not include U+00D7.
        return Di2048Game.game.bdl().get("fieldSize") + "\n" + fs + "x" + fs;
    }

    /**
     * Switch the board to the next/previous supported size and recreate the game.
     * Allowed range: 3..12 (matches SettingsMenu SelectBox items).
     */
    private void changeFieldSize(int delta) {
        int current = Storage.getCurrentFieldType();
        int next = current + delta;
        if (next < 3 || next > 12) return;
        Storage.setFieldType(next);
        // Refresh the label immediately so the user sees feedback even before
        // the recreate finishes.
        fieldSizeLabel.setText(getFieldSizeText());
        Di2048Game.game.create();
        Di2048Game.game.platformListener.trackEvent("ChangeFieldSize_" + next);
    }
}
