package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Created by nikas on 6/22/16.
 */
public class Utils {

    public static void textSizeTuning(Label nameLabel, float width) {
        float scale = 10f;
        nameLabel.setFontScale(scale);
        while (nameLabel.getPrefWidth() > width / 3f) {
            scale-=0.1f;
            nameLabel.setFontScale(scale);
            nameLabel.layout();
        }
        Gdx.app.log("FONT", "calibrated scale: " + scale);
    }

}
