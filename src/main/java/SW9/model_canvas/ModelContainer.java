package SW9.model_canvas;

import SW9.MouseTracker;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import SW9.utility.DragHelper;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class ModelContainer extends Parent implements DragHelper.Draggable {

    private final List<Location> locations = new ArrayList<>();
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

    public boolean addLocation(final Location location) {
        addChild(location);
        return locations.add(location);
    }

    public boolean removeLocation(final Location location) {
        removeChild(location);
        return locations.remove(location);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public boolean addEdge(final Edge edge) {
        addChild(edge);
        return edges.add(edge);
    }

    public boolean removeEdge(final Edge edge) {
        removeChild(edge);
        return edges.remove(edge);
    }

    public abstract ObservableDoubleValue getXLimit();
    public abstract ObservableDoubleValue getYLimit();
}
