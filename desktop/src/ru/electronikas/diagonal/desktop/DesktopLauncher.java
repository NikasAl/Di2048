package ru.electronikas.diagonal.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.electronikas.diagonal.Di2048Game;

import java.util.Locale;

public class DesktopLauncher {
	public static void main (String[] arg) {
//		Locale.setDefault(new Locale("ru", "RU"));
		Locale.setDefault(new Locale("en", "US"));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 700;
		config.height = 1000;
		new LwjglApplication(new Di2048Game(), config);
	}
}
