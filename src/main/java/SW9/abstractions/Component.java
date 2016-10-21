package SW9.abstractions;

import SW9.utility.colors.Color;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class Component {

    // Verification properties
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

    public Component(final String name) {
        this(new SimpleStringProperty(name));
    }

    public Component(final StringProperty name) {
        this.name = name;

        // A component must have at least one initial location
        final Location initialLocation = new Location();
        initialLocation.setType(Location.Type.INITIAL);
        this.initialLocation.set(initialLocation);

        // A component must have at least one final location
        final Location finalLocation = new Location();
        finalLocation.setType(Location.Type.FINAl);
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

    public ObservableList<Edge> getEdges() {
        return edges;
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

    public ObjectProperty<Location> initialLocationProperty() {
        return initialLocation;
    }

    public void setInitialLocation(final Location initialLocation) {
        this.initialLocation.set(initialLocation);
    }

    public Location getFinalLocation() {
        return finalLocation.get();
    }

    public ObjectProperty<Location> finalLocationProperty() {
        return finalLocation;
    }

    public void setFinalLocation(final Location finalLocation) {
        this.finalLocation.set(finalLocation);
    }
}
