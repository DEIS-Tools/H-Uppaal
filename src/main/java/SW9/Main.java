package SW9;

import SW9.model_canvas.ModelCanvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main extends Application {

    public final static MouseTracker mouseTracker = new MouseTracker();
    private ModelCanvas root;


    public static void main(String[] args) {
        launch(args);
    }

    public void start(final Stage stage) throws Exception {

        stage.setTitle("Kick-ass Modelchecker");

        // Create the root pane of the window and register mouse event listeners
        root = new ModelCanvas();
        root.setOnMouseMoved(mouseTracker.onMouseMovedEventHandler);
        root.setOnMouseClicked(mouseTracker.onMouseClickedEventHandler);

        final Scene scene = new Scene(root, 1000, 1000);

        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas/location.css");
        stage.setScene(scene);

        // Whenever a key is pressed, notify the keyboard tracker
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);

        stage.show();
    }


}
