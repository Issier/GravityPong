package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

/**
 * Created by Ryan on 11/12/2016.
 */
public class MainMenu implements Screen {

    private final Pong game;
    private OrthographicCamera camera;
    private GlyphLayout layout;
    private boolean isTouched;
    private double fader;
    private float titleX, titleY;

    MainMenu(final Pong gam){
        this.game = gam;
        fader = 1;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1200, 720);
        isTouched = false;
        layout = new GlyphLayout();
        layout.setText(this.game.font, "GRAVITY PONG", Color.WHITE, Gdx.graphics.getWidth(), Align.center, true);
        titleX = 0;
        titleY = (float)(Gdx.graphics.getHeight()*.75 + layout.height/2);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.font.draw(game.batch, layout, titleX, titleY);
        game.batch.end();


        // If the screen has been clicked once, run animation of title dropping
        // Start main game on completion of drop
        if(isTouched){
            fader -= .001;
            titleY = (float)(titleY*fader);
            if(titleY <= 0.0001){
                isTouched = false;
                game.setScreen(new GameScreen(game));
                dispose();
            }
        }
        else if(Gdx.input.isTouched()){
            isTouched = true;
            game.boop.play();
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        game.dispose();
    }
}
