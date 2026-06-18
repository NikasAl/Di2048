package ru.electronikas.diagonal.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.ui.LevelField;
import ru.electronikas.diagonal.ui.actors.CustomTextButton;

import static ru.electronikas.diagonal.ui.Utils.cellTextSizeTuning;

/**
 * Created by nikas on 1/9/16.
 */
public class CellModel {

    public int value;
    protected Pos pos;
    /**
     * Side length of a single cell, in screen pixels.
     * Set centrally by {@link LevelField#recomputeMetrics()} before any batch
     * of CellModel constructors runs. The constructor no longer rewrites it
     * (that was the source of the field-position drift bug).
     */
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
        // BUGFIX (panel-redesign): do NOT recompute `size` here.
        // It is now owned by LevelField.recomputeMetrics() and must be set
        // before constructing any CellModel.
        this.pos = pos;
        this.value = value;

        CellColors cellColor;
        if(value <= 2048)
            cellColor = CellColors.values()[getBitNum(value)];
        else
            cellColor = CellColors.values()[MathUtils.random(0, CellColors.values().length-1)];

        cell = new CustomTextButton("" + value,
                Di2048Game.game.getUiSkin().get(cellColor.name(), CustomTextButton.TextButtonStyle.class));

        cell.setDisabled(true);
        cell.clearListeners();
        setPositionByPosXY(pos);

        cell.setUserObject(this);
//        setDebug(true);

        cellTextSizeTuning(cell.getLabel(), size);
    }

    private int getBitNum(int value) {
        String binStr = Integer.toBinaryString(value);
        return new StringBuffer(binStr).reverse().toString().indexOf("1") - 1;
    }

    public void moveToNewPos() {
        cell.addAction(Actions.moveTo(
                pos.x * size, pos.y * size + LevelField.DY, 0.1f));
    }

    private void setPositionByPosXY(Pos pos) {
        cell.setSize(size,size);
        cell.setPosition(pos.x * size, pos.y * size + LevelField.DY);
    }

    public static final float dt = 0.3f;
    public void fadeInCell() {
        cell.setColor(1,1,1,0);
        cell.addAction(Actions.fadeIn(dt*2));
        float d = (size/2);
        cell.setSize(size-d, size-d);
        cell.addAction(Actions.sizeTo(size,size, dt));
        float xx = cell.getX();
        float yy = cell.getY();
        cell.setX(xx+d/2);
        cell.setY(yy+d/2);
        cell.addAction(Actions.moveTo(xx, yy, dt));
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
