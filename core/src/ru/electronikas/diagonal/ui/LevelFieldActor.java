package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
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
//                    for(int x=0; x<4; x++) {
//                        for(int y=0; y<4; y++) {
//                            TextButton tbCell = new TextButton("2", Textures.getUiSkin());
//                            tbCell.setSize(Gdx.graphics.getWidth()/4,Gdx.graphics.getHeight()/4);
//                            tbCell.setPosition(x * Gdx.graphics.getWidth()/4, y * Gdx.graphics.getHeight()/4);
//                            tbCell.setPosition(diAction.pos().x * Gdx.graphics.getWidth()/4, diAction.pos().y * Gdx.graphics.getWidth()/4);
//                    addActor(tbCell);
                    CellActor cellActor = new CellActor(diAction.pos());
//                        }
//                    }
                    cells.add(cellActor);
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

/*    @Override
    public void addActor(Actor actor) {
        super.addActor(actor);
        cells.add((CellActor) actor);
    }*/

    public void setLevelSize(float needSizeLevel) {
        float scale = 1;
        while (getWidth() < needSizeLevel) {
            scale += 0.5f;
            setScale(scale);
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