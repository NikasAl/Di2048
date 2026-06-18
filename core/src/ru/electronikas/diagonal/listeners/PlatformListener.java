package ru.electronikas.diagonal.listeners;

/**
 * Bridge between platform-agnostic core (libGDX) and Android launcher.
 *
 * Removed in P0-8 (billing cleanup):
 *  - void removeAds(Product product)
 *  - import of Product
 *
 * Added:
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

    void trackEvent(String eventId);
}
