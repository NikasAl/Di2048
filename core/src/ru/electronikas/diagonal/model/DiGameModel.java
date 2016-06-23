package ru.electronikas.diagonal.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import ru.electronikas.diagonal.model.action.DeleteCellAction;
import ru.electronikas.diagonal.model.action.DiAction;
import ru.electronikas.diagonal.model.action.MoveAction;
import ru.electronikas.diagonal.model.action.NewCellAction;
import ru.electronikas.diagonal.settings.GameSounds;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.LevelField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikas on 6/19/16.
 */
public class DiGameModel implements Json.Serializable {

    public static int FIELD_SIZE = 4;// = 4;

    int[][] cells;
    int score = 0;


    List<DiAction> stepActions = new ArrayList<DiAction>();

    public DiGameModel(Integer FIELD_SIZE) {
        this();
        this.FIELD_SIZE = FIELD_SIZE;
        cells = new int[FIELD_SIZE][FIELD_SIZE];
    }

    public DiGameModel() {
    }

    public List<DiAction> onMove(Dir dir, boolean afterLoad) {
        if(!afterLoad)
            stepActions.clear();
        runMoveCells(dir);
        runReplaceSameCells();
        runCheckGameOver();
        if(!afterLoad | countCellsInTheGame()==0)
            runCreateNewCell();
        GameSounds.flipSoundPlay();
        Storage.saveGameState(this);

//        dbprint(cells);
        return stepActions;
    }

    private int countCellsInTheGame() {
        int c=0;
        for (int i=0;i<FIELD_SIZE; i++) {
            for(int j=0; j<FIELD_SIZE; j++) {
                if(cells[i][j]!=0) c++;
            }
        }
        return c;
    }


    private void runCheckGameOver() {
        if(isGameOverState()) {

        }
    }

    private boolean isGameOverState() {
        boolean noEmptyFields = true;
        for (int i=0;i<FIELD_SIZE; i++) {
            for(int j=0; j<FIELD_SIZE; j++) {
                if(countCellsInThePos(i,j)==0) {
                    noEmptyFields = false;
                }
            }
        }
        return noEmptyFields;
    }
    private void runCreateNewCell() {
        byte xRnd;
        byte yRnd;
        int tryNum = 0;
        do {
             xRnd = (byte)MathUtils.random(0, FIELD_SIZE-1);
             yRnd = (byte)MathUtils.random(0, FIELD_SIZE-1);
            tryNum++;
            if(tryNum > 12000) {
                throw new RuntimeException("no stes");
            }
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
                    if(countCellsInThePos(x, pos.y)==3) {
                        x++;
                        cellModel.pos = new Pos(x, pos.y);
//                        System.out.print("ok. c3 for model this value:" + cellModel.value);
                    }

                    if(countCellsInThePos(cellModel.pos)==3) {
                        throw new RuntimeException("c3 for model this value:" + cellModel.value + "pos x:"+cellModel.pos.x+" y:"+cellModel.pos.y);
                    }

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
                    if(countCellsInThePos(x,pos.y)==3) {
                        x--;
                        cellModel.pos = new Pos(x, pos.y);
                    }

                    if(countCellsInThePos(cellModel.pos)==3)
                        throw new RuntimeException("odd must be");
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
                    if(countCellsInThePos(pos.x, y)==3) {
                        y--;
                        cellModel.pos = new Pos(pos.x, y);
                    }
                    cellModel.pos = new Pos(pos.x, y);
                    if(countCellsInThePos(cellModel.pos)==3)
                        throw new RuntimeException("odd must be");
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
                    if(countCellsInThePos(pos.x, y)==3) {
                        y++;
                        cellModel.pos = new Pos(pos.x, y);
                    }
                    cellModel.pos = new Pos(pos.x, y);
                    if(countCellsInThePos(cellModel.pos)==3)
                        throw new RuntimeException("odd must be");
                    cells[pos.x][y] = cellModel.value * countCellsInThePos(cellModel.pos);
                    stepActions.add(new MoveAction(cellModel.pos, cellModel));
                }
                break;
        }
    }

    private void dbprint(int[][] cells) {
        for(int i=cells.length-1; i>=0; i--) {
            for(int j=0; j<cells[i].length; j++) {
                System.out.print(cells[j][i]);

            }

            System.out.println();
        }

        System.out.println();
        for(CellModel cm : LevelField.cells) {
            System.out.println("" + cm.pos + " v:" + cm.value);
        }
    }

//    Pos tmp = new Pos(0,0);
    private int countCellsInThePos(int x, int y) {
//        tmp.x = x;
//        tmp.y = y;
        return countCellsInThePos(new Pos(x,y));
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
        json.writeValue("score", score);
        json.writeValue("fieldType", FIELD_SIZE);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        FIELD_SIZE = json.readValue("fieldType", Integer.class, jsonData);

//        cells = new int[FIELD_SIZE][FIELD_SIZE];

        cells = json.readValue("cells", int[][].class, jsonData);
        score = json.readValue("score", Integer.class, jsonData);

        for(int x=0; x<cells.length; x++) {
            for(int y=0; y<cells[x].length; y++) {
                if(cells[x][y]!=0) {
                    stepActions.add(new NewCellAction(new Pos(x,y), cells[x][y]));
                }
            }
        }
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
