package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Dir;
import ru.electronikas.diagonal.model.action.DiAction;

import java.util.ArrayList;
import java.util.List;

public class LevelField {

    //set by level manager

    public static List<CellModel> cells;
    private DiGameModel diGameModel;
    private Stage stage;

    public LevelField(DiGameModel diGameModel, Stage stage) {
        this.diGameModel = diGameModel;
        this.stage = stage;
        cells = new ArrayList<CellModel>();
        applyActions(diGameModel.onMove(Dir.none));
    }

    private void applyActions(List<DiAction> stepActions) {

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
//                    diAction.cellModel().cell.remove();
                    CellModel cellModel1 = diAction.cellModel();
                    cellModel1.remove();
                    cells.remove(cellModel1);
                    break;
            }

        }

    }

    public void onMove(Dir dir) {
        applyActions(diGameModel.onMove(dir));
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