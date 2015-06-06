package logic.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
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
		// 5 - About Page, the old state #5 became unecesary by design
	// 6 - Play or Scores
	// 7 - Ball Selection and Strafing
	// 8 - Playing the Ball
	// 9 - Wait Resolution, jump back to 6 


	public String ServerIP = "Unknown";
	private Client playerClient;

	private String textmessages;
	private String PlayerName = "";
	private int PlayerNumber = 0;
	private String OtherPlayerName = "";
	private int MyScore = 0;
	private int OtherPlayerScore = 0;

	private SpriteBatch batch;
	private	BitmapFont font42;
	private	BitmapFont fontAbout;
	private	BitmapFont fontLPOO;
	private BitmapFont font;
	private float width, height;
	private float width_scale, height_scale;
	private Texture QRbuttonText;
	private Sprite QRbuttonSprite;
	private Texture BallText;
	private Sprite  BallSprite;
	private Texture ArrowText;
	private Sprite ArrowSpriteLeft;
	private Sprite ArrowSpriteRight;
	private Texture PlayText;
	private Sprite PlaySprite;
	private Texture ScoreText;
	private Sprite ScoreButton;
	private Sprite BackgroundImage;
	private Sprite LogoImage;
	private Sprite ButaoEntradaJogar;
	private Sprite ButaoEntradaSobre;
	private Sprite ButaoEntradaSair;
	private Sprite BackButton;
	private String OtherScores = "";
	int Ballchoice;
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

	public interface Callback {	public void startScannerActivity();}
	public void setMyGameCallback(Callback callback) {callbackInterface = callback;}
	Callback callbackInterface;

	boolean stopsending = false;

	public void create() {
		batch = new SpriteBatch();
		initgraphics();
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
		BackgroundImage.draw(batch);
		if (Gamestate == 1)
		{
			LogoImage.draw(batch);
			font42.draw(batch, "Name:" + PlayerName, width/2 - width/4, height/2 - height/16);
			ButaoEntradaJogar.draw(batch);
			ButaoEntradaSobre.draw(batch);
			ButaoEntradaSair.draw(batch);
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
		if (Gamestate == 5)
		{
			fontLPOO.draw(batch, "LPOO", width/2-width/6, 5*height/6);
			fontAbout.draw(batch, "Pedro Carvalho nº 201306506", width/7, 5*height/6 - height/6);
			fontAbout.draw(batch, "   António Ramadas nº 201303568", width/7, 5*height/6 - height/6 - height/12);
			BackButton.draw(batch);
		}

		//font.draw(batch, log1, 0, 20);
		//font.draw(batch, log2, 0, 40);
		//font.draw(batch, log3, 0, 60);
		//font.draw(batch, log4, 0, 80);
		//font.draw(batch, log5, 0, 100);
		batch.end();

	}


	private void statePlayerName()
	{	
		Gamestate = 1; 
	}
	private void stateQRPrompt()
	{
		textmessages = PlayerName + ": Use QRCode to scan game server's IP address";
		QRbuttonText = new Texture(Gdx.files.internal("QRbutton.png"));
		QRbuttonSprite = new Sprite(QRbuttonText);
		defineSprite(width/2 - width/3, 3*height/4 - 100, QRbuttonSprite);

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
	private void stateAwaitingConnection()
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
	private void stateAwaitingPlayers()
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
	private void stateWaitingTurn()
	{
		textmessages = "You are now waiting for your turn";
		ScoreButton = new Sprite(ScoreText);
		defineSprite(width/2 - ScoreButton.getWidth()*width_scale/2 ,height/2 - ScoreButton.getHeight()*height_scale - 10, ScoreButton);

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
	private void stateGameOptions()
	{
		textmessages = "Now you get to choose a ball and where to throw from";
		BallSprite = new Sprite(BallText);
		defineSprite(width/2 - BallSprite.getWidth()*width_scale/2, 3*height/4 - BallSprite.getHeight()*height_scale/2 - 100, BallSprite);
		ArrowSpriteLeft = new Sprite(ArrowText);
		ArrowSpriteRight = new Sprite(ArrowText);
		ArrowSpriteRight.flip(true, false);
		defineSprite(width/9 - ArrowSpriteLeft.getWidth()*width_scale/2 , height/5 , ArrowSpriteLeft);
		defineSprite(8*width/9 - ArrowSpriteRight.getWidth()*width_scale/2, height/5 , ArrowSpriteRight);
		PlaySprite = new Sprite(PlayText);
		defineSprite(width/2 - PlaySprite.getWidth()*width_scale/2, height/5, PlaySprite);
		Gamestate = 7;




	}
	private void stateBallSwing()
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


					if (pitch > 45 && pointingdown == false && goingupwards == false)
					{
						pointingdown = true;
						motionState = 2;
					}
					else if (pitch < 45 && pointingdown == true && goingupwards == false)
					{
						pointingdown = false;
						goingupwards = true;
						startTime = System.currentTimeMillis();
					}
					else if (pitch > 45 && pointingdown == false && goingupwards == true)
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
	private void stateAwaitingResults()
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
	private void stateAbout()
	{
		BackButton = new Sprite(ArrowText);
		defineSprite(width/2 - BackButton.getWidth()*width_scale/2, height/8, BackButton);
		
		Gamestate = 5; 
	}

	public boolean keyTyped(char character) {

		if (Gamestate == 1 && PlayerName.length() < 8)
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
		if (Gamestate == 1 && checkColision(screenX, screenY, ButaoEntradaJogar))
		{
			Gdx.input.setOnscreenKeyboardVisible(true);
			return true;
		}
		if (Gamestate == 1 && checkColision(screenX, screenY, ButaoEntradaSair))
		{
			Gdx.app.exit();
			return true;
		}
		if (Gamestate == 1 && checkColision(screenX, screenY, ButaoEntradaSobre))
		{
			stateAbout();
			return true;
		}
		if (Gamestate == 1 && screenX < 100 && screenY < 100)
		{
			PlayerName = "AAA";
			stateQRPrompt();
			return true;
		}		
		if (Gamestate == 2 && checkColision(screenX, screenY, QRbuttonSprite))
		{
			callbackInterface.startScannerActivity();
			return true;
		}
		if (Gamestate == 6)
		{
			if (checkColision(screenX, screenY,ScoreButton))
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
			if (checkColision(screenX, screenY,ArrowSpriteLeft))
			{

				playerClient.sendMessageServer("Move", 0);  updatelog("Enviou Move|0");
				return true;
			}

			if (checkColision(screenX, screenY,ArrowSpriteRight))
			{

				playerClient.sendMessageServer("Move", 1);  updatelog("Enviou Move|1");
				return true;
			}

			if (checkColision(screenX, screenY,PlaySprite))
			{
				stateBallSwing();
				return true;
			}

		}
		if (Gamestate == 8)
		{
			motionState = 1;
			return true;
		}
		if (Gamestate == 5 )
		{
			if (checkColision(screenX, screenY,BackButton))
			{
				statePlayerName();
				return true;
			}
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


								if (screenX > ArrowSpriteLeft.getX() && checkColision(screenX, screenY,ArrowSpriteLeft))
								{

									playerClient.sendMessageServer("Move", 0); updatelog("Enviou Move|0");
								}

								if (screenX > ArrowSpriteRight.getX() && checkColision(screenX, screenY,ArrowSpriteRight))
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
	private boolean checkColision(int x, int y, Sprite colide)
	{
		return(
				x > 	colide.getX()
				&&  x <  	colide.getX()+(colide.getWidth()		) 
				&&	y < (	height - colide.getY()) 
				&& 	y > (	height - colide.getY() - colide.getHeight() 	)
				);

	}
	private void defineSprite (float x, float y, Sprite place)
	{
		place.setX(x); place.setY(y); 
		place.setBounds(x, y, place.getWidth()*width_scale, place.getHeight()*height_scale);	
	}
	private void updatelog(String msg)
	{
		log5 = log4;
		log4 = log3;
		log3 = log2;
		log2 = log1;
		log1 = msg;

	}
	public float resizeText(float width, float currentSize, float currentWidth){
	    return (width * currentSize / currentWidth);
	}
	private void initgraphics() {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		width_scale = width/480f;
		height_scale = height/720f;
		updatelog(""+width +"|"+height);
		updatelog(""+width_scale +"|"+height_scale);
		font = new BitmapFont();
		font.setColor(Color.RED);
		updatelog("Scales:"+font.getScaleX() +"|"+font.getScaleY());
		if(Gdx.input.isPeripheralAvailable(Peripheral.Compass))
			textmessages = "Please Input your Player Name";
		else
			textmessages = "Couldn't detect accelerometer";
		BallText = new Texture(Gdx.files.internal("BBall1.png"));
		ArrowText = new Texture(Gdx.files.internal("leftarrow.png"));
		PlayText = new Texture(Gdx.files.internal("PlayIcon.png"));
		ScoreText = new Texture(Gdx.files.internal("Scorebutton.png"));
		BackgroundImage = new Sprite(new Texture(Gdx.files.internal("Base.png")));
		defineSprite(0,0,BackgroundImage);
		LogoImage = new Sprite(new Texture(Gdx.files.internal("BowlingMain.png")));
		defineSprite(0,height-LogoImage.getHeight()*height_scale,LogoImage);
		ButaoEntradaJogar = new Sprite(new Texture(Gdx.files.internal("ButaoJogar.png")));
		ButaoEntradaSobre = new Sprite(new Texture(Gdx.files.internal("ButaoSobre.png")));
		ButaoEntradaSair = new Sprite(new Texture(Gdx.files.internal("ButaoSair.png")));
		
		defineSprite(width/2 - ButaoEntradaJogar.getWidth()*width_scale/2,3*height/12,ButaoEntradaJogar);
		defineSprite(width/2 - ButaoEntradaSobre.getWidth()*width_scale/2,2*height/12,ButaoEntradaSobre);
		defineSprite(width/2 - ButaoEntradaSair.getWidth()*width_scale/2, 1*height/12,ButaoEntradaSair);

		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("SCOREBOARD.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = (int) (42 * width_scale);
		parameter.color = Color.BLUE;
		font42 = generator.generateFont(parameter);
		generator.dispose();
		
		generator = new FreeTypeFontGenerator(Gdx.files.internal("TravelingTypewriter.ttf"));
		parameter = new FreeTypeFontParameter();
		parameter.size = (int) (22 * width_scale);
		parameter.color = Color.BLACK;
		fontAbout = generator.generateFont(parameter);
		generator.dispose();
		
		generator = new FreeTypeFontGenerator(Gdx.files.internal("Long_Shot.ttf"));
		parameter = new FreeTypeFontParameter();
		parameter.size = (int) (92* width_scale);
		parameter.color = Color.BLUE;
		fontLPOO = generator.generateFont(parameter);
		generator.dispose();
	
		
		
		statePlayerName();
	}
}

