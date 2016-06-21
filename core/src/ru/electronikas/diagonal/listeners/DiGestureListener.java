package ru.electronikas.diagonal.listeners;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import ru.electronikas.diagonal.model.Dir;
import ru.electronikas.diagonal.ui.LevelField;

public class DiGestureListener implements GestureDetector.GestureListener {

    private LevelField levelField;

    public DiGestureListener(LevelField levelField) {
        this.levelField = levelField;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        Dir dir = Dir.none;
        if(Math.abs(velocityX) > Math.abs(velocityY)) {
            if(velocityX > 0) dir = Dir.right;
            if(velocityX < 0) dir = Dir.left;
        }

        if(Math.abs(velocityX) < Math.abs(velocityY)) {
            if(velocityY > 0) dir = Dir.down;
            if(velocityY < 0) dir = Dir.up;
        }

        levelField.onMove(dir);
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return true;
    }

    @Override
    public boolean panStop(float v, float v2, int i, int i2) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

}