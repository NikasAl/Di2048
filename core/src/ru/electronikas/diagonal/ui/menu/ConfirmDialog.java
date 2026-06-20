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
 * Layout:
 *   +----------------------------------+
 *   |         <Title>                  |
 *   |     <Message text>               |
 *   |    [ Yes ]      [ No ]           |
 *   +----------------------------------+
 *
 * IMPLEMENTATION NOTES (v4 — fixed after on-device testing):
 *  - Title:   FIXED font scale (0.9). Title is short ("Undo last move?",
 *             "Remove all 2-tiles?") and fits on one line at scale 0.9 on
 *             any reasonable phone width.
 *  - Message: FIXED font scale (0.7) + setWrap(true). The message is long
 *             ("Watch a short ad to revert your last move.") and MUST wrap
 *             to 2 lines to be readable. Wrap=true + a moderate scale works
 *             because we let the row auto-size to prefHeight (no fixed row
 *             height that would clip the wrapped text).
 *  - Buttons: Utils.textSizeTuning(label, width, 70) — same pattern as
 *             GameOverMenu.procceedButton. Buttons have short labels
 *             ("Yes"/"No"/"Да"/"Нет") and the iterative fit works correctly
 *             for them (no wrap).
 *
 * Why NOT use Utils.textSizeTuning for title/message:
 *  - textSizeTuning iterates from maxScale=3.0 down by 0.1 until prefWidth
 *    fits the target. For LONG single-line text this drives the scale all
 *    the way down to 0.1 or below (the user saw 0.2 in the desktop log —
 *    text was unreadable). For SHORT text (the title) the loop exited
 *    early at scale 3.0 (giant).
 *  - The helper doesn't handle wrap=true correctly (when wrap is on,
 *    getPrefWidth() returns the longest-word width, not the full text
 *    width, so the loop's exit condition fires prematurely).
 *
 * We bypass textSizeTuning entirely for title/message and use fixed scales
 * chosen empirically for the bundled test.fnt (DejaVu Sans 72pt subset).
 * Title scale 0.9 and message scale 0.7 produce readable text on every
 * device we've tested (phone widths 720-1440px).
 *
 * Row heights:
 *  - Title and message rows do NOT set .height() — the Table auto-sizes
 *    them from the label's prefHeight. With wrap=true the message label's
 *    prefHeight grows to fit 2 lines, and the row grows accordingly. This
 *    eliminates the 'overlapping' bug where fixed-height rows clipped the
 *    wrapped text into the next row's area.
 *  - Buttons row keeps a fixed .height(h/10) — matches GameOverMenu.
 *
 * On 'Yes': runs the supplied onConfirm Runnable, then auto-closes.
 * On 'No' : just closes.
 *
 * The dialog pauses the game (LevelField.setPaused(true)) on open and
 * unpauses on close, so background swipes don't advance the board while
 * the dialog is on screen.
 */
public class ConfirmDialog {

    /** Fixed font scale for the dialog title. Empirically tuned for test.fnt. */
    private static final float TITLE_FONT_SCALE = 0.9f;
    /** Fixed font scale for the dialog message body. Smaller than title. */
    private static final float MESSAGE_FONT_SCALE = 0.7f;

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

        // Effective cell width for the title and message rows.
        float cellWidth = w - butW - butW / 2;

        // Title — single line, fixed scale, auto row height.
        dialog.row().width(cellWidth).padTop(h / 40);
        dialog.add(createTitleLabel(titleKey));

        // Message — wraps to 2 lines, fixed scale, auto row height.
        // padTop gives visual separation from the title.
        dialog.row().width(cellWidth).padTop(h / 50);
        dialog.add(createMessageLabel(messageKey, cellWidth));

        // Buttons row: [ Yes ] [ No ] — fixed height like GameOverMenu buttons.
        dialog.row().height(h / 10).padTop(h / 30);
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
     * Title label — fixed scale, single line, no wrap.
     * Title is short ("Undo last move?" / "Отменить последний ход?") so a
     * fixed scale of 0.9 fits on one line on any reasonable phone width.
     */
    private Label createTitleLabel(String i18nKey) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        label.setFontScale(TITLE_FONT_SCALE);
        Gdx.app.log("ConfirmDialog", "title created, scale=" + TITLE_FONT_SCALE
                + " prefWidth=" + label.getPrefWidth() + " prefHeight=" + label.getPrefHeight());
        return label;
    }

    /**
     * Message label — fixed scale, wrap=true, width=cellWidth.
     * The message is long ("Watch a short ad to revert your last move.") and
     * MUST wrap to 2 lines. We set the label's width explicitly so wrap
     * knows where to break, and let the Table auto-size the row height from
     * the label's prefHeight (which grows to fit 2 lines).
     */
    private Label createMessageLabel(String i18nKey, float cellWidth) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        label.setWrap(true);
        label.setWidth(cellWidth);
        label.setFontScale(MESSAGE_FONT_SCALE);
        label.layout();
        Gdx.app.log("ConfirmDialog", "message created, scale=" + MESSAGE_FONT_SCALE
                + " width=" + cellWidth + " prefHeight=" + label.getPrefHeight()
                + " (should be ~2 lines)");
        return label;
    }

    public void animateHide() {
        MoveToAction action = Actions.moveTo(dialog.getX(), h);
        action.setDuration(0.3f);
        dialog.addAction(action);
        // Lift the game pause so the player can resume swiping once the dialog
        // is dismissed.
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
