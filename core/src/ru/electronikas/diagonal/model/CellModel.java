package ru.electronikas.diagonal.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import ru.electronikas.diagonal.ui.Textures;

/**
 * Created by nikas on 1/9/16.
 */
public class CellModel {

    protected Pos pos;
    protected float size;

    public TextButton cell;

    public CellModel(Pos pos) {
        size = Gdx.graphics.getWidth() / DiGameModel.FIELD_SIZE;
        this.pos = pos;
        cell = new TextButton("2", Textures.getUiSkin());
        setPositionByPosXY(pos);

        cell.setUserObject(this);
//        setDebug(true);
    }

    public void moveTo(Pos pos) {
        cell.addAction(Actions.moveTo(
                pos.x * size, pos.y * size , 0.2f));
        this.pos = pos;
    }

    private void setPositionByPosXY(Pos pos) {
        cell.setSize(size,size);
        cell.setPosition(pos.x * size, pos.y * size);
    }

}
