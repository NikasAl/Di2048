package ru.electronikas.diagonal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import com.appodeal.ads.Appodeal;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import ru.electronikas.diagonal.listeners.PlatformListener;

import java.io.File;

public class AndroidLauncher extends AndroidApplication implements PlatformListener{
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new Di2048Game(this), config);

		String appKey = "a8c712e51e743592ccd80d4cea397a6b4bbe8e1958dd30ac";
		Appodeal.initialize(this, appKey, Appodeal.BANNER | Appodeal.INTERSTITIAL | Appodeal.NON_SKIPPABLE_VIDEO);
//		Appodeal.setTesting(true);
//		Appodeal.setLogging(true);
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
		Appodeal.show(this, Appodeal.INTERSTITIAL | Appodeal.NON_SKIPPABLE_VIDEO);
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


}
