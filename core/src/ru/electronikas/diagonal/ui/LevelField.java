package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Dir;
import ru.electronikas.diagonal.model.action.DiAction;
import ru.electronikas.diagonal.ui.menu.GameOverMenu;

import java.util.ArrayList;
import java.util.List;

public class LevelField {

    public static float DY;
    //set by level manager

    public static List<CellModel> cells;
    private DiGameModel diGameModel;
    private Stage stage;
    private StaticPanel staticPanel;
    private boolean isPause = false;

    public LevelField(DiGameModel diGameModel, Stage stage) {
        this.diGameModel = diGameModel;
        this.stage = stage;
        createFields();

        staticPanel = new StaticPanel(stage, diGameModel);

        cells = new ArrayList<CellModel>();
        applyActions(diGameModel.onMove(Dir.none, true));
    }

    private void createFields() {
        float size = Gdx.graphics.getWidth() / DiGameModel.FIELD_SIZE;
        DY = size / 2;
        for(int x=0; x < DiGameModel.FIELD_SIZE; x++) {
            for(int y=0; y < DiGameModel.FIELD_SIZE; y++) {
                Image img = new Image(Textures.getUiSkin().getPatch("graypane"));
                img.setPosition(x * size, y * size + DY);
                img.setSize(size,size);
                stage.addActor(img);
            }
        }
    }

    private void applyActions(List<DiAction> stepActions) {
        if(isPause) return;

        for(DiAction diAction : stepActions) {

            switch (diAction.type()) {
                case newCell:
                    CellModel cellModel = new CellModel(diAction.newPos(), diAction.getValue());
                    cells.add(cellModel);
                    cellModel.fadeInCell();
                    stage.addActor(cellModel.cell);
                    break;

                case move:
                    CellModel cell = diAction.cellModel();
                    cell.moveToNewPos();
                    break;

                case delCell:
                    CellModel cellModel1 = diAction.cellModel();
                    cellModel1.remove();
                    cells.remove(cellModel1);
                    break;

                case gameOver:
                    GameOverMenu gameOverMenu = new GameOverMenu(stage);
                    gameOverMenu.animateOpen();
                    isPause = true;
                    break;

                case scoreAnimation:
                    staticPanel.animatePlusScore(diAction.getValue());
                    break;
            }

        }

    }

    public void onMove(Dir dir) {
        applyActions(diGameModel.onMove(dir, false));
    }

/*

    public void setOnWinListener(ActListener onWinListener) {
        diGameModel.setOnWinListener(onWinListener);
    }

    public void setOnFailListener(ActListener onFailListener) {
        diGameModel.setOnFailListener(onFailListener);
    }
*/

}