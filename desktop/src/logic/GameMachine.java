package logic;

import graphics.GameWindow;
import graphics.ImagePontuation;
import graphics.QRCode;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
	public ImagePontuation image;

	public Sprite backgroundSprite;
	public Texture backgroundTexture;
	Texture player1Not;
	Texture player2Not;
	Texture player1Image;
	Texture player2Image;
	Sprite player1NotSprite;
	Sprite player2NotSprite;
	Sprite player1ImageSprite;
	Sprite player2ImageSprite;

	Sound soundStrike;
	Sound soundSpare;
	Sound soundGameOver;

	class ConnectionThread extends Thread {
		public void run() {
			try {
				gameServer.connectPlayers(1);

				while (!gameServer.player1_isConnected)
					Thread.sleep(10);

				gameServer.connectPlayers(2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

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

			spriteBatch = new SpriteBatch();
			QRimage = (new QRCode(InetAddress.getLocalHost().getHostAddress(), 200, 200)).getImage();
			initial = true;
			launching = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		new ConnectionThread().start();

		loadImages();

		image = new ImagePontuation();
		soundStrike = Gdx.audio.newSound(Gdx.files.internal("bin/strike.mp3"));
		soundSpare = Gdx.audio.newSound(Gdx.files.internal("bin/spare.mp3"));
		soundGameOver = Gdx.audio.newSound(Gdx.files.internal("bin/gameOver.mp3"));
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
					disposePlayerImages();
					gameServer.sendMessagePlayer(1, "TIME", 0);
					gameServer.sendMessagePlayer(2, "TIME", 0);
					image.writePlayersName(player1.getName(), player2.getName());
				}				
				else if ((connectionTime = temp) < 0)
				{
					initial = false;
					is2Players = false;
					connectionTime = 15f;
					isPlayer1Turn = true;
					ret = true;
					player2.setName("Computador");
					disposePlayerImages();
					gameServer.sendMessagePlayer(1, "TIME", 0);
					image.writePlayersName(player1.getName(), player2.getName());
				}
			}
		}

		if (initial)
		{
			spriteBatch.begin();
			QRimage.setCenter(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
			QRimage.draw(spriteBatch);

			if (gameServer.player1_isConnected)
			{
				player1ImageSprite.draw(spriteBatch);
			}
			else
			{
				player1NotSprite.draw(spriteBatch);
			}

			if (gameServer.player2_isConnected)
			{
				player2ImageSprite.draw(spriteBatch);
			}
			else
			{
				player2NotSprite.draw(spriteBatch);
			}			

			spriteBatch.end();
		}

		return ret;
	}

	private void disposePlayerImages() {
		// TODO Auto-generated method stub
		player1Not.dispose();
		player2Not.dispose();
		player1Image.dispose();
		player2Image.dispose();
	}

	private void loadImages() {
		// TODO Auto-generated method stub
		player1Not = new Texture("bin/playerNotLeft.png");
		player2Not = new Texture("bin/playerNotRight.png");
		player1Image = new Texture("bin/playerConnectedLeft.png");
		player2Image = new Texture("bin/playerConnectedRight.png");

		player1NotSprite = new Sprite(player1Not);
		player1NotSprite.setPosition(0, -30);

		player2NotSprite = new Sprite(player2Not);
		player2NotSprite.setPosition(Gdx.graphics.getWidth() - player2NotSprite.getWidth(), -30);

		player1ImageSprite = new Sprite(player1Image);
		player1ImageSprite.setPosition(0, -30);

		player2ImageSprite = new Sprite(player2Image);
		player2ImageSprite.setPosition(Gdx.graphics.getWidth() - player2ImageSprite.getWidth(), -30);
	}

	private void setNameOfPlayer(int i) throws InterruptedException {
		// TODO Auto-generated method stub
		waitPlayer(i);

		//System.out.println(i);

		DataPacket d = gameServer.getLatestDataPlayers(i);
		//System.out.println(i + " " + d.Event);
		if (d.Event.equals("Name"))
		{
			waitPlayer(i);
			d = gameServer.getLatestDataPlayers(i);
			setPlayerName(i, d.Event);
		}	
	}

	private void setPlayerName(int i, String event) {
		// TODO Auto-generated method stub
		//System.out.println(i + " Nome " + event);
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

		//System.out.println("count = " + count);
		//System.out.println("numberPinsDown = " + numberPinsDown);

		if (restartPins)
		{
			isPlayer1Turn = !isPlayer1Turn;
			launching = false;
			waitingPlayer = false;
		}

		return numberPinsDown;
	}

	public void notifyPlayer(Boolean[] pin) {
		// TODO Auto-generated method stub

		if (isPlayer1Turn)
		{
			isPlayer1Turn = player1.makePlay(numberPinsDown(pin));
			//System.out.println("---------------------------------------");
			//System.out.println("player 1 turn: " + isPlayer1Turn);
			//System.out.println("player 1 score: " + player1.getScoreBoard().getTotalScore());
			//System.out.println("---------------------------------------");
			gameServer.sendMessagePlayer(1, "jogou", 15);

			int play = player1.getScoreBoard().getLastPlay();
			int frame = (play+1)/2;

			checkAndPlaySound(player1.getScoreBoard().getPinsFelled(frame*2 - 1), player1.getScoreBoard().getPinsFelled(frame*2));
		}
		else
		{
			isPlayer1Turn = !player2.makePlay(numberPinsDown(pin));
			if (is2Players)
			{
				gameServer.sendMessagePlayer(2, "jogou", 15);
			}
			gameIsOver = (player2.getScoreBoard().getNextPlay() == -1);
			//System.out.println("---------------------------------------");
			//System.out.println("player 2 turn: " + !isPlayer1Turn);
			//System.out.println("player 2 score: " + player2.getScoreBoard().getTotalScore());
			//System.out.println("---------------------------------------");

			int play = player2.getScoreBoard().getLastPlay();
			int frame = (play+1)/2;

			checkAndPlaySound(player2.getScoreBoard().getPinsFelled(frame*2 - 1), player2.getScoreBoard().getPinsFelled(frame*2));

			if (isPlayer1Turn)
			{
				if (gameIsOver)
				{
					writeToImageFinal();
					gameServer.sendMessagePlayer(1, "acabou", 1);

					if (is2Players)
					{
						gameServer.sendMessagePlayer(2, "acabou", 1);
					}
				}
				else
				{
					writeToImage();
					image.exportImage();
				}
			}
		}
	}

	private void checkAndPlaySound(int pins1, int pins2)
	{
		if (pins1 == 10 && pins1+pins2 == 10)
		{
			soundStrike.play(0.3f);
			//System.out.println("play strike");
			return;
		}

		if (pins1 + pins2 == 10)
		{
			soundSpare.play(0.3f);
			//System.out.println("play spare");
		}

		return;
	}

	public void writeToImageFinal() {
		// TODO Auto-generated method stub
		int play = player1.getScoreBoard().latestScoredFrame();
		image.writeScoreHalfPlay(1, play, 1, firstSquare(player1.getScoreBoard().getPinsFelled(2*play-1)));
		image.writeScoreHalfPlay(1, play, 2, firstSquare(player1.getScoreBoard().getPinsFelled(2*play)));
		image.writeScoreHalfPlay(1, play, 3, firstSquare(player1.getScoreBoard().getPinsFelled(2*play+1)));

		image.writeScoreHalfPlay(2, play, 1, firstSquare(player2.getScoreBoard().getPinsFelled(2*play-1)));
		image.writeScoreHalfPlay(2, play, 2, firstSquare(player2.getScoreBoard().getPinsFelled(2*play)));
		image.writeScoreHalfPlay(2, play, 3, firstSquare(player2.getScoreBoard().getPinsFelled(2*play+1)));

		for (int i = 1; i <= 10; i++)
		{
			image.writeScorePlay(1, i, Integer.toString(player1.getScoreBoard().getScoreFrame(i)));
			image.writeScorePlay(2, i, Integer.toString(player2.getScoreBoard().getScoreFrame(i)));
		}

		image.writeFinalScore(player1.getScoreBoard().getTotalScore(), player2.getScoreBoard().getTotalScore());

		image.exportImage();

		backgroundTexture = new Texture("bin/scoreSheet.png");
		backgroundSprite = new Sprite(backgroundTexture);
		spriteBatch = new SpriteBatch();
		backgroundSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * (backgroundSprite.getHeight()/Gdx.graphics.getHeight()));
		backgroundSprite.setPosition(0, Gdx.graphics.getHeight() / 2 - backgroundSprite.getHeight() / 2);

		timerToEnd = 15f;

		soundGameOver.play(0.3f);
	}

	private String secondSquare(int first, int second)
	{
		if (first == 10)
		{
			return "";
		}

		if (first + second == 10)
		{
			return "/";
		}

		return Integer.toString(second);
	}

	private String firstSquare(int first)
	{
		if (first == 10)
		{
			return "X";
		}

		return Integer.toString(first);
	}

	private void writeToImage() {
		// TODO Auto-generated method stub
		int play = player1.getScoreBoard().latestScoredFrame();
		image.writeScoreHalfPlay(1, play, 1, firstSquare(player1.getScoreBoard().getPinsFelled(2*play-1)));
		image.writeScoreHalfPlay(1, play, 2, secondSquare(player1.getScoreBoard().getPinsFelled(2*play-1), player1.getScoreBoard().getPinsFelled(2*play)));

		image.writeScoreHalfPlay(2, play, 1, firstSquare(player2.getScoreBoard().getPinsFelled(2*play-1)));
		image.writeScoreHalfPlay(2, play, 2, secondSquare(player2.getScoreBoard().getPinsFelled(2*play-1), player2.getScoreBoard().getPinsFelled(2*play)));
	}

	public void getPlayerPlay(GameWindow gameWindow, boolean b) {

		//System.out.println("Entrou no getPlayerPlay!!!!");
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
					////System.out.println(b1 + " " + gameServer.readPlayer1);
					DataPacket data;
					////System.out.println("entrou...");
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (b1)
					{
						data = gameServer.getLatestDataPlayers(1);
					}
					else
					{
						data = gameServer.getLatestDataPlayers(2);
					}
					if (data == null)
					{
						continue;
					}
					////System.out.println("    |  e passou!");
					String s = new String(data.Event);
					float v = data.Value;
					//if (b1)
					{
						//System.out.println("s = " + s + " | v = " + v);
						if (s.equals("BallChange"))
						{
							ballType = (int) v;
							gw.chooseBallType(ballType-1);
						} else if (s.equals("Move"))
						{
							if ((int) v == 0)
							{
								gw.moveBallLeft();
							} else
							{
								gw.moveBallRight();
							}
						} else if (s.equals("BallForce"))
						{
							Force = -v;
							//System.out.println("ball force");
						} else if (s.equals("BallRoll"))
						{
							Roll = -v;
							gw.releaseBall(Force, Roll);
							//System.out.println("Ball type: " + ballType + " Force: " + Force + " Roll: " + Roll);
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
		//System.out.println("Computador: " + numero);
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
