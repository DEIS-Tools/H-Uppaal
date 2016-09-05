import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import model_canvas.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private static Location locationOnMouse = null;

    private List<Shape> locationList = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    public void start(final Stage stage) throws Exception {
        stage.setTitle("Kick-ass Modelchecker");

        final AnchorPane root = new AnchorPane();
        final Scene scene = new Scene(root, 1000, 1000);

        scene.getStylesheets().add("colors.css");
        scene.getStylesheets().add("model_canvas/location.css");
        stage.setScene(scene);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(!event.getCode().equals(KeyCode.L)) return;

                if(locationOnMouse == null) {
                    final Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                    locationOnMouse = new Location(mouseLocation.x, mouseLocation.y);
                    locationOnMouse.setOpacity(0.5);
                    root.getChildren().add(locationOnMouse);

                    root.setOnMouseMoved(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            locationOnMouse.setCenterX(event.getX());
                            locationOnMouse.setCenterY(event.getY());
                        }
                    });

                    root.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            locationOnMouse.setOpacity(1.0f);
                            locationOnMouse = null;
                            root.setOnMouseMoved(null);

                            root.setOnMouseClicked(null);
                        }
                    });
                }
            }
        });

        stage.show();

    }
}
