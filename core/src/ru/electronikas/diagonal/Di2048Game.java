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
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.settings.GameSounds;
import ru.electronikas.diagonal.ui.LevelField;

public class Di2048Game extends ApplicationAdapter {
	SpriteBatch spriteBatch;
	private Stage stage;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();

		Viewport viewport = new ScreenViewport();
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true); //false

		stage = new Stage(viewport, spriteBatch);
		DiGameModel diGameModel = new DiGameModel((byte)4); //TODO load from storage
		LevelField levelField = new LevelField(diGameModel, stage);
		DiGestureListener gestureListener = new DiGestureListener(levelField);
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, new GestureDetector(gestureListener)));

		GameSounds.soundsInit();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.7f, 0.2f, 0.9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.draw();
		stage.act();
	}
}
