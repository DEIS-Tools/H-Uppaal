package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class Edge {

    private final Location sourceLocation;
    private Location targetLocations = null;
    private final List<Nail> nails = new ArrayList<>();

    // Mouse trackers
    private final MouseTracker canvasMousetracker;

    public Edge(final Location sourceLocation, final MouseTracker canvasMousetracker) {
        this.sourceLocation = sourceLocation;
        this.canvasMousetracker = canvasMousetracker;

        // TODO  Distribute work to functions
        Nail nail = new Nail(canvasMousetracker);
        ((Pane) sourceLocation.getParent()).getChildren().add(nail);
        nails.add(nail);
    }

}
