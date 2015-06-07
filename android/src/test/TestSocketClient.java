package test;

import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import connections.Client;
import connections.DataPacket;

public class TestSocketClient implements ApplicationListener {



	public String ServerIP = "Unknown";
	private Client playerClient;
	private DataPacket receivedData;
	
	private SpriteBatch batch;
	private BitmapFont font;
	private String message;
	
	
	public interface Callback {		
		public void startScannerActivity();
	}
	public void setMyGameCallback(Callback callback) {
		callbackInterface = callback;
	}
	Callback callbackInterface;

	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.RED);
		message = "Startup";
		new Thread(new testThread()).start();

	}


	private class testThread implements Runnable
	{
		public void run() {
			callbackInterface.startScannerActivity();
			while(ServerIP == "Unknown")
				;
			message = "Got IP:" + ServerIP +  " - Connecting.";
			playerClient = new Client(ServerIP);
			playerClient.connectServer();
			while(playerClient.isConnected == false)
				;
			message = "Connected.";
			playerClient.sendMessageServer("Connected", 1);
			playerClient.startListeningPlayers();
			while(playerClient.readReady == false)
				;
			message = "Waiting Response";
			receivedData = playerClient.getLatestData();
			if (receivedData.Event.matches("Complete") && receivedData.Value == 1.0)
				message = "Completed!";
			else
				message = "Received Data, but... Event:" + receivedData.Event + " Value:" + Float.toString(receivedData.Value);

			try {
				playerClient.disconnectServer();
			} catch (IOException e) {e.printStackTrace();}
		}
	}


	@Override
	public void resize(int width, int height) {		
	}
	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, message, 100, 100);
		batch.end();
	}
	@Override
	public void pause() {

	}
	@Override
	public void resume() {
	}
	@Override
	public void dispose() {
	}

}
