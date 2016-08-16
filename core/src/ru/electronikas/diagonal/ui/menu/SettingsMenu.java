package ru.electronikas.diagonal.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.materials.Assets;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.Textures;
import ru.electronikas.diagonal.ui.Utils;

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

        float realW = w - w / 10;
        float padg = w/90;
        rateMenu.row().height(h / 10).width(realW);
        rateMenu.add(createHeader(realW)).colspan(2).pad(padg);

        rateMenu.row().height(h / 10);
        rateMenu.add(saveCurrentGameButton(realW)).colspan(2).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        rateMenu.add(restoreCurrentGameButton()).colspan(2).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        rateMenu.add(rateButton()).colspan(2).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        rateMenu.add(shareButton()).colspan(2).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        rateMenu.add(closeSettingsButton()).colspan(2).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        rateMenu.add(selectGameTypeButton()).colspan(2).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        rateMenu.add(newGameButton()).pad(padg).width(realW/2);
        rateMenu.add(quitGameButton()).pad(padg).width(realW/2);

//        rateMenu.setDebug(true);

        stage.addActor(rateMenu);

    }

    private Actor shareButton() {
        TextButton rateBut = new TextButton(Assets.bdl().get("share"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        rateBut.getLabel().setFontScale(scaleForButtons);
        rateBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                animateHideAndShare();
            }
        });
        return rateBut;
    }

    private Actor rateButton() {
        TextButton rateBut = new TextButton(Assets.bdl().get("rate"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        rateBut.getLabel().setFontScale(scaleForButtons);
        rateBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Di2048Game.game.platformListener.rate();
            }
        });
        return rateBut;
    }

    private Actor newGameButton() {
        TextButton newGameBut = new TextButton(Assets.bdl().get("newGame"),
                uiSkin.get("red-but", TextButton.TextButtonStyle.class));
        newGameBut.getLabel().setFontScale(scaleForButtons);
        newGameBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Storage.resetCurrentGame();
                Di2048Game.game.create();
            }
        });
        return newGameBut;
    }

    private Actor quitGameButton() {
        TextButton closeSettingsBut = new TextButton(Assets.bdl().get("quitGame"),
                uiSkin.get("red-but", TextButton.TextButtonStyle.class));
        closeSettingsBut.getLabel().setFontScale(scaleForButtons);
        closeSettingsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        return closeSettingsBut;
    }

    private Actor closeSettingsButton() {
        TextButton closeSettingsBut = new TextButton(Assets.bdl().get("resume"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        closeSettingsBut.getLabel().setFontScale(scaleForButtons);
        closeSettingsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                animateHide();
            }
        });
        return closeSettingsBut;
    }

    private Actor createLabel(String text, float width) {
        Label headLabel = new Label(text, uiSkin);
        headLabel.setAlignment(Align.center);
        textSizeTuning(headLabel, width);
        return headLabel;
    }

    private Actor selectGameTypeButton() {
        final SelectBox<String> selectBox = new SelectBox<String>(uiSkin);
        selectBox.setItems("3x3","4x4", "5x5", "6x6", "7x7", "8x8", "9x9", "10x10", "11x11", "12x12");
        int fieldTypeIndex = Storage.getCurrentFieldType() - 3;
        selectBox.setSelectedIndex(fieldTypeIndex);
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Storage.setFieldType(selectBox.getSelectedIndex()+3);
                Di2048Game.game.create();
            }
        });
        return selectBox;
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
                Di2048Game.game.createFromM();
            }
        });
        return restoreCurrentGameBut;
    }

    private Actor createHeader(float width) {
        Label headLabel = new Label(Assets.bdl().get("settings"), uiSkin);
        headLabel.setColor(Color.GREEN);
        headLabel.setAlignment(Align.center);
        textSizeTuning(headLabel, width);
        return headLabel;
    }

    public void animateHide() {
        MoveToAction action = Actions.moveTo(0, h);
        action.setDuration(0.5f);
        rateMenu.addAction(action);
        Di2048Game.game.platformListener.showFullScr();
    }

    public void animateHideAndShare() {
        MoveToAction action = Actions.moveTo(0, h);
        action.setDuration(0.5f);
        rateMenu.addAction(Actions.sequence(action, new Action() {
            @Override
            public boolean act(float delta) {
                Utils.saveScreenshot();
                Di2048Game.game.platformListener.share();
                return true;
            }
        }));
    }

    public void animateOpen() {
        MoveToAction action = Actions.moveTo(0, 0);
        action.setDuration(0.5f);
        rateMenu.addAction(action);

    }
}
