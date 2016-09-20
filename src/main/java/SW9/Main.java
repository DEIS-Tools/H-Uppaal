package SW9;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    public final static MouseTracker mouseTracker = new MouseTracker();
    private Parent root;
    private double xOffset;
    private double yOffset;


    public static void main(String[] args) {
        launch(Main.class, args);
    }

    public void start(final Stage stage) throws Exception {

        // Remove the classic
        stage.initStyle(StageStyle.UNDECORATED);

        root = FXMLLoader.load(getClass().getResource("main.fxml"));
        root.setOnMouseMoved(mouseTracker.onMouseMovedEventHandler);
        root.setOnMouseClicked(mouseTracker.onMouseClickedEventHandler);

        stage.setTitle("Kick-ass Modelchecker");

        final Scene scene = new Scene(root, 500, 500);
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);
        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas/location.css");
        stage.setScene(scene);

        final Node statusBar = scene.lookup("#status_bar");
        statusBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = stage.getX() - event.getScreenX();
                yOffset = stage.getY() - event.getScreenY();
            }
        });
        statusBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            }
        });

        stage.show();
    }


}
