package ru.electronikas.ads;

import android.app.Activity;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * AdController — thin wrapper over AdYandex.
 *
 * Responsibilities:
 *  - hold a single AdYandex instance
 *  - schedule the first banner show after a short delay
 *  - guard all calls against null (in case ads were stopped)
 *
 * Removed in P0-7:
 *  - locale-based switching to AdMobX (AdMob fully deleted)
 *  - Storage.isAdWareShowing() check (billing removed, ads always on)
 */
public class AdController implements UniAd {

    private AdYandex adYandex = null;
    private final Activity context;
    private final RelativeLayout layout;

    public AdController(Activity context, RelativeLayout layout) {
        this.context = context;
        this.layout = layout;
    }

    @Override
    public void initAd() {
        adYandex = new AdYandex(context, layout);
        adYandex.initAd();

        // Show banner shortly after init; Yandex SDK handles its own refresh afterwards.
        TimerTask showBannerTask = new TimerTask() {
            @Override
            public void run() {
                context.runOnUiThread(() -> showAdsBanner());
            }
        };
        new Timer().schedule(showBannerTask, 500);
    }

    @Override
    public void showAdsBanner() {
        if (adYandex == null) return;
        adYandex.showAdsBanner();
    }

    @Override
    public void hideAdsBanner() {
        if (adYandex == null) return;
        adYandex.hideAdsBanner();
    }

    @Override
    public void showInterstitialVideo() {
        if (adYandex == null) return;
        adYandex.showInterstitialVideo();
    }

    @Override
    public void showRewardVideo(Runnable onReward) {
        if (adYandex == null) return;
        adYandex.showRewardVideo(onReward);
    }

    public void stopAds() {
        if (adYandex != null) {
            adYandex.hideAdsBanner();
        }
        adYandex = null;
    }
}
