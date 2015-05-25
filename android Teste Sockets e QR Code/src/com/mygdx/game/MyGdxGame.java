package com.mygdx.game;


import java.util.HashMap;
import java.util.Map;



import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input.Orientation;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor{
	private SpriteBatch batch;
	private BitmapFont font;
	private Texture texture;
	private Plane sprite;

	private int movespeed = 5;
	float movespeedX = 0;
	float movespeedY = 0;

	Sound blink; 
	Sound glimmer;

	class TouchInfo {
		public float touchX = 0;
		public float touchY = 0;
		public boolean touched = false;
	}
	private Map<Integer,TouchInfo> touches = new HashMap<Integer,TouchInfo>();
	private String touchmessage = "touch something already!";

	private float maxAccel = 0;
	private String motionmessage = "";


	@Override
	public void create() {        
		batch = new SpriteBatch();    
		blink = Gdx.audio.newSound(Gdx.files.internal("blink.ogg"));
		glimmer = Gdx.audio.newSound(Gdx.files.internal("glimmer.ogg"));
		texture = new Texture(Gdx.files.internal("reimu.gif"));
		sprite = new Plane(texture);
		font = new BitmapFont();
		font.setColor(Color.RED);
		for(int i = 0; i < 5; i++){
			touches.put(i, new TouchInfo());
		}


	}

	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
		blink.dispose();
		glimmer.dispose();
	}

	@Override
	public void render() {        
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		Gdx.input.setInputProcessor(this);

		// -------------------------------------------
		// --  Controlling the Plane - Desktop		--
		// -------------------------------------------

		if(!Gdx.input.isPeripheralAvailable(Peripheral.Compass)){

			//Directions		
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
			{
				sprite.moveLeft(movespeed);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			{
				sprite.moveRight(movespeed);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.UP))
			{
				sprite.moveUp(movespeed);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
			{
				sprite.moveDown(movespeed);
			}

			//Clicking
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
			{
				sprite.goToPosition(Gdx.input.getX() - sprite.getWidth()/2, Gdx.graphics.getHeight() - Gdx.input.getY() - sprite.getHeight()/2);
			}
			if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
			{
				sprite.goToPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
			}


		}

		// -------------------------------------------
		// --  Controlling the Plane - Android		--
		// -------------------------------------------

		if(Gdx.input.isPeripheralAvailable(Peripheral.Compass)){

			movespeedX = Gdx.input.getRoll() / 5;
			movespeedY = (Gdx.input.getPitch()+35) / 5;

			if (movespeedX > 10)
				movespeedX = 10;
			if (movespeedY > 10)
				movespeedY = 10;
			if (movespeedX < -10)
				movespeedX = -10;
			if (movespeedY < -10)
				movespeedY = -10;

			if (Math.abs(movespeedX) < 0.5)
				movespeedX = 0;
			if (Math.abs(movespeedY) < 0.5)
				movespeedY = 0;


			if (movespeedX > 0)
				sprite.moveRight(movespeedX);
			else
				sprite.moveLeft(-movespeedX);

			if (movespeedY > 0)
				sprite.moveUp(movespeedY);
			else
				sprite.moveDown(-movespeedY);

		}


		batch.begin();
		sprite.draw(batch);

		// -------------------------------------------
		// --      On-screen Messages				--
		// -------------------------------------------

		//Movement Speed Message
		if(Gdx.input.isPeripheralAvailable(Peripheral.Compass)){
			movespeed = (int) Math.sqrt(movespeedX*movespeedX + movespeedY*movespeedY);
			font.draw(batch, "Speed:" + new Integer(movespeed).toString(), 100, 100);
		} else
			font.draw(batch, "Speed:" + new Integer(movespeed).toString(), 100, 100);


		//Touch Info Message
		for(int i = 0; i < 5; i++){
			if(touches.get(i).touched)
			{
				touchmessage = "";	
				touchmessage += "Finger:" + Integer.toString(i) + " touched at:(" +
						Float.toString(touches.get(i).touchX) +
						"," +
						Float.toString(touches.get(i).touchY) +
						")";
				font.draw(batch, touchmessage, 100, 200 - i*20);
			}
		}

		//Orientation
		int deviceAngle = Gdx.input.getRotation();
		Orientation orientation = Gdx.input.getNativeOrientation();
		motionmessage = "Device rotated to: " + Integer.toString(deviceAngle) + "º";
		font.draw(batch, motionmessage, 100, 400);
		motionmessage = "Device orientation is ";
		if (orientation == Orientation.Landscape)
			motionmessage += "Landscape";
		else if (orientation == Orientation.Portrait)
			motionmessage += "Portrait";
		else
			motionmessage += "I don't even know";
		font.draw(batch, motionmessage, 100, 380);

		//Acceleration
		float accelY = Gdx.input.getAccelerometerY();
		if(accelY > maxAccel)
			maxAccel = accelY;
		motionmessage = "Current/Max Speed: ";
		motionmessage += Float.toString(accelY) + "/" + Float.toString(maxAccel);
		font.draw(batch, motionmessage, 100, 360);

		//Resolution
		motionmessage = "Resolution: " + Integer.toString(Gdx.graphics.getWidth()) + "," +  Integer.toString(Gdx.graphics.getHeight());
		font.draw(batch, motionmessage, 100, 340);

		//Compass
		if(Gdx.input.isPeripheralAvailable(Peripheral.Compass)){
			motionmessage = "Azmuth:" + Float.toString(Gdx.input.getAzimuth()) + "\n";
			motionmessage += "Pitch:" + Float.toString(Gdx.input.getPitch()) + "\n";
			motionmessage += "Roll:" + Float.toString(Gdx.input.getRoll()) + "\n";
		}
		else{
			motionmessage = "No compass available\n";
		}
		font.draw(batch, motionmessage, 100, 320);

		// -------------------------------------------
		// --     Vibrate							--
		// -------------------------------------------
		/*if(Gdx.input.isPeripheralAvailable(Peripheral.Vibrator)){
			if(accelY > 7){
				Gdx.input.vibrate(100);
			}
		}*/



		batch.end();

	}

	@Override
	public void resize(int width, int height) {
		batch.dispose();
		batch = new SpriteBatch();
		String resolution = Integer.toString(width) + "," + Integer.toString(height);
		Gdx.app.log("MJF", "Resolution changed " + resolution);
	}
	@Override
	public boolean keyDown(int keycode) {

		if(!Gdx.input.isPeripheralAvailable(Peripheral.Compass)){
			//Speed modifier
			if (keycode == Keys.PLUS  && movespeed < 10)
			{
				movespeed++;
			}
			if (keycode == Keys.MINUS && movespeed > 1)
			{
				movespeed--;
			} 
		}



		return true;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {


		//Clicking
		if(button == Buttons.LEFT && pointer == 0)
		{
			sprite.goToPosition(Gdx.input.getX() - sprite.getWidth()/2, Gdx.graphics.getHeight() - Gdx.input.getY() - sprite.getHeight()/2);
			blink.play((float) 0.5);
		}
		if((button == Buttons.RIGHT && pointer == 0) || (button == Buttons.LEFT && pointer == 1) )
		{
			sprite.goToPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
			glimmer.play((float) 0.5);
		}


		//Dedos
		if(pointer < 5){
			touches.get(pointer).touchX = screenX;
			touches.get(pointer).touchY = screenY;
			touches.get(pointer).touched = true;
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		//Dedos
		if(pointer < 5){
			touches.get(pointer).touchX = screenX;
			touches.get(pointer).touchY = screenY;
			touches.get(pointer).touched = false;
		}

		return false;
	}

	
	
	
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {

		//Dedos
		if(pointer < 5){
			touches.get(pointer).touchX = screenX;
			touches.get(pointer).touchY = screenY;
		}
		return false;
	}

	
	
	@Override
	public void pause() {
	}
	@Override
	public void resume() {
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	@Override
	public boolean keyUp(int keycode) {

		return true;
	}
	@Override
	public boolean keyTyped(char character) {
		return false;
	}
}
