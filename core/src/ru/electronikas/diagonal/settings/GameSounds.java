package ru.electronikas.diagonal.settings;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

/**
 * Created by navdonin on 10/01/15.
 */
public class GameSounds {

    //TODO move to music and sound class
//    private static Music music;
    private static Sound flipSound;
    public static AssetManager assets;

    public static void soundsInit() {
        assets = new AssetManager();
        assets.clear();
        assets.load("data/sound/flip.ogg", Sound.class);
        assets.finishLoading();
        flipSound = assets.get("data/sound/flip.ogg");
//        flipSound = Gdx.audio.newSound(Gdx.files.internal("data/sound/flip.ogg"));
    }

/*
    public static void setMusicVolume(float volume) {
        music.setVolume(volume);
//        Storage.setMusicVolume(volume);
    }
*/

/*
    public static void musicStop() {
        if(music==null) return;
        music.stop();
    }
*/

/*
    public static void musicPlay() {
        musicStop();
        music = Gdx.audio.newMusic(Gdx.files.internal("data/sounds/cosmos.mp3"));
        setMusicVolume(Storage.getMusicVolume());
        music.setLooping(true);
        music.play();
    }
*/

    public static void flipSoundPlay() {
        if(flipSound!=null)
            flipSound.play(Storage.getSoundVolume());
    }

/*
    public static boolean isMusicPlaying() {
        return music!=null && music.isPlaying();
    }
*/

}
