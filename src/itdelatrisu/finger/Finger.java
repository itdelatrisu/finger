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

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.util.FileSystemLocation;
import org.newdawn.slick.util.Log;
import org.newdawn.slick.util.ResourceLoader;

/**
 * Main class.
 */
public class Finger extends BasicGame {
	/** Input file path. */
	private static final String INPUT_FILE = "input.txt";

	/** Border image path. */
	private static final String BORDER_FILE = "border.png";
	
	/** Transition time, in ms. */
	private static final int TRANSITION_TIME = 4000;

	/** Scroll time, in ms. */
	private static final int SCROLL_TIME = 4000;

	/** Delay before centering selected image, in ms. */
	private static final int SELECT_DELAY = 1250;

	/** Initial scroll speed multiplier. */
	private static final float INITIAL_SPEED = 10f;

	/** Scrolling start speed multiplier. */
	private static final float SCROLLING_START_SPEED = 0.35f;

	/** Final speed. */
	private static final float FINAL_SPEED = 0.1f;

	/** Pixels between each image. */
	private static final int IMAGE_OFFSET = 20;

	/** Maximum number of re-selections (randomly chosen). */
	private static final int MAX_RESELECTIONS = 3;

	/** Maximum number of indices to shift (randomly) upon re-selection. */
	private static final int MAX_SHIFT = 5;

	/** List of students. */
	private ArrayList<Student> students = new ArrayList<Student>();

	/** Animation states. */
	private enum State { INITIAL, TRANSITION, SCROLLING, CENTER, SELECT, FINAL };

	/** Current state. */
	private State state = State.INITIAL;

	/** Time spent in current state. */
	private int stateTime;

	/** Index in student list. */
	private int studentIndex;

	/** Current pixel offset from index. */
	private float offsetPos;

	/** Fixed width of images. */
	private int imgWidth = -1;

	/** Border image. */
	private Image border;

	/** Font. */
	private UnicodeFont font;

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

	/** Queue of re-selections. */
	private Queue<Reselect> reselect;

	/** Current re-selection object. */
	private Reselect r;

	/** Fields for a re-selection. */
	private class Reselect {
		/** The number of indices to shift (+:right, -:left). */
		public int shift;

		/** The time to pause after shifting, in ms. */
		public int delay;

		/** The speed to move the images. */
		public float speed;

		/**
		 * Constructor.
		 * @param shift the number of indices to shift (+:right, -:left)
		 * @param delay the time to pause after shifting, in ms
		 * @param speed the speed to move the images
		 */
		public Reselect(int shift, int delay, float speed) {
			this.shift = shift;
			this.delay = delay;
			this.speed = speed;
		}
	}

	/** Game container. */
	private GameContainer container;

	/**
	 * Creates the game.
	 * @param title the game title
	 */
	public Finger(String title) {
		super(title);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(GameContainer container) throws SlickException {
		this.container = container;

		// load fonts
		try {
			Font javaFont = new Font("Verdana", Font.PLAIN, 36);
			this.font = new UnicodeFont(javaFont);
			ColorEffect colorEffect = new ColorEffect();
			font.setPaddingTop(3);
			font.setPaddingBottom(3);
			font.addAsciiGlyphs();
			font.getEffects().add(colorEffect);
			font.loadGlyphs();
		} catch (Exception e) {
			Log.error("Failed to load fonts.", e);
		}

		// create student objects
		students.clear();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(ResourceLoader.getResourceAsStream(INPUT_FILE)))) {
			String line;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split("\t");
				if (tokens.length < 2)
					continue;
				Image img = new Image(String.format("%s.png", tokens[0]));
				if (imgWidth == -1) {
					img = img.getScaledCopy(container.getHeight() * 0.6f / img.getHeight());
					imgWidth = img.getWidth();
				} else
					img = img.getScaledCopy(imgWidth / img.getWidth());
				Student student = new Student(tokens[1], img);
				students.add(student);
			}
		} catch (IOException e) {
			Log.error("Failed to create student objects.", e);
		}
		students.trimToSize();

		// load border
		this.border = new Image(BORDER_FILE).getScaledCopy(container.getWidth(), container.getHeight());

		reset();
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		g.setBackground(Color.white);

		int width = container.getWidth();
		int height = container.getHeight();

		// draw pictures
		int numDraw = (width + imgWidth + IMAGE_OFFSET) / (imgWidth + IMAGE_OFFSET);
		for (int d = 0 - numDraw / 2; d <= numDraw / 2; ++d) {
			Image img = students.get(mod(studentIndex + d, students.size())).image;
			float alpha = 1f;
			if (d != 0) {
				if (state == State.INITIAL || state == State.TRANSITION)
					alpha = 0.5f;
				else if (state == State.SCROLLING && stateTime < SCROLL_TIME)
					alpha = 0.5f - ((float) stateTime / SCROLL_TIME) * 0.4f;
				else
					alpha = 0.1f;
			}
			img.setAlpha(alpha);
			img.draw(width / 2 - imgWidth + offsetPos + d * (imgWidth + IMAGE_OFFSET), height / 2 - img.getHeight() / 2);
		}

		if (state == State.INITIAL)
			return;

		// black bars
		if (state != State.TRANSITION) {
			float multiplier = (state == State.SCROLLING && stateTime <= SCROLL_TIME) ?
					((float) stateTime / SCROLL_TIME) : 1f;
			float barHeight = height * 0.2f * multiplier;
			g.setColor(Color.black);
			g.fillRect(0, 0, width, barHeight);
			g.fillRect(0, height - barHeight, width, barHeight);
		}

		// final
		if (state == State.FINAL) {
			// name
			String name = students.get(studentIndex).name;
			font.drawString((width - font.getWidth(name)) / 2f, height * 0.9f - font.getLineHeight() / 2f, name, Color.white);

			// border
			border.draw(0, 0);
		}
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException {
		switch (state) {
		case INITIAL:
			offsetPos += delta * INITIAL_SPEED;
			break;
		case TRANSITION:
			stateTime += delta;
			if (stateTime >= TRANSITION_TIME) {
				stateTime = 0;
				state = State.SCROLLING;
				return;
			}
			offsetPos += delta * speed(stateTime, TRANSITION_TIME, INITIAL_SPEED, SCROLLING_START_SPEED);
			break;
		case SCROLLING:
			stateTime += delta;
			if (stateTime > SCROLL_TIME) {
				if (stateTime > SCROLL_TIME + SELECT_DELAY) {
					stateTime = 0;
					state = State.CENTER;
				}
				return;
			}
			offsetPos += delta * speed(stateTime, SCROLL_TIME, SCROLLING_START_SPEED, 0f);
			break;
		case CENTER:
			float targetOffset = imgWidth / 2f;
			if (Math.abs(offsetPos - targetOffset) > 0.001f) {
				if (offsetPos > targetOffset) {
					offsetPos -= delta * r.speed;
					if (offsetPos < targetOffset)
						offsetPos = targetOffset;
				} else {
					offsetPos += delta * r.speed;
					if (offsetPos > targetOffset)
						offsetPos = targetOffset;
				}
			} else
				state = State.SELECT;
			return;
		case SELECT:
			// delay time
			if (r.shift == 0) {
				if (stateTime >= r.delay) {
					// next re-select, or finished
					if (reselect.isEmpty()) {
						state = State.FINAL;
						return;
					}
					r = reselect.remove();
					stateTime = 0;
				} else
					stateTime += delta;
			}

			// re-select
			if (r.shift < 0) {
				offsetPos += delta * r.speed;
				if (offsetPos > imgWidth + IMAGE_OFFSET) {
					studentIndex = mod(studentIndex - 1, students.size());
					offsetPos = mod((int) offsetPos, imgWidth + IMAGE_OFFSET);
					r.shift++;
					state = State.CENTER;
				}
			} else if (r.shift > 0) {
				offsetPos -= delta * r.speed;
				if (offsetPos <= 0) {
					studentIndex = mod(studentIndex + 1, students.size());
					offsetPos = imgWidth + IMAGE_OFFSET;
					r.shift--;
					state = State.CENTER;
				}
			}
			return;
		case FINAL:
			return;
		}
		if (offsetPos > imgWidth + IMAGE_OFFSET) {
			studentIndex = mod(studentIndex - 1, students.size());
			offsetPos = mod((int) offsetPos, imgWidth + IMAGE_OFFSET);
		}
	}

	@Override
	public void keyPressed(int key, char c) {
		if (key == Input.KEY_ESCAPE)
			container.exit();
		else
			userAction();
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		if (button == Input.MOUSE_LEFT_BUTTON)
			userAction();
	}

	/**
	 * Processes a user action (mouse click or key press).
	 */
	private void userAction() {
		if (state == State.INITIAL)
			state = State.TRANSITION;
		else if (state == State.FINAL)
			reset();
	}

	/**
	 * Resets all fields.
	 */
	private void reset() {
		state = State.INITIAL;
		stateTime = 0;
		Collections.shuffle(students);
		studentIndex = 0;
		offsetPos = 0f;

		// random lists
		reselect = new LinkedList<Reselect>();
		r = new Reselect(0, SELECT_DELAY, FINAL_SPEED);
		int reselections = (int) (Math.random() * MAX_RESELECTIONS);
		for (int i = 0; i < reselections; i++) {
			int shift = 1 + (int) (Math.random() * MAX_SHIFT);
			boolean left = (Math.random() < 0.5);
			int delay = SELECT_DELAY + (int) (Math.random() * SELECT_DELAY) * 2;
			reselect.add(new Reselect((left) ? -shift : shift, delay, FINAL_SPEED * (shift + 0.5f)));
		}
	}

	/**
	 * Calculates A modulo B with result in the range [0, B).
	 */
	private int mod(int a, int b) { return ((a % b) + b) % b; }

	/**
	 * Speed change function.
	 */
	private float speed(int currentTime, int endTime, float startSpeed, float target) {
		return 1f * (startSpeed - target) * (endTime - currentTime) / endTime;
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
			String[] icons = { "icon16.png", "icon32.png" };
			app.setIcons(icons);
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
