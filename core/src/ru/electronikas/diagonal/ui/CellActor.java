package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import ru.electronikas.diagonal.model.DiGameModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 1/9/16.
 */
public class CellActor extends Actor {

    protected Pos pos;
    protected float size;

    private TextButton meBut;

    public CellActor(Pos pos) {
        size = Gdx.graphics.getWidth() / DiGameModel.FIELD_SIZE;
        this.pos = pos;
        meBut = new TextButton("2", Textures.getUiSkin());
        setPositionByPosXY(pos);

//        setDebug(true);
    }

    private void setPositionByPosXY(Pos pos) {
        meBut.setSize(size,size);
        meBut.setPosition(pos.x * size, pos.y * size);
        setSize(size, size);
        setPosition(pos.x * size, pos.y * size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
//        float centerX = getParent().getX() + getX() + size / 2;
//        float centerY = getParent().getY() + getY() + size / 2;

        meBut.draw(batch, parentAlpha);
    }

}
