package ru.electronikas.diagonal.model.action;

import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/20/16.
 */
public class DeleteCellAction implements DiAction {

    private CellModel cellModel;

    public DeleteCellAction(CellModel cellModel) {
        this.cellModel = cellModel;
    }

    public Pos newPos() {
        return null;
    }

    @Override
    public int getValue() {
        return 0;
    }

    @Override
    public CellModel cellModel() {
        return cellModel;
    }

    @Override
    public ActType type() {
        return ActType.delCell;
    }

}
