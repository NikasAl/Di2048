package ru.electronikas.diagonal.settings;

import com.badlogic.gdx.utils.Timer;
import ru.electronikas.diagonal.Di2048Game;

/**
 * Created by nikas on 8/6/16.
 */
public class AdBannerController {


    Timer.Task adsBannerOffTask = new Timer.Task(){
        @Override
        public void run() {
            Di2048Game.game.platformListener.hideBanner();
        }
    };


    Timer.Task adsBannerOnTask = new Timer.Task(){
        @Override
        public void run() {
            Di2048Game.game.platformListener.showBanner();
            Timer.schedule(adsBannerOffTask, 30f);
        }
    };


    public void start() {
        Timer.schedule(adsBannerOnTask, 40f, 130f);
    }
}
