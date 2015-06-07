package desktop;

import graphics.GameWindow;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) throws Throwable {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		configWindow(config);
		new LwjglApplication(new GameWindow(), config);
	}
	
	public static void configWindow(LwjglApplicationConfiguration config)
	{
		config.fullscreen = true;
		config.width = 1366;
		config.height = 768;
		config.title = "Bowling Game";
		config.x = -1;
		config.y = -1;
		config.backgroundFPS = 60;
		config.foregroundFPS = 60;
		config.addIcon("BowlingIcon.png", Files.FileType.Internal);
	}
}
