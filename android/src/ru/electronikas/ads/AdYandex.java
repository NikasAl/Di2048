package ru.electronikas.ads;

import android.app.Activity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

import ru.electronikas.diagonal.Di2048Game;

public class AdYandex implements UniAd {
    private final String adInterstitialYaId = "R-M-2252991-3";
    private final String adRewardYaId = "R-M-2252991-2";
    private final String adBannerYaId = "R-M-2252991-1";

    private final Activity context;
    private final RelativeLayout layout;
    private BannerAdView adView;
    private RewardedAdLoader rewardedLoader;
    private RewardedAd rewardedAd;
    private InterstitialAdLoader interstitialLoader;
    private InterstitialAd interstitialAd;

    public AdYandex(Activity context, RelativeLayout layout) {
        this.context = context;
        this.layout = layout;
    }


    @Override
    public void initAd() {
        MobileAds.initialize(context, () -> {
            // now you can use ads
        });
        adView = new BannerAdView(context);
        adView.setAdUnitId(adBannerYaId);
        adView.setAdSize(BannerAdSize.fixedSize(context,1000,100));
//        adView.loadAd(new AdRequest.Builder().build());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setLayoutParams(layoutParams);
        layout.addView(adView);

        interstitialAdInit();
        rewardedAdInit();
    }

    private void interstitialAdInit() {
        interstitialLoader = new InterstitialAdLoader(context);
        interstitialLoader.setAdLoadListener(new InterstitialAdLoadListener() {
            @Override
            public void onAdLoaded(final InterstitialAd interstitialAd) {
                AdYandex.this.interstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(AdRequestError adRequestError) {
                if(adRequestError!=null) {
                    Toast.makeText(context, adRequestError.getDescription(), Toast.LENGTH_LONG).show();
                }
            }
        });
        interstitialLoader.loadAd(new AdRequestConfiguration.Builder(adInterstitialYaId).build());
    }

    private void rewardedAdInit() {
        rewardedLoader = new RewardedAdLoader(context);
        rewardedLoader.setAdLoadListener(new RewardedAdLoadListener() {
            @Override
            public void onAdLoaded(RewardedAd rewardedAd) {
                rewardedAd.setAdEventListener(new RewardedAdEventListener() {
                    @Override
                    public void onAdShown() {

                    }

                    @Override
                    public void onAdFailedToShow(@NonNull AdError adError) {

                    }

                    @Override
                    public void onAdDismissed() {

                    }

                    @Override
                    public void onAdClicked() {

                    }

                    @Override
                    public void onAdImpression(@Nullable ImpressionData impressionData) {

                    }

                    @Override
                    public void onRewarded(@NonNull Reward reward) {
                        Di2048Game.game.del2s();
                    }
                });
                AdYandex.this.rewardedAd = rewardedAd;

            }

            @Override
            public void onAdFailedToLoad(AdRequestError adRequestError) {}
        });
        rewardedLoader.loadAd(new AdRequestConfiguration.Builder(adRewardYaId).build());
    }


    @Override
    public void showAdsBanner() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.loadAd(new AdRequest.Builder().build());
                adView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void hideAdsBanner() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void showInterstitialVideo() {
        if(interstitialAd!=null) {
            interstitialAd.show(context);
            interstitialLoader.loadAd(new AdRequestConfiguration.Builder(adInterstitialYaId).build());
        }
    }

    @Override
    public void showRewardVideo() {
        if(rewardedAd!=null) {
            rewardedAd.show(context);
            rewardedLoader.loadAd(new AdRequestConfiguration.Builder(adRewardYaId).build());
        }

    }

}
