package ru.electronikas.diagonal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.InitializationListener;
import com.yandex.mobile.ads.common.MobileAds;
import com.yandex.mobile.ads.rewarded.RewardedAd;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import ru.electronikas.diagonal.listeners.PlatformListener;

public class AndroidLauncher extends AndroidApplication implements PlatformListener{
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		RelativeLayout layout = new RelativeLayout(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		View gameView = initializeForView(new Di2048Game(this), config);
		layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
		layout.addView(gameView);

		MobileAds.initialize(this, new InitializationListener() {
			@Override
			public void onInitializationCompleted() {
				Log.d("YA_ADS", "SDK initialized");
			}
		});

		BannerAdView adView = new BannerAdView(this);
		adView.setAdUnitId("R-M-2252991-1");
		adView.setAdSize(BannerAdSize.fixedSize(this,1000,100));

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adView.setLayoutParams(layoutParams);

		layout.addView(adView);

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				AdRequest adRequest = new AdRequest.Builder().build();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adView.loadAd(adRequest);
					}
				});
			}
		};

		Timer timer = new Timer();
		timer.schedule(timerTask, 500, 30000);

/*
		rewardedAd = new RewardedAd(this);
		rewardedAd.setAdUnitId("R-M-2252991-2");
		// Создание объекта таргетирования рекламы.
		final AdRequest adRequest = new AdRequest.Builder().build();
		rewardedAd.loadAd(adRequest);
*/

		setContentView(layout);
	}

//	RewardedAd rewardedAd;
	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void share() {
		File internalFile = new File(Environment.getExternalStorageDirectory() + "/" + "mypixmap.png");
		Uri screenshotUri = Uri.fromFile(internalFile);
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
//		trackEvent(TrCategory.target.name(), "userRateAppRun", "");
		launchMarket();
	}

	@Override
	public void showBanner() {
//		Appodeal.show(this, Appodeal.BANNER_BOTTOM);
	}

	@Override
	public void hideBanner() {
//		Appodeal.hide(this, Appodeal.BANNER_BOTTOM);
	}

	@Override
	public void showFullScr() {

	}

	private void launchMarket() {
		Uri uri = Uri.parse("market://details?id=" + getPackageName());
		Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(myAppLinkToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
		}
	}


/*	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(Assets.bdl().get("exitMenuHead"))
				.setMessage(Assets.bdl().get("exitMenuSure"))
				.setPositiveButton(Assets.bdl().get("yes"), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}

				})
				.setNegativeButton(Assets.bdl().get("no"), null)
				.show();
	}*/

}
