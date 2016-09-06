package SW9;

import SW9.model_canvas.Location;
import SW9.utility.DropShadowHelper;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    private AnchorPane root;

    private final MouseTracker mouseTracker = new MouseTracker();

    public static boolean mouseHasLocation = false;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(final Stage stage) throws Exception {

        stage.setTitle("Kick-ass Modelchecker");

        // Create the root pane of the window and register mouse event listeners
        root = new AnchorPane();
        root.setOnMouseMoved(mouseTracker.onMouseMovedEventHandler);
        root.setOnMouseClicked(mouseTracker.onMouseClickedEventHandler);

        final Scene scene = new Scene(root, 1000, 1000);

        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas/location.css");
        stage.setScene(scene);

        // Whenever the L key is pressed, create a new location following the cursor
        scene.setOnKeyPressed(event -> {
            if (!event.getCode().equals(KeyCode.L)) return;

            if (!mouseHasLocation) {
                mouseHasLocation = true;
                final Location newLocation = new Location(mouseTracker);

                newLocation.setEffect(DropShadowHelper.generateElevationShadow(22));
                root.getChildren().add(newLocation);
            }
        });

        stage.show();
    }


}
