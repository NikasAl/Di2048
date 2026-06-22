package ru.electronikas.diagonal.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.settings.GameSounds;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.Utils;

import static ru.electronikas.diagonal.ui.Utils.textSizeTuning;

/**
 * Created by navdonin on 03/01/15.
 */
public class SettingsMenu {
    Table rateMenu;
    Skin uiSkin;
    float h = 0;
    /** Effective content width — saved as a field so helper methods
     *  (selectGameTypeButton, etc.) can use it for font fitting. */
    float realW = 0;

    private Float scaleForButtons;

    public SettingsMenu(Stage stage) {
        float w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        uiSkin = Di2048Game.game.getUiSkin();
        rateMenu = new Table(uiSkin);
        rateMenu.align(Align.topLeft);
        rateMenu.setPosition(0, h);
        rateMenu.setWidth(w);
        rateMenu.setHeight(h);
        rateMenu.background("bluepane-t");

        realW = w - w / 10;
        float padg = w/90;
        rateMenu.row().height(h / 10);
        rateMenu.add(createHeader((realW))).width(realW*0.75f).colspan(3);
        rateMenu.add(soundButton()).width(realW*0.25f);

/*
        rateMenu.row().height(h / 10);
        rateMenu.add(saveCurrentGameButton(realW)).colspan(2).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        rateMenu.add(restoreCurrentGameButton()).colspan(2).pad(padg).width(realW);
*/

        rateMenu.row().height(h / 10);
        rateMenu.add(rateButton(realW)).colspan(4).pad(padg).width(realW);

        // P0-9: removed payForOneDayAdsRemovingButton (RuStore Billing removed)

        rateMenu.row().height(h / 10);
        rateMenu.add(closeSettingsButton()).colspan(4).pad(padg).width(realW);

        rateMenu.row().height(h / 10);
        // P4: .center() on the cell centers the SelectBox horizontally inside its
        // (realW-wide) cell — without it, libGDX defaults to left alignment and the
        // SelectBox hugs the left edge of the settings panel.
        rateMenu.add(selectGameTypeButton()).colspan(4).pad(padg).width(realW).center();
        rateMenu.clip();
        rateMenu.row().height(h / 10);
        rateMenu.add(newGameButton()).pad(padg).width(realW/2).colspan(2);
        rateMenu.add(quitGameButton()).pad(padg).width(realW/2).colspan(2);

//        rateMenu.setDebug(true);

        stage.addActor(rateMenu);

    }

    private Actor shareButton() {
        TextButton rateBut = new TextButton(Di2048Game.game.bdl().get("share"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        rateBut.getLabel().setFontScale(scaleForButtons);
        rateBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                animateHideAndShare();
            }
        });
        return rateBut;
    }

    private Actor rateButton(float width) {
        TextButton rateBut = new TextButton(Di2048Game.game.bdl().get("rate"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        scaleForButtons = textSizeTuning(rateBut.getLabel(), width, 50);
        rateBut.getLabel().setFontScale(scaleForButtons);
        rateBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Di2048Game.game.platformListener.rate();
            }
        });
        return rateBut;
    }

    private Actor newGameButton() {
        TextButton newGameBut = new TextButton(Di2048Game.game.bdl().get("newGame"),
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
        TextButton closeSettingsBut = new TextButton(Di2048Game.game.bdl().get("quitGame"),
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
        TextButton closeSettingsBut = new TextButton(Di2048Game.game.bdl().get("resume"),
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
        // P1-fix: build a custom SelectBoxStyle whose font has a scale that
        // adapts to the available width. The default skin style uses test.fnt
        // (DejaVu Sans 72pt) at scale 1.0 which is way too big for most screens
        // and doesn't recalibrate on resize().
        //
        // We clone the default style, clone its font, and iteratively fit the
        // font scale to ~70% of the cell width (same target as GameOverMenu
        // buttons). The longest item in the list ("12x12" — 5 chars) is used
        // as the fit reference so all items render at the same scale.
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle(
                uiSkin.get(SelectBox.SelectBoxStyle.class));

        // Clone the font by loading it fresh from the .fnt file — this avoids
        // mutating the shared skin font (which other UI elements use).
        BitmapFont scaledFont = new BitmapFont(
                Gdx.files.internal("data/skins/test.fnt"),
                Gdx.files.internal("data/skins/test.png"),
                false);
        // Iteratively fit scale to the longest list item ("12x12").
        // Use a temporary GlyphLayout to measure text width at each scale.
        String longestItem = "12x12";  // longest among "3x3".."12x12"
        float targetWidth = realW * 0.7f;
        float scale = 0.6f;
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        scaledFont.getData().setScale(scale);
        layout.setText(scaledFont, longestItem);
        while (scale > 0.2f && layout.width > targetWidth) {
            scale -= 0.05f;
            scaledFont.getData().setScale(scale);
            layout.setText(scaledFont, longestItem);
        }
        Gdx.app.log("SettingsMenu", "SelectBox font scale=" + scale
                + " textWidth=" + layout.width + " target=" + targetWidth);
        style.font = scaledFont;

        // Also scale the list-style font so the dropdown items render at the
        // same scale as the selected item.
        if (style.listStyle != null && style.listStyle.font != null) {
            BitmapFont listFont = style.listStyle.font;
            // Clone the list font too — but it's the same default-font, so
            // we can just reuse the scaledFont reference.
            style.listStyle.font = scaledFont;
        }

        final SelectBox<String> selectBox = new SelectBox<String>(style);
        selectBox.setItems("3x3","4x4", "5x5", "6x6", "7x7", "8x8", "9x9", "10x10", "11x11", "12x12");
        int fieldTypeIndex = Storage.getCurrentFieldType() - 3;
        selectBox.setSelectedIndex(fieldTypeIndex);
        // P4: center the selected-item text inside the SelectBox (was left-aligned).
        selectBox.setAlignment(Align.center);
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
        TextButton saveCurrentGameBut = new TextButton(Di2048Game.game.bdl().get("saveCurrentGame"),
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

    private Actor soundButton() {
        Drawable on = new Image(new Texture("data/skins/ns128.png")).getDrawable();
        Drawable off = new Image(new Texture("data/skins/nsoff128.png")).getDrawable();
        ImageButton soundBut = new ImageButton(on, off, off);
        if(Storage.getSoundVolume() < Storage.DEFAULT_VAL)
            soundBut.setChecked(true);
        soundBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if(Storage.getSoundVolume() < Storage.DEFAULT_VAL) {
                    Storage.setSoundVolume(Storage.DEFAULT_VAL);
                    GameSounds.flipSoundPlay();
                    Di2048Game.game.platformListener.trackEvent("Sound_On");
                } else {
                    Storage.setSoundVolume(0);
                    Di2048Game.game.platformListener.trackEvent("Sound_Off");
                }
            }
        });
        return soundBut;
    }

    private Actor restoreCurrentGameButton() {
        TextButton restoreCurrentGameBut = new TextButton(Di2048Game.game.bdl().get("restoreCurrentGame"),
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
        Label headLabel = new Label(Di2048Game.game.bdl().get("settings"), uiSkin);
        headLabel.setColor(Color.GREEN);
        headLabel.setAlignment(Align.center);
        textSizeTuning(headLabel, width);
        return headLabel;
    }

    public void animateHide() {
        MoveToAction action = Actions.moveTo(0, h);
        action.setDuration(0.5f);
        rateMenu.addAction(action);
        // P1-fix: lift the pause as soon as the close animation starts so the
        // player can resume swiping immediately (the 0.5s slide-out animation
        // is purely visual, the game state is not affected by it).
        if (Di2048Game.game != null && Di2048Game.game.levelField != null) {
            Di2048Game.game.levelField.setPaused(false);
        }
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
        if (Di2048Game.game != null && Di2048Game.game.levelField != null) {
            Di2048Game.game.levelField.setPaused(false);
        }
    }

    public void animateOpen() {
        MoveToAction action = Actions.moveTo(0, 0);
        action.setDuration(0.5f);
        rateMenu.addAction(action);
        // P1-fix: pause the game BEFORE the slide-in animation starts so any
        // fling that overlaps with the opening transition is also swallowed.
        if (Di2048Game.game != null && Di2048Game.game.levelField != null) {
            Di2048Game.game.levelField.setPaused(true);
        }
    }
}
