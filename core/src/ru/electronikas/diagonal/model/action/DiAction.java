package ru.electronikas.diagonal.model.action;

import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/20/16.
 */
public interface DiAction {
    public Pos newPos();
    public CellModel cellModel();
    public ActType type();

}
