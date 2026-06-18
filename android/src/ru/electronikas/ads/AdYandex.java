package ru.electronikas.ads;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.YandexAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

/**
 * AdYandex — single ad provider (Yandex Mobile Ads 8.x).
 *
 * Migrated from 7.x to 8.x in P0-7:
 *  - MobileAds.initialize -> YandexAds.INSTANCE.initialize
 *  - BannerAdSize.fixedSize/inlineSize -> BannerAdSize.inline
 *  - BannerAdView.setAdUnitId removed — ad unit id now passed via AdRequest.Builder(id)
 *  - loader.setAdLoadListener + loadAd(AdRequestConfiguration) -> loader.loadAd(AdRequest, listener)
 *  - AdRequestConfiguration.Builder(id) -> AdRequest.Builder(id)
 *
 * Architecture improvements retained from the 7.x rewrite:
 *  - Banner uses adaptive inline size and is loaded once
 *  - Rewarded uses a pending-callback model so different game actions can subscribe
 *  - Interstitial + Rewarded are preloaded on init and reloaded after each show
 *  - All show() calls are wrapped in runOnUiThread and null-safe
 */
public class AdYandex implements UniAd {
    private static final String AD_INTERSTITIAL_ID = "R-M-2252991-3";
    private static final String AD_REWARD_ID       = "R-M-2252991-2";
    private static final String AD_BANNER_ID       = "R-M-2252991-1";

    private final Activity context;
    private final RelativeLayout layout;

    private BannerAdView adView;
    private InterstitialAdLoader interstitialLoader;
    private InterstitialAd interstitialAd;

    private RewardedAdLoader rewardedLoader;
    private RewardedAd rewardedAd;

    @Nullable private Runnable pendingRewardCallback;

    public AdYandex(Activity context, RelativeLayout layout) {
        this.context = context;
        this.layout = layout;
    }

    @Override
    public void initAd() {
        YandexAds.INSTANCE.initialize(context, new InitializationListener() {
            @Override
            public void onInitializationCompleted() {
                // SDK ready — preload both full-screen formats
                loadInterstitial();
                loadRewarded();
            }
        });
        initBanner();
    }

    private void initBanner() {
        adView = new BannerAdView(context);

        // Adaptive inline banner: width = min(screenWidthDp, 728), height = 100 dp
        int widthPx = context.getResources().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        int widthDp = Math.round(widthPx / density);
        adView.setAdSize(BannerAdSize.inline(context, Math.min(widthDp, 728), 100));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setLayoutParams(lp);
        layout.addView(adView);
    }

    private void loadInterstitial() {
        if (interstitialLoader == null) {
            interstitialLoader = new InterstitialAdLoader(context);
        }
        AdRequest request = new AdRequest.Builder(AD_INTERSTITIAL_ID).build();
        interstitialLoader.loadAd(request, new InterstitialAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                interstitialAd = null;
            }
        });
    }

    private void loadRewarded() {
        if (rewardedLoader == null) {
            rewardedLoader = new RewardedAdLoader(context);
        }
        AdRequest request = new AdRequest.Builder(AD_REWARD_ID).build();
        rewardedLoader.loadAd(request, new RewardedAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                ad.setAdEventListener(new RewardedAdEventListener() {
                    @Override public void onAdShown() {}
                    @Override public void onAdFailedToShow(@NonNull AdError adError) {
                        rewardedAd = null;
                        loadRewarded();
                    }
                    @Override public void onAdDismissed() {
                        rewardedAd = null;
                        loadRewarded();
                    }
                    @Override public void onAdClicked() {}
                    @Override public void onAdImpression(@Nullable ImpressionData impressionData) {}
                    @Override public void onRewarded(@NonNull Reward reward) {
                        Runnable cb = pendingRewardCallback;
                        pendingRewardCallback = null;
                        if (cb != null) {
                            cb.run();
                        }
                    }
                });
                rewardedAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                rewardedAd = null;
            }
        });
    }

    @Override
    public void showAdsBanner() {
        context.runOnUiThread(() -> {
            if (adView != null) {
                AdRequest request = new AdRequest.Builder(AD_BANNER_ID).build();
                adView.loadAd(request);
                adView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideAdsBanner() {
        context.runOnUiThread(() -> {
            if (adView != null) {
                adView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void showInterstitialVideo() {
        context.runOnUiThread(() -> {
            if (interstitialAd != null) {
                interstitialAd.show(context);
                interstitialAd = null;
                loadInterstitial();
            }
        });
    }

    @Override
    public void showRewardVideo(@Nullable Runnable onReward) {
        context.runOnUiThread(() -> {
            pendingRewardCallback = onReward;
            if (rewardedAd != null) {
                rewardedAd.show(context);
                rewardedAd = null;
            } else {
                // Ad not ready — preload and fire callback as a fallback
                // so the user is not blocked. For a stricter UX, show a Toast instead.
                loadRewarded();
                if (onReward != null) {
                    onReward.run();
                }
                pendingRewardCallback = null;
            }
        });
    }
}
