package itdelatrisu.finger;

import java.io.File;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.FileSystemLocation;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

/**
 * Main class.
 */
public class Finger extends BasicGame {

	public Finger(String title) {
		super(title);
	}

	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		
	}

	@Override
	public void init(GameContainer container) throws SlickException {
		
	}

	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		
	}

	public static void main(String[] args) {
		Log.setVerbose(false);

		// set path for lwjgl natives - NOT NEEDED if using JarSplice
		File nativeDir = new File("./target/natives/");
		if (nativeDir.isDirectory())
			System.setProperty("org.lwjgl.librarypath", nativeDir.getAbsolutePath());

		// set the resource paths
		ResourceLoader.addResourceLocation(new FileSystemLocation(new File("./res/")));

		// start the application
		Finger finger = new Finger("Finger");
		AppGameContainer app;
		try {
			app = new AppGameContainer(finger);

			// basic app settings
			app.setDisplayMode(1024, 768, false);
//			String[] icons = { "icon16.png", "icon32.png" };
//			app.setIcons(icons);
			app.setForceExit(true);
			app.setTargetFrameRate(60);
			app.setVSync(true);
			app.setMusicOn(false);
			app.setShowFPS(false);
			app.setAlwaysRender(true);

			app.start();
		} catch (SlickException e) {
			Log.error("Could not start application.", e);
		}
	}
}
