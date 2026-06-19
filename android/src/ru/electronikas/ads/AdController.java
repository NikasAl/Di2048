package ru.electronikas.ads;

import android.app.Activity;
import android.widget.RelativeLayout;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * AdController — thin wrapper over AdYandex.
 *
 * P1-4: added onGameOver() with frequency-capped interstitial:
 *   - shows interstitial every 3rd game over
 *   - never more than once per 2 minutes (so a frustrated player who dies
 *     repeatedly isn't bombarded)
 *   - skips if no ad is loaded (no blocking)
 *
 * P1-fix (banner refresh): the previous single-shot 'show banner after 500ms'
 * design left the banner stuck after the app was paused and resumed (the Yandex
 * SDK's internal refresh stops firing when the activity goes through onPause/onResume
 * without the BannerAdView being explicitly re-loaded). This commit brings back
 * a periodic banner reload (every 20 seconds) AND adds an onResume() hook that
 * immediately reloads the banner so users see a fresh ad within 1 second of
 * returning to the app.
 *
 * Responsibilities:
 *  - hold a single AdYandex instance
 *  - schedule the first banner show + periodic banner refresh (every 20 s)
 *  - expose onResume() for the AndroidLauncher lifecycle hook
 *  - guard all calls against null (in case ads were stopped)
 *  - track game-over events and decide on interstitial frequency
 */
public class AdController implements UniAd {

    private static final String TAG = "AdController";

    /** Show interstitial every N-th game over. */
    private static final int INTERSTITIAL_EVERY_N_GAME_OVERS = 3;
    /** Minimum interval between two interstitial shows, in milliseconds. */
    private static final long INTERSTITIAL_MIN_INTERVAL_MS = 120_000L; // 2 minutes

    /** Periodic banner refresh interval, in milliseconds. */
    private static final long BANNER_REFRESH_INTERVAL_MS = 20_000L; // 20 seconds
    /** Initial banner-show delay, in milliseconds. */
    private static final long BANNER_INITIAL_DELAY_MS = 500L;

    private AdYandex adYandex = null;
    private final Activity context;
    private final RelativeLayout layout;

    private Timer bannerRefreshTimer = null;

    private int gameOverCounter = 0;
    private long lastInterstitialShownAt = 0L;

    public AdController(Activity context, RelativeLayout layout) {
        this.context = context;
        this.layout = layout;
    }

    @Override
    public void initAd() {
        adYandex = new AdYandex(context, layout);
        adYandex.initAd();

        // Schedule periodic banner refresh.
        // Yandex Mobile Ads SDK performs its own server-driven refresh, but in practice
        // the BannerAdView can stop refreshing after the activity is paused and resumed
        // (especially on Android 12+ where onPause/onResume fire aggressively). A
        // client-side periodic reload every 20s is a robust safety net that guarantees
        // a fresh ad impression at least every 20s during active play.
        startBannerRefreshTimer();
    }

    private void startBannerRefreshTimer() {
        stopBannerRefreshTimer();
        bannerRefreshTimer = new Timer("AdController-BannerRefresh", true);
        bannerRefreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                context.runOnUiThread(() -> {
                    if (adYandex != null) {
                        adYandex.showAdsBanner();
                    }
                });
            }
        }, BANNER_INITIAL_DELAY_MS, BANNER_REFRESH_INTERVAL_MS);
    }

    private void stopBannerRefreshTimer() {
        if (bannerRefreshTimer != null) {
            bannerRefreshTimer.cancel();
            bannerRefreshTimer = null;
        }
    }

    /**
     * P1-fix: called from AndroidLauncher.onResume() to immediately reload the
     * banner after the user returns to the app. Without this, the banner would
     * stay blank (or show a stale ad) until the next 20s tick.
     */
    public void onResume() {
        context.runOnUiThread(() -> {
            if (adYandex != null) {
                adYandex.showAdsBanner();
            }
            // Make sure the periodic timer is alive (it can die after process restart).
            if (bannerRefreshTimer == null) {
                startBannerRefreshTimer();
            }
        });
    }

    /**
     * P1-fix: called from AndroidLauncher.onPause() to stop the periodic timer.
     * The banner itself stays loaded; we just stop hammering it with reloads
     * while the app is not visible.
     */
    public void onPause() {
        stopBannerRefreshTimer();
    }

    @Override
    public void showAdsBanner() {
        if (adYandex == null) return;
        adYandex.showAdsBanner();
    }

    @Override
    public void hideAdsBanner() {
        if (adYandex == null) return;
        adYandex.hideAdsBanner();
    }

    @Override
    public void showInterstitialVideo() {
        if (adYandex == null) return;
        adYandex.showInterstitialVideo();
    }

    @Override
    public void showRewardVideo(Runnable onReward) {
        if (adYandex == null) return;
        adYandex.showRewardVideo(onReward);
    }

    /**
     * P1-4: Called from GameOverMenu constructor (via PlatformListener.onGameOver).
     * Increments the game-over counter and shows an interstitial if both:
     *   - counter is a multiple of {@link #INTERSTITIAL_EVERY_N_GAME_OVERS}
     *   - at least {@link #INTERSTITIAL_MIN_INTERVAL_MS} has elapsed since the last interstitial
     * Always returns silently if no interstitial is loaded or the cap blocks it.
     */
    public void maybeShowInterstitialOnGameOver() {
        gameOverCounter++;
        if (gameOverCounter % INTERSTITIAL_EVERY_N_GAME_OVERS != 0) {
            Log.d(TAG, "Game over #" + gameOverCounter + " — interstitial skipped (not Nth)");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastInterstitialShownAt < INTERSTITIAL_MIN_INTERVAL_MS) {
            Log.d(TAG, "Game over #" + gameOverCounter + " — interstitial skipped (rate limit)");
            return;
        }
        lastInterstitialShownAt = now;
        Log.d(TAG, "Game over #" + gameOverCounter + " — showing interstitial");
        showInterstitialVideo();
    }

    public void stopAds() {
        stopBannerRefreshTimer();
        if (adYandex != null) {
            adYandex.hideAdsBanner();
        }
        adYandex = null;
    }
}
