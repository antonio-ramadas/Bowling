package connection;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class DesktopServerTest extends ApplicationAdapter {
	private SpriteBatch batch;
	private BitmapFont font;
	ServerSocket serverSocket;
	Socket clientSocket;
	BufferedReader inputFromClient;
	PrintStream outputToClient;
	Sprite QRcode;

	static float inboundValue = 0;
	static String HOST_ADDRESS = "TOTAL_FAILURE";
	static final int IP_PORT = 34567;
	static boolean connected = false;
	static boolean receiving = false;
	Thread connecthread;

	static int i = 0;

	static int Playercount = 1;

	static int BallChoice = 0;
	


	public void create() {        
		batch = new SpriteBatch();
		font = new BitmapFont();	

		//Open Socket and run connection threads
		try {
			serverSocket = new ServerSocket(IP_PORT);
			connecthread = new Thread(new onCreateConnect());
			connecthread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Get Host address
		try {
			HOST_ADDRESS = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Generate QR code texture
		BitMatrix QRmatrix = generateQRCode(HOST_ADDRESS);
		Pixmap QRimage = new Pixmap(200,200, Pixmap.Format.RGB565);		
		for (int i = 0; i < 200; i++) {
			for (int j = 0; j < 200; j++) {
				if (QRmatrix.get(i, j))
				{
					QRimage.setColor(Color.BLACK);
					QRimage.drawPixel(i, j);
				}
				else {
					QRimage.setColor(Color.WHITE);
					QRimage.drawPixel(i, j);
				}
			}
		}
		Texture QRtext = new Texture(QRimage);
		QRcode = new Sprite(QRtext);
		QRcode.setX(Gdx.graphics.getWidth()/2 - QRcode.getWidth()/2); QRcode.setY(350);
	}


	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		font.setColor(Color.BLACK);


		//Draw the QRCode
		QRcode.draw(batch);

		//Server Address Message
		font.draw(batch,  "Server Address: " + HOST_ADDRESS + ":" + Integer.toString(IP_PORT), 100, 100);


		if (!connected)
		{
			font.setColor(Color.RED);
			font.draw(batch,  " - - Waiting for Connection... - - ", 100, 80);
		}
		else
		{
			font.setColor(Color.GREEN);
			font.draw(batch,  " - - Connected! - - ", 100, 80);
		}



		font.setColor(Color.RED);
		font.draw(batch, "Accelerometer value = " + Float.toString(inboundValue), 100,160);
		String orientationMessage = "I still dunno";
		if (connected)
		{
			if (inboundValue > 0)
				orientationMessage = "Upwards";
			else
				orientationMessage = "Downwards";
		}
		font.draw(batch, "Phone is " + orientationMessage + ", Ball Coiche was: " + Integer.toString(BallChoice), 100,140);
		font.draw(batch, Integer.toString(i), 10,20);
		i++;

		batch.end();
	}



	class onCreateConnect implements Runnable
	{
		public void run() {
			try {
				System.out.println("Opening a server socket...");
				clientSocket = serverSocket.accept();
				connected = true;
				inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputToClient = new PrintStream(clientSocket.getOutputStream());

				//Send Player Number to Socket
				System.out.println("Connected, sending out player number " + Integer.toString(Playercount));
				outputToClient.println("Player");
				outputToClient.println(Integer.toString(Playercount));	

				System.out.println("Listening to Player " + Integer.toString(Playercount) + "...");
				if (!receiving)
				{
					receiving = true;
					new Thread(new receiveData()).start();
				}
				
				//The variables I was using are bad for multi-client, I'll just remake a connection class with what I learned
				new Thread(new onCreateConnect()).start();
				Playercount++;

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	class receiveData implements Runnable
	{
		public void run() {

			while(connected)
			{
				try {
					if (inputFromClient.ready())
					{

						String input = inputFromClient.readLine();

						if (input.matches("Ball"))
						{
							while(!inputFromClient.ready() && connected)
								;
							BallChoice = Integer.parseInt(inputFromClient.readLine());
						} else 

							if (input.matches("accelY"))
							{
								while(!inputFromClient.ready() && connected)
									;
								inboundValue = Float.parseFloat(inputFromClient.readLine());
							}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
	}



	public BitMatrix generateQRCode (String HostAddress){

		BitMatrix QRmatrix = null;
		try {
			QRCodeWriter writer = new QRCodeWriter();
			QRmatrix = writer.encode(HostAddress, BarcodeFormat.QR_CODE, 200, 200);

		} catch (WriterException e) {
			e.printStackTrace();
		}
		return QRmatrix;
	}


	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}
	@Override
	public void pause() {
		super.pause();
	}
	@Override
	public void resume() {
		super.resume();
	}



}
