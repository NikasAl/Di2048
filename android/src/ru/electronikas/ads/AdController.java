package ru.electronikas.ads;

import android.app.Activity;
import android.widget.RelativeLayout;

import java.util.Locale;

public class AdController {

    private UniAd uniAd;

    public AdController(Activity context, RelativeLayout layout) {
        if(Locale.getDefault().getLanguage().equals("ru")) {
            uniAd = new AdYandex(context, layout);
        } else {
            uniAd = new AdMobX(context, layout);
        }
        uniAd.initAd();
    }

    public void showAdsBanner() {
        uniAd.showAdsBanner();
    }

    public void hideAdsBanner() {
        uniAd.hideAdsBanner();
    }

    public void showVideo() {
        uniAd.showVideo();
    }

}
