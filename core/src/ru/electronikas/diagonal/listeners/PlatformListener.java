package ru.electronikas.diagonal.listeners;

/**
 * Bridge between platform-agnostic core (libGDX) and Android launcher.
 *
 * P1-4: added onGameOver() — AdController decides whether to show an
 * interstitial based on frequency cap (every 3rd game-over, no more than
 * once per 2 minutes).
 *
 * Removed in P0-8 (billing cleanup):
 *  - void removeAds(Product product)
 *  - import of Product
 *
 * Added in P0-8:
 *  - showInterstitial()           -> used by GameOverMenu frequency-capped interstitial
 *  - showRewardVideo(Runnable)    -> callback-based rewarded (replaces legacy showFullScr)
 */
public interface PlatformListener {

    void share();
    void rate();

    void showBanner();
    void hideBanner();

    /** Legacy rewarded entry point (no callback). Prefer {@link #showRewardVideo(Runnable)}. */
    void showFullScr();

    /** Show interstitial ad (frequency-capped by AdController). */
    void showInterstitial();

    /** Show rewarded ad and invoke onReward after the user earns the reward. */
    void showRewardVideo(Runnable onReward);

    /**
     * Called by the core game whenever a Game Over occurs.
     * The implementation may decide to show an interstitial based on
     * a frequency cap (every N-th game over, no more than once per M minutes).
     */
    void onGameOver();

    void trackEvent(String eventId);
}
