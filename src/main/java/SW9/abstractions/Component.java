package SW9.abstractions;

import SW9.utility.colors.Color;
import SW9.utility.serialize.Serializable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.atomic.AtomicInteger;

public class Component implements Serializable {

    // Used to generate unique IDs
    private static final AtomicInteger hiddenID = new AtomicInteger(0);

    // Verification properties
    @Expose
    private final StringProperty name;
    private final StringProperty declarations = new SimpleStringProperty("");
    private final ObservableList<Location> locations = FXCollections.observableArrayList();
    private final ObservableList<Edge> edges = FXCollections.observableArrayList();
    private final ObjectProperty<Location> initialLocation = new SimpleObjectProperty<>();
    private final ObjectProperty<Location> finalLocation = new SimpleObjectProperty<>();

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty width = new SimpleDoubleProperty(450d);
    private final DoubleProperty height = new SimpleDoubleProperty(600d);
    private final BooleanProperty declarationOpen = new SimpleBooleanProperty(false);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I700);

    public Component() {
        this("Component" + hiddenID.getAndIncrement());
    }

    public Component(final String name) {
        this(new SimpleStringProperty(name));
    }

    public Component(final StringProperty name) {
        this.name = name;

        // A component must have at least one initial location
        final Location initialLocation = new Location();
        initialLocation.setType(Location.Type.INITIAL);
        initialLocation.setColorIntensity(getColorIntensity());
        initialLocation.setColor(getColor());
        this.initialLocation.set(initialLocation);

        // A component must have at least one final location
        final Location finalLocation = new Location();
        finalLocation.setType(Location.Type.FINAl);
        finalLocation.setColorIntensity(getColorIntensity());
        finalLocation.setColor(getColor());
        this.finalLocation.set(finalLocation);
    }

    public String getName() {
        return name.get();
    }

    public void setName(final String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getDeclarations() {
        return declarations.get();
    }

    public void setDeclarations(final String declarations) {
        this.declarations.set(declarations);
    }

    public StringProperty declarationsProperty() {
        return declarations;
    }

    public ObservableList<Location> getLocations() {
        return locations;
    }

    public boolean addLocation(final Location location) {
        return locations.add(location);
    }

    public boolean removeLocation(final Location location) {
        return locations.remove(location);
    }

    public ObservableList<Edge> getEdges() {
        return edges;
    }

    public boolean addEdge(final Edge edge) {
        return edges.add(edge);
    }

    public boolean removeEdge(final Edge edge) {
        return edges.remove(edge);
    }

    public double getX() {
        return x.get();
    }

    public void setX(final double x) {
        this.x.set(x);
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public double getY() {
        return y.get();
    }

    public void setY(final double y) {
        this.y.set(y);
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double getWidth() {
        return width.get();
    }

    public void setWidth(final double width) {
        this.width.set(width);
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public double getHeight() {
        return height.get();
    }

    public void setHeight(final double height) {
        this.height.set(height);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor(final Color color) {
        this.color.set(color);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public Color.Intensity getColorIntensity() {
        return colorIntensity.get();
    }

    public void setColorIntensity(final Color.Intensity colorIntensity) {
        this.colorIntensity.set(colorIntensity);
    }

    public ObjectProperty<Color.Intensity> colorIntensityProperty() {
        return colorIntensity;
    }

    public boolean isDeclarationOpen() {
        return declarationOpen.get();
    }

    public BooleanProperty declarationOpenProperty() {
        return declarationOpen;
    }

    public Location getInitialLocation() {
        return initialLocation.get();
    }

    public void setInitialLocation(final Location initialLocation) {
        this.initialLocation.set(initialLocation);
    }

    public ObjectProperty<Location> initialLocationProperty() {
        return initialLocation;
    }

    public Location getFinalLocation() {
        return finalLocation.get();
    }

    public void setFinalLocation(final Location finalLocation) {
        this.finalLocation.set(finalLocation);
    }

    public ObjectProperty<Location> finalLocationProperty() {
        return finalLocation;
    }

    public Edge getUnfinishedEdge() {
        for (final Edge edge : edges) {
            if (edge.getTargetLocation() == null) return edge;
        }

        return null;
    }

    @Override
    public JsonElement serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty("name", getName());
        result.addProperty("declarations", getDeclarations());
        result.addProperty("locations", "");
        result.addProperty("edges", "");

        result.addProperty("x", getX());
        result.addProperty("y", getY());
        result.addProperty("width", getWidth());
        result.addProperty("height", getHeight());
        result.addProperty("color", "");
        result.addProperty("colorIntensity", "");

        return result;
    }
}
