package ru.electronikas.diagonal;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.appodeal.ads.Appodeal;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import ru.electronikas.diagonal.listeners.PlatformListener;
import ru.electronikas.diagonal.materials.Assets;

import java.io.File;

public class AndroidLauncher extends AndroidApplication implements PlatformListener{
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		RelativeLayout layout = new RelativeLayout(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		View gameView = initializeForView(new Di2048Game(this), config);
		layout.addView(gameView);

		String appKey = "a8c712e51e743592ccd80d4cea397a6b4bbe8e1958dd30ac";
		Appodeal.disableNetwork(this, "cheetah");
		Appodeal.initialize(this, appKey,  Appodeal.NON_SKIPPABLE_VIDEO | Appodeal.BANNER | Appodeal.INTERSTITIAL);
//		Appodeal.setTesting(true);
//		layout.addView(Appodeal.getMrecView(this));

		setContentView(layout);
	}

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
		Appodeal.show(this, Appodeal.BANNER_BOTTOM);
	}

	@Override
	public void hideBanner() {
		Appodeal.hide(this, Appodeal.BANNER_BOTTOM);
	}

	@Override
	public void showFullScr() {
		if(Appodeal.isLoaded(Appodeal.NON_SKIPPABLE_VIDEO)) {
			Appodeal.show(this, Appodeal.NON_SKIPPABLE_VIDEO);
		} else if(Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
			Appodeal.show(this, Appodeal.INTERSTITIAL);
		}
//		Appodeal.show(this, Appodeal.MREC);
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


	@Override
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
	}

}
