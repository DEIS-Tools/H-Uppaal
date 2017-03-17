package SW9.abstractions;

import SW9.HUPPAAL;
import SW9.controllers.HUPPAALController;
import SW9.presentations.DropDownMenu;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.colors.EnabledColor;
import SW9.utility.serialize.Serializable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Component implements Serializable, DropDownMenu.HasColor {

    private static final AtomicInteger hiddenID = new AtomicInteger(0); // Used to generate unique IDs

    private static final String NAME = "name";
    private static final String DECLARATIONS = "declarations";
    private static final String LOCATIONS = "locations";
    private static final String JORKS = "jorks";
    private static final String INITIAL_LOCATION = "initial_location";
    private static final String FINAL_LOCATION = "final_location";
    private static final String SUBCOMPONENTS = "sub_components";
    private static final String EDGES = "edges";
    private static final String IS_MAIN = "main";
    private static final String DESCRIPTION = "description";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String COLOR = "color";
    private static final String INCLUDE_IN_PERIODIC_CHECK = "include_in_periodic_check";
    private static final String COLOR_INTENSITY = "color_intensity";

    // Verification properties
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty declarations = new SimpleStringProperty("");
    private final ObservableList<Location> locations = FXCollections.observableArrayList();
    private final ObservableList<Jork> jorks = FXCollections.observableArrayList();
    private final ObservableList<Edge> edges = FXCollections.observableArrayList();
    private final ObjectProperty<Location> initialLocation = new SimpleObjectProperty<>();
    private final ObjectProperty<Location> finalLocation = new SimpleObjectProperty<>();
    private final ObservableList<SubComponent> subComponents = FXCollections.observableArrayList();
    private final BooleanProperty isMain = new SimpleBooleanProperty(false);
    private final StringProperty description = new SimpleStringProperty("");

    // Background check
    private final BooleanProperty includeInPeriodicCheck = new SimpleBooleanProperty(true);

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty width = new SimpleDoubleProperty(450d);
    private final DoubleProperty height = new SimpleDoubleProperty(600d);
    private final BooleanProperty declarationOpen = new SimpleBooleanProperty(false);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I700);

    private final BooleanProperty firsTimeShown = new SimpleBooleanProperty(false);

    public Component() {
        this(false);
    }

    public Component(final boolean doRandomColor) {
        this("Component" + hiddenID.getAndIncrement(), doRandomColor);
    }

    public Component(final String name, final boolean doRandomColor) {
        setName(name);

        if(doRandomColor) {
            // Color the new component in such a way that we avoid clashing with other components if possible
            final List<EnabledColor> availableColors = new ArrayList<>();
            EnabledColor.enabledColors.forEach(availableColors::add);
            HUPPAAL.getProject().getComponents().forEach(component -> {
                availableColors.removeIf(enabledColor -> enabledColor.color.equals(component.getColor()));
            });
            if (availableColors.size() == 0) {
                EnabledColor.enabledColors.forEach(availableColors::add);
            }
            final int randomIndex = (new Random()).nextInt(availableColors.size());
            final EnabledColor selectedColor = availableColors.get(randomIndex);
            setColorIntensity(selectedColor.intensity);
            setColor(selectedColor.color);
        }

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

        bindReachabilityAnalysis();
    }

    public Component(final JsonObject object) {
        hiddenID.incrementAndGet();
        setFirsTimeShown(true);
        deserialize(object);
        bindReachabilityAnalysis();
    }

    public String getName() {
        return name.get();
    }

    public void setName(final String name) {
        this.name.unbind();
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

    public List<Location> getLocationsWithInitialAndFinal() {
        final List<Location> locations = new ArrayList<>();
        locations.addAll(getLocations());
        locations.add(initialLocation.get());
        locations.add(finalLocation.get());
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

    public List<Edge> getRelatedEdges(final Location location) {
        final ArrayList<Edge> relatedEdges = new ArrayList<>();

        edges.forEach(edge -> {
            if(location.equals(edge.getSourceLocation()) ||location.equals(edge.getTargetLocation())) {
                relatedEdges.add(edge);
            }
        });

        return relatedEdges;
    }

    public List<Edge> getRelatedEdges(final SubComponent subComponent) {
        final ArrayList<Edge> relatedEdges = new ArrayList<>();

        edges.forEach(edge -> {
            if(subComponent.equals(edge.getSourceSubComponent()) ||subComponent.equals(edge.getTargetSubComponent())) {
                relatedEdges.add(edge);
            }
        });

        return relatedEdges;
    }

    public List<Edge> getIncomingEdges(final SubComponent subComponent) {
        final ArrayList<Edge> relatedEdges = new ArrayList<>();

        edges.forEach(edge -> {
            if (subComponent.equals(edge.getTargetSubComponent())) {
                relatedEdges.add(edge);
            }
        });

        return relatedEdges;
    }

    public List<Edge> getOutGoingEdges(final SubComponent subComponent) {
        final ArrayList<Edge> relatedEdges = new ArrayList<>();

        edges.forEach(edge -> {
            if (subComponent.equals(edge.getSourceSubComponent())) {
                relatedEdges.add(edge);
            }
        });

        return relatedEdges;
    }

    public List<Edge> getRelatedEdges(final Jork jork) {
        final ArrayList<Edge> relatedEdges = new ArrayList<>();

        edges.forEach(edge -> {
            if(jork.equals(edge.getSourceJork()) ||jork.equals(edge.getTargetJork())) {
                relatedEdges.add(edge);
            }
        });

        return relatedEdges;
    }

    public List<Edge> getIncomingEdges(final Jork jork) {
        final ArrayList<Edge> relatedEdges = new ArrayList<>();

        edges.forEach(edge -> {
            if(jork.equals(edge.getTargetJork())) {
                relatedEdges.add(edge);
            }
        });

        return relatedEdges;
    }

    public List<Edge> getOutGoingEdges(final Jork jork) {
        final ArrayList<Edge> relatedEdges = new ArrayList<>();

        edges.forEach(edge -> {
            if(jork.equals(edge.getSourceJork())) {
                relatedEdges.add(edge);
            }
        });

        return relatedEdges;
    }

    public ObservableList<Jork> getJorks() {
        return jorks;
    }

    public boolean addJork(final Jork jork) {
        return jorks.add(jork);
    }

    public boolean removeJork(final Jork jork) {
        return jorks.remove(jork);
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

    public ObservableList<SubComponent> getSubComponents() {
        return subComponents;
    }

    public boolean addSubComponent(final SubComponent component) {
        return subComponents.add(component);
    }

    public boolean removeSubComponent(final SubComponent component) {
        return subComponents.remove(component);
    }

    public Edge getUnfinishedEdge() {
        for (final Edge edge : edges) {
            if (edge.getTargetLocation() == null && edge.getTargetSubComponent() == null && edge.getTargetJork() == null)
                return edge;
        }

        return null;
    }

    public boolean isIsMain() {
        return isMain.get();
    }

    public void setIsMain(boolean isMain) {
        this.isMain.set(isMain);
    }

    public BooleanProperty isMainProperty() {
        return isMain;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(final String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public boolean isFirsTimeShown() {
        return firsTimeShown.get();
    }

    public BooleanProperty firsTimeShownProperty() {
        return firsTimeShown;
    }

    public void setFirsTimeShown(final boolean firsTimeShown) {
        this.firsTimeShown.set(firsTimeShown);
    }

    public boolean isIncludeInPeriodicCheck() {
        return includeInPeriodicCheck.get();
    }

    public BooleanProperty includeInPeriodicCheckProperty() {
        return includeInPeriodicCheck;
    }

    public void setIncludeInPeriodicCheck(final boolean includeInPeriodicCheck) {
        this.includeInPeriodicCheck.set(includeInPeriodicCheck);
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(NAME, getName());
        result.addProperty(DECLARATIONS, getDeclarations());

        final JsonArray locations = new JsonArray();
        getLocations().forEach(location -> locations.add(location.serialize()));
        result.add(LOCATIONS, locations);

        result.add(INITIAL_LOCATION, getInitialLocation().serialize());
        result.add(FINAL_LOCATION, getFinalLocation().serialize());

        final JsonArray jorks = new JsonArray();
        getJorks().forEach(jork -> jorks.add(jork.serialize()));
        result.add(JORKS, jorks);

        final JsonArray subComponents = new JsonArray();
        getSubComponents().forEach(subComponent -> subComponents.add(subComponent.serialize()));
        result.add(SUBCOMPONENTS, subComponents);

        final JsonArray edges = new JsonArray();
        getEdges().forEach(edge -> edges.add(edge.serialize()));
        result.add(EDGES, edges);

        result.addProperty(IS_MAIN, isIsMain());

        result.addProperty(DESCRIPTION, getDescription());

        result.addProperty(X, getX());
        result.addProperty(Y, getY());
        result.addProperty(WIDTH, getWidth());
        result.addProperty(HEIGHT, getHeight());
        result.addProperty(COLOR, EnabledColor.getIdentifier(getColor()));

        result.addProperty(INCLUDE_IN_PERIODIC_CHECK, isIncludeInPeriodicCheck());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setName(json.getAsJsonPrimitive(NAME).getAsString());
        setDeclarations(json.getAsJsonPrimitive(DECLARATIONS).getAsString());

        json.getAsJsonArray(LOCATIONS).forEach(jsonElement -> {
            final Location newLocation = new Location((JsonObject) jsonElement);
            locations.add(newLocation);
        });

        final Location newInitialLocation = new Location(json.getAsJsonObject(INITIAL_LOCATION));
        setInitialLocation(newInitialLocation);

        final Location newFinalLocation = new Location(json.getAsJsonObject(FINAL_LOCATION));
        setFinalLocation(newFinalLocation);

        json.getAsJsonArray(JORKS).forEach(jsonElement -> {
            final Jork newJork = new Jork((JsonObject) jsonElement);
            jorks.add(newJork);
        });

        json.getAsJsonArray(SUBCOMPONENTS).forEach(jsonElement -> {
            final SubComponent newSubComponent = new SubComponent((JsonObject) jsonElement);
            subComponents.add(newSubComponent);
        });

        json.getAsJsonArray(EDGES).forEach(jsonElement -> {
            final Edge newEdge = new Edge((JsonObject) jsonElement, this);
            edges.add(newEdge);
        });

        setIsMain(json.getAsJsonPrimitive(IS_MAIN).getAsBoolean());

        setDescription(json.getAsJsonPrimitive(DESCRIPTION).getAsString());

        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
        setWidth(json.getAsJsonPrimitive(WIDTH).getAsDouble());
        setHeight(json.getAsJsonPrimitive(HEIGHT).getAsDouble());

        final EnabledColor enabledColor = EnabledColor.fromIdentifier(json.getAsJsonPrimitive(COLOR).getAsString());
        if (enabledColor != null) {
            setColorIntensity(enabledColor.intensity);
            setColor(enabledColor.color);
        }

        setIncludeInPeriodicCheck(json.getAsJsonPrimitive(INCLUDE_IN_PERIODIC_CHECK).getAsBoolean());
    }

    public void color(final Color color, final Color.Intensity intensity) {
        final Color previousColor = colorProperty().get();
        final Color.Intensity previousColorIntensity = colorIntensityProperty().get();

        final Map<Location, Pair<Color, Color.Intensity>> previousLocationColors = new HashMap<>();

        for (final Location location : getLocations()) {
            if (!location.getColor().equals(previousColor)) continue;
            previousLocationColors.put(location, new Pair<>(location.getColor(), location.getColorIntensity()));
        }

        UndoRedoStack.push(() -> { // Perform
            // Color the component
            setColorIntensity(intensity);
            setColor(color);

            // Color all of the locations
            previousLocationColors.keySet().forEach(location -> {
                location.setColorIntensity(intensity);
                location.setColor(color);
            });
        }, () -> { // Undo
            // Color the component
            setColorIntensity(previousColorIntensity);
            setColor(previousColor);

            // Color the locations accordingly to the previous color for them
            previousLocationColors.keySet().forEach(location -> {
                location.setColorIntensity(previousLocationColors.get(location).getValue());
                location.setColor(previousLocationColors.get(location).getKey());
            });
        }, String.format("Changed the color of %s to %s", this, color.name()), "color-lens");
    }

    private void bindReachabilityAnalysis() {
        locations.addListener((ListChangeListener<? super Location>) c -> HUPPAALController.runReachabilityAnalysis());
        edges.addListener((ListChangeListener<? super Edge>) c -> HUPPAALController.runReachabilityAnalysis());
        declarationsProperty().addListener((observable, oldValue, newValue) -> HUPPAALController.runReachabilityAnalysis());
        includeInPeriodicCheckProperty().addListener((observable, oldValue, newValue) -> HUPPAALController.runReachabilityAnalysis());
    }
}
