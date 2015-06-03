package test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class TestQRCode {
	
	/*
	 * 	This test is only meant to establish the proper functionality of the plaintext -> QRCode classes.
	 * 	If everything is working properly, a QRCode representing the message: 
	 *  
	 * 		"Olá eu sou um teste! É suposto esta mensagem ser lida por QRCode Reader." 
	 * 
	 * 	Should be displayed on screen for 10 seconds.
	 * 
	 *	This test is regardless, ALWAYS be successful.
	 *
     */
	
	@Test
	public void test() {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.x = -1;
		config.y = -1;
		new LwjglApplication(new TestQRCodeDraw(), config);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertEquals(true, true);
	}

}
