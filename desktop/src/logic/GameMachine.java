package logic;

import graphics.QRCode;

import java.io.IOException;
import java.net.InetAddress;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import connections.*;

public class GameMachine {
	public float spawnTimer;
	public float timerToEnd;
	public final float timeToSpawn = 3.5f;
	public final float timeToEnd = 15f;
	public boolean launching;
	public boolean initial;
	public Server gameServer;
	public Sprite QRimage;
	public SpriteBatch spriteBatch;

	public Player player1;
	public Player player2;
	private float connectionTime;
	public boolean is2Players;
	public boolean isPlayer1Turn;

	private int numberPinsDown;
	private boolean connectedPlayer1;

	public GameMachine () throws IOException
	{
		player1 = new Player("");
		player2 = new Player("");
		connectionTime = 15f;
		newPlay();
		connectedPlayer1 = false;

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

	public boolean checkInitialConnection(float delta) throws InterruptedException {
		// TODO Auto-generated method stub
		Boolean ret = false;

		if (gameServer.player1_isConnected)
		{
			if (!connectedPlayer1)
			{
				connectedPlayer1 = true;
				setNameOfPlayer(1);
			}


			if (initial)
			{
				float temp = connectionTime - delta;

				gameServer.sendMessagePlayer(1, "TIME", temp);
				
				if (gameServer.player2_isConnected)
				{
					initial = false;
					is2Players = true;
					isPlayer1Turn = true;
					connectionTime = 15f;
					ret = true;
					setNameOfPlayer(2);
					gameServer.sendMessagePlayer(1, "TIME", 0);
					gameServer.sendMessagePlayer(2, "TIME", 0);
				}				
				else if ((connectionTime = temp) < 0)
				{
					initial = false;
					is2Players = false;
					connectionTime = 15f;
					isPlayer1Turn = true;
					ret = true;
					player2.setName("Computador");
					gameServer.sendMessagePlayer(1, "TIME", 0);
				}
			}
		}

		if (initial)
		{
			spriteBatch.begin();
			QRimage.setCenter(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
			QRimage.draw(spriteBatch);
			spriteBatch.end();
		}

		return ret;
	}

	private void setNameOfPlayer(int i) throws InterruptedException {
		// TODO Auto-generated method stub
		waitPlayer(i);
		
		System.out.println(i);

		DataPacket d = gameServer.getLatestDataPlayers(i);
		System.out.println(i + " " + d.Event);
		if (d.Event.matches("Name"))
		{
			waitPlayer(i);
			d = gameServer.getLatestDataPlayers(i);
			setPlayerName(i, d.Event);
		}	
	}

	private void setPlayerName(int i, String event) {
		// TODO Auto-generated method stub
		System.out.println(i + " Nome " + event);
		if (i == 1)
		{
			player1.setName(event);
		}
		else
		{
			player2.setName(event);
		}
	}

	private void waitPlayer(int i) throws InterruptedException {
		// TODO Auto-generated method stub
		if (i == 1)
		{
			while (!gameServer.readPlayer1)
				Thread.sleep(10);
		}
		else
		{
			while (!(gameServer.readPlayer2))
				Thread.sleep(10);
		}
	}

	public void newPlay()
	{
		numberPinsDown = 0;
	}

	public int numberPinsDown(Boolean[] pin)
	{		
		int count = 0;
		for (Boolean a : pin)
		{
			if (!a)//cairam ao chao
			{
				count++;
			}
		}

		numberPinsDown = count - numberPinsDown;

		return numberPinsDown;
	}
}
