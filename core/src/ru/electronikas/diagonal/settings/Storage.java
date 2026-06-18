package ru.electronikas.diagonal.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;

import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.model.ActiveRes;
import ru.electronikas.diagonal.model.DiGameModel;

/**
 * Persisted game state wrapper around libGDX Preferences.
 *
 * Removed in P0-10 (billing/adware cleanup):
 *  - setShowAds(boolean)
 *  - isAdWareShowing()
 *  - setNoAdTime()
 *  - isNoAdTimeOver()
 *  - java.util.Calendar / java.util.Date imports
 */
public class Storage {
    public static final float DEFAULT_VAL = 0.3f;

    private static final String PREFS_NAME = "prefs_di2048";
    private static final String SOUND_VOLUME = "soundVolume";
    private static final String GAME_MODEL_JSON = "game_";
    private static final String SAVE_GAME_MODEL_JSON = "memgame_";

    private static Preferences prefs;

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

    public static <T> void saveParam(ActiveRes activeResource, T storingParam) {
        if (storingParam instanceof Boolean) {
            getPrefs().putBoolean(activeResource.name(), (Boolean) storingParam);
        }

        if (storingParam instanceof Integer)
            getPrefs().putInteger(activeResource.name(), (Integer) storingParam);

        if (storingParam instanceof Long)
            getPrefs().putLong(activeResource.name(), (Long) storingParam);

        if (storingParam instanceof String)
            getPrefs().putString(activeResource.name(), (String) storingParam);

        getPrefs().flush();
    }

    public static Boolean getBoolParam(ActiveRes activeResource, boolean b) {
        return getPrefs().getBoolean(activeResource.name(), b);
    }

    public static Integer getIntParam(ActiveRes activeResource) {
        if (activeResource.equals(ActiveRes.gameFieldType))
            return getPrefs().getInteger(activeResource.name(), 4);
        return getPrefs().getInteger(activeResource.name(), 0);
    }

    public static String getStrParam(ActiveRes activeResource) {
        return getPrefs().getString(activeResource.name(), "");
    }

    private static Json json = new Json();
    public static DiGameModel getGameFromFileByFieldSize(Integer fieldSize) {
        String gameStringJSON = getPrefs().getString(GAME_MODEL_JSON + fieldSize, "");
        if (!"".equals(gameStringJSON)) {
            DiGameModel diGameModel = json.fromJson(DiGameModel.class, gameStringJSON);
            return diGameModel;
        } else {
            return new DiGameModel(fieldSize);
        }
    }

    public static DiGameModel getMSavedGame() {
        String gameStringJSON = getPrefs().getString(SAVE_GAME_MODEL_JSON + getCurrentFieldType(), "");
        if (!"".equals(gameStringJSON)) {
            DiGameModel diGameModel = json.fromJson(DiGameModel.class, gameStringJSON);
            return diGameModel;
        } else {
            return new DiGameModel(getCurrentFieldType());
        }
    }

    public static int getCurrentFieldType() {
        return getIntParam(ActiveRes.gameFieldType);
    }

    public static void saveGameState(DiGameModel diGameModel) {
        String gameJSON = json.prettyPrint(diGameModel);
        getPrefs().putString(GAME_MODEL_JSON + getCurrentFieldType(), gameJSON);
        getPrefs().flush();
    }

    public static void saveGameStateM() {
        DiGameModel currentGame = Di2048Game.game.diGameModel;
        String gameJSON = json.prettyPrint(currentGame);
        getPrefs().putString(SAVE_GAME_MODEL_JSON + getCurrentFieldType(), gameJSON);
        getPrefs().flush();
    }

    public static void resetCurrentGame() {
        getPrefs().putString(GAME_MODEL_JSON + getCurrentFieldType(), "");
        getPrefs().flush();
    }

    public static Integer getRecord() {
        return getPrefs().getInteger(ActiveRes.record.name() + getCurrentFieldType(), 0);
    }

    public static void saveScoreAsRecord(int score) {
        getPrefs().putInteger(ActiveRes.record.name() + getCurrentFieldType(), score);
        getPrefs().flush();
    }

    public static void setFieldType(int fieldType) {
        saveParam(ActiveRes.gameFieldType, fieldType);
    }
}
