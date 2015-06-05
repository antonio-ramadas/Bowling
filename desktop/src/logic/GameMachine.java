package logic;

import graphics.GameWindow;
import graphics.QRCode;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import connections.*;

public class GameMachine {
	public float spawnTimer;
	public float timerToEnd;
	public final float timeToSpawn = 6f;
	public final float timeToEnd = 30f;
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
	public boolean restartPins;
	public int ballType;

	private int numberPinsDown;
	private boolean connectedPlayer1;
	public boolean gameIsOver;
	public boolean waitingPlayer;

	public GameMachine () throws IOException
	{
		player1 = new Player("");
		player2 = new Player("");
		connectionTime = 15f;
		newPlay();
		connectedPlayer1 = false;
		gameIsOver = false;
		waitingPlayer = false;

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
				//temp = -1;

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

		restartPins = (count == 10);

		return numberPinsDown;
	}


	public void notifyPlayer(Boolean[] pin) {
		// TODO Auto-generated method stub
		if (isPlayer1Turn)
		{
			isPlayer1Turn = player1.makePlay(numberPinsDown(pin));
			gameServer.sendMessagePlayer(1, "JogadaCompleta", 1);
		}
		else
		{
			isPlayer1Turn = !player2.makePlay(numberPinsDown(pin));
			if (is2Players)
			{
				gameServer.sendMessagePlayer(2, "JogadaCompleta", 1);
				gameIsOver = (player2.getScoreBoard().getNextPlay() == -1);
			}
		}
	}

	public void getPlayerPlay(GameWindow gameWindow, boolean b) {

		class ThreadBetter implements Runnable{
			private GameWindow gw;
			boolean b1;

			boolean stop = false;
			float Force = 0;
			float Roll = 0;

			public ThreadBetter(GameWindow g, boolean bb)
			{
				gw = g;
				b1 = bb;
			}

			@Override
			public void run() {
				while (!stop)
				{
					DataPacket data;
					if (b1 && gameServer.readPlayer1)
					{

						data = gameServer.getLatestDataPlayers(1);
						if (data.Event.matches("BallChange"))
						{
							ballType = (int) data.Value;
							gw.chooseBallType(ballType-1);
						} else if (data.Event.matches("Move"))
						{
							if ((int) data.Value == 0)
							{
								gw.moveBallLeft();
							} else
							{
								gw.moveBallRight();
							}
						} else if (data.Event.matches("BallForce"))
						{
							Force = -data.Value;
						} else if (data.Event.matches("BallRoll"))
						{
							Roll = -data.Value;
							gw.releaseBall(Force, Roll);
							System.out.println("Ball type: " + ballType + " Force: " + Force + " Roll: " + Roll);
							stop = true;
							launching = true;
							waitingPlayer = false;
						}

					}
				}
			}
			
			
		}

		Thread threading = new Thread( new ThreadBetter(gameWindow, b));
		threading.start();

	}

	public void computerPlay(GameWindow gameWindow) {
		// TODO Auto-generated method stub
		gameWindow.chooseBallType(-1);
		Random rnd = new Random();
		int move = rnd.nextInt(30);
		
		if (rnd.nextInt(2) == 0)
		{
			gameWindow.moveBallLeft(move);
		}
		else
		{
			gameWindow.moveBallRight(move);
			move = -move;
		}
		
		if (move < 0)
		{
			move = rnd.nextInt(30);
		}
		else
		{
			move = -rnd.nextInt(30);
		}
		
		int numero = -(rnd.nextInt(1000) + 1000);
		gameWindow.releaseBall(numero, move);
		System.out.println("Computador: " + numero);
	}

	public void sendPoints(boolean b) {
		if (b)
		{
			gameServer.sendMessagePlayer(1, "NomeOutroJogador", 1);
			gameServer.sendMessagePlayer(1, player2.getName(), 1);
			gameServer.sendMessagePlayer(1, "Pontuacao", player1.getScoreBoard().getTotalScore());
			gameServer.sendMessagePlayer(1, "PontuacaoOutro", player2.getScoreBoard().getTotalScore());
		}
		else 
		{
			gameServer.sendMessagePlayer(2, "NomeOutroJogador", 1);
			gameServer.sendMessagePlayer(2, player1.getName(), 1);
			gameServer.sendMessagePlayer(2, "Pontuacao", player2.getScoreBoard().getTotalScore());
			gameServer.sendMessagePlayer(2, "PontuacaoOutro", player1.getScoreBoard().getTotalScore());

		}
	}


}
