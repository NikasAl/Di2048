package ru.electronikas.diagonal.model.action;

import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/20/16.
 */
public class GameContinueAction implements DiAction {

    public Pos newPos() {
        return null;
    }

    @Override
    public int getValue() {
        return 0;
    }

    @Override
    public CellModel cellModel() {
        return null;
    }

    @Override
    public ActType type() {
        return ActType.gameContinue;
    }

}
