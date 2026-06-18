package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.settings.Storage;

/**
 * BottomActionBar — fills the gap between the bottom of the board and the
 * top of the ad banner with two useful controls:
 *
 *   +--------------------------------------------------+
 *   |  [↻]  НОВАЯ ИГРА   |   Поле: 4×4   <  >          |
 *   +--------------------------------------------------+
 *
 * Left side  : 'New game' button (restarts the current board size with a fresh
 *               2-tile spawn). Uses the restart icon from the skin + a short
 *               text label.
 * Right side : Current field-size indicator with prev/next chevrons so the
 *               player can switch board sizes (3..12) without opening Settings.
 *
 * Both controls read from / write to Storage.getCurrentFieldType() so they
 * stay in sync with the SelectBox in SettingsMenu.
 *
 * Geometry: anchored to the bottom of the screen, occupying the strip ABOVE
 * the ad banner (which is itself anchored to the very bottom via RelativeLayout
 * ALIGN_PARENT_BOTTOM in AdYandex.initBanner). The bar's height is
 * BOTTOM_BAR_HEIGHT_FRACTION of the screen height.
 */
public class BottomActionBar {

    /** Height of the bottom action bar as a fraction of screen height. */
    public static final float BOTTOM_BAR_HEIGHT_FRACTION = 0.08f;
    /** Reserved space at the very bottom of the screen for the ad banner. */
    public static final float AD_BANNER_RESERVE_FRACTION = 0.10f;

    private final Stage stage;
    private Label fieldSizeLabel;

    public BottomActionBar(Stage stage) {
        this.stage = stage;

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float barHeight = h * BOTTOM_BAR_HEIGHT_FRACTION;
        float bannerReserve = h * AD_BANNER_RESERVE_FRACTION;

        Table table = new Table(Di2048Game.game.getUiSkin());
        table.setWidth(w);
        table.setHeight(barHeight);
        // Anchor: bottom of the bar sits on top of the banner reserve.
        table.setPosition(0, bannerReserve);
        table.setBackground("bluepane");
        table.defaults().height(barHeight);

        // ----- Left side: New Game button -----
        table.row();
        float newGameW = w * 0.45f;
        table.add(createNewGameButton(newGameW)).width(newGameW).pad(w * 0.02f);

        // ----- Right side: field-size indicator + prev/next chevrons -----
        Table fieldSizeTable = new Table(Di2048Game.game.getUiSkin());
        fieldSizeTable.defaults().height(barHeight * 0.8f);

        TextButton prevBut = new TextButton("<",
                Di2048Game.game.getUiSkin().get("green-but", TextButton.TextButtonStyle.class));
        prevBut.getLabel().setFontScale(1.0f);
        prevBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                changeFieldSize(-1);
            }
        });
        fieldSizeTable.add(prevBut).width(barHeight * 0.9f).padRight(w * 0.01f);

        fieldSizeLabel = createFieldSizeLabel();
        fieldSizeTable.add(fieldSizeLabel).width(w * 0.22f).pad(w * 0.005f);

        TextButton nextBut = new TextButton(">",
                Di2048Game.game.getUiSkin().get("green-but", TextButton.TextButtonStyle.class));
        nextBut.getLabel().setFontScale(1.0f);
        nextBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                changeFieldSize(+1);
            }
        });
        fieldSizeTable.add(nextBut).width(barHeight * 0.9f).padLeft(w * 0.01f);

        table.add(fieldSizeTable).width(w * 0.45f).pad(w * 0.02f);

        stage.addActor(table);
    }

    /**
     * 'New Game' button: a Table with a restart icon on the left and the
     * 'NEW GAME' text on the right, wrapped in a green TextButton-style background.
     *
     * Using a TextButton with a long label "↻ НОВАЯ ИГРА" would not work because
     * the BitmapFont doesn't render unicode arrow glyphs reliably. Instead we
     * compose an Image (from the 'restart128' texture) + Label inside a Table
     * that itself uses the 'greenpane' background.
     */
    private Actor createNewGameButton(float width) {
        Table inner = new Table(Di2048Game.game.getUiSkin());
        inner.setBackground("greenpane");
        inner.defaults().pad(Gdx.graphics.getWidth() * 0.015f);

        // Restart icon — pull the texture directly from the skin.
        Image icon = new Image(Di2048Game.game.getUiSkin().getDrawable("restart128"));
        float iconSize = Gdx.graphics.getHeight() * BOTTOM_BAR_HEIGHT_FRACTION * 0.6f;
        inner.add(icon).size(iconSize).padLeft(Gdx.graphics.getWidth() * 0.02f);

        Label label = new Label(Di2048Game.game.bdl().get("newGameShort"),
                Di2048Game.game.getUiSkin());
        label.setAlignment(Align.center);
        label.setFontScale(0.7f);
        inner.add(label).expandX().center().padRight(Gdx.graphics.getWidth() * 0.02f);

        // Wrap inner in a Container so clicks register on the whole button area.
        Container<Table> wrap = new Container<>(inner);
        wrap.fill().width(width);
        wrap.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Storage.resetCurrentGame();
                Di2048Game.game.create();
                Di2048Game.game.platformListener.trackEvent("NewGameFromBottomBar");
            }
        });
        return wrap;
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
        return Di2048Game.game.bdl().get("fieldSize") + "\n" + fs + "×" + fs;
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
