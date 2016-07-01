package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import ru.electronikas.diagonal.materials.Assets;
import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Pos;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.menu.SettingsMenu;

/**
 * Created by nikas on 6/23/16.
 */
public class StaticPanel {

    private Stage stage;
    private DiGameModel diGameModel;
    float h;
    float w;

    Label scoreLabel;

    SettingsMenu settingsMenu;

    public StaticPanel(Stage stage, DiGameModel diGameModel) {
        this.stage = stage;
        this.diGameModel = diGameModel;

        h = Gdx.graphics.getHeight();
        w = Gdx.graphics.getWidth();

        Table table = new Table(Textures.getUiSkin());
        table.align(Align.center);
        table.setPosition(0, h - h/5);
        table.setWidth(w - w/20);
        table.setHeight(h/8);
        table.setBackground("bluepane");
        table.defaults().height(h/8);
        table.row();
        scoreLabel = createScoreLabel(w/3);
        table.add(scoreLabel).width(w/3).pad(w/80);
        table.add(createRecordLabel()).width(w/3).pad(w/80);
        table.add(createSettingsBut()).width(h/8).pad(w/80);

        table.pack();
//        table.setDebug(true);
        stage.addActor(table);
    }

    private Actor createSettingsBut() {
        final ImageButton settingsBut = new ImageButton(
                Textures.getUiSkin().get("settings-but", ImageButton.ImageButtonStyle.class));
        settingsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                settingsMenu = new SettingsMenu(stage);
                settingsMenu.animateOpen();
            }
        });
        return settingsBut;
    }

    private Label createRecordLabel() {
        Label label = new Label(getRecordText(), Textures.getUiSkin().get("score-lbl", Label.LabelStyle.class));
        label.setFontScale(scale);
        label.pack();
        return label;
    }

    private String getRecordText() {
        return Assets.bdl().get("record") + "\n" + Storage.getRecord();
    }

    float scale;
    private Label createScoreLabel(float width) {
        Label label = new Label(getScoreText(), Textures.getUiSkin()
                .get("score-lbl", Label.LabelStyle.class));
        scale = Utils.textSizeTuning(label, width, 70);
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
