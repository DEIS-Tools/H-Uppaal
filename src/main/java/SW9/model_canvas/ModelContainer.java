package SW9.model_canvas;

import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Bounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ModelContainer extends Parent implements MouseTrackable, Colorable, Removable {

    // Modelling properties
    private final List<Location> locations = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Location, List<Edge>> locationEdgeMap = new HashMap<>();
    private final List<String> clocks = new ArrayList<>();
    private final List<String> variables = new ArrayList<>();
    private final List<String> channels = new ArrayList<>();
    private final StringProperty name = new SimpleStringProperty();

    protected Color color = null;
    protected Color.Intensity intensity = null;
    protected boolean colorIsSet = false;

    protected final MouseTracker mouseTracker = new MouseTracker(this);

    public ModelContainer(final String name) {
        super();

        this.name.set(name);

        mouseTracker.registerOnMouseEnteredEventHandler(event -> {
            ModelCanvas.setHoveredModelContainer(this);

            // If we have a location on the mouse, color it accordingly to our color
            if (ModelCanvas.mouseHasLocation()) {
                ModelCanvas.getLocationOnMouse().resetColor(getColor(), getColorIntensity());
            }
        });

        mouseTracker.registerOnMouseExitedEventHandler(event -> {
            if (this.equals(ModelCanvas.getHoveredModelContainer())) {
                ModelCanvas.setHoveredModelContainer(null);
            }

            // If we have a location on the mouse, reset its color (to "undo" our coloring when the mouse entered us)
            if (ModelCanvas.mouseHasLocation()) {
                ModelCanvas.getLocationOnMouse().resetColor();
            }
        });
    }

    public abstract Bounds getInternalBounds();

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

    // Modelling accessors
    public List<Location> getLocations() {
        return locations;
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

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Edge> getEdges(final Location location) {
        return locationEdgeMap.get(location);
    }

    public void add(final Edge... edges) {
        for (final Edge edge : edges) {
            edge.color(getColor(), getColorIntensity());

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

    public List<String> getClocks() {
        return clocks;
    }

    public void addClock(final String clock) {
        clocks.add(clock);
    }

    public void removeClock(final String clock) {
        for (int i = 0; i < clocks.size(); i++) {
            if (clock.equals(clocks.get(i))) {
                clocks.remove(i);
                return;
            }
        }
    }

    public List<String> getVariables() {
        return variables;
    }

    public void addVariable(final String var) {
        variables.add(var);
    }

    public void removeVariable(final String var) {
        for (int i = 0; i < variables.size(); i++) {
            if (var.equals(variables.get(i))) {
                variables.remove(i);
                return;
            }
        }
    }

    public List<String> getChannels() {
        return channels;
    }

    public void addChannel(final String chan) {
        channels.add(chan);
    }

    public void removeChannels(final String chan) {
        for (int i = 0; i < channels.size(); i++) {
            if (chan.equals(channels.get(i))) {
                channels.remove(i);
                return;
            }
        }
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return nameProperty().get();
    }
}
