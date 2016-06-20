package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Dir;
import ru.electronikas.diagonal.model.action.DiAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikas on 0/8/06.
 * Здесь информация о расположении ячеек в уровне
 */
public class LevelFieldActor extends Group {

    //set by level manager

    private List<CellActor> cells;
    private DiGameModel diGameModel;

    public LevelFieldActor(DiGameModel diGameModel) {
        this.diGameModel = diGameModel;
        cells = new ArrayList<CellActor>();
        applyActions(diGameModel.onMove(Dir.none));
    }

    private void applyActions(List<DiAction> stepActions) {

        for(DiAction diAction : stepActions) {

            switch (diAction.type()) {
                case newCell:
                    CellActor cellActor = new CellActor(diAction.pos());
                    addActor(cellActor);
                    break;
            }

        }

    }

    public void setScale(float scaleXY) {
//        super.setScale(scaleXY);
        for (CellActor cell : cells) {
            cell.setScale(scaleXY);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

/*
        batch.end();
        drawAllBranches(parentAlpha);
        batch.begin();
        drawAllSources(batch, parentAlpha);
*/
    }

    @Override
    public void addActor(Actor actor) {
        super.addActor(actor);
        cells.add((CellActor) actor);
    }

    public void setLevelSize(float needSizeLevel) {
        float scale = 1;
        while (getWidth() < needSizeLevel) {
            scale += 0.5f;
            setScale(scale);
        }
    }

    public float getWidth() {
        int maxX = 0;
        for (CellActor cell : cells) {
            if (cell.posX > maxX) maxX = cell.posX;
        }
        return (maxX + 1) * cells.get(0).size * 2;
    }

    public float getHeight() {
        int maxY = 0;
        for (CellActor cell : cells) {
            if (cell.posY > maxY) maxY = cell.posY;
        }
        return (maxY + 1) * cells.get(0).size * 2;
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