package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Created by nikas on 6/22/16.
 */
public class Utils {

    public static void textSizeTuning(Label nameLabel, float width) {
        float maxScale = 3f;
        nameLabel.setFontScale(maxScale);
        while (nameLabel.getPrefWidth() > width / 3f) {
            maxScale -= 0.1f;
            nameLabel.setFontScale(maxScale);
            nameLabel.layout();
        }

//        float minScale = 0.05f;
//        nameLabel.setFontScale(maxScale);
//        while (nameLabel.getPrefWidth() < width / 4f) {
//            maxScale += 0.1f;
//            nameLabel.setFontScale(maxScale);
//            nameLabel.layout();
//        }
//        Math.round(maxScale)

        Gdx.app.log("FONT", "calibrated maxScale: " + maxScale);
    }

    private static ShaderProgram fontShader;
    public static ShaderProgram getFontShader() {
        if(fontShader==null) {
            fontShader = new ShaderProgram(Gdx.files.internal("data/shader/font.vert"), Gdx.files.internal("data/shader/font.frag"));
            if (!fontShader.isCompiled()) {
                Gdx.app.error("fontShader", "compilation failed:\n" + fontShader.getLog());
            }
        }
        return fontShader;
    }
}
