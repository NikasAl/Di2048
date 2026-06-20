package ru.electronikas.diagonal.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ru.electronikas.diagonal.Di2048Game;

/**
 * Reusable Yes/No confirmation dialog.
 *
 * Two-line layout:
 *   +----------------------------------+
 *   |         <Title>                  |
 *   |     <Message text>               |
 *   |    [ Yes ]      [ No ]           |
 *   +----------------------------------+
 *
 * The dialog slides in from the top (animateOpen) and slides back out
 * (animateHide). It uses the same blue-pane background as GameOverMenu
 * and SettingsMenu so it reads as part of the same UI family.
 *
 * On 'Yes': runs the supplied onConfirm Runnable, then auto-closes.
 * On 'No' : just closes.
 *
 * The dialog pauses the game (LevelField.setPaused(true)) on open and
 * unpauses on close, so background swipes don't advance the board while
 * the dialog is on screen.
 *
 * Font scaling:
 *  - We do NOT use Utils.textSizeTuning because that helper has two bugs:
 *      1) It starts at maxScale=3.0 — for a short title like "Undo last move?"
 *         the prefWidth at scale=3.0 may already be < width*50% (because the
 *         cell width on a phone is ~700px), so the while loop NEVER executes
 *         and the label keeps scale=3.0 (giant).
 *      2) It has no lower bound, so for long text the loop can drive scale
 *         negative.
 *    We instead use a custom fitFontToWidth() that starts SMALL (0.3) and
 *    GROWS UP until prefWidth > target, then takes the previous step. This
 *    guarantees we end up at the largest scale that fits, with a sane floor
 *    (0.2) and ceiling (1.0).
 */
public class ConfirmDialog {

    /** Floor for the iterative font fit — text never gets smaller than this. */
    private static final float FONT_SCALE_FLOOR = 0.2f;
    /** Ceiling for the iterative font fit — text never gets larger than this. */
    private static final float FONT_SCALE_CEIL  = 1.0f;
    /** Step size for the iterative font fit. */
    private static final float FONT_SCALE_STEP  = 0.05f;
    /** Start scale for the upward search — must be <= FONT_SCALE_FLOOR-ish. */
    private static final float FONT_SCALE_START = 0.3f;

    private final Table dialog;
    private final Skin uiSkin;
    private final float h;

    public ConfirmDialog(Stage stage, String titleKey, String messageKey, final Runnable onConfirm) {
        float w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        float butW = w / 6f;

        uiSkin = Di2048Game.game.getUiSkin();
        dialog = new Table(uiSkin);
        dialog.align(Align.topLeft);
        dialog.setPosition(butW / 2, h);
        dialog.setWidth(w - butW);
        dialog.setHeight(h / 2.3f);
        dialog.background("bluepane-t");

        // Effective cell width (matches the .width() set on each row below).
        float cellWidth = w - butW - butW / 2;

        // Title
        dialog.row().height(h / 12).width(cellWidth);
        dialog.add(createTitleLabel(titleKey, cellWidth));

        // Message (wraps inside the dialog width)
        dialog.row().height(h / 8).width(cellWidth);
        dialog.add(createMessageLabel(messageKey, cellWidth));

        // Buttons row: [ Yes ] [ No ]
        dialog.row().height(h / 10);
        Table buttonRow = new Table(uiSkin);
        float yesW = butW * 1.8f;
        float noW = butW * 1.8f;

        TextButton yesBut = new TextButton(Di2048Game.game.bdl().get("dialogYes"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        fitButtonFont(yesBut.getLabel(), yesW);
        yesBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                animateHide();
                try {
                    onConfirm.run();
                } catch (Throwable t) {
                    Gdx.app.error("ConfirmDialog", "onConfirm threw", t);
                }
            }
        });
        buttonRow.add(yesBut).width(yesW).pad(10);

        TextButton noBut = new TextButton(Di2048Game.game.bdl().get("dialogNo"),
                uiSkin.get("red-but", TextButton.TextButtonStyle.class));
        fitButtonFont(noBut.getLabel(), noW);
        noBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                animateHide();
            }
        });
        buttonRow.add(noBut).width(noW).pad(10);

        dialog.add(buttonRow).colspan(1);

        stage.addActor(dialog);
    }

    /**
     * Title label — fitted to ~70% of cellWidth so there's room for the
     * message below it.
     */
    private Label createTitleLabel(String i18nKey, float cellWidth) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        label.setWrap(true);
        fitFontToWidth(label, cellWidth * 0.70f);
        return label;
    }

    /**
     * Message label — fitted to ~95% of cellWidth so it can use the full
     * available width for wrapping.
     */
    private Label createMessageLabel(String i18nKey, float cellWidth) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        label.setWrap(true);
        fitFontToWidth(label, cellWidth * 0.95f);
        return label;
    }

    /**
     * Button label fit — buttons are short ('Yes' / 'No' / 'Да' / 'Нет'),
     * so we want them big but not absurd. Cap at 0.8 of the button width.
     */
    private void fitButtonFont(Label label, float buttonWidth) {
        fitFontToWidth(label, buttonWidth * 0.8f);
    }

    /**
     * Iteratively find the largest font scale in [FONT_SCALE_FLOOR, FONT_SCALE_CEIL]
     * such that the label's preferred width fits inside targetWidth.
     *
     * Algorithm: start at FONT_SCALE_START (small), measure prefWidth. If it fits,
     * try the next step up; if it doesn't fit, stop and use the last scale that
     * fit. This is the OPPOSITE direction of Utils.textSizeTuning (which starts
     * at 3.0 and shrinks) — we start small and grow, which guarantees we end up
     * at a sensible scale even for very short text.
     *
     * layout() is called on every step so getPrefWidth() returns fresh data.
     */
    private void fitFontToWidth(Label label, float targetWidth) {
        float bestScale = FONT_SCALE_FLOOR;
        float scale = FONT_SCALE_START;
        while (scale <= FONT_SCALE_CEIL) {
            label.setFontScale(scale);
            label.layout();
            float prefWidth = label.getPrefWidth();
            if (prefWidth <= targetWidth) {
                bestScale = scale;
                scale += FONT_SCALE_STEP;
            } else {
                // This scale no longer fits — stop, keep the last good one.
                break;
            }
        }
        // Apply the best scale we found (may be the floor if nothing fit,
        // or the ceiling if even the largest scale fit).
        label.setFontScale(bestScale);
        label.layout();
        Gdx.app.log("ConfirmDialog", "fitFontToWidth: bestScale=" + bestScale
                + " prefWidth=" + label.getPrefWidth()
                + " targetWidth=" + targetWidth);
    }

    public void animateHide() {
        MoveToAction action = Actions.moveTo(dialog.getX(), h);
        action.setDuration(0.3f);
        dialog.addAction(action);
        // Lift the game pause so the player can resume swiping once the dialog
        // is dismissed. The caller is responsible for calling setPaused(true)
        // before showing the dialog.
        if (Di2048Game.game != null && Di2048Game.game.levelField != null) {
            Di2048Game.game.levelField.setPaused(false);
        }
    }

    public void animateOpen() {
        MoveToAction action = Actions.moveTo(dialog.getX(), h / 3f);
        action.setDuration(0.3f);
        dialog.addAction(action);
        // Pause the game while the dialog is on screen so background swipes
        // don't advance the board.
        if (Di2048Game.game != null && Di2048Game.game.levelField != null) {
            Di2048Game.game.levelField.setPaused(true);
        }
    }
}
