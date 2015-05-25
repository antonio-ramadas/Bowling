package connection;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Plane;

public class AndroidClientTest implements ApplicationListener, GestureListener {


	private SpriteBatch batch;
	private BitmapFont font;
	private Texture texture;
	private Texture scanTexture;
	private Plane sprite;
	private Sprite scanButton;

	Socket clientSocket;
	BufferedReader  inputFromServer;
	PrintStream outputToServer;
	static final int IP_PORT = 34567;
	static final String HARDCODED_EXTERNAL_IP = "192.168.1.8";
	static String EXTERNAL_IP = "STILL UNKNOWN";

	String message = "Startup!";

	static boolean connected = false;
	static boolean connecting = false;

	static int i = 0;

	float accelY = 0;

	int PlayerNumber = 0;

	int BallChoice = 1;
	boolean SendBallChange = false;


	//This is required to make a call to a new Android activity
	Callback callbackInterface;


	@Override
	public void create() {
		connected = false;
		batch = new SpriteBatch();
		font = new BitmapFont();
		texture = new Texture(Gdx.files.internal("BBall1.png"));
		sprite = new Plane(texture);
		sprite.goToPosition(Gdx.graphics.getWidth()/2 - sprite.getWidth()/2, 300);

		GestureDetector gd = new GestureDetector(this);
		Gdx.input.setInputProcessor(gd);

		scanTexture = new Texture(Gdx.files.internal("shoddybutton.png"));
		scanButton = new Sprite(scanTexture);
		scanButton.setX(Gdx.graphics.getWidth()/2 - scanButton.getWidth()/2); scanButton.setY(300); 


	}



	@Override
	public void render() {

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();


		font.setColor(Color.BLUE);
		font.draw(batch,  "Events: " + message, 100, 20);


		if (!connecting)
		{
			scanButton.draw(batch);
		}


		if (!connected)
		{

			//Connection Message
			font.setColor(Color.BLACK);
			font.draw(batch,  "Reaching for IP: "  + EXTERNAL_IP + ":" + Integer.toString(IP_PORT), 100, 100);
			font.setColor(Color.RED);
			font.draw(batch,  " - - Trying to connect... (Hardcoded) - - ", 100, 80);



		} 
		else
		{
			//Connection Message
			font.setColor(Color.GREEN);
			font.draw(batch,  " - - Connected! - - ", 100, 80);

			//Accelerometer Tracking
			accelY = Gdx.input.getAccelerometerY();
			String motionmessage = "Current Y-axis force: ";
			motionmessage += Float.toString(accelY);
			font.draw(batch, motionmessage, 100, 120);

			//Player Number
			font.draw(batch, "Player " + Integer.toString(PlayerNumber), 100, 140);

			//Draw the Ball
			sprite.draw(batch);

		}


		font.setColor(Color.RED);
		font.draw(batch, Integer.toString(i), 10,20);
		i++;


		batch.end();
	}


	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		if (connected)
		{
			if (velocityX < -500)
			{
				BallChoice ++;
				if (BallChoice > 5)
					BallChoice = 1;
				SendBallChange = true;
			}
			if (velocityX > 500)
			{
				BallChoice --;
				if (BallChoice < 1)
					BallChoice = 5;
				SendBallChange = true;
			}

			switch (BallChoice)
			{
			case 1:
				texture = new Texture(Gdx.files.internal("BBall1.png"));
				break;
			case 2:
				texture = new Texture(Gdx.files.internal("BBall2.png"));
				break;
			case 3:
				texture = new Texture(Gdx.files.internal("BBall3.png"));
				break;
			case 4:
				texture = new Texture(Gdx.files.internal("BBall4.png"));
				break;
			case 5:
				texture = new Texture(Gdx.files.internal("BBall5.png"));
				break;
			default:
				break;
			}
			sprite.changeTexture(texture);
			message = "Changed to Ball " + Integer.toString(BallChoice) + ", check if Server noticed!";      
		}	
		return true;
	}


	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {

		if (x > scanButton.getX() && x < scanButton.getX()+scanButton.getWidth()
				&& y < (Gdx.graphics.getHeight() - scanButton.getY()) 
				&& y > (Gdx.graphics.getHeight() - (scanButton.getY()+scanButton.getHeight()) )
				&& connecting == false)
		{

			callbackInterface.startScannerActivity();

			connecting = true;
			Thread connectToServer = new Thread(new ConnectThread());
			connectToServer.start();

		}		


		//--------------------------------------------------------
		// Existe aqui um erro: Se o Barcode Scanner, uma aplicação da goole usada pela biblioteca ZXing, ainda não
		// estiver instalada no sistema android, o "jogo libgdx" do android crasha. Caso esteja, corre normalmente.
		
		// As duas soluções seriam correr o Barcode Scanner antes do jogo (garantindo a sua intalação). Ou não usar libdx 
		// para formumlar esta GUI. O teste é "throwaway" por isso não se fez a correcção.
		
		// Este erro não ocorrerá na entrega final pois o libdx é desnecessário à GUI android e só foi usado
		// por conveniencia.
		//--------------------------------------------------------

		return true;
	}

	// Define an interface for your a callbackbto the android launcher
	public interface Callback {		
		public void startScannerActivity();
	}
	public void setMyGameCallback(Callback callback) {
		callbackInterface = callback;
	}
	public void setExternalIP(String newIP) {
		EXTERNAL_IP = newIP;
	}




	class ConnectThread implements Runnable
	{
		public void run() {
			try {

				while (EXTERNAL_IP == "STILL UNKNOWN")
					;

				clientSocket =  new Socket(InetAddress.getByName(EXTERNAL_IP), IP_PORT);
				if (clientSocket.isConnected())
					connected = true;
				inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputToServer = new PrintStream(clientSocket.getOutputStream());



				message = "Waiting for a Player message";
				while(!inputFromServer.ready() && connected)
					;
				String input = inputFromServer.readLine();
				if (input.matches("Player"))
				{
					message = "Waiting for a Number message";
					while(!inputFromServer.ready() && connected)
						;
					PlayerNumber = Integer.parseInt(inputFromServer.readLine());
				}


				message = "Got Player Number starting to send info";
				SendBallChange = true;
				new Thread(new sendInfo()).start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	class sendInfo implements Runnable
	{
		public void run() {

			while (true)
			{
				if(SendBallChange)
				{
					//Send Ball value to Server, once
					SendBallChange = false;
					outputToServer.println("Ball");
					outputToServer.println(Integer.toString(BallChoice));
				}

				if(connected)
				{
					//Send Accelerometer's value to Server
					outputToServer.println("accelY");
					outputToServer.println(Float.toString(accelY));	
				}
			}
		}

	}


	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}
	@Override
	public boolean longPress(float x, float y) {
		return false;
	}
	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		return false;
	}
	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		return false;
	}
	@Override
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}
	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		return false;
	}
	@Override
	public void resize(int width, int height) {
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
