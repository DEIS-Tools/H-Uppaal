package SW9;

import SW9.model_canvas.ModelCanvas;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public final static MouseTracker mouseTracker = new MouseTracker();
    private ModelCanvas root;


    public static void main(String[] args) {
        launch(Main.class, args);
    }

    public void start(final Stage stage) throws Exception {

        root = FXMLLoader.load(getClass().getResource("main.fxml"));
        root.setOnMouseMoved(mouseTracker.onMouseMovedEventHandler);
        root.setOnMouseClicked(mouseTracker.onMouseClickedEventHandler);

        stage.setTitle("Kick-ass Modelchecker");

        final Scene scene = new Scene(root, 500, 500);
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);
        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas/location.css");
        stage.setScene(scene);

        stage.show();
    }


}
