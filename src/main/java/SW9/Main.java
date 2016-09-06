package SW9;

import SW9.model_canvas.Edge;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import SW9.model_canvas.Location;
import SW9.utility.DropShadowHelper;

public class Main extends Application {

    private AnchorPane root;

    private final MouseTracker mouseTracker = new MouseTracker();

    private static boolean mouseHasLocation = false;

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

                final EventHandler<MouseEvent> mousePlacedEvent = event1 -> {
                    mouseHasLocation = false;
                };

                newLocation.localMouseTracker.registerOnMouseClickedEventHandler(mousePlacedEvent);
                newLocation.setEffect(DropShadowHelper.generateElevationShadow(22));
                root.getChildren().add(newLocation);

                // Start a new edge from the location
                newLocation.localMouseTracker.registerOnMouseClickedEventHandler(mouseClickedHandler -> {
                    final Edge edge = new Edge(newLocation, mouseTracker);
                    root.getChildren().add(edge);
                });
            }
        });

        stage.show();
    }
}
