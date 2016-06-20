package ru.electronikas.diagonal.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

/**
 * User: navdonin
 * Date: 07/01/14
 */
public class Assets {

    private static AssetManager assets;

    public static AssetManager am() {
        if(assets==null) loadAssets();
        return assets;
    }

    private static void loadAssets() {
        assets = new AssetManager();
//        assets.load("data/models/tank_base2.obj", Model.class);
//        assets.load("data/models/tank_turel.obj", Model.class);
//        assets.load("data/sky/spacesphere.obj", Model.class);
//        assets.load("data/models/splash.obj", Model.class);
//        assets.load("data/models/kolobok.obj", Model.class);
//        assets.load("data/models/target.obj", Model.class);

        assets.finishLoading();
    }

    private static I18NBundle myBundle;

    public static I18NBundle bdl() {
        if(myBundle==null) {
            FileHandle baseFileHandle = Gdx.files.internal("i18n/gameBundle");
            Locale locale = Locale.getDefault();
            myBundle = I18NBundle.createBundle(baseFileHandle, locale);
        }
        return myBundle;
    }

    public static String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
