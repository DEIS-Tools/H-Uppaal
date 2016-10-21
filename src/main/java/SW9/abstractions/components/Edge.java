package SW9.abstractions.components;

import SW9.utility.colors.Color;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Edge {

    // Verification properties
    private final ObjectProperty<Location> sourceLocation;
    private final ObjectProperty<Location> targetLocation = new SimpleObjectProperty<>();
    private final StringProperty select = new SimpleStringProperty("");
    private final StringProperty guard = new SimpleStringProperty("");
    private final StringProperty update = new SimpleStringProperty("");
    private final StringProperty sync = new SimpleStringProperty("");

    // Styling properties
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I700);
    private final ObservableList<Nail> nails = FXCollections.observableArrayList();

    public Edge(final Location sourceLocation) {
        this(new SimpleObjectProperty<>(sourceLocation));
    }

    public Edge(final ObjectProperty<Location> sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public Location getSourceLocation() {
        return sourceLocation.get();
    }

    public ObjectProperty<Location> sourceLocationProperty() {
        return sourceLocation;
    }

    public void setSourceLocation(final Location sourceLocation) {
        this.sourceLocation.set(sourceLocation);
    }

    public Location getTargetLocation() {
        return targetLocation.get();
    }

    public ObjectProperty<Location> targetLocationProperty() {
        return targetLocation;
    }

    public void setTargetLocation(final Location targetLocation) {
        this.targetLocation.set(targetLocation);
    }

    public String getSelect() {
        return select.get();
    }

    public StringProperty selectProperty() {
        return select;
    }

    public void setSelect(final String select) {
        this.select.set(select);
    }

    public String getGuard() {
        return guard.get();
    }

    public StringProperty guardProperty() {
        return guard;
    }

    public void setGuard(final String guard) {
        this.guard.set(guard);
    }

    public String getUpdate() {
        return update.get();
    }

    public StringProperty updateProperty() {
        return update;
    }

    public void setUpdate(final String update) {
        this.update.set(update);
    }

    public String getSync() {
        return sync.get();
    }

    public StringProperty syncProperty() {
        return sync;
    }

    public void setSync(final String sync) {
        this.sync.set(sync);
    }

    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(final Color color) {
        this.color.set(color);
    }

    public Color.Intensity getColorIntensity() {
        return colorIntensity.get();
    }

    public ObjectProperty<Color.Intensity> colorIntensityProperty() {
        return colorIntensity;
    }

    public void setColorIntensity(final Color.Intensity colorIntensity) {
        this.colorIntensity.set(colorIntensity);
    }

    public ObservableList<Nail> getNails() {
        return nails;
    }

}
