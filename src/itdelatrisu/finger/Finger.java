/*
 * Finger
 * Copyright (C) 2015 Jeffrey Han
 *
 * Finger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Finger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Finger.  If not, see <http://www.gnu.org/licenses/>.
 */

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

	/**
	 * Creates the game.
	 * @param title the game title
	 */
	public Finger(String title) {
		super(title);
	}

	@Override
	public void init(GameContainer container) throws SlickException {
		
	}

	@Override
	public void render(GameContainer container, Graphics g)
			throws SlickException {
		
	}

	@Override
	public void update(GameContainer container, int delta)
			throws SlickException {
		
	}

	/**
	 * Sets up an launches the application.
	 */
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
