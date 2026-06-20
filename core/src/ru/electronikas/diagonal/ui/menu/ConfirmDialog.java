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

import static ru.electronikas.diagonal.ui.Utils.textSizeTuning;

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
 *  - Title:   uses Utils.textSizeTuning(label, cellWidth, 50) — the same
 *             pattern as SettingsMenu buttons. Produces a scale where the
 *             title's prefWidth <= 50% of cellWidth, so it fits comfortably.
 *  - Message: uses a custom iterative fitMessageFont() that starts at 0.6
 *             and steps down by 0.05 until prefWidth <= cellWidth (so the
 *             wrapped text fits the cell width without overflowing).
 *             Utils.textSizeTuning starts at maxScale=3.0 which is way too
 *             big for a long message — for a single line of body text we
 *             want a much smaller scale (typically 0.3-0.5).
 */
public class ConfirmDialog {

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
        textSizeTuning(yesBut.getLabel(), yesW, 70);
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
        textSizeTuning(noBut.getLabel(), noW, 70);
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
     * Title label: uses Utils.textSizeTuning(label, cellWidth, 50) so the
     * title's prefWidth fits in 50% of cellWidth — the same pattern as
     * SettingsMenu buttons. Produces a comfortable readable scale.
     */
    private Label createTitleLabel(String i18nKey, float cellWidth) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        label.setWrap(true);
        textSizeTuning(label, cellWidth, 50);
        return label;
    }

    /**
     * Message label: iterative fit that starts at a small scale (0.6) and
     * steps down by 0.05 until the message's prefWidth fits inside cellWidth.
     *
     * Why not use Utils.textSizeTuning: that helper starts at maxScale=3.0
     * (way too big for a 60-char message) and steps down by 0.1, so it
     * would either land on a too-large scale or take many iterations. For
     * a single-line body message we want a much smaller scale (0.3-0.5
     * typical), and a finer 0.05 step.
     */
    private Label createMessageLabel(String i18nKey, float cellWidth) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        label.setWrap(true);
        fitMessageFont(label, cellWidth);
        return label;
    }

    /**
     * Iteratively shrink the font scale from 0.6 down to 0.2 (in 0.05 steps)
     * until the label's preferred width fits inside cellWidth.
     * The message is wrapped (setWrap(true)), so this controls how many
     * lines the message takes — smaller scale => fewer lines but smaller
     * text. We pick the largest scale that still fits the cell width
     * without overflowing horizontally.
     */
    private void fitMessageFont(Label label, float cellWidth) {
        float scale = 0.6f;
        label.setFontScale(scale);
        label.layout();
        // Allow a small horizontal margin (use 95% of cellWidth as the target
        // so the wrapped text doesn't touch the cell edges).
        float target = cellWidth * 0.95f;
        while (scale > 0.2f && label.getPrefWidth() > target) {
            scale -= 0.05f;
            label.setFontScale(scale);
            label.layout();
        }
        Gdx.app.log("ConfirmDialog", "message fontScale=" + scale
                + " prefWidth=" + label.getPrefWidth() + " target=" + target);
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
