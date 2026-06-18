package ru.electronikas.diagonal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.my.tracker.MyTracker;
import com.my.tracker.MyTrackerConfig;
import com.my.tracker.MyTrackerParams;

import ru.electronikas.ads.AdController;
import ru.electronikas.diagonal.listeners.PlatformListener;

/**
 * Android entry point.
 *
 * Removed in P0-8 (billing cleanup):
 *  - Payer field + initialization
 *  - onNewIntent() override (was only used by RuStore Billing deeplink)
 *  - removeAds(Product) implementation
 *  - imports of ru.electronikas.pay.* and Product
 *
 * Added:
 *  - showInterstitial() and showRewardVideo(Runnable) from new PlatformListener
 */
public class AndroidLauncher extends AndroidApplication implements PlatformListener {
    private AdController adController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyTrackerParams trackerParams = MyTracker.getTrackerParams();
        MyTrackerConfig trackerConfig = MyTracker.getTrackerConfig();
        MyTracker.initTracker("95574106897946621826", getApplication());

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        RelativeLayout layout = new RelativeLayout(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        View gameView = initializeForView(new Di2048Game(this), config);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        layout.addView(gameView);

        adController = new AdController(this, layout);
        adController.initAd();

        setContentView(layout);
    }

    /**
     * P1-fix: forward lifecycle events to AdController so the banner refresh
     * timer stops while paused and immediately reloads on resume (otherwise the
     * banner stays blank/white after returning from background).
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (adController != null) {
            adController.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adController != null) {
            adController.onResume();
        }
    }

    @Override
    public void share() {
        // share logic retained verbatim from the previous version
        // (screenshot sharing via Intent.ACTION_SEND)
        Uri screenshotUri = Uri.fromFile(getFileStreamPath("mypixmap.png"));
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        String shareBody = "" + getText(R.string.share_text) +
                getText(R.string.referer_share);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getText(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, screenshotUri);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    public void rate() {
        MyTracker.trackEvent("userRateAppOnClBut");
        launchMarket();
    }

    @Override
    public void showBanner() {
        // deprecated, banner lifecycle managed by AdController
    }

    @Override
    public void hideBanner() {
        // deprecated, banner lifecycle managed by AdController
    }

    @Override
    public void showFullScr() {
        // legacy alias — invoke rewarded without a callback
        adController.showRewardVideo(null);
    }

    @Override
    public void showInterstitial() {
        adController.showInterstitialVideo();
    }

    @Override
    public void showRewardVideo(Runnable onReward) {
        adController.showRewardVideo(onReward);
    }

    @Override
    public void onGameOver() {
        // P1-4: AdController applies its own frequency cap (every 3rd game over, 2 min rate limit)
        adController.maybeShowInterstitialOnGameOver();
    }

    @Override
    public void trackEvent(String eventId) {
        MyTracker.trackEvent(eventId);
    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            runOnUiThread(() -> Toast.makeText(AndroidLauncher.this,
                    " unable to find market app", Toast.LENGTH_LONG).show());
        }
    }

    private boolean isExitReady = false;

    @Override
    public void onBackPressed() {
        if (isExitReady) {
            onExit();
            return;
        }
        adController.showInterstitialVideo();
        Toast.makeText(AndroidLauncher.this, getString(R.string.onExitToast), Toast.LENGTH_SHORT).show();
        isExitReady = true;
    }

    public void onExit() {
        super.onBackPressed();
    }
}
