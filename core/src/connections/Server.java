package connections;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public Boolean player1_isConnected = false;
	public Boolean player2_isConnected = false;

	public Boolean readPlayer1 = false;
	public Boolean readPlayer2 = false;
	

	private ServerSocket serverSocket;
	private Socket clientSocket[] = new Socket[2];

	private BufferedReader inputFromPlayer1;
	private BufferedReader inputFromPlayer2;
	private PrintStream outputToPlayer1;
	private PrintStream outputToPlayer2;

	private Thread connect_Player1 = new Thread();
	private Thread connect_Player2 = new Thread();
	private Thread receive_Player1 = new Thread();
	private Thread receive_Player2 = new Thread();

	private String messagePlayer1;
	private String messagePlayer2;
	private float valuePlayer1;
	private float valuePlayer2;
	private DataPacket Data1 = new DataPacket();
	private DataPacket Data2 = new DataPacket();

	private final int IP_PORT = 34567; 

	public Server() throws IOException
	{
		serverSocket = new ServerSocket(IP_PORT);
	}

	public Boolean connectPlayers(int PlayerNumber) throws IOException
	{
		if (PlayerNumber == 1 && connect_Player1.isAlive() == false && !player1_isConnected)
		{
			connect_Player1 = new Thread(new connectPlayer1());
			connect_Player1.start();
			return true;
		}
		if (PlayerNumber == 2 && connect_Player2.isAlive() == false && !player2_isConnected)
		{
			connect_Player2 = new Thread(new connectPlayer2());
			connect_Player2.start();
			return true;
		}
		return false;
	}

	public Boolean startListeningPlayers(int PlayerNumber)
	{
		if (PlayerNumber == 1 && receive_Player1.isAlive() == false && player1_isConnected == true)
		{
			receive_Player1 = new Thread(new receivePlayer1());
			receive_Player1.start();
			return true;
		}
		if (PlayerNumber == 2 && receive_Player2.isAlive() == false && player2_isConnected == true)
		{
			receive_Player2 = new Thread(new receivePlayer2());
			receive_Player2.start();
			return true;
		}

		return false;
	}

	public DataPacket getLatestDataPlayers(int PlayerNumber){
		if(PlayerNumber == 1 && readPlayer1)
		{
			readPlayer1 = false;
			return Data1;
		}
		if(PlayerNumber == 2 && readPlayer2)
		{
			readPlayer2 = false;
			return Data2;
		}
		
		return null;
	}

	public Boolean disconnectPlayer(int PlayerNumber) throws IOException{
		if (PlayerNumber == 1 && connect_Player1.isAlive() == false && player1_isConnected == true)
		{
			player1_isConnected = false;
			readPlayer1 = false;
			clientSocket[0].close();
			return true;
		}
		if (PlayerNumber == 2 && connect_Player2.isAlive() == false && player2_isConnected == true)
		{
			player2_isConnected = false;
			readPlayer2 = false;
			clientSocket[1].close();
			return true;
		}
		return false;

	}

	public void sendMessagePlayer(int PlayerNumber, String event, float value)
	{
		if (PlayerNumber == 1 && player1_isConnected == true)
		{
			outputToPlayer1.println(event);
			outputToPlayer1.println(Float.toString(value));	
		}
		if (PlayerNumber == 2 && player2_isConnected == true)
		{
			outputToPlayer2.println(event);
			outputToPlayer2.println(Float.toString(value));	
		}
	}


	private class connectPlayer1 implements Runnable
	{
		public void run() {

			try {
				clientSocket[0] = serverSocket.accept();

				inputFromPlayer1 = new BufferedReader(new InputStreamReader(clientSocket[0].getInputStream()));
				outputToPlayer1 = new PrintStream(clientSocket[0].getOutputStream());

				//Send Player Number to Socket
				outputToPlayer1.println("Player");
				outputToPlayer1.println(Integer.toString(1));

				player1_isConnected = true;

			} catch (IOException e) {e.printStackTrace();}
		}
	}
	private class connectPlayer2 implements Runnable
	{
		public void run() {

			try {
				clientSocket[1] = serverSocket.accept();

				inputFromPlayer2 = new BufferedReader(new InputStreamReader(clientSocket[1].getInputStream()));
				outputToPlayer2 = new PrintStream(clientSocket[1].getOutputStream());

				//Send Player Number to Socket
				outputToPlayer2.println("Player");
				outputToPlayer2.println(Integer.toString(2));

				player2_isConnected = true;


			} catch (IOException e) {e.printStackTrace();}
		}
	}

	private class receivePlayer1 implements Runnable
	{
		public void run() {
			while(player1_isConnected)
			{
				try {
					if (inputFromPlayer1.ready() && !readPlayer1)
					{
						messagePlayer1 = inputFromPlayer1.readLine();
						while(!inputFromPlayer1.ready())
							;
						valuePlayer1 = Float.parseFloat(inputFromPlayer1.readLine());
						Data1.setData(messagePlayer1, valuePlayer1);

						readPlayer1 = true;

						//Controlled
						/*
							 String input = inputFromPlayer2.readLine();

							if (input.matches("IMPLEMENTSOMETHING"))
							{
								while(!inputFromPlayer2.ready() && player2_isConnected)
									;
								valuePlayer2 = Float.parseFloat(inputFromPlayer2.readLine());
							} 
						 */
					}
				}	catch (NumberFormatException e) {e.printStackTrace();} 
				catch (IOException e) {e.printStackTrace();}
			}

		}
	}
	private class receivePlayer2 implements Runnable
	{
		public void run() {

			while(player2_isConnected)
			{
				try {
					if (inputFromPlayer2.ready() && !readPlayer2)
					{
						//Freedom Version
						messagePlayer2 = inputFromPlayer2.readLine();
						while(!inputFromPlayer2.ready())
							;
						valuePlayer2 = Float.parseFloat(inputFromPlayer2.readLine());

						Data2.setData(messagePlayer2, valuePlayer2);

						readPlayer2 = true;

					}
				}	catch (NumberFormatException e) {e.printStackTrace();} 
				catch (IOException e) {e.printStackTrace();}
			}

		}
	}

}
