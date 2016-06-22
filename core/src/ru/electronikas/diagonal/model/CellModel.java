package ru.electronikas.diagonal.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import ru.electronikas.diagonal.ui.Textures;

/**
 * Created by nikas on 1/9/16.
 */
public class CellModel {

    public int value;
    protected Pos pos;
    protected float size;

    public TextButton cell;

    public CellModel(Pos pos, int value) {
        size = Gdx.graphics.getWidth() / DiGameModel.FIELD_SIZE;
        this.pos = pos;
        this.value = value;
        switch (value) {
            case 4:
                cell = new TextButton("" + value,
                        Textures.getUiSkin().get("yellow", TextButton.TextButtonStyle.class));
                break;
            case 8:
                cell = new TextButton("" + value,
                        Textures.getUiSkin().get("red", TextButton.TextButtonStyle.class));
                break;
            case 16:
                cell = new TextButton("" + value,
                        Textures.getUiSkin().get("green", TextButton.TextButtonStyle.class));
                break;
            case 32:
                cell = new TextButton("" + value,
                        Textures.getUiSkin().get("orange", TextButton.TextButtonStyle.class));
                break;
            case 64:
                cell = new TextButton("" + value,
                        Textures.getUiSkin().get("violet", TextButton.TextButtonStyle.class));
                break;
            case 128:
                cell = new TextButton("" + value,
                        Textures.getUiSkin().get("ledenec", TextButton.TextButtonStyle.class));
                break;
            case 256:
                cell = new TextButton("" + value,
                        Textures.getUiSkin().get("magenta", TextButton.TextButtonStyle.class));
                break;


        }
        if(cell==null)
            cell = new TextButton("" + value, Textures.getUiSkin());

        cell.setDisabled(true);
        cell.clearListeners();
        setPositionByPosXY(pos);

        cell.setUserObject(this);
//        setDebug(true);
    }

    public void moveToNewPos() {
        cell.addAction(Actions.moveTo(
                pos.x * size, pos.y * size , 0.1f));
    }

    private void setPositionByPosXY(Pos pos) {
        cell.setSize(size,size);
        cell.setPosition(pos.x * size, pos.y * size);
    }

    public void fadeInCell() {
//        cell.setColor(1,1,1,0);
        cell.setSize(size, size);
//        cell.setPosition(cell.getX() - (size - 0.6f)/2, cell.getY() + (size - 0.6f)/2);
//        cell.setPosition(cell.getX() + size, cell.getY() + size);
//        cell.addAction(Actions.fadeIn(1f));
        cell.addAction(Actions.sequence(Actions.sizeTo(size*1.1f,size*1.1f, 0.3f), Actions.sizeTo(size,size, 0.3f)));
//        cell.addAction(Actions.scaleTo(1,1,0.3f));//moveBy(-0.6f,-0.6f, 0.6f));

    }

    public void remove() {
        //cell.remove();
        cell.addAction(new Action() {
            @Override
            public boolean act(float delta) {
                if(cell.getActions().size > 1) return false;
                cell.remove();
                return true;
            }
        });
    }
}
