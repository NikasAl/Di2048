package ru.electronikas.diagonal.model.action;

import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/20/16.
 */
public class NewCellAction implements DiAction {
    private Pos pos;

    public NewCellAction(Pos pos) {
        this.pos = pos;
    }

    public Pos newPos() {
        return pos;
    }

    public Pos pos() {
        return pos;
    }

    @Override
    public ActType type() {
        return ActType.newCell;
    }

}
