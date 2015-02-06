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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.FileSystemLocation;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

/**
 * Main class.
 */
public class Finger extends BasicGame {
	/** Input file path. */
	private static final String INPUT_FILE = "input.txt";

	/** Scroll time, in ms. */
	private static final int SCROLL_TIME = 5000;

	/** Pixels between each image. */
	private static final int IMAGE_OFFSET = 20;

	/** List of students. */
	private ArrayList<Student> students = new ArrayList<Student>();

	/** Animation states. */
	private enum State { INITIAL, SCROLLING, SELECTED };

	/** Current state. */
	private State state = State.INITIAL;

	/** Time spent in current state. */
	private int stateTime = 0;

	/** Index in student list. */
	private int studentIndex = 0;

	/** Current pixel offset from index. */
	private float offsetPos = 0f;

	/** Student. */
	private class Student {
		/** The student's name. */
		public String name;

		/** Image of the student. */
		public Image image;

		/**
		 * Constructor.
		 * @param name the student's name
		 * @param image image of the student
		 */
		public Student(String name, Image image) {
			this.name = name;
			this.image = image;
		}
	}

	/**
	 * Creates the game.
	 * @param title the game title
	 */
	public Finger(String title) {
		super(title);
	}

	@Override
	public void init(GameContainer container) throws SlickException {
		// create student objects
		students.clear();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(ResourceLoader.getResourceAsStream(INPUT_FILE)))) {
			String line;
			float width = -1f;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (tokens.length < 2)
					continue;
				Image img = new Image(String.format("%s.png", tokens[0]));
				if (width == -1) {
					img = img.getScaledCopy(container.getHeight() * 0.6f / img.getHeight());
					width = img.getWidth();
				} else
					img = img.getScaledCopy(width / img.getWidth());
				Student student = new Student(tokens[1], img);
				students.add(student);
			}
		} catch (IOException e) {
			Log.error("Failed to create student objects.", e);
		}
		students.trimToSize();
		Collections.shuffle(students);
		studentIndex = 0;

		// TODO
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		g.setBackground(Color.white);
		if (state == State.INITIAL)
			return;

		int width = container.getWidth();
		int height = container.getHeight();

		if (state == State.SCROLLING) {
			int imgWidth = students.get(studentIndex).image.getWidth();
			int numDraw = (width + imgWidth + IMAGE_OFFSET) / (imgWidth + IMAGE_OFFSET);

			for (int d = 0 - numDraw / 2; d <= numDraw / 2; ++d) {
				Image img = students.get(mod(studentIndex + d, students.size())).image;
				if (d == 0) {
					img.setAlpha(1f);
				} else {
					img.setAlpha(0.5f);
				}
				img.draw(width / 2 - imgWidth + offsetPos + d * (imgWidth + IMAGE_OFFSET), height / 2 - img.getHeight() / 2);
			}
		}
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException {
		if (state == State.SCROLLING) {
			stateTime += delta;
			if (stateTime > SCROLL_TIME) {
//				state = State.SELECTED;
//				return;
			}

			int imgWidth = students.get(0).image.getWidth();
			offsetPos += (int) (delta * acceleration());
			if (offsetPos > imgWidth + IMAGE_OFFSET) {
				System.out.println(studentIndex);
				studentIndex = mod(studentIndex - 1, students.size());
				offsetPos = mod((int) offsetPos, imgWidth + IMAGE_OFFSET);
			}
		}
	}

	@Override
	public void keyPressed(int key, char c) {
		if (state == State.INITIAL)
			state = State.SCROLLING;
	}

	/**
	 * Calculates A modulo B with result in the range [0, B).
	 */
	private int mod(int a, int b) {
		return ((a % b) + b) % b;
	}

	// TODO come up with a function
	private float acceleration() {
		return 0.2f;
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
