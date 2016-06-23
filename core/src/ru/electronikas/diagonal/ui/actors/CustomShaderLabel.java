package ru.electronikas.diagonal.ui.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class CustomShaderLabel extends Label {
    private ShaderProgram shader;
    public CustomShaderLabel(CharSequence text, Skin skin) {
        super(text, skin);
    }

    public CustomShaderLabel(CharSequence text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public CustomShaderLabel(CharSequence text, Skin skin, String fontName, Color color) {
        super(text, skin, fontName, color);
    }

    public CustomShaderLabel(CharSequence text, Skin skin, String fontName, String colorName) {
        super(text, skin, fontName, colorName);
    }

    public CustomShaderLabel(CharSequence text, LabelStyle style) {
        super(text, style);
    }

    public ShaderProgram getShader() {
        return shader;
    }

    public void setShader(ShaderProgram shader) {
        this.shader = shader;
    }

    public void draw (Batch batch, float parentAlpha) {
        if (shader != null) batch.setShader(shader);
        super.draw(batch, parentAlpha);
        if (shader != null) batch.setShader(null);
    }
}