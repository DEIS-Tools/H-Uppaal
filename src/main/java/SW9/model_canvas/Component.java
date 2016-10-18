package SW9.model_canvas;

import SW9.backend.HUPPAALDocument;
import SW9.backend.UPPAALDriver;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.DragHelper;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.mouse.MouseTracker;
import com.google.gson.*;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.*;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.shape.*;

import java.lang.reflect.Type;
import java.util.*;


public class Component extends JsonParent implements Colorable, MouseTrackable, Removable{

    // Modelling properties
    private final List<Location> locations = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Location, List<Edge>> locationEdgeMap = new HashMap<>();
    private final StringProperty declarationsProperty = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();

    // Verification property
    private final BooleanProperty hasDeadlock = new SimpleBooleanProperty(true);

    // Styling properties
    private static final double CORNER_SIZE = ModelCanvas.GRID_SIZE * 2 + Location.RADIUS;
    private final MouseTracker mouseTracker = new MouseTracker(this);
    private final Rectangle labelContainer;
    private final Polygon labelTriangle;
    private final Label label;
    private final Path frame;
    private final DoubleProperty xProperty;
    private final DoubleProperty yProperty;
    private final DoubleProperty widthProperty;
    private final DoubleProperty heightProperty;
    private Color color = null;
    private Color.Intensity intensity = null;
    private boolean colorIsSet = false;

    private boolean mouseIsHoveringTopBar = false;
    private TitledPane titledPane;
    private final Location finalLocation;
    private final Location initialLocation;

    public Component(final double x, final double y, final double width, final double height, final String name, final MouseTracker canvasMouseTracker) {

        // Initialize the spacial properties
        xProperty = new SimpleDoubleProperty(x);
        yProperty = new SimpleDoubleProperty(y);
        widthProperty = new SimpleDoubleProperty(width);
        heightProperty = new SimpleDoubleProperty(height);

        DragHelper.makeDraggable(this, mouseEvent -> {
            return (mouseEvent.getX() < xProperty().get() + widthProperty.get()) &&
                    (mouseEvent.getY() < yProperty().get() + heightProperty.get());
        });

        // Initialize the frame of the component
        frame = new Path();
        initializeFrame(frame);

        // Initialize locations
        initialLocation = new Location(
                xProperty.add(CORNER_SIZE / 2d),
                yProperty.add(CORNER_SIZE / 2d),
                canvasMouseTracker,
                Location.Type.INITIAL,
                this
        );

        finalLocation = new Location(
                xProperty.add(widthProperty.subtract(CORNER_SIZE / 2d)),
                yProperty.add(heightProperty.subtract(CORNER_SIZE / 2d)),
                canvasMouseTracker,
                Location.Type.FINAL,
                this
        );

        // Initialize properties for the name of the component
        labelContainer = new Rectangle();
        label = new Label(name);
        label.textProperty().bind(nameProperty());
        labelTriangle = new Polygon(
                xProperty.get() + CORNER_SIZE / 2, yProperty.get() + CORNER_SIZE / 2,
                xProperty.get() + CORNER_SIZE, yProperty.get(),
                xProperty.get() + CORNER_SIZE, yProperty.get() + CORNER_SIZE / 2
        );

        labelContainer.setOnMouseEntered(event -> mouseIsHoveringTopBar = true);
        labelContainer.setOnMouseExited(event -> mouseIsHoveringTopBar = false);

        // Bind the properties for the name of the component
        labelContainer.xProperty().bind(xProperty.add(CORNER_SIZE));
        labelContainer.yProperty().bind(yProperty.add(1));
        labelContainer.widthProperty().bind(widthProperty.subtract(CORNER_SIZE).subtract(1));
        labelContainer.heightProperty().set(CORNER_SIZE / 2);

        label.layoutXProperty().bind(labelContainer.xProperty());
        label.layoutYProperty().bind(labelContainer.yProperty().subtract(2));

        // Styling below
        labelContainer.getStyleClass().add("component-label-container");
        labelContainer.setStrokeType(StrokeType.INSIDE);
        labelContainer.setStrokeLineJoin(StrokeLineJoin.ROUND);

        label.getStyleClass().add("component-label");
        label.getStyleClass().add("subhead");
        label.alignmentProperty().set(Pos.CENTER);
        label.setPrefHeight(CORNER_SIZE / 2);
        label.paddingProperty().set(new Insets(0, 0, 0, 10));

        labelTriangle.getStyleClass().add("component-label-container");
        labelTriangle.setStrokeType(StrokeType.INSIDE);
        labelTriangle.setStrokeLineJoin(StrokeLineJoin.ROUND);
        labelTriangle.layoutXProperty().bind(xProperty.subtract(x).add(0));
        labelTriangle.layoutYProperty().bind(yProperty.subtract(y).add(1));

        // Show more button
        final JFXButton showMoreButton = new JFXButton("show more");
        showMoreButton.setOnMouseClicked(event -> titledPane.setExpanded(!titledPane.isExpanded()));


        // Initialize declarations box and bind the content to declarations property
        initializeTextBox();

        addChildren(
                titledPane,
                frame,
                labelTriangle,
                labelContainer,
                label,
                showMoreButton
        );

        add(initialLocation);
        add(finalLocation);

        // Will add color to the different children depending on their classes
        resetColor();

        this.name.set(name);

        mouseTracker.registerOnMouseEnteredEventHandler(event -> {
            ModelCanvas.setHoveredComponent(this);

            // If we have a location on the mouse, color it accordingly to our color
            if (ModelCanvas.mouseHasLocation()) {
                ModelCanvas.getLocationOnMouse().resetColor(getColor(), getColorIntensity());
            }
        });

        mouseTracker.registerOnMouseExitedEventHandler(event -> {
            if (this.equals(ModelCanvas.getHoveredComponent())) {
                ModelCanvas.setHoveredComponent(null);
            }

            // If we have a location on the mouse, reset its color (to "undo" our coloring when the mouse entered us)
            if (ModelCanvas.mouseHasLocation()) {
                ModelCanvas.getLocationOnMouse().resetColor();
            }
        });

        new Timer().schedule(
                new TimerTask() {

                    @Override
                    public void run() {
                        UPPAALDriver.verify("E<> deadlock", hasDeadlock::set, e -> {
                            System.out.println("Exception thrown from deadlock checker");
                        }, Component.this);
                    }
                }, 0, 5000);

    }

    private void initializeTextBox() {
        titledPane = new TitledPane();
        titledPane.getStyleClass().add("code-container");

        // Make a text area and add it to the titled pane
        final TextArea textArea = new TextArea();
        textArea.getStyleClass().add("body2-mono");
        titledPane.setContent(textArea);

        // Bind the content of the text area to declarations property
        declarationsProperty.bind(textArea.textProperty());

        titledPane.minWidthProperty().bind(widthProperty);
        titledPane.minHeightProperty().bind(heightProperty);

        titledPane.layoutXProperty().bind(xProperty());
        titledPane.layoutYProperty().bind(yProperty());

        // Whenever the titled pane is expanded of contracted, bring it to the front to overlap with locations, edges etc
        titledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                titledPane.toFront();
            } else {
                titledPane.toBack();
            }
        });

        // TODO: The following code must run every time the width and height updates

        // Generate first corner (to subtract)
        final Polygon corner1 = new Polygon(
                0, 0,
                CORNER_SIZE + 2, 0,
                0, CORNER_SIZE + 2
        );

        // Generate second corner (to subtract)
        final Polygon corner2 = new Polygon(
                widthProperty.get(), heightProperty.get(),
                widthProperty.get() - CORNER_SIZE - 2, heightProperty.get(),
                widthProperty.get(), heightProperty.get() - CORNER_SIZE - 2
        );

        // Make a mask
        Shape mask = new Rectangle(widthProperty.get() - 2, heightProperty.get() - 2);
        mask = Shape.subtract(mask, corner1);
        mask = Shape.subtract(mask, corner2);
        mask = Shape.subtract(mask, new Circle(CORNER_SIZE / 2, CORNER_SIZE / 2, Location.RADIUS));
        mask = Shape.subtract(mask, new Circle(CORNER_SIZE / 2 + widthProperty.get() - Location.RADIUS * 2, CORNER_SIZE / 2 + heightProperty.get() - Location.RADIUS * 2, Location.RADIUS));
        mask = Shape.subtract(mask, new Rectangle(widthProperty.get(), labelContainer.getHeight()));

        titledPane.setClip(mask);
    }

    private void initializeFrame(final Path frame) {
        MoveTo p1 = new MoveTo();
        LineTo p2 = new LineTo();
        LineTo p3 = new LineTo();
        LineTo p4 = new LineTo();
        LineTo p5 = new LineTo();
        LineTo p6 = new LineTo();
        LineTo p7 = new LineTo();

        p1.xProperty().bind(xProperty);
        p1.yProperty().bind(yProperty.add(CORNER_SIZE));

        p2.xProperty().bind(xProperty.add(CORNER_SIZE));
        p2.yProperty().bind(yProperty);

        p3.xProperty().bind(xProperty.add(widthProperty));
        p3.yProperty().bind(yProperty);

        p4.xProperty().bind(xProperty.add(widthProperty));
        p4.yProperty().bind(yProperty.add(heightProperty).subtract(CORNER_SIZE));

        p5.xProperty().bind(xProperty.add(widthProperty).subtract(CORNER_SIZE));
        p5.yProperty().bind(yProperty.add(heightProperty));

        p6.xProperty().bind(xProperty);
        p6.yProperty().bind(yProperty.add(heightProperty));

        p7.xProperty().bind(p1.xProperty());
        p7.yProperty().bind(p1.yProperty());

        frame.getElements().addAll(p1, p2, p3, p4, p5, p6, p7);

        frame.getStyleClass().add("component-frame");
        frame.blendModeProperty().set(BlendMode.MULTIPLY);

        // Our bounds are bound to our x and y properties
        boundingRectangle.xProperty().bind(xProperty().add(Location.RADIUS));
        boundingRectangle.yProperty().bind(yProperty().add(Location.RADIUS * 2));

        // Make us selectable
        SelectHelper.makeSelectable(this);
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    @Override
    public DoubleProperty xProperty() {
        return xProperty;
    }

    @Override
    public DoubleProperty yProperty() {
        return yProperty;
    }

    private final Rectangle boundingRectangle = new Rectangle();

    public Bounds getInternalBounds() {
        // The width must be set every time we access these bounds, because the width of the container might have changed
        boundingRectangle.setWidth(widthProperty.subtract(Location.RADIUS * 2).get());
        boundingRectangle.setHeight(heightProperty.subtract(Location.RADIUS * 3).get());

        return boundingRectangle.getLayoutBounds();
    }

    public ObservableDoubleValue getXLimit() {
        return widthProperty;
    }

    public ObservableDoubleValue getYLimit() {
        return heightProperty;
    }

    @Override
    public boolean color(final Color color, final Color.Intensity intensity) {
        // If the color should not be changed, do nothing
        if (color.equals(getColor()) && intensity.equals(getColorIntensity())) {
            return false;
        }

        colorIsSet = true;

        this.color = color;
        this.intensity = intensity;

        labelContainer.setFill(color.getColor(intensity));
        labelTriangle.setFill(color.getColor(intensity));
        frame.setStroke(color.getColor(intensity.next(2)));
        label.setTextFill(color.getTextColor(intensity));
        frame.setFill(color.getColor(intensity.next(-10).next(1)));

        // Color all of our children location, unless they are already colored
        getLocations().forEach(location -> {
            // If the location is not colored, of if the color is the same af us
            if (!location.isColored() || (location.getColor().equals(color) && location.getColorIntensity().equals(intensity))) {
                location.resetColor(color, intensity);
            }
        });

        // Color all of out children edges (their nails)
        getEdges().forEach(edge -> edge.color(color, intensity));

        return true;
    }

    @Override
    public void resetColor() {
        resetColor(Color.GREY_BLUE, Color.Intensity.I700); // default color
    }

    @Override
    public boolean select() {
        if (!mouseIsHoveringTopBar) return false;
        styleSelected();
        return true;
    }

    @Override
    public void styleSelected() {
        frame.getStyleClass().add("selected");
        labelContainer.getStyleClass().add("selected");
        labelTriangle.getStyleClass().add("selected");

        getLocations().forEach((location) -> {
            if (location.type.equals(Location.Type.INITIAL) || location.type.equals(Location.Type.FINAL)) {
                location.styleSelected();
            }
        });
    }

    @Override
    public void deselect() {
        styleDeselected();
    }

    @Override
    public void styleDeselected() {
        frame.getStyleClass().remove("selected");
        labelContainer.getStyleClass().remove("selected");
        labelTriangle.getStyleClass().remove("selected");

        getLocations().forEach((location) -> {
            if (location.type.equals(Location.Type.INITIAL) || location.type.equals(Location.Type.FINAL)) {
                location.styleDeselected();
            }
        });
    }

    private IParent previousParent = null;

    @Override
    public boolean remove() {
        previousParent = (IParent) this.getParent();

        if (previousParent == null) return false;
        previousParent.removeChild(this);

        return true;
    }

    @Override
    public void reAdd() {
        if (previousParent == null) return;
        previousParent.addChild(this);
    }

    public BooleanProperty hasDeadlockProperty() {
        return hasDeadlock;
    }

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

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return nameProperty().get();
    }

    public StringProperty declarationsProperty() {
        return declarationsProperty;
    }

    public String getDeclarations() {
        return declarationsProperty.get();
    }

    @Override
    protected void fromJsonObject() {

    }

    @Override
    public JsonObject toJsonObject() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(HUPPAALDocument.HUUPPAALField.NAME.toString(), getName());
        jsonObject.addProperty(HUPPAALDocument.HUUPPAALField.COLOR.toString(), getColor().toHexColor(getColorIntensity()));
        jsonObject.addProperty(HUPPAALDocument.HUUPPAALField.POS_X.toString(), xProperty().get());
        jsonObject.addProperty(HUPPAALDocument.HUUPPAALField.POS_Y.toString(), yProperty().get());
        jsonObject.addProperty(HUPPAALDocument.HUUPPAALField.WIDTH.toString(), getXLimit().get());
        jsonObject.addProperty(HUPPAALDocument.HUUPPAALField.HEIGHT.toString(), getYLimit().get());
        return jsonObject;
    }
}
