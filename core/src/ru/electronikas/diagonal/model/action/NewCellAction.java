package ru.electronikas.diagonal.model.action;

import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/20/16.
 */
public class NewCellAction implements DiAction {
    private Pos pos;
    private int value;

    public NewCellAction(Pos pos, int value) {
        this.pos = pos;
        this.value = value;
    }

    public Pos newPos() {
        return pos;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public CellModel cellModel() {
        return null;
    }

    @Override
    public ActType type() {
        return ActType.newCell;
    }

}
