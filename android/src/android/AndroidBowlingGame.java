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
	DataPacket data;

	private SpriteBatch batch;
	private BitmapFont font;
	private int width, height;
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

	public interface Callback {		public void startScannerActivity();}
	public void setMyGameCallback(Callback callback) {callbackInterface = callback;}
	Callback callbackInterface;



	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.RED);
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight(); 
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
				Gdx.gl.glClearColor(0, .93f, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				break;
			case 2:
				Gdx.gl.glClearColor(1, 1, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				break;
			case 3:
				Gdx.gl.glClearColor(1, 0, 0, 1);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				break;
			default:
				break;
			}
		}

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
		Gamestate = 2;

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
					while (!playerClient.readReady)
						;
					data = playerClient.getLatestData();
					if(data.Value == 0)
						stop = true;
					else
						textmessages = "Connected! You are Player Number " + Integer.toString(PlayerNumber) +  "Timer:" + Integer.toString( (int) data.Value);

				}
				stateWaitingTurn();

			}
		}
				).start();

		playerClient.sendMessageServer("Name", 1);
		playerClient.sendMessageServer(PlayerName, 1);

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


		(new Thread() {
			boolean stop = false;

			public void run() {
				while (!stop)
				{
					while (!playerClient.readReady)
						;
					data = playerClient.getLatestData();
					if(data.Event == "NomeOutroJogador")
					{
						while (!playerClient.readReady)
							;
						data = playerClient.getLatestData();
						OtherPlayerName = data.Event;
					} 
					else if (data.Event == "Pontuacao")
					{
						MyScore = (int) data.Value;
					} 
					else if (data.Event == "PontuacaoOutro")
					{
						OtherPlayerScore = (int) data.Value;
					} 
					else if (data.Event == "Turno")
					{
						stop = true;
					}


				}
				stateGameOptions();

			}
		}
				).start();

		Gamestate = 6;
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
		textmessages = "Touch anywhere on the screen, hold and SWING!";
		motionState = 0;
		Gamestate = 8;


		(new Thread() {
			public void run() {
				boolean pointingdown = false;
				boolean goingupwards = false;
				float pitch;

				while(motionState != 3)
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
						motionState = 3;
					}


				}
			}
		}
				).start();



	}
	public void stateAwaitingResults()
	{
		textmessages = "The Ball is moving, please wait...";


		(new Thread() {
			boolean stop = false;

			public void run() {
				while (!stop)
				{
					while (!playerClient.readReady)
						;
					data = playerClient.getLatestData();
					if (data.Event == "JogadaCompleta")
					{
						stop = true;
					}


				}
				stateWaitingTurn();

			}
		}
				).start();


		Gamestate = 9;
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
		if (Gamestate == 2 && screenX > QRbuttonSprite.getX() && screenX <  QRbuttonSprite.getX()+QRbuttonSprite.getWidth() 
				&& screenY < (height - QRbuttonSprite.getY()) 
				&& screenY > (height- (QRbuttonSprite.getY()+QRbuttonSprite.getHeight()))
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
				playerClient.sendMessageServer("Pontuacao", 1);

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

				playerClient.sendMessageServer("Move", 0);
				return true;
			}

			if (screenX > ArrowSpriteRight.getX() && screenX <  ArrowSpriteRight.getX()+ArrowSpriteRight.getWidth() 
					&& screenY < (height - ArrowSpriteRight.getY()) 
					&& screenY > (height- (ArrowSpriteRight.getY()+ArrowSpriteRight.getHeight()))
					)
			{

				playerClient.sendMessageServer("Move", 1);
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
			playerClient.sendMessageServer("BallChange", Ballchoice);

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
				playerClient.sendMessageServer("BallForce", elapsedtime);
				playerClient.sendMessageServer("BallRoll", finalrollvalue);
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




}
