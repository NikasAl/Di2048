package ru.electronikas.ads;

/**
 * Unified ad interface.
 * Only Yandex Mobile Ads is supported now (AdMob removed in P0-7).
 */
public interface UniAd {
    void initAd();
    void showAdsBanner();
    void hideAdsBanner();
    void showInterstitialVideo();
    void showRewardVideo(Runnable onReward);
}
