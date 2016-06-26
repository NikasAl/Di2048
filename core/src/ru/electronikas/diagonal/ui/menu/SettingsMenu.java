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
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.Textures;

import static ru.electronikas.diagonal.ui.Utils.textSizeTuning;

/**
 * Created by navdonin on 03/01/15.
 */
public class SettingsMenu {
    Table rateMenu;
    Skin uiSkin;
    float h = 0;

    private Float scaleForButtons;

    public SettingsMenu(Stage stage) {
        float w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        uiSkin = Textures.getUiSkin();
        rateMenu = new Table(uiSkin);
        rateMenu.align(Align.topLeft);
        rateMenu.setPosition(0, h);
        rateMenu.setWidth(w);
        rateMenu.setHeight(h);
        rateMenu.background("bluepane-t");

        rateMenu.row().height(h / 10).width(w);
        rateMenu.add(createHeader(w)).pad(10);

        rateMenu.row().height(h / 10);
        rateMenu.add(saveCurrentGameButton(w*0.8f)).pad(10).width(w*0.8f);

        rateMenu.row().height(h / 10);
        rateMenu.add(restoreCurrentGameButton()).pad(10).width(w*0.8f);

        rateMenu.setDebug(true);

        stage.addActor(rateMenu);

    }

    private Actor saveCurrentGameButton(float width) {
        TextButton saveCurrentGameBut = new TextButton(Assets.bdl().get("saveCurrentGame"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        scaleForButtons = textSizeTuning(saveCurrentGameBut.getLabel(), width, 70);
        saveCurrentGameBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Storage.saveGameStateM();
                animateHide();
            }
        });
        return saveCurrentGameBut;
    }

    private Actor restoreCurrentGameButton() {
        TextButton restoreCurrentGameBut = new TextButton(Assets.bdl().get("restoreCurrentGame"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        restoreCurrentGameBut.getLabel().setFontScale(scaleForButtons);
        restoreCurrentGameBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                DiGameModel gameModel = Storage.getMSavedGame();
                Di2048Game.game.createFromM(gameModel);
            }
        });
        return restoreCurrentGameBut;
    }

    private Actor createHeader(float width) {
        Label headLabel =  new Label(Assets.bdl().get("settings"), uiSkin);
        headLabel.setAlignment(Align.center);
        textSizeTuning(headLabel, width);
        return headLabel;
    }

    public void animateHide() {
        MoveToAction action = Actions.moveTo(0, h);
        action.setDuration(0.5f);
        rateMenu.addAction(action);
    }

    public void animateOpen() {
        MoveToAction action = Actions.moveTo(0, 0);
        action.setDuration(0.5f);
        rateMenu.addAction(action);

    }
}
