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
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;
import com.yandex.mobile.ads.rewarded.Reward;
import com.yandex.mobile.ads.rewarded.RewardedAd;
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener;
import com.yandex.mobile.ads.rewarded.RewardedAdLoader;

/**
 * AdYandex — single ad provider after P0-7 (AdMob removed).
 *
 * Improvements over the legacy version:
 *  - Banner uses BannerAdSize.inlineSize (adaptive width) instead of fixedSize(1000,100)
 *  - Banner is loaded once and left VISIBLE; Yandex SDK manages its own refresh
 *    (previous code reloaded the ad every 30s via Timer which is an anti-pattern)
 *  - Rewarded uses a pending-callback model so different game actions
 *    (del2s, undo, continue-after-game-over) can subscribe to the same ad unit
 *  - Interstitial / rewarded are preloaded on init() and reloaded after each show
 *  - All show paths are wrapped in runOnUiThread and null-safe
 */
public class AdYandex implements UniAd {
    private static final String AD_INTERSTITIAL_ID = "R-M-2252991-3";
    private static final String AD_REWARD_ID       = "R-M-2252991-2";
    private static final String AD_BANNER_ID       = "R-M-2252991-1";

    private final Activity context;
    private final RelativeLayout layout;

    private BannerAdView adView;
    private RewardedAdLoader rewardedLoader;
    private RewardedAd rewardedAd;
    private InterstitialAdLoader interstitialLoader;
    private InterstitialAd interstitialAd;

    @Nullable private Runnable pendingRewardCallback;

    public AdYandex(Activity context, RelativeLayout layout) {
        this.context = context;
        this.layout = layout;
    }

    @Override
    public void initAd() {
        MobileAds.initialize(context, () -> {
            // SDK ready — preload both full-screen formats
            loadInterstitial();
            loadRewarded();
        });
        initBanner();
    }

    private void initBanner() {
        adView = new BannerAdView(context);
        adView.setAdUnitId(AD_BANNER_ID);

        // Adaptive inline banner: width = min(screenWidthDp, 728), height = 100 dp
        int widthPx = context.getResources().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        int widthDp = Math.round(widthPx / density);
        adView.setAdSize(BannerAdSize.inlineSize(context, Math.min(widthDp, 728), 100));

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
        interstitialLoader = new InterstitialAdLoader(context);
        interstitialLoader.setAdLoadListener(new InterstitialAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                interstitialAd = null;
            }
        });
        interstitialLoader.loadAd(new AdRequestConfiguration.Builder(AD_INTERSTITIAL_ID).build());
    }

    private void loadRewarded() {
        rewardedLoader = new RewardedAdLoader(context);
        rewardedLoader.setAdLoadListener(new RewardedAdLoadListener() {
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
        rewardedLoader.loadAd(new AdRequestConfiguration.Builder(AD_REWARD_ID).build());
    }

    @Override
    public void showAdsBanner() {
        context.runOnUiThread(() -> {
            if (adView != null) {
                adView.loadAd(new AdRequest.Builder().build());
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
                // Ad not ready — preload and try to fire the reward immediately as a fallback
                // so the user is not blocked. For a stricter UX, you may instead show a Toast.
                loadRewarded();
                if (onReward != null) {
                    onReward.run();
                }
                pendingRewardCallback = null;
            }
        });
    }
}
