package SW9.abstractions;

import SW9.code_analysis.Nearable;
import SW9.controllers.HUPPAALController;
import SW9.presentations.JorkPresentation;
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

public class Edge implements Serializable, Nearable {

    private static final String SOURCE_LOCATION = "source_location";
    private static final String TARGET_LOCATION = "target_location";
    private static final String SOURCE_SUB_COMPONENT = "source_sub_component";
    private static final String TARGET_SUB_COMPONENT = "target_sub_component";
    private static final String SOURCE_JORK = "source_jork";
    private static final String TARGET_JORK = "target_jork";
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
    private final ObjectProperty<Jork> sourceJork = new SimpleObjectProperty<>();

    private final ObjectProperty<Jork> targetJork = new SimpleObjectProperty<>();
    private final StringProperty select = new SimpleStringProperty("");
    private final StringProperty guard = new SimpleStringProperty("");
    private final StringProperty update = new SimpleStringProperty("");
    private final StringProperty sync = new SimpleStringProperty("");

    // Styling properties
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I700);
    private final ObservableList<Nail> nails = FXCollections.observableArrayList();

    // Circulars
    private ObjectProperty<Circular> sourceCircular = new SimpleObjectProperty<>();
    private ObjectProperty<Circular> targetCircular = new SimpleObjectProperty<>();

    public Edge(final Location sourceLocation) {
        setSourceLocation(sourceLocation);
        bindReachabilityAnalysis();
    }

    public Edge(final SubComponent sourceComponent) {
        setSourceSubComponent(sourceComponent);
        bindReachabilityAnalysis();
    }

    public Edge(final Jork sourceJork) {
        setSourceJork(sourceJork);
        bindReachabilityAnalysis();
    }

    public Edge(final JsonObject jsonObject, final Component component) {
        deserialize(jsonObject, component);
        bindReachabilityAnalysis();
    }

    public Location getSourceLocation() {
        return sourceLocation.get();
    }

    private void setSourceLocation(final Location sourceLocation) {
        this.sourceLocation.set(sourceLocation);
        updateSourceCircular();
    }

    public ObjectProperty<Location> sourceLocationProperty() {
        return sourceLocation;
    }

    public Location getTargetLocation() {
        return targetLocation.get();
    }

    public void setTargetLocation(final Location targetLocation) {
        this.targetLocation.set(targetLocation);
        updateTargetCircular();
    }

    public ObjectProperty<Location> targetLocationProperty() {
        return targetLocation;
    }

    public String getSelect() {
        return select.get();
    }

    private void setSelect(final String select) {
        this.select.set(select);
    }

    public StringProperty selectProperty() {
        return select;
    }

    public String getGuard() {
        return guard.get();
    }

    private void setGuard(final String guard) {
        this.guard.set(guard);
    }

    public StringProperty guardProperty() {
        return guard;
    }

    public String getUpdate() {
        return update.get();
    }

    private void setUpdate(final String update) {
        this.update.set(update);
    }

    public StringProperty updateProperty() {
        return update;
    }

    public String getSync() {
        return sync.get();
    }

    private void setSync(final String sync) {
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

    public void setSourceSubComponent(final SubComponent sourceSubComponent) {
        this.sourceSubComponent.set(sourceSubComponent);
        updateSourceCircular();
    }

    public ObjectProperty<SubComponent> sourceSubComponentProperty() {
        return sourceSubComponent;
    }

    public SubComponent getTargetSubComponent() {
        return targetSubComponent.get();
    }

    public void setTargetSubComponent(final SubComponent targetSubComponent) {
        this.targetSubComponent.set(targetSubComponent);
        updateTargetCircular();
    }

    public ObjectProperty<SubComponent> targetSubComponentProperty() {
        return targetSubComponent;
    }

    public Jork getSourceJork() {
        return sourceJork.get();
    }

    public void setSourceJork(final Jork sourceJork) {
        this.sourceJork.set(sourceJork);
        updateSourceCircular();
    }

    public ObjectProperty<Jork> sourceJorkProperty() {
        return sourceJork;
    }

    public Jork getTargetJork() {
        return targetJork.get();
    }

    public void setTargetJork(final Jork targetJork) {
        this.targetJork.set(targetJork);
        updateTargetCircular();
    }

    public ObjectProperty<Jork> targetJorkProperty() {
        return targetJork;
    }

    public ObjectProperty<Circular> sourceCircularProperty() {
        return sourceCircular;
    }

    public ObjectProperty<Circular> targetCircularProperty() {
        return targetCircular;
    }

    public Circular getSourceCircular() {
        if(sourceCircular != null) {
            return sourceCircular.get();
        }
        return null;
    }

    public Circular getTargetCircular() {
        if(targetCircular != null) {
            return targetCircular.get();
        }
        return null;

    }

    private void updateSourceCircular() {
        if(getSourceLocation() != null) {
            sourceCircular.set(getSourceLocation());
        } else if(getSourceSubComponent() != null) {
            sourceCircular.set(new Circular() {
                DoubleProperty x = new SimpleDoubleProperty();
                DoubleProperty y = new SimpleDoubleProperty();
                {
                    x.bind(getSourceSubComponent().xProperty().add(getSourceSubComponent().widthProperty()).subtract(GRID_SIZE * 2));
                    y.bind(getSourceSubComponent().yProperty().add(getSourceSubComponent().heightProperty()).subtract(GRID_SIZE * 2));
                }

                @Override
                public DoubleProperty radiusProperty() {
                    return getSourceSubComponent().getComponent().getFinalLocation().radiusProperty();
                }

                @Override
                public DoubleProperty scaleProperty() {
                    return getSourceSubComponent().getComponent().getFinalLocation().scaleProperty();
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
            });
        } else if (getSourceJork() != null) {
            sourceCircular.set(new Circular() {
                DoubleProperty x = new SimpleDoubleProperty();
                DoubleProperty y = new SimpleDoubleProperty();

                {
                    x.bind(getSourceJork().xProperty().add(JorkPresentation.JORK_WIDTH / 2));
                    y.bind(getSourceJork().yProperty().add(JorkPresentation.JORK_HEIGHT + JorkPresentation.JORK_Y_TRANSLATE));
                }

                @Override
                public DoubleProperty radiusProperty() {
                    return new SimpleDoubleProperty(0);
                }

                @Override
                public DoubleProperty scaleProperty() {
                    return new SimpleDoubleProperty(0);
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
            });
        }
    }

    private void updateTargetCircular() {
        if(getTargetLocation() != null) {
            targetCircular.set(getTargetLocation());
        } else if(getTargetSubComponent() != null) {
            targetCircular.set(new Circular() {
                DoubleProperty x = new SimpleDoubleProperty();
                DoubleProperty y = new SimpleDoubleProperty();
                {
                    x.bind(getTargetSubComponent().xProperty().add(GRID_SIZE * 2));
                    y.bind(getTargetSubComponent().yProperty().add(GRID_SIZE * 2));
                }

                @Override
                public DoubleProperty radiusProperty() {
                    return getTargetSubComponent().getComponent().getInitialLocation().radiusProperty();
                }

                @Override
                public DoubleProperty scaleProperty() {
                    return getTargetSubComponent().getComponent().getInitialLocation().scaleProperty();
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
            });
        } else if (getTargetJork() != null) {
            targetCircular.set(new Circular() {
                DoubleProperty x = new SimpleDoubleProperty();
                DoubleProperty y = new SimpleDoubleProperty();

                {
                    x.bind(getTargetJork().xProperty().add(JorkPresentation.JORK_WIDTH / 2));
                    y.bind(getTargetJork().yProperty().add(JorkPresentation.JORK_Y_TRANSLATE));
                }

                @Override
                public DoubleProperty radiusProperty() {
                    return new SimpleDoubleProperty(0);
                }

                @Override
                public DoubleProperty scaleProperty() {
                    return new SimpleDoubleProperty(0);
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
            });
        }
    }

    public String getProperty(final PropertyType propertyType) {
        if (propertyType.equals(PropertyType.SELECTION)) {
            return getSelect();
        } else if (propertyType.equals(PropertyType.GUARD)) {
            return getGuard();
        } else if (propertyType.equals(PropertyType.SYNCHRONIZATION)) {
            return getSync();
        } else if (propertyType.equals(PropertyType.UPDATE)) {
            return getUpdate();
        } else {
            return "";
        }
    }

    public void setProperty(final PropertyType propertyType, final String newProperty) {
        if (propertyType.equals(PropertyType.SELECTION)) {
            selectProperty().unbind();
            setSelect(newProperty);
        } else if (propertyType.equals(PropertyType.GUARD)) {
            guardProperty().unbind();
            setGuard(newProperty);
        } else if (propertyType.equals(PropertyType.SYNCHRONIZATION)) {
            syncProperty().unbind();
            setSync(newProperty);
        } else if (propertyType.equals(PropertyType.UPDATE)) {
            updateProperty().unbind();
            setUpdate(newProperty);
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
        if (getSourceJork() != null) {
            result.addProperty(SOURCE_JORK, getSourceJork().getId());
        }
        if (getTargetJork() != null) {
            result.addProperty(TARGET_JORK, getTargetJork().getId());
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
            }
            if (json.get(TARGET_LOCATION) != null && location.getId().equals(json.getAsJsonPrimitive(TARGET_LOCATION).getAsString())) {
                setTargetLocation(location);
            }
        };

        component.getLocations().forEach(setFromAndToLocationIfMatches);
        setFromAndToLocationIfMatches.accept(initialLocation);
        setFromAndToLocationIfMatches.accept(finalLocation);

        // Sets a sub component to be either source or target sub component if the sub component matches the json content
        final Consumer<SubComponent> setFromAndToSubComponentIfMatches = (subComponent) -> {
            if (json.get(SOURCE_SUB_COMPONENT) != null && subComponent.getIdentifier().equals(json.getAsJsonPrimitive(SOURCE_SUB_COMPONENT).getAsString())) {
                setSourceSubComponent(subComponent);
            }
            if (json.get(TARGET_SUB_COMPONENT) != null && subComponent.getIdentifier().equals(json.getAsJsonPrimitive(TARGET_SUB_COMPONENT).getAsString())) {
                setTargetSubComponent(subComponent);
            }
        };

        component.getSubComponents().forEach(setFromAndToSubComponentIfMatches);

        // Sets a jork to be either source or target jork if the jork matches the json content
        final Consumer<Jork> setFromAndToJorkIfMatches = (jork) -> {
            if (json.get(SOURCE_JORK) != null && jork.getId().equals(json.getAsJsonPrimitive(SOURCE_JORK).getAsString())) {
                setSourceJork(jork);
            }
            if (json.get(TARGET_JORK) != null && jork.getId().equals(json.getAsJsonPrimitive(TARGET_JORK).getAsString())) {
                setTargetJork(jork);
            }
        };

        component.getJorks().forEach(setFromAndToJorkIfMatches);

        setSelect(json.getAsJsonPrimitive(SELECT).getAsString());
        setGuard(json.getAsJsonPrimitive(GUARD).getAsString());
        setUpdate(json.getAsJsonPrimitive(UPDATE).getAsString());
        setSync(json.getAsJsonPrimitive(SYNC).getAsString());

        json.getAsJsonArray(NAILS).forEach(jsonElement -> {
            final Nail newNail = new Nail((JsonObject) jsonElement);
            nails.add(newNail);
        });
    }

    @Override
    public String generateNearString() {
        String result = "Edge";

        if (getSourceLocation() != null) {
            result += " from " + getSourceLocation().generateNearString();
        } else if (getSourceJork() != null) {
            result += " from " + getSourceJork().generateNearString();
        } else {
            result += " from " + getSourceCircular();
        }

        if (getTargetLocation() != null) {
            result += " to " + getTargetLocation().generateNearString();
        } else if (getTargetJork() != null) {
            result += " to " + getTargetJork().generateNearString();
        } else {
            result += " to " + getTargetCircular();
        }

        return result;
    }

    public enum PropertyType {
        NONE(-1),
        SELECTION(0),
        GUARD(1),
        SYNCHRONIZATION(2),
        UPDATE(3);

        private int i;

        PropertyType(final int i) {
            this.i = i;
        }

        public int getI() {
            return i;
        }
    }

    public boolean isSelfLoop() {

        return (getSourceLocation() != null && getSourceLocation().equals(getTargetLocation())) ||
                (getSourceSubComponent() != null && getSourceSubComponent().equals(getTargetSubComponent())) ||
                (getSourceJork() != null && getSourceJork().equals(getTargetJork()));

    }

    private void bindReachabilityAnalysis() {

        selectProperty().addListener((observable, oldValue, newValue) -> HUPPAALController.runReachabilityAnalysis());
        guardProperty().addListener((observable, oldValue, newValue) -> HUPPAALController.runReachabilityAnalysis());
        syncProperty().addListener((observable, oldValue, newValue) -> HUPPAALController.runReachabilityAnalysis());
        updateProperty().addListener((observable, oldValue, newValue) -> HUPPAALController.runReachabilityAnalysis());
    }

}
