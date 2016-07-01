package ru.electronikas.diagonal.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

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

    public static float textSizeTuning(Label nameLabel, float width, int widthPercent) {
        float maxScale = 3f;
        nameLabel.setFontScale(maxScale);
        while (nameLabel.getPrefWidth() > (width / 100) * widthPercent) {
            maxScale -= 0.1f;
            nameLabel.setFontScale(maxScale);
            nameLabel.layout();
        }

        return maxScale;
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
            case 1: return Color.MAROON;
            case 2: return Color.CLEAR;
            case 3: return Color.CYAN;
            case 4: return Color.OLIVE;
            case 5: return Color.PINK;
            case 6: return Color.PURPLE;
            case 7: return Color.GREEN;
            case 8: return Color.NAVY;
            case 9: return Color.MAGENTA;
            case 10: return Color.ORANGE;
        }
        return Color.TEAL;
    }

    public static void saveScreenshot() {
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);

//        Pixmap temp = new Pixmap(pixmap.getWidth() / 3, pixmap.getHeight() / 3, pixmap.getFormat());
//        temp.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, temp.getWidth() - 1, temp.getHeight() - 1);

        PixmapIO.writePNG(Gdx.files.external("mypixmap.png"), pixmap);
        pixmap.dispose();
    }
}
