package com.bowling.game.Graphics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.bowling.game.Elements.Alley;
import com.bowling.game.Elements.Ball;
import com.bowling.game.Elements.Pin;

public class GameWindow  extends ApplicationAdapter {
	PerspectiveCamera cam;
	CameraInputController camController;
	Environment environment;
	Alley bowlingAlley;
	Ball bowlingBall;
	Pin bowlingPin;
	
	@Override
	public void create () {
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(250f, 200f, 200f);
		cam.lookAt(0,0,0);
		cam.near = 5f;
		cam.far = 3000f;
		cam.update();

		bowlingAlley = new Alley();
		bowlingAlley.create();

		bowlingBall = new Ball();
		bowlingBall.create();

		bowlingPin = new Pin();
		bowlingPin.create();
		
		camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        
		camController.update();
	}

	@Override
	public void render () {
		//fundo
		Gdx.gl.glClearColor(1, 0, 0, 1);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		bowlingAlley.modelBatch.begin(cam);
		bowlingAlley.modelBatch.render(bowlingAlley.instance, environment);
		bowlingAlley.modelBatch.end();

		bowlingBall.instance.transform.setTranslation(50, -5, 25);
		bowlingBall.modelBatch.begin(cam);
		bowlingBall.modelBatch.render(bowlingBall.instance, environment);
		bowlingBall.modelBatch.end();
		
		bowlingPin.instance.transform.setTranslation(-700, -16, -520);
		bowlingPin.modelBatch.begin(cam);
		bowlingPin.modelBatch.render(bowlingPin.instance, environment);
		bowlingPin.modelBatch.end();
	}
}
