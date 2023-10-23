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
import ru.electronikas.diagonal.materials.Assets;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.Textures;

import static ru.electronikas.diagonal.ui.Utils.currentScale;
import static ru.electronikas.diagonal.ui.Utils.textSizeTuning;

/**
 * Created by navdonin on 03/01/15.
 */
public class GameOverMenu {
    Table rateMenu;
    Skin uiSkin;
    float butW = 0;
    float h = 0;

    public GameOverMenu(Stage stage) {
        float w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        butW = w / 8f;

        uiSkin = Textures.getUiSkin();
        rateMenu = new Table(uiSkin);
        rateMenu.align(Align.topLeft);
        rateMenu.setPosition(butW / 2, h);
        rateMenu.setWidth(w - butW);
        rateMenu.setHeight(h / 2.3f);
        rateMenu.background("bluepane-t");

        rateMenu.row().height(h / 10).width(w - butW - butW/2);
        rateMenu.add(createHeader(w - butW));

        rateMenu.row().height(h / 10);
        rateMenu.add(procceedButton(butW * 4f)).pad(10).width(butW * 4f);

        rateMenu.row().height(h / 10);
        rateMenu.add(tryAgaingButton()).pad(10).width(butW * 4f);

//        rateMenu.setDebug(true);

        stage.addActor(rateMenu);

    }

    private Actor tryAgaingButton() {
        TextButton openTipsBut = new TextButton(Assets.bdl().get("tryAgain"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        openTipsBut.getLabel().setFontScale(currentScale);
        openTipsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Storage.resetCurrentGame();
                Di2048Game.game.create();
                animateHide();
            }
        });
        return openTipsBut;
    }

    private Actor procceedButton(float width) {
        TextButton openTipsBut = new TextButton(Assets.bdl().get("removeAndGo"),
                uiSkin.get("magenta-but", TextButton.TextButtonStyle.class));
        textSizeTuning(openTipsBut.getLabel(), width, 80);
        openTipsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if(!Storage.isAdWareShowing()) {
                    Di2048Game.game.del2s();
                    return;
                }
                Di2048Game.game.platformListener.showFullScr();
            }
        });
        return openTipsBut;
    }

    private Actor createHeader(float width) {
        Label headLabel =  new Label(Assets.bdl().get("gameOver"), uiSkin);
        headLabel.setAlignment(Align.center);
        textSizeTuning(headLabel, width);
        return headLabel;
    }

    public void animateHide() {
        MoveToAction action = Actions.moveTo(butW / 2, h);
        action.setDuration(0.5f);
        rateMenu.addAction(action);
    }

    public void animateOpen() {
        MoveToAction action = Actions.moveTo(butW / 2, h/4);
        action.setDuration(0.5f);
        rateMenu.addAction(action);

    }
}
