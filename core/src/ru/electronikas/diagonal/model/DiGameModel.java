package ru.electronikas.diagonal.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import ru.electronikas.diagonal.model.action.DiAction;
import ru.electronikas.diagonal.model.action.MoveAction;
import ru.electronikas.diagonal.model.action.NewCellAction;
import ru.electronikas.diagonal.ui.LevelField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikas on 6/19/16.
 */
public class DiGameModel implements Json.Serializable {

    public static final byte FIELD_SIZE = 4;

    int[][] cells;

    List<DiAction> stepActions;

    public DiGameModel() {
        cells = new int[FIELD_SIZE][FIELD_SIZE];
        stepActions = new ArrayList<DiAction>();
    }

    public List<DiAction> onMove(Dir dir) {
        stepActions.clear();
        runMoveCells(dir);
        runReplaceSameCells();
        runCheckGameOver();
        runCreateNewCell();

        return stepActions;
    }


    private void runCheckGameOver() {

    }

    private void runCreateNewCell() {
        byte xRnd;
        byte yRnd;
        byte tryNum = 0;
        do {
             xRnd = (byte)MathUtils.random(0, FIELD_SIZE-1);
             yRnd = (byte)MathUtils.random(0, FIELD_SIZE-1);
            tryNum++;
            if(tryNum > 120) break;
        } while (cells[xRnd][yRnd] != 0);

        cells[xRnd][yRnd] = 2;

        stepActions.add(new NewCellAction(new Pos(xRnd, yRnd)));
    }

    private void runReplaceSameCells() {

    }

    List<CellModel> cellModels = new ArrayList<CellModel>();

    private void runMoveCells(Dir dir) {
        switch (dir) {
            case left:
                for(CellModel cellModel : LevelField.cells) {
                    Pos pos = cellModel.pos;
                    cells[pos.x][pos.y]=0;
                    int x = pos.x;
                    while(cells[x][pos.y] == 0) {
                        x--;
                        if(x < 0) {
                            x=0; break;
                        }
                    }
                    stepActions.add(new MoveAction(new Pos(x, pos.y), cellModel));
                }
                /*
                for(int x=0; x < cells.length; x++) {
                    for(int y=0; y < cells[x].length; y++) {

                    }
                }*/
                break;
        }

    }

    @Override
    public void write(Json json) {
        json.writeValue("cells", cells);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
//        ArrayList<CellActor> cs = json.readValue("cells", ArrayList.class, jsonData);
//        for (CellActor v : cs) {
//            addActor(v);
//        }
    }

/*    @Override
    public void write(Json json) {
//        json.setTypeName("cellModel");
//        json.writeValue("x", getX());
        json.writeValue("posX", posX);
//        json.writeValue("y", getY());
        json.writeValue("posY", posY);

    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        posX = json.readValue("posX", Integer.class, jsonData);
        posY = json.readValue("posY", Integer.class, jsonData);
        setPositionByPosXY(posX, posY);
    }*/

}
