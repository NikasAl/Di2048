package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 1/9/16.
 */
public class CellActor extends Actor {
    public static final float SIZE_CELL = 20;
    public int posX;
    public int posY;
    protected float size;


    public void setScale (float scaleXY) {
//        super.setScale(scaleXY);
        size = SIZE_CELL * scaleXY;

        setPositionByPosXY(posX, posY);
    }

    public float getCenterX() {
        return getX() + size / 2;
    }

    public float getCenterY() {
        return getY() + size / 2;
    }

    public CellActor() {
        setSize(size, size);
//        setDebug(true);
    }

    public CellActor(Pos pos) {
        this.posX = pos.x;
        this.posY = pos.y;
        setPositionByPosXY(pos.x, pos.y);
//        setDebug(true);
    }

    public CellActor(int x, int y) {
        this.posX = x;
        this.posY = y;
        setPositionByPosXY(x, y);
//        setDebug(true);
    }

    private void setPositionByPosXY(int x, int y) {
        setSize(size, size);
        setPosition(x * size, y * size);
    }
/*

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
//        float centerX = getParent().getX() + getX() + size / 2;
//        float centerY = getParent().getY() + getY() + size / 2;

    }
*/

}
