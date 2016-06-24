package ru.electronikas.diagonal.model.action;

import ru.electronikas.diagonal.model.CellModel;
import ru.electronikas.diagonal.model.Pos;

/**
 * Created by nikas on 6/20/16.
 */
public class ScoreAnimationAction implements DiAction {

    private int value;
    private Pos pos;

    public ScoreAnimationAction(int value, Pos pos) {
       this.value = value;
        this.pos = pos;
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
        return ActType.scoreAnimation;
    }

}
