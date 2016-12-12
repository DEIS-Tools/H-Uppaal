package SW9.abstractions;

import SW9.utility.colors.Color;
import SW9.utility.helpers.Circular;
import SW9.utility.serialize.Serializable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class Edge implements Serializable {

    private static final String SOURCE_LOCATION = "source_location";
    private static final String TARGET_LOCATION = "target_location";
    private static final String SOURCE_SUB_COMPONENT = "source_sub_component";
    private static final String TARGET_SUB_COMPONENT = "target_sub_component";
    private static final String SELECT = "select";
    private static final String GUARD = "guard";
    private static final String UPDATE = "update";
    private static final String SYNC = "sync";
    private static final String NAILS = "nails";

    // Verification properties
    private final ObjectProperty<Location> sourceLocation = new SimpleObjectProperty<>();
    private final ObjectProperty<Location> targetLocation = new SimpleObjectProperty<>();

    private final ObjectProperty<SubComponent> sourceSubComponent = new SimpleObjectProperty<>();
    private final ObjectProperty<SubComponent> targetSubComponent = new SimpleObjectProperty<>();
    private final StringProperty select = new SimpleStringProperty("");
    private final StringProperty guard = new SimpleStringProperty("");
    private final StringProperty update = new SimpleStringProperty("");
    private final StringProperty sync = new SimpleStringProperty("");

    // Styling properties
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I700);
    private final ObservableList<Nail> nails = FXCollections.observableArrayList();

    public Edge(final Location sourceLocation) {
        this.sourceLocation.set(sourceLocation);
    }

    public Edge(final SubComponent sourceComponent) {
        this.sourceSubComponent.set(sourceComponent);
    }

    public Edge(final JsonObject jsonObject, final Component component) {
        deserialize(jsonObject, component);
    }

    public Location getSourceLocation() {
        return sourceLocation.get();
    }

    public void setSourceLocation(final Location sourceLocation) {
        this.sourceLocation.set(sourceLocation);
    }

    public ObjectProperty<Location> sourceLocationProperty() {
        return sourceLocation;
    }

    public Location getTargetLocation() {
        return targetLocation.get();
    }

    public void setTargetLocation(final Location targetLocation) {
        this.targetLocation.set(targetLocation);
    }

    public ObjectProperty<Location> targetLocationProperty() {
        return targetLocation;
    }

    public String getSelect() {
        return select.get();
    }

    public void setSelect(final String select) {
        this.select.set(select);
    }

    public StringProperty selectProperty() {
        return select;
    }

    public String getGuard() {
        return guard.get();
    }

    public void setGuard(final String guard) {
        this.guard.set(guard);
    }

    public StringProperty guardProperty() {
        return guard;
    }

    public String getUpdate() {
        return update.get();
    }

    public void setUpdate(final String update) {
        this.update.set(update);
    }

    public StringProperty updateProperty() {
        return update;
    }

    public String getSync() {
        return sync.get();
    }

    public void setSync(final String sync) {
        this.sync.set(sync);
    }

    public StringProperty syncProperty() {
        return sync;
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

    public ObservableList<Nail> getNails() {
        return nails;
    }

    public boolean addNail(final Nail nail) {
        return nails.add(nail);
    }

    public void insertNailAt(final Nail nail, final int index) {
        nails.add(index, nail);
    }

    public boolean removeNail(final Nail nail) {
        return nails.remove(nail);
    }

    public SubComponent getSourceSubComponent() {
        return sourceSubComponent.get();
    }

    public ObjectProperty<SubComponent> sourceSubComponentProperty() {
        return sourceSubComponent;
    }

    public void setSourceSubComponent(final SubComponent sourceSubComponent) {
        this.sourceSubComponent.set(sourceSubComponent);
    }

    public SubComponent getTargetSubComponent() {
        return targetSubComponent.get();
    }

    public ObjectProperty<SubComponent> targetSubComponentProperty() {
        return targetSubComponent;
    }

    public void setTargetSubComponent(final SubComponent targetSubComponent) {
        this.targetSubComponent.set(targetSubComponent);
    }

    public Circular getSourceCircular() {
        if(getSourceLocation() != null) {
            return getSourceLocation();
        } else {
            return new Circular() {
                DoubleProperty x = new SimpleDoubleProperty();
                DoubleProperty y = new SimpleDoubleProperty();
                {
                    x.bind(getSourceSubComponent().xProperty().add(getSourceSubComponent().widthProperty()).subtract(GRID_SIZE * 2));
                    y.bind(getSourceSubComponent().yProperty().add(getSourceSubComponent().heightProperty()).subtract(GRID_SIZE * 2));
                }

                @Override
                public DoubleProperty radiusProperty() {
                    return new SimpleDoubleProperty(1.5);
                }

                @Override
                public DoubleProperty scaleProperty() {
                    return getSourceSubComponent().scaleProperty();
                }

                @Override
                public DoubleProperty xProperty() {
                    return x;
                }

                @Override
                public DoubleProperty yProperty() {
                    return y;
                }

                @Override
                public double getX() {
                    return xProperty().get();
                }

                @Override
                public double getY() {
                    return yProperty().get();
                }
            };
        }
    }

    public Circular getTargetCircular() {
        if(getTargetLocation() != null) {
            return getTargetLocation();
        } else {
            return getTargetSubComponent();
        }
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        if (getSourceLocation() != null) {
            result.addProperty(SOURCE_LOCATION, getSourceLocation().getId());
        }
        if (getTargetLocation() != null) {
            result.addProperty(TARGET_LOCATION, getTargetLocation().getId());
        }
        if (getSourceSubComponent() != null) {
            result.addProperty(SOURCE_SUB_COMPONENT, getSourceSubComponent().getIdentifier());
        }
        if (getTargetSubComponent() != null) {
            result.addProperty(TARGET_SUB_COMPONENT, getTargetSubComponent().getIdentifier());
        }
        result.addProperty(SELECT, getSelect());
        result.addProperty(GUARD, getGuard());
        result.addProperty(UPDATE, getUpdate());
        result.addProperty(SYNC, getSync());

        final JsonArray nails = new JsonArray();
        getNails().forEach(nail -> nails.add(nail.serialize()));
        result.add(NAILS, nails);

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        // todo: We can't deserialize this object without knowledge about locations. Use the method below
    }

    public void deserialize(final JsonObject json, final Component component) {
        // Find the initial and final location of the component of the edge
        final Location initialLocation = component.getInitialLocation();
        final Location finalLocation = component.getFinalLocation();

        // Sets a location to be either source or target location if the location matches the json content
        final Consumer<Location> setFromAndToLocationIfMatches = (location) -> {
            if (json.get(SOURCE_LOCATION) != null && location.getId().equals(json.getAsJsonPrimitive(SOURCE_LOCATION).getAsString())) {
                setSourceLocation(location);
            } else if (json.get(TARGET_LOCATION) != null && location.getId().equals(json.getAsJsonPrimitive(TARGET_LOCATION).getAsString())) {
                setTargetLocation(location);
            }
        };

        component.getLocations().forEach(setFromAndToLocationIfMatches);
        setFromAndToLocationIfMatches.accept(initialLocation);
        setFromAndToLocationIfMatches.accept(finalLocation);

        // Sets a location to be either source or target sub component if the sub component matches the json content
        final Consumer<SubComponent> setFromAndToSubComponentIfMatches = (subComponent) -> {
            if (json.get(SOURCE_SUB_COMPONENT) != null && subComponent.getIdentifier().equals(json.getAsJsonPrimitive(SOURCE_SUB_COMPONENT).getAsString())) {
                setSourceSubComponent(subComponent);
            } else if (json.get(TARGET_SUB_COMPONENT) != null && subComponent.getIdentifier().equals(json.getAsJsonPrimitive(TARGET_SUB_COMPONENT).getAsString())) {
                setTargetSubComponent(subComponent);
            }
        };

        component.getSubComponents().forEach(setFromAndToSubComponentIfMatches);

        setSelect(json.getAsJsonPrimitive(SELECT).getAsString());
        setGuard(json.getAsJsonPrimitive(GUARD).getAsString());
        setUpdate(json.getAsJsonPrimitive(UPDATE).getAsString());
        setSync(json.getAsJsonPrimitive(SYNC).getAsString());

        json.getAsJsonArray(NAILS).forEach(jsonElement -> {
            final Nail newNail = new Nail((JsonObject) jsonElement);
            nails.add(newNail);
        });
    }
}
