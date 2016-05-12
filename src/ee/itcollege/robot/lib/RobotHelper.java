package ee.itcollege.robot.lib;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A helper library for working with robot
 * 
 * @todo Use either Swing or JavaFX, but not both
 * @author Mikk Mangus
 */
public class RobotHelper {
	
	private Rectangle2D screen;
	private Map<String, Rectangle> captureAreas = new HashMap<>();
	
	private Map<String, BufferedImage> lastImages = new HashMap<>();
	private Robot robot;
	

	public RobotHelper() {
		screen = Screen.getPrimary().getVisualBounds();
	    try {
	    	robot = new Robot();
	    } catch (AWTException e) {
	    	e.printStackTrace();
	    }
	    
	}
	
	/**
	 * @return The screen dimensions
	 */
	public Rectangle2D getScreen() {
		return screen;
	}
	
	/**
	 * Creates a new are with given id and coordinates.
	 * The area is also remembered by ID in this class
	 * 
	 * @param areaId
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return the newly created area
	 */
	public Rectangle setArea(String areaId, double x, double y, double width, double height) {
		 Rectangle area = new Rectangle((int) x, (int) y, (int)screen.getWidth(), (int) screen.getHeight());
		 captureAreas.put(areaId, area);
		 return area;
	}
	
	/**
	 * Returns the area by ID
	 * @param areaId
	 */
	public Rectangle getArea(String areaId) {
		return captureAreas.get(areaId);
	}
	
	/**
	 * Captures the screen image of given area
	 * @param areaId
	 */
	public void capture(String areaId) {
		BufferedImage image = robot.createScreenCapture(getArea(areaId));
		lastImages.put(areaId, image);
	}
	
	/**
	 * Returns the color of the pixel of given captured area
	 * from given coordinates
	 * 
	 * @param areaId
	 * @param x
	 * @param y
	 * @return
	 */
	public Color getColor(String areaId, int x, int y) {
		try {
			return new Color(lastImages.get(areaId).getRGB(x, y));
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return Color.black;
		} 
	}
	
	/**
	 * Moves the cursor to the given coordinates
	 * @param x
	 * @param y
	 */
	public void mouseMove(double x, double y) {
		robot.mouseMove((int)x, (int)y);
	}
	
	/**
	 * Creates a mousclick at given coordinates
	 * @param x
	 * @param y
	 */
	public void mouseClick(double x, double y) {
		mouseMove(x, y);
		mouseClick();
	}
	/**
	 * Creates a mouseClick
	 */
	public void mouseClick() {
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	/**
	 * Sleeps current thread for given milliseconds
	 * @param millis
	 */
	public void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a little transparent overlay over given area
	 * @param areaId - the capture area ID
	 * @return the stage window created
	 */
	public Stage createOverlay(String areaId) {
		Rectangle area = getArea(areaId);
		return createOverlay(area.getX(), area.getY(), area.getWidth(), area.getHeight());
	}
	
	/**
	 * Create an overlay window by providing the coordinates and width/height
	 * @return the stage created 
	 */
	public Stage createOverlay(double x, double y, double width, double height) {
		Pane pane = new Pane();
		pane.setStyle("-fx-background-color: rgba(255, 0, 0, .7);");
		Scene scene = new Scene(pane, width, height);
		scene.setFill(null);
		
		Stage stage = new Stage();
		stage.setX(x);
		stage.setY(y);
		stage.setScene(scene);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setAlwaysOnTop(true);
		stage.show();
		return stage;
	}
	
	/**
	 * Press down a key
	 * @param keyEvent - a keycode constant from {@link java.awt.event.KeyEvent}
	 */
	public void keyPress(int keyEvent) {
		robot.keyPress(keyEvent);
	}
	/**
	 * Press down a key, sleep for some time, and release the key
	 * @param keyEvent - a keycode constant from {@link java.awt.event.KeyEvent}
	 * @param millis - for how long to sleep
	 */
	public void keyPress(int keyEvent, int millis) {
		keyPress(keyEvent);
		sleep(millis);
		keyRelease(keyEvent);
	}
	/**
	 * Release a key
	 * @param keyEvent - a keycode constant from {@link java.awt.event.KeyEvent}
	 */
	public void keyRelease(int keyEvent) {
		robot.keyRelease(keyEvent);
	}
	
	/**
	 * Shows a fancy "animation" before closing the overlay after some time
	 * @param overlay - the Stage to close
	 * @param millis - after what time
	 */
	public void closeOverlayAfter(Stage overlay, int millis) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			int times = 0;
			@Override
			public void run() {
				if (++times > 4) {
					Platform.runLater(() -> overlay.close());
					timer.cancel();
					return;
				}
				if (overlay.getOpacity() > .5) {
					Platform.runLater(() -> overlay.setOpacity(0));
				}
				else {
					Platform.runLater(() -> overlay.setOpacity(1));
				}
			}
		}, millis, 100);
	}
	
}
