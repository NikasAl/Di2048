package ru.electronikas.diagonal.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import ru.electronikas.diagonal.model.action.DeleteCellAction;
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

        stepActions.add(new NewCellAction(new Pos(xRnd, yRnd), 2));
    }

    private void runReplaceSameCells() {
        ArrayList<CellModel> cms = new ArrayList<CellModel>();
        cms.addAll(LevelField.cells);
        while (findDubles(cms).size() > 0) {
            List<CellModel> cellModels = findDubles(cms);
            stepActions.add(new DeleteCellAction(cellModels.get(0)));
            stepActions.add(new DeleteCellAction(cellModels.get(1)));
            cms.remove(cellModels.get(0));
            cms.remove(cellModels.get(1));
            stepActions.add(new NewCellAction(cellModels.get(0).pos, cellModels.get(0).value*2));
        }
    }

    private List<CellModel> findDubles(ArrayList<CellModel> cms) {
        List<CellModel> dubCells = new ArrayList<CellModel>();
        for(CellModel cellModel : cms) {
            for (CellModel cellModel1 : cms) {
                if(!cellModel.equals(cellModel1) & cellModel.pos.equals(cellModel1.pos)) {
                    dubCells.add(cellModel);
                    dubCells.add(cellModel1);
                    return dubCells;
                }
            }
        }
        return dubCells;
    }

    private void runMoveCells(Dir dir) {
        switch (dir) {
            case left:
                List<CellModel> lefterCells = getCellModelsByDir(dir);
                for(CellModel cellModel : lefterCells) {
                    Pos pos = cellModel.pos;
                    cells[pos.x][pos.y]=0;
                    int x = pos.x;
                    while(cells[x][pos.y] == 0) {
                        x--;
                        if(x < 0 || (cells[x][pos.y] != 0 & cells[x][pos.y] != cellModel.value)) {
                            x++;
                            break;
                        }
                    }
                    cellModel.pos = new Pos(x, pos.y);
                    cells[x][pos.y] = cellModel.value * countCellsInThePos(cellModel.pos);
                    stepActions.add(new MoveAction(cellModel.pos, cellModel));
                }
                break;

            case right:
                List<CellModel> rightCells = getCellModelsByDir(dir);
                for(CellModel cellModel : rightCells) {
                    Pos pos = cellModel.pos;
                    cells[pos.x][pos.y]=0;
                    int x = pos.x;
                    while(cells[x][pos.y] == 0) {
                        x++;
                        if(x > FIELD_SIZE-1 || (cells[x][pos.y] != 0 & cells[x][pos.y] != cellModel.value)) {
                            x--;
                            break;
                        }
                    }
                    cellModel.pos = new Pos(x, pos.y);
                    cells[x][pos.y] = cellModel.value * countCellsInThePos(cellModel.pos);
                    stepActions.add(new MoveAction(cellModel.pos, cellModel));
                }
                break;

            case up:
                List<CellModel> upCells = getCellModelsByDir(dir);
                for(CellModel cellModel : upCells) {
                    Pos pos = cellModel.pos;
                    cells[pos.x][pos.y]=0;
                    int y = pos.y;
                    while(cells[pos.x][y] == 0) {
                        y++;
                        if(y > FIELD_SIZE-1 || (cells[pos.x][y] != 0 & cells[pos.x][y] != cellModel.value)) {
                            y--;
                            break;
                        }
                    }
                    cellModel.pos = new Pos(pos.x, y);
                    cells[pos.x][y] = cellModel.value * countCellsInThePos(cellModel.pos);
                    stepActions.add(new MoveAction(cellModel.pos, cellModel));
                }
                break;

            case down:
                List<CellModel> downCells = getCellModelsByDir(dir);
                for(CellModel cellModel : downCells) {
                    Pos pos = cellModel.pos;
                    cells[pos.x][pos.y]=0;
                    int y = pos.y;
                    while(cells[pos.x][y] == 0) {
                        y--;
                        if(y < 0 || (cells[pos.x][y] != 0 & cells[pos.x][y] != cellModel.value)) {
                            y++;
                            break;
                        }
                    }
                    cellModel.pos = new Pos(pos.x, y);
                    cells[pos.x][y] = cellModel.value * countCellsInThePos(cellModel.pos);
                    stepActions.add(new MoveAction(cellModel.pos, cellModel));
                }
                break;
        }

    }

    private int countCellsInThePos(Pos pos) {
        int c = 0;
        for(CellModel cellModel : LevelField.cells) {
            if(cellModel.pos.equals(pos)) c++;
        }

        return c;
    }

    private List<CellModel> getCellModelsByDir(Dir dir) {
        List<CellModel> lefterCells = new ArrayList<CellModel>();
        List<CellModel> cms = new ArrayList<CellModel>();
        cms.addAll(LevelField.cells);

        while(!cms.isEmpty()) {
            CellModel minCell = cms.get(0);
            for (CellModel cm : cms) {
                switch (dir) {
                    case left:
                        if (cm.pos.x < minCell.pos.x) {
                            minCell = cm;
                        }
                        break;
                    case right:
                        if (cm.pos.x > minCell.pos.x) {
                            minCell = cm;
                        }
                        break;
                    case up:
                        if (cm.pos.y > minCell.pos.y) {
                            minCell = cm;
                        }
                        break;
                    case down:
                        if (cm.pos.y < minCell.pos.y) {
                            minCell = cm;
                        }
                        break;
                }
            }
            cms.remove(minCell);
            lefterCells.add(minCell);
        }
        return lefterCells;
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
