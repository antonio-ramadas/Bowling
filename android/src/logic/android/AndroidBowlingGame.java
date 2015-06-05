package logic.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

import connections.Client;
import connections.DataPacket;

public class AndroidBowlingGame implements ApplicationListener, InputProcessor, GestureListener  {

	private int Gamestate = 0;
	// 0 - Not yet initialized
	// 1 - Player Inputs Name
	// 2 - Before Connecting / QR Prompt
	// 3 - Attempting Connection
	// 4 - Wait for other Players
	// 5 - Waiting for your turn --- We had a change of design ideas but don't want to delete it.
	// 6 - Play or Scores
	// 7 - Ball Selection and Strafing
	// 8 - Playing the Ball
	// 9 - Wait Resolution, jump back to 6 


	public String ServerIP = "Unknown";
	private Client playerClient;

	private SpriteBatch batch;
	private BitmapFont font;
	private float width, height;
	private float width_scale, height_scale;


	private String textmessages;

	private String PlayerName = "";
	private int PlayerNumber = 0;
	private String OtherPlayerName = "";
	private int MyScore = 0;
	private int OtherPlayerScore = 0;

	private TextField inputPlayerName;
	TextFieldStyle style;

	Texture QRbuttonText;
	Sprite QRbuttonSprite;
	Texture BallText;
	Sprite  BallSprite;
	int Ballchoice;
	Texture ArrowText;
	Sprite ArrowSpriteLeft;
	Sprite ArrowSpriteRight;
	Texture PlayText;
	Sprite PlaySprite;
	Texture ScoreText;
	Sprite  ScoreButton;
	String OtherScores = "";
	private boolean receivingScores = false;

	int motionState = 0;
	float finalrollvalue = 0;	
	long startTime;
	long elapsedtime;

	String log1 = "nada";
	String log2 = "nada";
	String log3 = "nada";
	String log4 = "nada";
	String log5 = "nada";

	public interface Callback {		public void startScannerActivity();}
	public void setMyGameCallback(Callback callback) {callbackInterface = callback;}
	Callback callbackInterface;

	boolean stopsending = false;

	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.RED);
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		width_scale = width/480f;
		height_scale = height/720f;
		if(Gdx.input.isPeripheralAvailable(Peripheral.Compass))
			textmessages = "Please Input your Player Name";
		else
			textmessages = "Couldn't detect accelerometer";
		Gamestate = 1; statePlayerName();
		BallText = new Texture(Gdx.files.internal("BBall1.png"));
		ArrowText = new Texture(Gdx.files.internal("leftarrow.png"));
		PlayText = new Texture(Gdx.files.internal("PlayIcon.png"));
		ScoreText = new Texture(Gdx.files.internal("Scorebutton.png"));
		InputMultiplexer im = new InputMultiplexer();
		GestureDetector gd = new GestureDetector(this);
		im.addProcessor(gd);
		im.addProcessor(this);
		Gdx.input.setInputProcessor(im);

		startPermanentPollers();
	}
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, textmessages, width/2 - width/3, 3*height/4);
		if (Gamestate == 1)
		{
			inputPlayerName.setText(PlayerName);
			inputPlayerName.draw(batch, 1);
		}
		if (Gamestate == 2)
		{
			QRbuttonSprite.draw(batch);
		}
		if (Gamestate == 6)
		{
			ScoreButton.draw(batch);
			//PlaySprite.draw(batch);
			if (receivingScores)
			{
				font.draw(batch, OtherScores, width/2 + width/4, 3*height/4);		
			}
		}
		if (Gamestate == 7)
		{
			BallSprite.draw(batch);
			ArrowSpriteLeft.draw(batch);
			ArrowSpriteRight.draw(batch);
			PlaySprite.draw(batch);
		}
		if (Gamestate == 8)
		{
			switch(motionState)
			{
			case 0:
				break;
			case 1:
				Gdx.gl.glClearColor(1, 0, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				break;
			case 2:
				Gdx.gl.glClearColor(1, 1, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				break;
			case 3:
				Gdx.gl.glClearColor(0, .93f, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				break;
			default:
				break;
			}
		}

		font.draw(batch, log1, 0, 20);
		font.draw(batch, log2, 0, 40);
		font.draw(batch, log3, 0, 60);
		font.draw(batch, log4, 0, 80);
		font.draw(batch, log5, 0, 100);
		batch.end();

	}


	public void statePlayerName()
	{	
		style = new TextFieldStyle();
		style.fontColor = Color.RED;
		style.font = new BitmapFont();
		inputPlayerName = new TextField("", style);	
		inputPlayerName.setMessageText("Click here!");
		inputPlayerName.setX(width/2 - width/3); inputPlayerName.setY(3*height/4 - 100); 
	}
	public void stateQRPrompt()
	{
		textmessages = PlayerName + ": Use QRCode to scan game server's IP address";
		QRbuttonText = new Texture(Gdx.files.internal("QRbutton.png"));
		QRbuttonSprite = new Sprite(QRbuttonText);
		QRbuttonSprite.setX(width/2 - width/3); QRbuttonSprite.setY(3*height/4 - 100); 
		QRbuttonSprite.setScale(width_scale, height_scale);
		Gamestate = 2;

		updatelog("estado2");

		(new Thread() {
			public void run() {
				while(ServerIP == "Unknown")
					try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
				stateAwaitingConnection();
			}
		}
				).start();
	}
	public void stateAwaitingConnection()
	{
		textmessages = PlayerName + ": You are now trying to connect to " + ServerIP;
		playerClient = new Client(ServerIP);
		playerClient.connectServer();

		Gamestate = 3;


		(new Thread() {
			public void run() {
				while(playerClient.isConnected == false)
					;
				PlayerNumber = playerClient.playerNumber;
				playerClient.startListeningPlayers();


				stateAwaitingPlayers();
			}
		}
				).start();



	}
	public void stateAwaitingPlayers()
	{
		textmessages = "Connected! You are Player Number " + Integer.toString(PlayerNumber) +  " Timer: 15" ;
		Gamestate = 4;

		(new Thread() {
			boolean stop = false;

			public void run() {
				while (!stop)
				{
					DataPacket received;
					while (!playerClient.readReady)
						;
					received = playerClient.getLatestData(); 
					if(received.Value == 0)
						stop = true;
					else
						textmessages = "Connected! You are Player Number " + Integer.toString(PlayerNumber) +  "Timer:" + Integer.toString( (int) received.Value);

				}
				stateWaitingTurn();

			}
		}
				).start();

		playerClient.sendMessageServer("Name", 1); updatelog("Enviou Name|1");
		playerClient.sendMessageServer(PlayerName, 1); updatelog("Enviou " + PlayerName + "|1");

	}
	public void stateORIGINALAwaitingTurns()
	{
		textmessages = PlayerName + ": Now you're just waiting for your turn to come";
		Gamestate = 5;

		//....
		stateWaitingTurn();
	}
	public void stateWaitingTurn()
	{
		textmessages = "You should be able to pick to make a play, or to request your scores";
		/*PlaySprite = new Sprite(PlayText);
		PlaySprite.setX(width/2 - PlaySprite.getWidth()/2);PlaySprite.setY(height/2);*/
		ScoreButton = new Sprite(ScoreText);
		ScoreButton.setX(width/2 - ScoreButton.getWidth()/2);ScoreButton.setY(height/2 - ScoreButton.getHeight() - 10);

		Gamestate = 6;


		(new Thread() {
			boolean stop = false;

			public void run() {
				while (!stop)
				{
					DataPacket received;
					while (!playerClient.readReady)
						;
					received = playerClient.getLatestData(); updatelog("Recebeu " + received.Event + "|" + received.Value);
					if(received.Event.equals("NomeOutroJogador"))
					{
						while (!playerClient.readReady)
							;
						received = playerClient.getLatestData();
						OtherPlayerName = received.Event; updatelog("Recebeu " + received.Event + "|" + received.Value);
					} 
					else if (received.Event.equals("Pontuacao"))
					{
						MyScore = (int) received.Value;
					} 
					else if (received.Event.equals("PontuacaoOutro"))
					{
						OtherPlayerScore = (int) received.Value;
					} 
					else if (received.Event.equals("Turno"))
					{
						stop = true;
					}


				}
				stateGameOptions();

			}
		}
				).start();

	}
	public void stateGameOptions()
	{
		textmessages = "Now you get to choose a ball and where to throw from";
		BallSprite = new Sprite(BallText);
		BallSprite.setX(width/2 - BallSprite.getWidth()/2); BallSprite.setY(3*height/4 - BallSprite.getHeight()/2 - 100);
		Ballchoice = 1;
		ArrowSpriteLeft = new Sprite(ArrowText);
		ArrowSpriteRight = new Sprite(ArrowText);
		ArrowSpriteRight.flip(true, false);
		ArrowSpriteLeft.setX(width/9 - ArrowSpriteLeft.getWidth()/2);ArrowSpriteLeft.setY(height/5);
		ArrowSpriteRight.setX(8*width/9 - ArrowSpriteRight.getWidth()/2);ArrowSpriteRight.setY(height/5);
		PlaySprite = new Sprite(PlayText);
		PlaySprite.setX(width/2 - PlaySprite.getWidth()/2);PlaySprite.setY(height/5);

		Gamestate = 7;




	}
	public void stateBallSwing()
	{
		stopsending = false;
		textmessages = "Touch anywhere on the screen, hold and SWING!";
		motionState = 0;
		Gamestate = 8;


		(new Thread() {
			public void run() {
				boolean pointingdown = false;
				boolean goingupwards = false;
				float pitch;

				while(motionState != 3 && !stopsending)
				{
					pitch = Gdx.input.getPitch();

					if (motionState == 0)
					{
						pointingdown = false;
						goingupwards = false;
					} 


					if (pitch > 70 && pointingdown == false && goingupwards == false)
					{
						pointingdown = true;
						motionState = 2;
					}
					else if (pitch < 70 && pointingdown == true && goingupwards == false)
					{
						pointingdown = false;
						goingupwards = true;
						startTime = System.currentTimeMillis();
					}
					else if (pitch > 70 && pointingdown == false && goingupwards == true)
					{
						pointingdown = true;
						goingupwards = false;
					}
					else if (pitch < -20 && pointingdown == false && goingupwards == true)
					{
						finalrollvalue = Gdx.input.getRoll();
						elapsedtime = System.currentTimeMillis() - startTime;
						elapsedtime = ((elapsedtime -50) * (-30/19)) + 2500;
						motionState = 3;
					}


				}
			}
		}
				).start();



	}
	public void stateAwaitingResults()
	{
		stopsending = true;
		textmessages = "The Ball is moving, please wait...";
		Gamestate = 9;


		(new Thread() {
			boolean stop = false;

			public void run() {
				while (!stop)
				{
					while (!playerClient.readReady)
						;
					DataPacket received;
					received = playerClient.getLatestData();
					String s = new String(received.Event);
					if (s.equals("jogou"))// == 15)
					{
						stop = true;
					}

				}
				stateWaitingTurn();

			}
		}
				).start();


	}


	public boolean keyTyped(char character) {

		if (Gamestate == 1)
		{
			PlayerName += character;
			return true;
		}

		return false;
	}
	public boolean keyDown(int keycode) {
		if (Gamestate == 1 && keycode == Input.Keys.ENTER && PlayerName != "")
		{
			Gdx.input.setOnscreenKeyboardVisible(false);
			stateQRPrompt();
			return true;
		}
		if (Gamestate == 1 && keycode == Input.Keys.BACKSPACE)
		{
			PlayerName = "";
			return true;
		}
		return false;
	}
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (Gamestate == 1 && screenX > inputPlayerName.getX() && screenX <  inputPlayerName.getX()+inputPlayerName.getWidth() 
				&& screenY < (height - inputPlayerName.getY()) 
				&& screenY > (height- (inputPlayerName.getY()+inputPlayerName.getHeight()))
				)
		{
			Gdx.input.setOnscreenKeyboardVisible(true);
			return true;
		}
		if (Gamestate == 1 && screenX < 100 && screenY < 100)
		{
			PlayerName = "Default";
			stateQRPrompt();
			return true;
		}		
		if (Gamestate == 2 && screenX > QRbuttonSprite.getX() && screenX <  QRbuttonSprite.getX()+(QRbuttonSprite.getWidth()*width_scale) 
				&& screenY < (height - QRbuttonSprite.getY()) 
				&& screenY > (height- (QRbuttonSprite.getY()+QRbuttonSprite.getHeight()*height_scale))
				)
		{
			callbackInterface.startScannerActivity();
			return true;
		}
		if (Gamestate == 6)
		{
			/*if (screenX > PlaySprite.getX() && screenX <  PlaySprite.getX()+PlaySprite.getWidth() 
					&& screenY < (height - PlaySprite.getY()) 
					&& screenY > (height- (PlaySprite.getY()+PlaySprite.getHeight()))
					)
			{
				stateGameOptions();
				return true;
			}*/

			if (screenX > ScoreButton.getX() && screenX <  ScoreButton.getX()+ScoreButton.getWidth() 
					&& screenY < (height - ScoreButton.getY()) 
					&& screenY > (height- (ScoreButton.getY()+ScoreButton.getHeight()))
					)
			{			
				playerClient.sendMessageServer("Pontuacao", 1);  updatelog("Enviou Pontuacao|1");

				receivingScores = true;
				textmessages = PlayerName + "Score: " + Integer.toString(MyScore);
				OtherScores = OtherPlayerName + "Score: " + Integer.toString(OtherPlayerScore);

				return true;
			}
		}	
		if (Gamestate == 7)
		{
			if (screenX > ArrowSpriteLeft.getX() && screenX <  ArrowSpriteLeft.getX()+ArrowSpriteLeft.getWidth() 
					&& screenY < (height - ArrowSpriteLeft.getY()) 
					&& screenY > (height- (ArrowSpriteLeft.getY()+ArrowSpriteLeft.getHeight()))
					)
			{

				playerClient.sendMessageServer("Move", 0);  updatelog("Enviou Move|0");
				return true;
			}

			if (screenX > ArrowSpriteRight.getX() && screenX <  ArrowSpriteRight.getX()+ArrowSpriteRight.getWidth() 
					&& screenY < (height - ArrowSpriteRight.getY()) 
					&& screenY > (height- (ArrowSpriteRight.getY()+ArrowSpriteRight.getHeight()))
					)
			{

				playerClient.sendMessageServer("Move", 1);  updatelog("Enviou Move|1");
				return true;
			}

			if (screenX > PlaySprite.getX() && screenX <  PlaySprite.getX()+PlaySprite.getWidth() 
					&& screenY < (height - PlaySprite.getY()) 
					&& screenY > (height- (PlaySprite.getY()+PlaySprite.getHeight()))
					)
			{
				stateBallSwing();
				return true;
			}

		}
		if (Gamestate == 8)
		{
			motionState = 1;
		}

		return false;
	}
	public boolean fling(float velocityX, float velocityY, int button) {
		if (Gamestate == 7)
		{
			if (velocityX < -500)
			{
				Ballchoice ++;
				if (Ballchoice > 5)
					Ballchoice = 1;
			}
			if (velocityX > 500)
			{
				Ballchoice --;
				if (Ballchoice < 1)
					Ballchoice = 5;
			}

			switch (Ballchoice)
			{
			case 1:
				BallText = new Texture(Gdx.files.internal("BBall1.png"));
				break;
			case 2:
				BallText = new Texture(Gdx.files.internal("BBall2.png"));
				break;
			case 3:
				BallText = new Texture(Gdx.files.internal("BBall3.png"));
				break;
			case 4:
				BallText = new Texture(Gdx.files.internal("BBall4.png"));
				break;
			case 5:
				BallText = new Texture(Gdx.files.internal("BBall5.png"));
				break;
			default:
				break;
			}
			BallSprite.setTexture(BallText);
			playerClient.sendMessageServer("BallChange", Ballchoice);  updatelog("Enviou BallChange|" + Integer.toString(Ballchoice));

			return true;
		}


		return false;
	}
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(Gamestate == 8)
		{
			if (motionState == 1 || motionState == 2)
			{
				motionState = 0;
			}
			if (motionState == 3)
			{
				playerClient.sendMessageServer("BallForce", elapsedtime);  updatelog("Enviou BallForce|" + Long.toString(elapsedtime));
				playerClient.sendMessageServer("BallRoll", finalrollvalue); updatelog("Enviou BallRoll|" + Float.toString(finalrollvalue));
				stateAwaitingResults();
			}
			return true;
		}
		return false;
	}


	public boolean longPress(float x, float y) 
	{

		return false;
	}
	public void resize(int width, int height) {		
	}
	public void pause() {
	}
	public void resume() {
	}
	public void dispose() {
	}
	public boolean keyUp(int keycode) {
		return false;
	}
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
	public boolean scrolled(int amount) {
		return false;
	}
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		return false;
	}
	public boolean panStop(float x, float y, int pointer, int button) {
		return false;
	}
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	
	private void startPermanentPollers() {
		//Holding buttons on state 7;
		(new Thread() {
			public void run() {
				long time = System.currentTimeMillis();
				while(true)
				{
					if (Gamestate == 7)
					{
						long timedisplacement = System.currentTimeMillis() - time;
						if (timedisplacement > 200)
						{
							time = System.currentTimeMillis();
							timedisplacement = 0;

							if (Gdx.input.isTouched())
							{
								int screenX = Gdx.input.getX();
								int screenY = Gdx.input.getY();


								if (screenX > ArrowSpriteLeft.getX() && screenX <  ArrowSpriteLeft.getX()+ArrowSpriteLeft.getWidth() 
										&& screenY < (height - ArrowSpriteLeft.getY()) 
										&& screenY > (height- (ArrowSpriteLeft.getY()+ArrowSpriteLeft.getHeight()))
										)
								{

									playerClient.sendMessageServer("Move", 0); updatelog("Enviou Move|0");
								}

								if (screenX > ArrowSpriteRight.getX() && screenX <  ArrowSpriteRight.getX()+ArrowSpriteRight.getWidth() 
										&& screenY < (height - ArrowSpriteRight.getY()) 
										&& screenY > (height- (ArrowSpriteRight.getY()+ArrowSpriteRight.getHeight()))
										)
								{

									playerClient.sendMessageServer("Move", 1);  updatelog("Enviou Move|1");
								}


							}
						}
					}

				}
			}
		}
				).start();
	}
	public void updatelog(String msg)
	{
		log5 = log4;
		log4 = log3;
		log3 = log2;
		log2 = log1;
		log1 = msg;

	}



}
