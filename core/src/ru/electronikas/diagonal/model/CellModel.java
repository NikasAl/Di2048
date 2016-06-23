package ru.electronikas.diagonal.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import ru.electronikas.diagonal.ui.Textures;
import ru.electronikas.diagonal.ui.actors.CustomTextButton;

import static ru.electronikas.diagonal.ui.Utils.textSizeTuning;

/**
 * Created by nikas on 1/9/16.
 */
public class CellModel {

    public int value;
    protected Pos pos;
    public static float size;

    public CustomTextButton cell;

    private enum CellColors {
        blue,    //2
        yellow,  //4
        red,
        green,
        orange,
        violet,
        ledenec,
        magenta,  //256
        reddy,
        cyan,
        lgreen

    }

    public CellModel(Pos pos, int value) {
        size = Gdx.graphics.getWidth() / DiGameModel.FIELD_SIZE;
        this.pos = pos;
        this.value = value;

        CellColors cellColor;
        if(value <= 2048)
            cellColor = CellColors.values()[getBitNum(value)];
        else
            cellColor = CellColors.values()[MathUtils.random(0, CellColors.values().length-1)];

        cell = new CustomTextButton("" + value,
                Textures.getUiSkin().get(cellColor.name(), CustomTextButton.TextButtonStyle.class));

        cell.setDisabled(true);
        cell.clearListeners();
        setPositionByPosXY(pos);

        cell.setUserObject(this);
//        setDebug(true);

        textSizeTuning(cell.getLabel(), size);
    }

    private int getBitNum(int value) {
        String binStr = Integer.toBinaryString(value);
        return new StringBuffer(binStr).reverse().toString().indexOf("1") - 1;
    }

    public void moveToNewPos() {
        cell.addAction(Actions.moveTo(
                pos.x * size, pos.y * size , 0.1f));
    }

    private void setPositionByPosXY(Pos pos) {
        cell.setSize(size,size);
        cell.setPosition(pos.x * size, pos.y * size);
    }

    public static final float dt = 0.3f;
    public void fadeInCell() {
        cell.setSize(size, size);
        cell.setColor(1,1,1,0);
        cell.addAction(Actions.fadeIn(dt*2));
        float d = size/11;
        cell.addAction(
                Actions.sequence(Actions.sizeTo(size+d,size+d, dt), Actions.sizeTo(size,size, dt))
        );
        float xx = cell.getX();
        float yy = cell.getY();
        cell.addAction(
                Actions.sequence(Actions.moveTo(xx - d/2, yy - d/2, dt), Actions.moveTo(xx, yy, dt))
        );
    }

    public void remove() {
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
