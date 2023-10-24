package ru.electronikas.ads;

import android.app.Activity;
import android.widget.RelativeLayout;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ru.electronikas.diagonal.settings.Storage;

public class AdController implements UniAd {

    private UniAd uniAd = null;
    private Activity context;
    private RelativeLayout layout;

    public AdController(Activity context, RelativeLayout layout) {
        this.context = context;
        this.layout = layout;
    }

    @Override
    public void initAd() {
        if (!Storage.isAdWareShowing()) return;

        if (Locale.getDefault().getLanguage().equals("ru")) {
            uniAd = new AdYandex(context, layout);
        } else {
            uniAd = new AdMobX(context, layout);
        }
        uniAd.initAd();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAdsBanner();
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, 500, 30000);

    }

    @Override
    public void showAdsBanner() {
        if (uniAd == null) return;
        uniAd.showAdsBanner();
    }

    @Override
    public void hideAdsBanner() {
        if (uniAd == null) return;
        uniAd.hideAdsBanner();
    }

    @Override
    public void showInterstitialVideo() {
        if (uniAd == null) return;
        uniAd.showInterstitialVideo();
    }

    @Override
    public void showRewardVideo() {
        if (uniAd == null) return;
        uniAd.showRewardVideo();
    }

    public void stopAds() {
        if(uniAd != null)
            uniAd.hideAdsBanner();
        uniAd = null;
    }
}
