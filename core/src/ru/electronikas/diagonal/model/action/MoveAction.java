package ru.electronikas.diagonal.model.action;

import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/20/16.
 */
public class MoveAction implements DiAction {
    private Pos newPos;
    private CellModel cell;

    public MoveAction(Pos newPos, CellModel cell) {
        this.newPos = newPos;
        this.cell = cell;
    }


    public Pos newPos() {
        return newPos;
    }

    @Override
    public CellModel cellModel() {
        return cell;
    }

    @Override
    public ActType type() {
        return ActType.move;
    }

}
