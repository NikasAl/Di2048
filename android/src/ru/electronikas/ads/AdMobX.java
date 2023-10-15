package ru.electronikas.ads;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import ru.electronikas.diagonal.Di2048Game;


public class AdMobX implements UniAd {
    private final Activity context;
    private final RelativeLayout layout;

    private AdView adView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    public AdMobX(Activity context, RelativeLayout layout) {
        this.context = context;
        this.layout = layout;
    }


    @Override
    public void initAd() {
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        adView = new AdView(context);
//        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); //Test
        adView.setAdUnitId("ca-app-pub-6482272553178584/6600239425");
        adView.setAdSize(AdSize.LARGE_BANNER);

        // Set layout parameters for the AdView
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setLayoutParams(layoutParams);
        layout.addView(adView);

        interstitialAdInitAndLoad();
        rewardedAdInitAndLoad();
    }

    private void rewardedAdInitAndLoad() {
        AdRequest adRequest = new AdRequest.Builder().build();
        String rewardedId = "ca-app-pub-6482272553178584/7732539452";
//        String rewardedTestid = "ca-app-pub-3940256099942544/5224354917";
        RewardedAd.load(context, rewardedId, adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        super.onAdLoaded(rewardedAd);
                        AdMobX.this.rewardedAd = rewardedAd;
                    }
                });
    }

    private void interstitialAdInitAndLoad() {
        AdRequest adRequest = new AdRequest.Builder().build();
        String interstitialId = "ca-app-pub-6482272553178584/3974076087";
//        String interstTestid = "ca-app-pub-3940256099942544/1033173712";
        InterstitialAd.load(context, interstitialId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        AdMobX.this.interstitialAd = interstitialAd;
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error
//                        Log.d("!!!", loadAdError.toString());
                        AdMobX.this.interstitialAd = null;
                    }

                });
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
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(interstitialAd!=null) {
                    interstitialAd.show(context);
                }
                interstitialAdInitAndLoad();
            }
        });
    }

    @Override
    public void showRewardVideo() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(rewardedAd!=null) {
                    rewardedAd.show(context, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            Di2048Game.game.del2s();
                        }
                    });
                }
                rewardedAdInitAndLoad();
            }
        });
    }

}
