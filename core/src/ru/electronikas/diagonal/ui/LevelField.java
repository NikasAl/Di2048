package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Dir;
import ru.electronikas.diagonal.model.Pos;
import ru.electronikas.diagonal.model.action.DiAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikas on 0/8/06.
 * Здесь информация о расположении ячеек в уровне
 */
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
                    CellModel cellModel = new CellModel(diAction.newPos());
                    cells.add(cellModel);
                    stage.addActor(cellModel.cell);
                    break;

                case move:
                    Pos newPos = diAction.newPos();
                    CellModel cell = diAction.cellModel();
                    cell.moveTo(newPos);
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