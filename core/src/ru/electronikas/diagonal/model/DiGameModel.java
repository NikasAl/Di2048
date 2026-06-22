package ru.electronikas.diagonal.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import ru.electronikas.diagonal.model.action.*;
import ru.electronikas.diagonal.settings.GameSounds;
import ru.electronikas.diagonal.settings.Storage;
import ru.electronikas.diagonal.ui.LevelField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nikas on 6/19/16.
 */
public class DiGameModel implements Json.Serializable {

    public static int FIELD_SIZE = 4;// = 4;

    int[][] cells;
    public int score = 0;

    /**
     * P1-2: snapshot of (cells, score) taken at the start of the last successful
     * onMove(). Used by {@link #undo()} to revert the board after a rewarded watch.
     * Null means no snapshot available (e.g. right after a new game / load).
     */
    private int[][] prevCells = null;
    private int prevScore = 0;

    List<DiAction> stepActions = new ArrayList<DiAction>();

    public DiGameModel(Integer FIELD_SIZE) {
        this();
        this.FIELD_SIZE = FIELD_SIZE;
        cells = new int[FIELD_SIZE][FIELD_SIZE];
    }

    public DiGameModel() {
    }

    public List<DiAction> onMove(Dir dir, boolean afterLoad) {
        if(!afterLoad) {
            // P1-2: snapshot before mutating, so undo can revert this exact move
            saveUndoSnapshot();
            stepActions.clear();
        }
        runMoveCells(dir);
        runReplaceSameCells();
        if(isGameOverState()) {
            stepActions.add(new GameOverAction());
            return stepActions;
        }
        if(!afterLoad | countCellsInTheGame()==0)
            runCreateNewCell();
        GameSounds.flipSoundPlay();
        Storage.saveGameState(this);

//        dbprint(cells);
        return stepActions;
    }

    /**
     * P1-2: whether undo is currently available (one-shot, last move only).
     */
    public boolean canUndo() {
        return prevCells != null;
    }

    /**
     * P1-2: revert the last move.
     *
     * Returns a list of DiActions that the caller (LevelField) must apply to
     * rebuild the visual board from scratch:
     *   1. GameContinueAction (unpauses the field, clears any game-over overlay)
     *   2. NewCellAction for every non-zero cell in the restored snapshot
     *
     * The caller is responsible for clearing the existing CellModel actors
     * BEFORE applying these actions, since the new NewCellActions assume an
     * empty board (otherwise duplicate actors will be added).
     *
     * After this call the undo snapshot is consumed (prevCells = null),
     * so undo cannot be chained.
     */
    public List<DiAction> undo() {
        if (prevCells == null) {
            return new ArrayList<DiAction>();
        }
        cells = prevCells;
        score = prevScore;
        prevCells = null;
        if (score > Storage.getRecord()) {
            Storage.saveScoreAsRecord(score);
        }
        Storage.saveGameState(this);

        stepActions.clear();
        stepActions.add(new GameContinueAction());
        for (int x = 0; x < FIELD_SIZE; x++) {
            for (int y = 0; y < FIELD_SIZE; y++) {
                if (cells[x][y] != 0) {
                    stepActions.add(new NewCellAction(new Pos(x, y), cells[x][y]));
                }
            }
        }
        return stepActions;
    }

    /**
     * P1-2: take a deep copy of (cells, score) for undo.
     */
    private void saveUndoSnapshot() {
        if (cells == null) {
            prevCells = null;
            return;
        }
        prevCells = new int[FIELD_SIZE][];
        for (int i = 0; i < FIELD_SIZE; i++) {
            prevCells[i] = Arrays.copyOf(cells[i], cells[i].length);
        }
        prevScore = score;
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
        Pos pos = new Pos(0,0);
        int tryNum = 0;
        do {
            pos.x = MathUtils.random(0, FIELD_SIZE-1);
            pos.y = MathUtils.random(0, FIELD_SIZE-1);
            tryNum++;
            if(tryNum > 100) {
                pos = getEmptyCell();
                break;
            }
        } while (cells[pos.x][pos.y] != 0);

        cells[pos.x][pos.y] = 2;

        stepActions.add(new NewCellAction(pos, 2));
    }

    private Pos getEmptyCell() {
        for (byte x=0; x<FIELD_SIZE ; x++){
            for (byte y=0; y<FIELD_SIZE ; y++) {
                if(cells[x][y]==0) {
                    return new Pos(x,y);
                }
            }
        }
        return null;
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
            score += cellModels.get(0).value*2;
            if(score > Storage.getRecord()) {
                Storage.saveScoreAsRecord(score);
            }
            stepActions.add(new ScoreAnimationAction(cellModels.get(0).value*2, cellModels.get(0).pos));
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

        cells = json.readValue("cells", int[][].class, jsonData);
        score = json.readValue("score", Integer.class, jsonData);

        // P1-2: a freshly loaded game has no undo history
        prevCells = null;
        prevScore = 0;

        for(int x=0; x<cells.length; x++) {
            for(int y=0; y<cells[x].length; y++) {
                if(cells[x][y]!=0) {
                    stepActions.add(new NewCellAction(new Pos(x,y), cells[x][y]));
                }
            }
        }
    }

    /**
     * P1-fix: rebuild the model's cells[][] array from the CURRENT LevelField.cells.
     *
     * Why this exists: after del2s() (or any other operation that mutates
     * LevelField.cells without going through onMove), DiGameModel.cells is out
     * of sync with what's actually on screen. When resize() recreates the
     * LevelField from diGameModel, the recreated board would be wrong (e.g.
     * empty after del2s).
     *
     * This method makes diGameModel.cells match the current LevelField.cells,
     * so a subsequent LevelField recreation produces the same tiles the player
     * was seeing.
     *
     * Also invalidates the undo snapshot (prevCells = null) because the
     * pre-sync state is no longer meaningful as an undo target.
     */
    public void syncFromLevelField() {
        if (cells == null) {
            cells = new int[FIELD_SIZE][FIELD_SIZE];
        } else {
            for (int x = 0; x < FIELD_SIZE; x++) {
                for (int y = 0; y < FIELD_SIZE; y++) {
                    cells[x][y] = 0;
                }
            }
        }
        if (ru.electronikas.diagonal.ui.LevelField.cells != null) {
            for (CellModel cm : ru.electronikas.diagonal.ui.LevelField.cells) {
                if (cm.pos.x >= 0 && cm.pos.x < FIELD_SIZE
                        && cm.pos.y >= 0 && cm.pos.y < FIELD_SIZE) {
                    cells[cm.pos.x][cm.pos.y] = cm.value;
                }
            }
        }
        // The undo snapshot is no longer valid as a restore target after this
        // sync — restoring it would create CellModel positions that don't
        // exist on stage. Disable undo until the next onMove takes a fresh
        // snapshot.
        prevCells = null;
    }

    public List<DiAction> del2s() {
        stepActions.clear();
        stepActions.add(new GameContinueAction());
        // Iterate over a COPY of LevelField.cells — the for-each loop below
        // mutates LevelField.cells indirectly (applyActions calls cells.remove
        // for each DeleteCellAction), and ConcurrentModificationException would
        // crash the game. The copy is just for reading which cells to delete;
        // the actual mutation happens via the DeleteCellAction list.
        java.util.List<CellModel> snapshot = new java.util.ArrayList<CellModel>(LevelField.cells);
        for(CellModel cellModel : snapshot) {
            if(cellModel.value == 2) {
                cells[cellModel.pos.x][cellModel.pos.y] = 0;
                stepActions.add(new DeleteCellAction(cellModel));
            }
        }
        // P1-fix: invalidate the undo snapshot. The pre-del2s state is no
        // longer a valid undo target because the visual cells have been
        // removed from the stage — restoring the old cells[][] array would
        // create CellModel actors that don't exist on stage, breaking the
        // next onMove which relies on LevelField.cells. Clearing prevCells
        // disables undo until the next onMove() takes a fresh snapshot.
        prevCells = null;
        // P1-fix: persist the new state so a restart / process-death reload
        // doesn't resurrect the deleted 2-tiles. Without this, Storage would
        // still have the pre-del2s snapshot, and the next launch would show
        // the 2-tiles again — exactly the bug the user reported.
        Storage.saveGameState(this);
        return stepActions;
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
