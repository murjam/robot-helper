package ee.itcollege.robot;


import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import ee.itcollege.robot.lib.RobotHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CatchTheEggGame extends Application {
	
	private static final String GAME_FIELD = "gameField";
	private static final String NEST_ROW = "nestRow";
	private static final String ALL = "all";
	
	RobotHelper r;
	
	@Override
	public void start(Stage win) throws Exception {
		r = new RobotHelper();
		
		VBox layout = new VBox();
		layout.setSpacing(10);
		layout.setPadding(new Insets(30));
		
		Button calibrate = new Button("Calibrate game area");
		layout.getChildren().add(calibrate);
		calibrate.addEventFilter(MouseEvent.MOUSE_CLICKED, this::startCalibrate);
		
		Button start = new Button("Start");
		layout.getChildren().add(start);
		start.addEventFilter(MouseEvent.MOUSE_CLICKED, this::playTheGame);
		
		Button nest = new Button("Find nest");
		layout.getChildren().add(nest);
		nest.addEventFilter(MouseEvent.MOUSE_CLICKED, this::showNest);
		
		Scene scene = new Scene(layout);
		win.setX(60);
		win.setY(200);
		win.setScene(scene);
		win.setAlwaysOnTop(true);
		win.show();
		win.setOnCloseRequest(e -> System.exit(0));
	}
	
	void playTheGame(MouseEvent e) {
		Rectangle area = r.getArea(GAME_FIELD);
		int x = (int) area.getX();
		int y = (int) area.getY();
		int w = (int) area.getWidth();
		int h = (int) area.getHeight();
		
		r.mouseClick(x + w / 2, y - 15); // click on browser address bar
		r.sleep(100);
		r.keyPress(KeyEvent.VK_ENTER, 100);
		r.sleep(500);
		r.mouseClick(x + w / 2, y + h / 2);
		
		
		r.sleep(1000);
		
		int nestY = y + h - h / 10;
		r.setArea(NEST_ROW, x, nestY, w, 1);
		
		
		
		
		/*
		while (true) {
			r.keyPress(KeyEvent.VK_RIGHT, 200 + (int)(Math.random() * 200));
			r.keyPress(KeyEvent.VK_LEFT, 200 + (int)(Math.random() * 200));
		}*/
		
	}
	
	void showNest(MouseEvent e) {
		Rectangle area = r.getArea(GAME_FIELD);
		int x = (int) area.getX();
		int y = (int) area.getY();
		int w = (int) area.getWidth();
		int h = (int) area.getHeight();
		int nestY = y + h - h / 10;
		int nestX = findNestX();
		
		r.mouseMove(x + nestX, nestY);
	}
	
	int findNestX() {
		r.capture(NEST_ROW);
		double w = r.getArea(NEST_ROW).getWidth();
		Color nestColor = new Color(99, 69, 36);
		
		for (int i = 0; i < w; i++) {
			if (isSimilarColor(nestColor, r.getColor(NEST_ROW, i, 0))) {
				return i + (int)w / 40;
			}
		}
		
		return -1;
	}
	
	void startCalibrate(MouseEvent e) {
		// create an overlay to capture the click
		Rectangle2D s = r.getScreen();
		r.setArea(ALL, 0, 0, s.getWidth(), s.getHeight());
		Stage stage = r.createOverlay(ALL);

		stage.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			// when the overlay is clicked, find the game area
			stage.close();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					calibrateBorders(event.getSceneX(), event.getSceneY());
				}
			}, 300);
		});
	}
	
	
	boolean isSimilarColor(Color c1, Color c2) {
		return Math.abs(c1.getRed()-c2.getRed()) < 10
				&& Math.abs(c1.getGreen()-c2.getGreen()) < 10
				&& Math.abs(c1.getBlue()-c2.getBlue()) < 10;
	}
	
	boolean isBorderColor(Color c) {
		Color borderColor = new Color(102, 102, 102);
		return isSimilarColor(c, borderColor);
	}
	
	
	void calibrateBorders(double startX, double startY) {
		r.capture(ALL);
		int x = (int) startX;
		int y = (int) startY;
		
		
		// move left
		while (!isBorderColor(r.getColor(ALL, x, y)) && x > 0) {
			x--;
		}
		x++;
		
		// move up
		while (!isBorderColor(r.getColor(ALL, x, y)) && y > 0) {
			y--;
		}
		y++;
		// set field top left corner
		Rectangle gamefield = r.setArea(GAME_FIELD, x, y, 10, 10);
		
		// move down
		while (!isBorderColor(r.getColor(ALL, x, y)) && y < r.getScreen().getHeight()) {
			y++;
		}
		y--;
		
		// move right
		while (!isBorderColor(r.getColor(ALL, x, y)) && x < r.getScreen().getWidth()) {
			x++;
		}
		// set field size
		gamefield.setSize(x - (int) gamefield.getX(), y - (int) gamefield.getLocation().getY());
		
		Platform.runLater(() -> {
			// show the calibrated area for 2 seconds
			Stage gameArea = r.createOverlay(GAME_FIELD);
			gameArea.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> gameArea.close());
			r.closeOverlayAfter(gameArea, 1000);
		});
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
