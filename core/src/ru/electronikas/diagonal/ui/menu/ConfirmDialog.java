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
 * IMPLEMENTATION NOTES (v3 — learned from GameOverMenu):
 *  - We use the SAME font-scaling pattern as GameOverMenu: Utils.textSizeTuning
 *    WITHOUT setWrap(true). When wrap is on, Label.getPrefWidth() returns only
 *    the width of the longest SINGLE WORD, not the full text width — so any
 *    iterative fit that compares prefWidth to a target will think the text
 *    fits when it actually doesn't (the multi-line layout will overflow the
 *    cell vertically and overlap the row below).
 *  - Instead, leave wrap OFF. The text stays on one line, and textSizeTuning
 *    shrinks the font until the one-line prefWidth <= width/2 (the default
 *    target when no percent is given). For long messages this produces a
 *    small but readable font, matching how GameOverMenu renders its long
 *    button labels ("Del 2s Continue", "Try Again", etc.).
 *  - Row heights: do NOT set fixed .height() on title/message rows — let the
 *    Table auto-size them based on the label's prefHeight at the chosen
 *    scale. Fixed heights were causing vertical overflow when the label's
 *    prefHeight exceeded the assigned row height (the text spilled into
 *    the next row's area, producing the 'overlapping' visual the user saw).
 *
 * On 'Yes': runs the supplied onConfirm Runnable, then auto-closes.
 * On 'No' : just closes.
 *
 * The dialog pauses the game (LevelField.setPaused(true)) on open and
 * unpauses on close, so background swipes don't advance the board while
 * the dialog is on screen.
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

        // Effective cell width for the title and message rows.
        float cellWidth = w - butW - butW / 2;

        // Title — auto row height, single line, font tuned to fit width/2
        // (same pattern as GameOverMenu.createHeader).
        dialog.row().width(cellWidth);
        dialog.add(createTitleLabel(titleKey, cellWidth));

        // Message — auto row height, single line, font tuned to fit width*0.7
        // (slightly larger target than title because the message is longer and
        // we want it to remain readable).
        dialog.row().width(cellWidth).padTop(h / 40);
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
     * Title label — uses Utils.textSizeTuning(label, width) which is the SAME
     * pattern as GameOverMenu.createHeader(). Produces a scale where
     * prefWidth <= width/2. No wrap — text stays on one line.
     */
    private Label createTitleLabel(String i18nKey, float cellWidth) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        // NO setWrap(true) — see class javadoc for why.
        textSizeTuning(label, cellWidth);
        Gdx.app.log("ConfirmDialog", "title scale set, prefWidth=" + label.getPrefWidth()
                + " cellWidth=" + cellWidth);
        return label;
    }

    /**
     * Message label — uses Utils.textSizeTuning(label, width, 70) which is the
     * SAME pattern as GameOverMenu.procceedButton (60-70% of width). Produces a
     * scale where prefWidth <= width*0.7. No wrap — text stays on one line.
     */
    private Label createMessageLabel(String i18nKey, float cellWidth) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        // NO setWrap(true) — see class javadoc for why.
        textSizeTuning(label, cellWidth, 70);
        Gdx.app.log("ConfirmDialog", "message scale set, prefWidth=" + label.getPrefWidth()
                + " cellWidth=" + cellWidth);
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
