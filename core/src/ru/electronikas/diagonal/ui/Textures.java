package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * User: navdonin
 * Date: 25/11/13
 */
public class Textures {
    private static Skin uiSkin;

    public static Skin getUiSkin() {
        if(uiSkin == null) {
            uiSkin = new Skin(Gdx.files.internal("data/skins/mainatlas.json"));
        }
        return uiSkin;
    }

}
