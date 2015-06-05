package connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
	
	public Boolean isConnected = false;
	public Boolean readReady = false;
	public int playerNumber;
	
	private Socket clientSocket;

	private BufferedReader  inputFromServer;
	private PrintStream outputToServer;

	private Thread connect_Server = new Thread();
	private Thread receive_Server = new Thread();

	private String messageServer;
	private float valueServer;
	private DataPacket Data = new DataPacket();

	private String EXTERNAL_IP;
	private final int IP_PORT = 34567; 
	
	private boolean send = true;

	
	public Client(String s)
	{
		EXTERNAL_IP = s;
	}
	
	public Boolean connectServer()
	{
		if (connect_Server.isAlive() == false &&  !isConnected)
		{
			connect_Server = new Thread(new connectServerThread());
			connect_Server.start();
			return true;
		}
		return false;
	}
	
	public Boolean startListeningPlayers()
	{
		if (receive_Server.isAlive() == false && isConnected)
		{
			receive_Server = new Thread(new receiveServerThread());
			receive_Server.start();
			return true;
		}
		return false;
	}
	
	public DataPacket getLatestData(){
		if (readReady)
		{
			readReady = false;
			return Data;
		}
		return null;
	}
	
	public Boolean disconnectServer() throws IOException{
		if (connect_Server.isAlive() == false && isConnected == true)
		{
			isConnected = false;
			readReady = false;
			clientSocket.close();
			return true;
		}
		return false;
		
	}

	public void sendMessageServer(String event, float value)
	{
		while (!send);
		
		if (isConnected == true)
		{
			send = false;
			outputToServer.println(event);
			outputToServer.println(Float.toString(value));	
			
			long startTime = System.currentTimeMillis();
			long elapsed = System.currentTimeMillis() - startTime;
			while (elapsed < 200)
				elapsed = System.currentTimeMillis() - startTime;
			send = true;
		}

	}
	
	private class connectServerThread implements Runnable
	{
		public void run() {
			try {
				clientSocket =  new Socket(InetAddress.getByName(EXTERNAL_IP), IP_PORT);
				inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputToServer = new PrintStream(clientSocket.getOutputStream());


				//Server should send Player Number Info
				while(!inputFromServer.ready())
					;
				messageServer = inputFromServer.readLine();
				if (messageServer.matches("Player"))
				{
					while(!inputFromServer.ready())
						;
					playerNumber = Integer.parseInt(inputFromServer.readLine());
					
					isConnected = true;
				}
				else
				{
					playerNumber = -1;
				}
					
				

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private class receiveServerThread implements Runnable
	{
		public void run() {

			while(isConnected)
			{
				try {
					if (inputFromServer.ready() && !readReady)
					{
						
						messageServer = inputFromServer.readLine();
						while(!inputFromServer.ready())
							;
						valueServer = Float.parseFloat(inputFromServer.readLine());

						Data.setData(messageServer, valueServer);

						readReady = true;

					}
				}	
				catch (NumberFormatException e) {e.printStackTrace();} 
				catch (IOException e) {e.printStackTrace();}
			}

		}
	}
	
}
