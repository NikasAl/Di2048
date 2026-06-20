package ru.electronikas.diagonal.listeners;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import ru.electronikas.diagonal.model.Dir;
import ru.electronikas.diagonal.ui.LevelField;

/**
 * Gesture listener that translates flings into board moves.
 *
 * PAUSE-GUARD: every gesture method checks {@link LevelField#isPaused()} first
 * and bails out (returning true to 'consume' the event so it doesn't bubble
 * further). Without this guard, opening the SettingsMenu overlay does NOT
 * stop the InputMultiplexer from forwarding touch events to the underlying
 * GestureDetector, which means a fling on the settings panel would still
 * register as a board move — the game would silently advance underneath
 * the settings menu and the player would come back to a 'teleported' board.
 *
 * The cleaner fix would be to swap InputProcessors when the settings menu
 * opens, but that requires reworking how Di2048Game.create() wires up the
 * multiplexer. The pause-guard here is minimal, correct, and sufficient.
 */
public class DiGestureListener implements GestureDetector.GestureListener {

    private LevelField levelField;

    public DiGestureListener(LevelField levelField) {
        this.levelField = levelField;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        // Consume the touch when paused so the GestureDetector doesn't even
        // start tracking a potential fling/pan.
        return levelField.isPaused();
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return levelField.isPaused();
    }

    @Override
    public boolean longPress(float x, float y) {
        return levelField.isPaused();
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if (levelField.isPaused()) return true;

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
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // Consume pans while paused (same rationale as touchDown).
        return levelField.isPaused();
    }

    @Override
    public boolean panStop(float v, float v2, int i, int i2) {
        return levelField.isPaused();
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return levelField.isPaused();
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return levelField.isPaused();
    }

//    @Override
    public void pinchStop() {

    }

}