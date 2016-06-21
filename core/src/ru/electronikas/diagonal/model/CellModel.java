package ru.electronikas.diagonal.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import ru.electronikas.diagonal.ui.Textures;

/**
 * Created by nikas on 1/9/16.
 */
public class CellModel {

    public int value;
    protected Pos pos;
    protected float size;

    public TextButton cell;

    public CellModel(Pos pos, int value) {
        size = Gdx.graphics.getWidth() / DiGameModel.FIELD_SIZE;
        this.pos = pos;
        this.value = value;
        cell = new TextButton("" + value, Textures.getUiSkin());
        cell.setDisabled(true);
        cell.clearListeners();
        setPositionByPosXY(pos);

        cell.setUserObject(this);
//        setDebug(true);
    }

    public void moveToNewPos() {
        cell.addAction(Actions.moveTo(
                pos.x * size, pos.y * size , 0.2f));
    }

    private void setPositionByPosXY(Pos pos) {
        cell.setSize(size,size);
        cell.setPosition(pos.x * size, pos.y * size);
    }

    public void fadeInCell() {
        cell.setColor(1,1,1,0);
        cell.addAction(Actions.fadeIn(1f));
    }

    public void remove() {
        //cell.remove();
        cell.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                if(cell.getActions().size > 1) return false;
                cell.remove();
                return true;
            }
        });
    }
}
