package ru.electronikas.diagonal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ru.electronikas.diagonal.listeners.DiGestureListener;
import ru.electronikas.diagonal.listeners.PlatformListener;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.settings.AdBannerController;
import ru.electronikas.diagonal.settings.GameSounds;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.LevelField;

public class Di2048Game extends ApplicationAdapter {
	SpriteBatch spriteBatch;
	private Stage stage;

	public static Di2048Game game;

	public DiGameModel diGameModel;
	public PlatformListener platformListener;

	public Di2048Game(PlatformListener platformListener) {
		this.platformListener = platformListener;
	}

	private boolean isLoadGameFromM;
	public void createFromM() {
		isLoadGameFromM = true;
		create();
	}

	@Override
	public void create () {
		game = this;
		spriteBatch = new SpriteBatch();

		Viewport viewport = new ScreenViewport();
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true); //false

		stage = new Stage(viewport, spriteBatch);

		int fieldType = Storage.getCurrentFieldType();
		if(isLoadGameFromM) {
			diGameModel = Storage.getMSavedGame();
			isLoadGameFromM = false;
		} else
			diGameModel = Storage.getGameFromFileByFieldSize(fieldType);
		LevelField levelField = new LevelField(diGameModel, stage);
		DiGestureListener gestureListener = new DiGestureListener(levelField);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, new GestureDetector(gestureListener)));

		GameSounds.soundsInit();

		AdBannerController adBannerController = new AdBannerController();
		adBannerController.start();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.8f, 0.3f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.draw();
		stage.act();
	}
}
