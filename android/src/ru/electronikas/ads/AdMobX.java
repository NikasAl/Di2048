package ru.electronikas.ads;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


public class AdMobX implements UniAd {
    private final Activity context;
    private final RelativeLayout layout;

    private AdView adView;
//    private RewardedAd rewardedAd;
    private InterstitialAd interstitialAd;

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
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); //Test
//        adView.setAdUnitId("ca-app-pub-6482272553178584/8049769070");
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
//        rewardedAdInit();
    }

    private void interstitialAdInitAndLoad() {
        AdRequest adRequest = new AdRequest.Builder().build();
        String interstitialId = "ca-app-pub-6482272553178584/7296720916";
        String interstTestid = "ca-app-pub-3940256099942544/1033173712";
        InterstitialAd.load(context, interstTestid, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        AdMobX.this.interstitialAd = interstitialAd;
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("!!!", loadAdError.toString());
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
    public void showVideo() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(interstitialAd!=null) {
                    interstitialAd.show(context);
                }
                interstitialAdInitAndLoad();
            }
        });
//        if(rewardedAd!=null) {
//            rewardedAd.show(context);
//            rewardedLoader.loadAd(new AdRequestConfiguration.Builder("R-M-2821884-2").build());
//        }
    }

}
