package ru.electronikas.diagonal.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ru.electronikas.diagonal.Di2048Game;
import ru.electronikas.diagonal.settings.Storage;

import static ru.electronikas.diagonal.ui.Utils.currentScale;
import static ru.electronikas.diagonal.ui.Utils.textSizeTuning;

/**
 * Game Over overlay.
 *
 * v4 changes (this commit):
 *  - Removed the 'Continue (ad)' button (continueButton). It was redundant with
 *    the 'Del 2s' button — both ultimately called del2s() after a rewarded video.
 *    The user found two near-identical buttons confusing. Now only 'Del 2s' and
 *    'Try Again' remain, matching the original pre-P1 layout.
 *  - Menu height tightened back to h/1.8f (was h/1.55f to fit the extra button).
 *
 * P1-4 retained: triggers AdController.maybeShowInterstitialOnGameOver() with
 * the existing frequency cap when the game-over overlay opens.
 *
 * P0-7/8 retained: procceedButton uses PlatformListener.showRewardVideo(Runnable)
 * with del2s() as the reward callback (no longer hard-coupled inside AdYandex).
 *
 * Removed in P0-9: removeAdsButton + conditional isAdWareShowing layout.
 */
public class GameOverMenu {
    Table rateMenu;
    Skin uiSkin;
    float butW = 0;
    float h = 0;

    public GameOverMenu(Stage stage) {
        float w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();
        butW = w / 6f;

        uiSkin = Di2048Game.game.getUiSkin();
        rateMenu = new Table(uiSkin);
        rateMenu.align(Align.topLeft);
        rateMenu.setPosition(butW / 2, h);
        rateMenu.setWidth(w - butW);
        // 2 buttons now (was 3): tighter layout.
        rateMenu.setHeight(h / 1.8f);
        rateMenu.background("bluepane-t");

        rateMenu.row().height(h / 10).width(w - butW - butW / 2);
        rateMenu.add(createHeader(w - butW));

        // Del 2s (watch rewarded -> remove all 2-tiles -> continue playing)
        rateMenu.row().height(h / 10);
        rateMenu.add(procceedButton(butW * 4f)).pad(10).width(butW * 4f);

        // Try again (resets the board)
        rateMenu.row().height(h / 10);
        rateMenu.add(tryAgaingButton()).pad(10).width(butW * 4f);

        stage.addActor(rateMenu);

        // P1-4: maybe show interstitial (frequency-capped inside AdController)
        Di2048Game.game.platformListener.onGameOver();
    }

    private Actor tryAgaingButton() {
        TextButton openTipsBut = new TextButton(Di2048Game.game.bdl().get("tryAgain"),
                uiSkin.get("green-but", TextButton.TextButtonStyle.class));
        openTipsBut.getLabel().setFontScale(currentScale);
        openTipsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Storage.resetCurrentGame();
                Di2048Game.game.create();
                animateHide();
            }
        });
        return openTipsBut;
    }

    private Actor procceedButton(float width) {
        TextButton openTipsBut = new TextButton(Di2048Game.game.bdl().get("removeAndGo"),
                uiSkin.get("magenta-but", TextButton.TextButtonStyle.class));
        textSizeTuning(openTipsBut.getLabel(), width, 60);
        openTipsBut.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // Watch rewarded -> delete all 2s on the board -> continue playing
                Di2048Game.game.platformListener.showRewardVideo(() -> Di2048Game.game.del2s());
            }
        });
        return openTipsBut;
    }

    private Actor createHeader(float width) {
        Label headLabel = new Label(Di2048Game.game.bdl().get("gameOver"), uiSkin);
        headLabel.setAlignment(Align.center);
        textSizeTuning(headLabel, width);
        return headLabel;
    }

    public void animateHide() {
        MoveToAction action = Actions.moveTo(butW / 2, h);
        action.setDuration(0.5f);
        rateMenu.addAction(action);
    }

    public void animateOpen() {
        MoveToAction action = Actions.moveTo(butW / 2, h / 4);
        action.setDuration(0.5f);
        rateMenu.addAction(action);
    }
}
