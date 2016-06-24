package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import ru.electronikas.diagonal.materials.Assets;
import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/23/16.
 */
public class StaticPanel {

    private Stage stage;
    private DiGameModel diGameModel;
    float h;
    float w;

    Label scoreLabel;

    public StaticPanel(Stage stage, DiGameModel diGameModel) {
        this.stage = stage;
        this.diGameModel = diGameModel;

        h = Gdx.graphics.getHeight();
        w = Gdx.graphics.getWidth();

        scoreLabel = createScoreLabel(w/3, h/10);
        stage.addActor(scoreLabel);


    }

    private Label createScoreLabel(float width, float height) {
        Label label = new Label(getScoreText(), Textures.getUiSkin());
        label.setPosition(w/20, 8.8f*h/10);
        label.setFontScale(0.6f);
        label.pack();
        return label;
    }

    private String getScoreText() {
        return Assets.bdl().get("count") + "\n" + diGameModel.score;
    }

    public void refresh() {
        scoreLabel.setText(getScoreText());
    }

    public void animatePlusScore(int value, Pos pos) {
        final Label scoreAnimLabel = new Label("+" + value, Textures.getUiSkin());
        scoreAnimLabel.setColor(Utils.getRandomColor());
        scoreAnimLabel.setPosition(pos.x * CellModel.size + CellModel.size / 4, pos.y * CellModel.size + CellModel.size);
        scoreAnimLabel.pack();
        stage.addActor(scoreAnimLabel);
        scoreAnimLabel.addAction(Actions.fadeOut(1f));
        scoreAnimLabel.addAction(Actions.sequence(
                Actions.moveBy(0, 20, 1f),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        scoreAnimLabel.remove();
                        return true;
                    }
                }
        ));
        refresh();
    }
}
