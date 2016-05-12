package ee.itcollege.robot;


import java.awt.Color;
import java.awt.Rectangle;

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

public class BubbleClickGame extends Application {
	
	private static final String GAME_FIELD = "gameField";
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
		
		// click on start
		r.mouseClick(x + w / 2, y + h * .6);
		r.sleep(100);
		
		for (int size = 1; size < 10; size++) {
			int padding = w / 10;
			int stepX = (w - padding * 2) / size;
			int stepY = h / size;
			
			boolean foundStep = false;
			step:
			for (int offsetX = padding + stepX / 2; offsetX < w - padding; offsetX += stepX) {
				for (int offsetY = stepY / 2 + (size > 7 ? 30: 0); offsetY < h; offsetY += stepY) {
					r.mouseMove(x + offsetX, y + offsetY);
					r.sleep(40);
					r.capture(GAME_FIELD);
					Color color = r.getColor(GAME_FIELD, offsetX, offsetY);
					//System.out.println(color);
					if (color.getGreen() > 150 && color.getBlue() < 10 && color.getBlue() < 10) {
						r.mouseClick();
						r.sleep(100);
						System.out.println("found green for " + size + " rows.");
						foundStep = true;
						break step;
					}
				}
			}
			if (!foundStep) {
				System.out.println("failed for " + size + " rows.");
				break;
			}
		}
	}
	
	void startCalibrate(MouseEvent e) {
		// create an overlay to capture the click
		Rectangle2D s = r.getScreen();
		r.setArea(ALL, 0, 0, s.getWidth(), s.getHeight());
		Stage stage = r.createOverlay(0, 0, (int) s.getWidth(), (int) s.getHeight());

		stage.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			// when the overlay is clicked, find the game area
			stage.close();
			Platform.runLater(() -> calibrateBorders(event.getSceneX(), event.getSceneY()));
		});
	}
	
	
	void calibrateBorders(double startX, double startY) {
		r.capture(ALL);
		int x = (int) startX;
		int y = (int) startY;
		Color initialColor = r.getColor(ALL, x, y);
		// move left
		while (initialColor.equals(r.getColor(ALL, x, y)) && x > 0) {
			x--;
		}
		x++;
		// move up
		while (initialColor.equals(r.getColor(ALL, x, y)) && y > 0) {
			y--;
		}
		y++;
		// set field top left corner
		Rectangle gamefield = r.setArea(GAME_FIELD, x, y, 10, 10);
		
		// move down
		while (initialColor.equals(r.getColor(ALL, x, y)) && y < r.getScreen().getHeight()) {
			y++;
		}
		y--;
		
		// move right
		while (initialColor.equals(r.getColor(ALL, x, y)) && x < r.getScreen().getWidth()) {
			x++;
		}
		// set field size
		gamefield.setSize(x - (int) gamefield.getX(), y - (int) gamefield.getLocation().getY());
		
		// show the calibrated area for 2 seconds
		Stage gameArea = r.createOverlay(GAME_FIELD);
		gameArea.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> gameArea.close());
		r.closeOverlayAfter(gameArea, 2000);

	}

}
