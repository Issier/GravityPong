package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Align;

/**
 * Created by Ryan on 11/12/2016.
 */
public class GameScreen implements Screen {

    private final Pong game;
    private OrthographicCamera myCamera;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private float paddleWidth = 3, paddleHeight = 15;
    private Body play1Body, play2Body, ballBody, groundBody, ceilingBody;
    private int viewportWidth = 150, viewportHeight = 90;
    private float ballStartX = viewportWidth / 2;
    private float ballStartY = viewportHeight / 2;
    private Sound plop;
    private float xGrav = 0, yGrav = 0;
    private int play1Score = 0, play2Score = 0;
    private BitmapFont myFont;
    private ShapeRenderer shapeRend;
    private Rectangle paddleVis1, paddleVis2;
    private int scalingFactor = 8;
    private float ballRadius = (float)(viewportWidth*.01);


    GameScreen(Pong game){
        this.game = game;
        myCamera = new OrthographicCamera();
        myCamera.setToOrtho(false, viewportWidth, viewportHeight);
        plop = Gdx.audio.newSound(Gdx.files.internal("plop.ogg"));
        myFont = new BitmapFont(Gdx.files.internal("fonts/gameFont.fnt"));
        paddleVis1 = new Rectangle();
        paddleVis2 = new Rectangle();

        Box2D.init();
        debugRenderer = new Box2DDebugRenderer();
        world = new World(new Vector2(xGrav, yGrav), true);

        play1Body = getPlayerBody(paddleVis1, (float)(viewportWidth*.05));
        play2Body = getPlayerBody(paddleVis2, (float)(viewportWidth - (viewportWidth*.05)));

        BodyDef ball = spawnBall(ballStartX, ballStartY);

        groundBody = createYLimit(-10);
        ceilingBody = createYLimit(myCamera.viewportHeight + 10);

        paddleVis1.setSize(paddleWidth, paddleHeight);
        paddleVis2.setSize(paddleWidth, paddleHeight);

        PolygonShape play1Box = new PolygonShape();
        play1Box.setAsBox(paddleWidth/2, paddleHeight/2);

        CircleShape ballCircle = new CircleShape();
        ballCircle.setRadius(ballRadius);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(Gdx.graphics.getWidth(), 10);

        FixtureDef paddleFix = new FixtureDef();
        paddleFix.shape = play1Box;
        paddleFix.density = 1f;
        paddleFix.friction = 2f;
        paddleFix.restitution = 0;

        FixtureDef ballFix = new FixtureDef();
        ballFix.shape = ballCircle;
        ballFix.density = 1f;
        ballFix.friction = 2f;
        ballFix.restitution = 1f;

        FixtureDef groundFixDef = new FixtureDef();
        groundFixDef.shape = groundBox;
        groundFixDef.friction = 0;

        // Create fixtures associated with bodies
        play1Body.createFixture(paddleFix);
        play2Body.createFixture(paddleFix);
        ballBody.createFixture(ballFix);
        groundBody.createFixture(groundFixDef);
        ceilingBody.createFixture(groundFixDef);

        //Set the initial trajectory of the ball
        ballBody.setLinearVelocity(2000, 2000);

        shapeRend = new ShapeRenderer();
        shapeRend.setAutoShapeType(true);

        play1Box.dispose();
        ballCircle.dispose();
    }

    private Body createYLimit(float y){
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(0, y);
        return world.createBody(groundBodyDef);
    }

    // Creates and returns a body def for a player with the given x position
    private Body getPlayerBody(Rectangle paddleVis, float xPosition){
        BodyDef player = new BodyDef();
        player.type = BodyDef.BodyType.KinematicBody;
        player.position.set(xPosition, (float)(viewportHeight*.1));
        paddleVis.setPosition(scalingFactor * xPosition, (float)(scalingFactor * viewportHeight*.1));
        return world.createBody(player);
    }

    private BodyDef spawnBall(float ballX, float ballY){
        BodyDef ball = new BodyDef();
        ball.type = BodyDef.BodyType.DynamicBody;
        ball.position.set(ballStartX, ballStartY);
        ballBody = world.createBody(ball);
        return ball;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        myCamera.update();
        game.batch.setProjectionMatrix(myCamera.combined);

        game.batch.begin();
        myFont.draw(game.batch, play1Score + "     " + play2Score, viewportWidth/2, viewportHeight - 2, 0, Align.center, false);
        game.batch.end();

        shapeRend.begin();
        shapeRend.setColor(Color.WHITE);
        shapeRend.line(scalingFactor * viewportWidth/2, scalingFactor * viewportHeight, scalingFactor * viewportWidth/2, 0);
        shapeRend.circle(scalingFactor * ballBody.getPosition().x, scalingFactor * ballBody.getPosition().y, scalingFactor * (float)(viewportWidth * .01));
        shapeRend.end();

        shapeRend.begin(ShapeRenderer.ShapeType.Filled);
        shapeRend.setColor(Color.WHITE);
        shapeRend.rect(scalingFactor * paddleVis1.getX(), scalingFactor * paddleVis1.getY(),
                scalingFactor * paddleVis1.getWidth(), scalingFactor * paddleVis1.getHeight());
        shapeRend.rect(scalingFactor * paddleVis2.getX(), scalingFactor * paddleVis2.getY(),
                scalingFactor * paddleVis2.getWidth(), scalingFactor * paddleVis2.getHeight());
        shapeRend.end();

        play1Body.setLinearVelocity(0, movePaddle(Input.Keys.W, Input.Keys.S, play1Body));
        paddleVis1.setPosition(play1Body.getPosition().x - paddleWidth/2, play1Body.getPosition().y - paddleHeight/2);
        play2Body.setLinearVelocity(0, movePaddle(Input.Keys.UP, Input.Keys.DOWN, play2Body));
        paddleVis2.setPosition(play2Body.getPosition().x - paddleWidth/2, play2Body.getPosition().y - paddleHeight/2);

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Body fixABody = contact.getFixtureA().getBody();
                Body fixBBody = contact.getFixtureB().getBody();
                if(fixBBody == play1Body || fixABody == play1Body){
                    game.boop.play();
                    world.setGravity(new Vector2(xGrav-MathUtils.random(-2, 2)*1000, yGrav+MathUtils.random(-2, 2)*1000));
                }
                if(fixBBody == play2Body || fixABody == play2Body){
                    game.boop.play();
                    world.setGravity(new Vector2(xGrav+MathUtils.random(-2, 2)*1000, yGrav+MathUtils.random(-2, 2)*1000));
                }
                if(fixBBody == ceilingBody || fixABody == ceilingBody || fixBBody == groundBody || fixABody == groundBody){
                    plop.play();
                }
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
        checkBallBounds();

        world.step(1/300f, 6, 2);
    }

    // Checks to see if ball has left bounds
    // If so, update player score and update ball position
    private void checkBallBounds(){
        if(ballBody.getPosition().x > viewportWidth){
            ballBody.setLinearVelocity(200, MathUtils.random(-1f, 1)*100);
            ballBody.setTransform(viewportWidth/2, viewportHeight/2, 0);
            play1Score += Gdx.input.isKeyPressed(Input.Keys.ESCAPE) ? 0 : 1;
        }
        else if(ballBody.getPosition().x < 0 || Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
            ballBody.setLinearVelocity(-200, MathUtils.random(-1f, 1)*100);
            ballBody.setTransform(viewportWidth/2, viewportHeight/2, 0);
            play2Score += Gdx.input.isKeyPressed(Input.Keys.ESCAPE) ? 0 : 1;
        }
    }

    public float movePaddle(int upKey, int downKey, Body playBody){
        if(Gdx.input.isKeyPressed(upKey) && playBody.getPosition().y + paddleHeight/2 + 3*ballRadius < viewportHeight){
            return 2000000000;
        }
        if(Gdx.input.isKeyPressed(downKey) && playBody.getPosition().y - paddleHeight/2 - 3*ballRadius > 0){
            return -200000000;
        }

        return 0;
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
        game.boop.dispose();
        game.batch.dispose();
        shapeRend.dispose();
        plop.dispose();
        world.dispose();
        debugRenderer.dispose();
        game.dispose();
    }
}
