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
 *
 * Launch arguments (all optional, presets are auto-scaled to height=1080):
 *   -W &lt;width&gt;  - window width (no scaling)
 *   -H &lt;height&gt;   - window height (no scaling)
 *   --phone        - 360×640  -> 607.5x1080
 *   --chromebook   - 640×1136 -> 608x1080
 *   --tablet       - 800×1280 -> 675x1080
 *   --large        - 1080×1920 -> 607.5x1080
 *
 * Examples:
 *   java DesktopLauncher --phone
 *   java DesktopLauncher -W 480 -H 800
 */
public class DesktopLauncher implements PlatformListener {
        public static void main(String[] arg) {
                DesktopLauncher desktopLauncher = new DesktopLauncher();
                Locale.setDefault(new Locale("ru", "RU"));
//              Locale.setDefault(new Locale("en", "US"));

                int width = 700;
                int height = 1000;
                boolean scaleTo1080 = false;

                // Parse launch arguments for custom resolution
                for (int i = 0; i < arg.length; i++) {
                        switch (arg[i]) {
                                case "--phone":     // 9:16 (360×640)
                                        width = 360;
                                        height = 640;
                                        scaleTo1080 = false;
                                        break;
                                case "--chromebook": // 9:16 (640×1136)
                                        width = 640;
                                        height = 1136;
                                        scaleTo1080 = true;
                                        break;
                                case "--tablet":    // 5:8 (800×1280)
                                        width = 800;
                                        height = 1280;
                                        scaleTo1080 = true;
                                        break;
                                case "--large":     // 9:16 (1080×1920)
                                        width = 1080;
                                        height = 1920;
                                        scaleTo1080 = true;
                                        break;
                                case "-W":
                                        if (i + 1 < arg.length) width = Integer.parseInt(arg[++i]);
                                        scaleTo1080 = false;
                                        break;
                                case "-H":
                                        if (i + 1 < arg.length) height = Integer.parseInt(arg[++i]);
                                        scaleTo1080 = false;
                                        break;
                        }
                }

                // Scale to 1080 height if requested
                if (scaleTo1080) {
                        double scale = 1080.0 / height;
                        width = (int) Math.round(width * scale);
                        height = 1080;
                }

                LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = width;
                config.height = height;
                System.out.println("[INFO] Window: " + width + "x" + height);
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
