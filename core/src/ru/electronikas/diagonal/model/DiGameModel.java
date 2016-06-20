package ru.electronikas.diagonal.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import ru.electronikas.diagonal.model.action.DiAction;
import ru.electronikas.diagonal.model.action.NewCellAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikas on 6/19/16.
 */
public class DiGameModel implements Json.Serializable {

    public static final byte FIELD_SIZE = 4;

    int[][] cells = new int[FIELD_SIZE][FIELD_SIZE];

    List<DiAction> stepActions;

    public void createNewCell()  {
        stepActions = new ArrayList<DiAction>();
    }

    public List<DiAction> onMove(Dir dir) {
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
             xRnd = (byte)MathUtils.random(1, FIELD_SIZE);
             yRnd = (byte)MathUtils.random(1, FIELD_SIZE);
            tryNum++;
        } while (cells[xRnd][yRnd] != 0 | tryNum < 120);

        cells[xRnd][yRnd] = 2;

        stepActions.add(new NewCellAction(new Pos(xRnd, yRnd)));
    }

    private void runReplaceSameCells() {

    }

    private void runMoveCells(Dir dir) {

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
//        json.setTypeName("cell");
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
