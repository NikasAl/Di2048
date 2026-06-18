package ru.electronikas.diagonal.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.listeners.PlatformListener;

import java.util.Locale;

/**
 * Desktop launcher (debug / dev only).
 *
 * Updated in P0-11 to match the new PlatformListener interface
 * (removed removeAds(Product); added showInterstitial() and showRewardVideo(Runnable)).
 */
public class DesktopLauncher implements PlatformListener {
        public static void main(String[] arg) {
                DesktopLauncher desktopLauncher = new DesktopLauncher();
                Locale.setDefault(new Locale("ru", "RU"));
//              Locale.setDefault(new Locale("en", "US"));
                LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = 700;
                config.height = 1000;
                new LwjglApplication(new Di2048Game(desktopLauncher), config);
        }

        @Override
        public void share() {
                Gdx.app.log("INFO", "share open");
        }

        @Override
        public void rate() {
                Gdx.app.log("INFO", "rate open");
        }

        @Override
        public void showBanner() {
        }

        @Override
        public void hideBanner() {
        }

        @Override
        public void showFullScr() {
        }

        @Override
        public void showInterstitial() {
        }

        @Override
        public void showRewardVideo(Runnable onReward) {
                // Desktop dev stub: no real ad SDK available, just fire the callback
                if (onReward != null) {
                        onReward.run();
                }
        }

        @Override
        public void onGameOver() {
                // Desktop dev stub: no interstitial on desktop
        }

        @Override
        public void trackEvent(String eventId) {
        }
}
