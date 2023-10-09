package ru.electronikas.ads;

import android.app.Activity;
import android.widget.RelativeLayout;

import java.util.Locale;

public class AdController implements UniAd {

    private UniAd uniAd;

    public AdController(Activity context, RelativeLayout layout) {
        if(Locale.getDefault().getLanguage().equals("ru")) {
            uniAd = new AdYandex(context, layout);
        } else {
            uniAd = new AdMobX(context, layout);
        }
        uniAd.initAd();
    }

    @Override
    public void initAd() {}

    @Override
    public void showAdsBanner() {
        uniAd.showAdsBanner();
    }

    @Override
    public void hideAdsBanner() {
        uniAd.hideAdsBanner();
    }

    @Override
    public void showInterstitialVideo() {
        uniAd.showInterstitialVideo();
    }

    @Override
    public void showRewardVideo() {
        uniAd.showRewardVideo();
    }

}
