package ru.electronikas.diagonal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import java.io.File;

import ru.electronikas.ads.AdController;
import ru.electronikas.diagonal.listeners.PlatformListener;
import ru.electronikas.diagonal.model.Product;
import ru.electronikas.pay.Payer;

public class AndroidLauncher extends AndroidApplication implements PlatformListener{
	private AdController adController;
	private Payer payer = null;

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

		adController = new AdController(this, layout);

		payer = new Payer(getContext(), adController);
		if (savedInstanceState == null) {
			payer.billingClient.onNewIntent(getIntent());
		}

		adController.initAd();

		setContentView(layout);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		payer.billingClient.onNewIntent(intent);
	}

/*
	@Override
	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
*/

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
		adController.showRewardVideo();
	}

	@Override
	public void removeAds(Product product) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				payer.purchaseProduct(product);
			}
		});
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


	private boolean isExitReady = false;
	@Override
	public void onBackPressed() {
		if(isExitReady) {onExit(); return;}
		adController.showInterstitialVideo();
		Toast.makeText(AndroidLauncher.this, getString(R.string.onExitToast), Toast.LENGTH_SHORT).show();
		isExitReady = true;
	}

	public void onExit() {
		super.onBackPressed();
//		finish();
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
