package ru.electronikas.diagonal.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.listeners.PlatformListener;
import ru.electronikas.diagonal.model.Product;

import java.util.Locale;

public class DesktopLauncher implements PlatformListener {
	public static void main (String[] arg) {
		DesktopLauncher desktopLauncher = new DesktopLauncher();
		Locale.setDefault(new Locale("ru", "RU"));
//		Locale.setDefault(new Locale("en", "US"));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 700;
		config.height = 1000;
		new LwjglApplication(new Di2048Game(desktopLauncher), config);
	}

	@Override
	public void share() {
		Gdx.app.log("INFO", "share open");
	}

	@Override
	public void rate() {
		Gdx.app.log("INFO", "rate open");
	}

	@Override
	public void showBanner() {

	}

	@Override
	public void hideBanner() {

	}

	@Override
	public void showFullScr() {

	}

	@Override
	public void removeAds(Product product) {

	}
}
