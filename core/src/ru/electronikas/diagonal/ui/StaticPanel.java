package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ru.electronikas.diagonal.Di2048Game;
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

        Table table = new Table(Di2048Game.game.getUiSkin());
        table.align(Align.center);
        table.setPosition(0, h - h/5);
        table.setWidth(w - w/20);
        table.setHeight(h/8);
        table.setBackground("bluepane");
        table.defaults().height(h/8);
        table.row();
        // P1-2: layout is now score | record | undo | settings
        // score+record share ~60% of width; undo+settings take ~40%
        scoreLabel = createScoreLabel(w * 0.30f);
        table.add(scoreLabel).width(w * 0.30f).pad(w/80);
        table.add(createRecordLabel()).width(w * 0.30f).pad(w/80);
        // P1-2: undo button (rewarded)
        table.add(createUndoBut()).width(w * 0.18f).pad(w/80);
        table.add(createSettingsBut()).width(h/8).pad(w/80);

        table.pack();
//        table.setDebug(true);
        stage.addActor(table);
    }

    /**
     * P1-2: 'Undo last move' button. Watches a rewarded video, then reverts the
     * most recent move. Silently no-ops if no undo snapshot is available
     * (Di2048Game.undoLastMove guards on canUndo()).
     *
     * Visual: re-uses the 'green-but' style with an i18n label ('UNDO' / 'ОТМЕНА')
     * so we don't need to modify the existing mainatlas.png asset.
     */
    private Actor createUndoBut() {
        TextButton undoBut = new TextButton(Di2048Game.game.bdl().get("undo"),
                Di2048Game.game.getUiSkin().get("green-but", TextButton.TextButtonStyle.class));
        undoBut.getLabel().setFontScale(0.9f);
        undoBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!diGameModel.canUndo()) {
                    // Nothing to undo — silently ignore
                    return;
                }
                Di2048Game.game.platformListener.showRewardVideo(() -> Di2048Game.game.undoLastMove());
                Di2048Game.game.platformListener.trackEvent("UndoMoveOnClBut");
            }
        });
        return undoBut;
    }

    private Actor createSettingsBut() {
        final ImageButton settingsBut = new ImageButton(
                Di2048Game.game.getUiSkin().get("settings-but", ImageButton.ImageButtonStyle.class));
        settingsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                settingsMenu = new SettingsMenu(stage);
                settingsMenu.animateOpen();
                Di2048Game.game.platformListener.trackEvent("SettingsOnClBut");
            }
        });
        final Color baseColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        final Color alphaColor = new Color(1.0f, 1.0f, 1.0f, 0.5f);
        // Создаем последовательность действий для анимации
        settingsBut.addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.color(baseColor, 1.8f),
                                Actions.color(alphaColor, 1.8f)
                        )
                )
        );
        return settingsBut;
    }

    private Label createRecordLabel() {
        Label label = new Label(getRecordText(), Di2048Game.game.getUiSkin().get("score-lbl", Label.LabelStyle.class));
        label.setFontScale(scale);
        label.pack();
        return label;
    }

    private String getRecordText() {
        return Di2048Game.game.bdl().get("record") + "\n" + Storage.getRecord();
    }

    float scale;
    private Label createScoreLabel(float width) {
        Label label = new Label(getScoreText(), Di2048Game.game.getUiSkin()
                .get("score-lbl", Label.LabelStyle.class));
        scale = Utils.textSizeTuning(label, width, 70);
        label.pack();
        return label;
    }

    private String getScoreText() {
        return Di2048Game.game.bdl().get("count") + "\n" + diGameModel.score;
    }

    public void refresh() {
        scoreLabel.setText(getScoreText());
    }

    public void animatePlusScore(int value, Pos pos) {
        final Label scoreAnimLabel = new Label("+" + value, Di2048Game.game.getUiSkin());
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
