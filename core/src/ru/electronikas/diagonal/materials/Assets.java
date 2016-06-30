package ru.electronikas.diagonal.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

/**
 * User: navdonin
 * Date: 07/01/14
 */
public class Assets {

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
