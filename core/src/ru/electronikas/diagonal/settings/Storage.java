package ru.electronikas.diagonal.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import ru.electronikas.diagonal.model.ActiveRes;

/**
 * Created by navdonin on 09/01/15.
 */
public class Storage {
    public static final float DEFAULT_VAL = 0.5f;

    private static final String PREFS_NAME = "prefs_di2048";
    private static final String SOUND_VOLUME = "soundVolume";
//    public static final String MUSIC_VOLUME = "musicVolume";
    private static Preferences prefs;

    public static boolean isPurchaseActivated = false;

    public static boolean isAdWareShowing() {
//        boolean isAppFree = Mahjong3DBoxGame.requestHandler.isFreeApp();
//        boolean isAdWareShowing = isAppFree && !Storage.getBoolParam(ActiveRes.isAdwareRemoved);
        return true;
    }


    private static Preferences getPrefs() {
        if (prefs == null)
            prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs;
    }


    public static float getSoundVolume() {
        return getPrefs().getFloat(SOUND_VOLUME, DEFAULT_VAL);

    }

    public static void setSoundVolume(float volume) {
        getPrefs().putFloat(SOUND_VOLUME, volume);
        getPrefs().flush();
    }
/*
    public static float getMusicVolume() {
        return getPrefs().getFloat(MUSIC_VOLUME, DEFAULT_VAL);

    }

    public static void setMusicVolume(float volume) {
        getPrefs().putFloat(MUSIC_VOLUME, volume);
        getPrefs().flush();
    }*/

    public static void saveRecord(int rec) {
        getPrefs().putInteger("record", rec);
        getPrefs().flush();
    }

    public static <T> void saveParam(ActiveRes activeResource, T storingParam) {
        if (storingParam instanceof Boolean) {
            getPrefs().putBoolean(activeResource.name(), (Boolean) storingParam);
        }

        if (storingParam instanceof Integer)
            getPrefs().putInteger(activeResource.name(), (Integer) storingParam);

        if (storingParam instanceof String)
            getPrefs().putString(activeResource.name(), (String) storingParam);

        getPrefs().flush();
    }

    public static Boolean getBoolParam(ActiveRes activeResource) {
        return getPrefs().getBoolean(activeResource.name(), false);
    }

    public static Integer getIntParam(ActiveRes activeResource) {
//        if (activeResource.equals(ActiveRes.coins))
//            return getPrefs().getInteger(activeResource.name(), DEFAULT_COINS);
        return getPrefs().getInteger(activeResource.name(), 0);
    }

    public static String getStrParam(ActiveRes activeResource) {
        return getPrefs().getString(activeResource.name(), "");
    }
}
