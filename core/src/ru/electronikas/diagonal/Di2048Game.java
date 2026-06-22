package ru.electronikas.diagonal;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Locale;

import ru.electronikas.diagonal.listeners.DiGestureListener;
import ru.electronikas.diagonal.listeners.PlatformListener;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.settings.GameSounds;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.LevelField;

public class Di2048Game extends ApplicationAdapter {
        SpriteBatch spriteBatch;
        private Stage stage;

        public static Di2048Game game;

        public DiGameModel diGameModel;
        /**
         * P1-fix: changed from private to public so SettingsMenu can call
         * setPaused(true/false) on it when the overlay opens/closes.
         * See SettingsMenu.animateOpen / animateHide.
         */
        public LevelField levelField;
        public PlatformListener platformListener;

        private I18NBundle myBundle;
        public I18NBundle bdl() {
                if(myBundle==null) {
                        FileHandle baseFileHandle = Gdx.files.internal("i18n/gameBundle");
                        Locale locale = Locale.getDefault();
                        myBundle = I18NBundle.createBundle(baseFileHandle, locale);
                }
                return myBundle;
        }

        private Skin uiSkin;
        public Skin getUiSkin() {
                if(uiSkin == null) {
                        uiSkin = new Skin(Gdx.files.internal("data/skins/mainatlas.json"));
                }
                return uiSkin;
        }

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
                Gdx.app.log("Di2048", "create() start");
                
                // Clear cached drawables to avoid black icons after resolution change
                ru.electronikas.diagonal.ui.StaticPanel.clearCachedDrawables();
                ru.electronikas.diagonal.ui.BottomActionBar.clearCachedDrawables();
                
                spriteBatch = new SpriteBatch();

                Viewport viewport = new ScreenViewport();
                viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true); //false

                stage = new Stage(viewport, spriteBatch);
                Gdx.app.log("Di2048", "stage ready");

                int fieldType = Storage.getCurrentFieldType();
                if(isLoadGameFromM) {
                        diGameModel = Storage.getMSavedGame();
                        isLoadGameFromM = false;
                } else
                        diGameModel = Storage.getGameFromFileByFieldSize(fieldType);
                Gdx.app.log("Di2048", "game model ready, fieldType=" + fieldType);

                try {
                        levelField = new LevelField(diGameModel, stage);
                        Gdx.app.log("Di2048", "levelField ready");
                } catch (Throwable t) {
                        Gdx.app.error("Di2048", "levelField init FAILED", t);
                        throw t;
                }

                DiGestureListener gestureListener = new DiGestureListener(levelField);
                Gdx.input.setInputProcessor(new InputMultiplexer(stage, new GestureDetector(gestureListener)));

                GameSounds.soundsInit();
                Gdx.app.log("Di2048", "create() done");

//              AdBannerController adBannerController = new AdBannerController();
//              adBannerController.start();
        }

        @Override
        public void render () {
                // P1-10: was (0.8, 0.3, 1.0, 1) = bright magenta-purple that hurt conversion.
                // Switched to a soft dark navy (#1A1F33) which:
                //  - does not trigger "looks abandoned / cheap" reaction in store screenshots
                //  - keeps high contrast with the colorful tile palette
                //  - reads as "modern dark UI" rather than "2012 Android template"
                Gdx.gl.glClearColor(0.102f, 0.122f, 0.200f, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                stage.draw();
                stage.act();
        }

        public void del2s() {
                levelField.applyActions(diGameModel.del2s());
                levelField.hideGameOverMenu();
                platformListener.trackEvent("Delete 2s");
        }

        /**
         * P1-2: undo the last move.
         * Caller (StaticPanel undo button) is expected to gate this behind a rewarded video.
         * Returns silently if no undo snapshot is available.
         */
        public void undoLastMove() {
                if (!diGameModel.canUndo()) {
                        return;
                }
                levelField.applyUndoActions(diGameModel.undo());
                levelField.hideGameOverMenu();
                platformListener.trackEvent("UndoMove");
        }
}
