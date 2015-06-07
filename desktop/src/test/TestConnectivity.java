package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import connections.DataPacket;
import connections.Server;

public class TestConnectivity {

	/*
	 *	This test requires an auxiliary program to complete as it is meant to test wether or not data transfer
	 *	between a desktop server and two android clients is successful.
	 * 	The desktop clients may be launched by going to the Android manifest in this project and setting the 
	 * 	activity launcher java file as follows: 
	 *
	 *		<activity
	 *			android:name="test.ConnectivityTestLauncher"
	 *
	 *	You have 30 seconds to connect the Android devices.
	 *
	 */

	private Boolean Player1Connected = false;
	private Boolean Player2Connected = false;

	@Test
	public void test_connections() {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.x = -1;
		config.y = -1;
		new LwjglApplication(new TestConnectivityDraw(), config);

		new Thread(new testThread()).start();

		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertEquals(Player1Connected, true);
		assertEquals(Player2Connected, true);
	}


	private class testThread implements Runnable
	{
		public void run() {
			try {
				Server testServer = new Server();
				testServer.connectPlayers(1);
				while(testServer.player1_isConnected == false)
					Thread.sleep(10);
				testServer.startListeningPlayers(1);
				while(testServer.readPlayer1 == false)
					Thread.sleep(10);
				DataPacket receivedData;
				receivedData = testServer.getLatestDataPlayers(1);
				if (receivedData.Event.matches("Connected") && receivedData.Value == 1.0)
					Player1Connected = true;
				testServer.sendMessagePlayer(1, "Complete", 1);
				testServer.disconnectPlayer(1);
				
				testServer.connectPlayers(2);
				while(testServer.player2_isConnected == false)
					Thread.sleep(10);
				testServer.startListeningPlayers(2);
				while(testServer.readPlayer2 == false)
					Thread.sleep(10);
				receivedData = testServer.getLatestDataPlayers(2);
				if (receivedData.Event.matches("Connected") && receivedData.Value == 1.0)
					Player2Connected = true;
				testServer.sendMessagePlayer(2, "Complete", 1);
				testServer.disconnectPlayer(2);

			} catch (IOException e) {e.printStackTrace();} catch (InterruptedException e) {e.printStackTrace();
			}
		}
	}

}
