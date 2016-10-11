package SW9.model_canvas;

import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.value.ObservableDoubleValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ModelContainer extends Parent implements MouseTrackable, Colorable {

    protected Color color = null;
    protected Color.Intensity intensity = null;
    protected boolean colorIsSet = false;

    private final List<Location> locations = new ArrayList<>();
    private final Map<Location, List<Edge>> locationEdgeMap = new HashMap<>();

    private final List<Edge> edges = new ArrayList<>();
    protected final MouseTracker mouseTracker = new MouseTracker(this);

    public ModelContainer() {
        super();

        mouseTracker.registerOnMouseEnteredEventHandler(event -> {
            ModelCanvas.setHoveredModelContainer(this);

            // If we have a location on the mouse, color it accordingly to our color
            if(ModelCanvas.mouseHasLocation()) {
                ModelCanvas.getLocationOnMouse().resetColor(getColor(), getColorIntensity());
            }
        });

        mouseTracker.registerOnMouseExitedEventHandler(event -> {
            if (this.equals(ModelCanvas.getHoveredModelContainer())) {
                ModelCanvas.setHoveredModelContainer(null);
            }

            // If we have a location on the mouse, reset its color (to "undo" our coloring when the mouse entered us)
            if(ModelCanvas.mouseHasLocation()) {
                ModelCanvas.getLocationOnMouse().resetColor();
            }
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

    public void add(final Location... locations) {
        for (final Location location : locations) {
            addChild(location);
            locationEdgeMap.put(location, new ArrayList<>());
            this.locations.add(location);
        }
    }

    public void remove(final Location... locations) {

        for (final Location location : locations) {
            removeChild(location);

            while (!locationEdgeMap.get(location).isEmpty()) {
                remove(locationEdgeMap.get(location).get(0));
            }

            locationEdgeMap.remove(location);
            this.locations.remove(location);
        }
    }

    public void add(final Edge... edges) {
        for (final Edge edge : edges) {
            addChild(edge);

            locationEdgeMap.get(edge.getSourceLocation()).add(edge);

            if (!edge.targetLocationIsSet.get()) {
                edge.targetLocationIsSet.addListener((observable, oldValue, newValue) -> {
                    // The new value of the boolean is true, hence the target location is set
                    if (!oldValue && newValue && !edge.getSourceLocation().equals(edge.getTargetLocation())) {
                        locationEdgeMap.get(edge.getTargetLocation()).add(edge);
                    }
                });
            } else if (!edge.getSourceLocation().equals(edge.getTargetLocation())) {
                locationEdgeMap.get(edge.getTargetLocation()).add(edge);
            }

            this.edges.add(edge);
        }
    }

    public void remove(final Edge... edges) {
        for (final Edge edge : edges) {
            removeChild(edge);
            locationEdgeMap.get(edge.getSourceLocation()).remove(edge);
            if (edge.targetLocationIsSet.get()) {
                locationEdgeMap.get(edge.getTargetLocation()).remove(edge);
            }
            this.edges.remove(edge);
        }
    }

    public abstract ObservableDoubleValue getXLimit();

    public abstract ObservableDoubleValue getYLimit();

    @Override
    public boolean isColored() {
        return colorIsSet;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return intensity;
    }

    @Override
    public void resetColor(final Color color, final Color.Intensity intensity) {
        color(color, intensity);
        colorIsSet = false;
    }
}
