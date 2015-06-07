package test;

import graphics.QRCode;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


/*
 * 
 * 	This class is part of the JUnit test initialized by TestQRCode.java
 * 
 * 
 */


public class TestQRCodeDraw  extends ApplicationAdapter{

	QRCode QRtest;
	private SpriteBatch batch;

	@Override
	public void create() {
		batch = new SpriteBatch();
		QRtest = new QRCode("Olá eu sou um teste! É suposto esta mensagem ser lida por QRCode Reader.", 400, 400);
		QRtest.getImage().setX(50);
		QRtest.getImage().setY(50);
		super.create();
	}
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}
	@Override
	public void render() {
		super.render();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		QRtest.getImage().draw(batch);
		batch.end();
	}
	@Override
	public void pause() {
		super.pause();
	}
	@Override
	public void resume() {
		super.resume();
	}
	@Override
	public void dispose() {
		super.dispose();
	}


}