package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Created by nikas on 6/22/16.
 */
public class Utils {

    public static void cellTextSizeTuning(Label nameLabel, float width) {
        String saveStr = nameLabel.getText().toString();
        nameLabel.setText("1024");

        float maxScale = 3f;
        nameLabel.setFontScale(maxScale);
        while (nameLabel.getPrefWidth() > width / 1.4f) {
            maxScale -= 0.1f;
            nameLabel.setFontScale(maxScale);
            nameLabel.layout();
        }

        nameLabel.setText(saveStr);

        Gdx.app.log("FONT", "calibrated maxScale: " + maxScale);
    }

    public static void textSizeTuning(Label nameLabel, float width) {
        float maxScale = 3f;
        nameLabel.setFontScale(maxScale);
        while (nameLabel.getPrefWidth() > width / 2f) {
            maxScale -= 0.1f;
            nameLabel.setFontScale(maxScale);
            nameLabel.layout();
        }

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

    public static Color getRandomColor() {
        int i = MathUtils.random(0,10);
        switch (i){
            case 0: return Color.BLUE;
            case 1: return Color.CORAL;
            case 2: return Color.CLEAR;
            case 3: return Color.CYAN;
            case 4: return Color.FIREBRICK;
            case 5: return Color.GOLD;
            case 6: return Color.FOREST;
            case 7: return Color.GREEN;
            case 8: return Color.LIME;
            case 9: return Color.MAGENTA;
            case 10: return Color.ORANGE;
        }
        return Color.CORAL;
    }
}
