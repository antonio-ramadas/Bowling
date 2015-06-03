package logic;

import graphics.QRCode;

import java.io.IOException;
import java.net.InetAddress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import connections.Server;

public class GameMachine {
	public float spawnTimer;
	public float timerToEnd;
	public final float timeToSpawn = 3.5f;
	public final float timeToEnd = 15f;
	public boolean launching;
	public int timeNumber = 0;
	public boolean initial;
	public Server gameServer;
	public Sprite QRimage;
	public SpriteBatch spriteBatch;
	
	public GameMachine ()
	{
		try {
			gameServer = new Server();
			gameServer.connectPlayers(1);
			gameServer.connectPlayers(2);
			
			spriteBatch = new SpriteBatch();
			QRimage = (new QRCode(InetAddress.getLocalHost().getHostAddress(), 200, 200)).getImage();
			initial = true;
			launching = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void configTimerToEnd() {
		// TODO Auto-generated method stub
		timerToEnd = timeToEnd;
	}

	public void configSpawnTime(float f) {
		spawnTimer = f;
	}

	public void checkInitialConnection() {
		// TODO Auto-generated method stub
		if (gameServer.player1_isConnected && gameServer.player2_isConnected)
		{
			initial = false;
		}
		
		if (initial)
		{
			spriteBatch.begin();
			QRimage.setCenter(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
			QRimage.draw(spriteBatch);
			spriteBatch.end();
		}
	}
}
