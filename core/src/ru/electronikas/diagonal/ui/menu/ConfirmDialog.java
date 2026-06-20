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
 * The dialog does NOT touch LevelField.isPause — the caller is expected
 * to setPaused(true) before showing the dialog (so background swipes
 * don't advance the board while the dialog is on screen). The dialog
 * will call setPaused(false) when it closes via either button.
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

        // Title
        dialog.row().height(h / 12).width(w - butW - butW / 2);
        dialog.add(createLabel(titleKey, w - butW, true));

        // Message (wraps inside the dialog width)
        dialog.row().height(h / 8).width(w - butW - butW / 2);
        dialog.add(createLabel(messageKey, w - butW - butW / 2, false));

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

    private Label createLabel(String i18nKey, float width, boolean isTitle) {
        Label label = new Label(Di2048Game.game.bdl().get(i18nKey), uiSkin);
        label.setAlignment(Align.center);
        label.setWrap(true);
        if (isTitle) {
            textSizeTuning(label, width, 70);
        } else {
            // Message: smaller font, wrap on word boundaries
            label.setFontScale(0.7f);
        }
        return label;
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
