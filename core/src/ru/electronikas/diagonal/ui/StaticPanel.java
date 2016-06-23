package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import ru.electronikas.diagonal.materials.Assets;
import ru.electronikas.diagonal.model.DiGameModel;

/**
 * Created by nikas on 6/23/16.
 */
public class StaticPanel {

    private Stage stage;
    private DiGameModel diGameModel;

    public StaticPanel(Stage stage, DiGameModel diGameModel) {
        this.stage = stage;
        this.diGameModel = diGameModel;

        float h = Gdx.graphics.getHeight();
        float w = Gdx.graphics.getWidth();


        Label lbl = createScoreLabel(w, h);

        stage.addActor(lbl);


    }

    private Label createScoreLabel(float w, float h) {
        Label label = new Label(Assets.bdl().get("count") + "\n" + diGameModel.score, Textures.getUiSkin());
        label.setPosition(w/20, 8.8f*h/10);
        label.setFontScale(0.6f);
        label.pack();

        return label;
    }
}
