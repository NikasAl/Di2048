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
 * Responsibilities:
 *  - hold a single AdYandex instance
 *  - schedule the first banner show after a short delay
 *  - guard all calls against null (in case ads were stopped)
 *  - track game-over events and decide on interstitial frequency
 */
public class AdController implements UniAd {

    private static final String TAG = "AdController";

    /** Show interstitial every N-th game over. */
    private static final int INTERSTITIAL_EVERY_N_GAME_OVERS = 3;
    /** Minimum interval between two interstitial shows, in milliseconds. */
    private static final long INTERSTITIAL_MIN_INTERVAL_MS = 120_000L; // 2 minutes

    private AdYandex adYandex = null;
    private final Activity context;
    private final RelativeLayout layout;

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

        // Show banner shortly after init; Yandex SDK handles its own refresh afterwards.
        TimerTask showBannerTask = new TimerTask() {
            @Override
            public void run() {
                context.runOnUiThread(() -> showAdsBanner());
            }
        };
        new Timer().schedule(showBannerTask, 500);
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
        if (adYandex != null) {
            adYandex.hideAdsBanner();
        }
        adYandex = null;
    }
}
