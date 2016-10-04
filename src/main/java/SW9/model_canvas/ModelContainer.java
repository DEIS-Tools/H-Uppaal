package SW9.model_canvas;

import SW9.MouseTracker;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import SW9.utility.DragHelper;
import javafx.beans.value.ObservableDoubleValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ModelContainer extends Parent implements DragHelper.Draggable {

    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, List<Edge>> locationEdgeMap = new HashMap<>();

    private final List<Edge> edges = new ArrayList<>();
    protected final MouseTracker mouseTracker = new MouseTracker(this);

    public ModelContainer() {
        super();

        mouseTracker.registerOnMouseEnteredEventHandler(event -> {
            ModelCanvas.setHoveredModelContainer(this);
        });

        mouseTracker.registerOnMouseExitedEventHandler(event -> {
            if (this.equals(ModelCanvas.getHoveredModelContainer())) ModelCanvas.setHoveredModelContainer(null);
        });
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Edge> getEdges(final Location location) {
        return locationEdgeMap.get(location);
    }

    public boolean add(final Location location) {
        addChild(location);
        locationEdgeMap.put(location, new ArrayList<>());
        return locations.add(location);
    }

    public boolean remove(final Location location) {
        removeChild(location);

        while(!locationEdgeMap.get(location).isEmpty()) {
            remove(locationEdgeMap.get(location).get(0));
        }

        locationEdgeMap.remove(location);
        return locations.remove(location);
    }

    public boolean add(final Edge edge) {
        addChild(edge);
        locationEdgeMap.get(edge.getSourceLocation()).add(edge);
        return edges.add(edge);
    }

    public boolean remove(final Edge edge) {
        removeChild(edge);
        locationEdgeMap.get(edge.getSourceLocation()).remove(edge);
        return edges.remove(edge);
    }

    public abstract ObservableDoubleValue getXLimit();

    public abstract ObservableDoubleValue getYLimit();
}
