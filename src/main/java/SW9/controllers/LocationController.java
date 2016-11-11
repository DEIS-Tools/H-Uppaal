package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.backend.UPPAALDriver;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.presentations.LocationPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationController implements Initializable, SelectHelper.ColorSelectable {

    private static final AtomicInteger hiddenLocationID = new AtomicInteger(0);
    private static final long DOUBLE_PRESS_SHOW_PROPERTIES_DELAY = 500;
    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    public Group root;
    public Circle initialIndicator;
    public StackPane finalIndicator;
    public Group shakeContent;
    public Label nameLabel;
    public Rectangle rectangle;
    public Rectangle rectangleShakeIndicator;
    public Circle circle;
    public Circle circleShakeIndicator;
    public Path octagon;
    public Path octagonShakeIndicator;
    public StackPane propertiesPane;
    public JFXTextField nameField;
    public TextArea invariantField;
    public Group scaleContent;

    private boolean isPlaced;
    private long lastPress = 0;

    private TimerTask reachabilityCheckTask;

    private double previousX;
    private double previousY;
    private boolean wasDragged = false;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.location.addListener((obsLocation, oldLocation, newLocation) -> {
            // The radius property on the abstraction must reflect the radius in the view
            newLocation.radiusProperty().bind(circle.radiusProperty());

            // The scale property on the abstraction must reflect the radius in the view
            newLocation.scaleProperty().bind(scaleContent.scaleXProperty());

            // initialize the name field and its bindings
            nameField.setText(newLocation.getName());
            newLocation.nameProperty().bind(nameField.textProperty());

            // initialize the invariant field and its bindings
            invariantField.setText(newLocation.getInvariant());
            newLocation.invariantProperty().bind(invariantField.textProperty());
        });

        // Scale x and y 1:1 (based on the x-scale)
        scaleContent.scaleYProperty().bind(scaleContent.scaleXProperty());

        // Register click listener on canvas to hide the property pane when the canvas is clicked
        CanvasPresentation.mouseTracker.registerOnMousePressedEventHandler(event -> propertiesPane.setVisible(false));

        // Register a key-bind for hiding the property pane (using a hidden locationID)
        KeyboardTracker.registerKeybind(KeyboardTracker.HIDE_LOCATION_PROPERTY_PANE + hiddenLocationID.getAndIncrement(), new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
            propertiesPane.setVisible(false);
        }));

        initializeReachabilityCheck();
    }

    public void initializeReachabilityCheck() {
        final int interval = 5000; // ms

        // Could not run query
        reachabilityCheckTask = new TimerTask() {

            @Override
            public void run() {
                if (getComponent() == null || getLocation() == null) return;

                // The location might have been remove from the component (through ctrl + z)
                if (getLocation().getType() == Location.Type.NORMAL && !getComponent().getLocations().contains(getLocation())) return;

                UPPAALDriver.verify(
                        "E<> " + getComponent().getName() + "." + getLocation().getName(),
                        result -> {
                            final LocationPresentation locationPresentation = (LocationPresentation) LocationController.this.root;

                            locationPresentation.animateShakeWarning(!result);
                        },
                        e -> {
                            System.out.println(e);
                            // Could not run query
                        },
                        HUPPAAL.getProject().getComponents()
                );
            }

        };

        new Timer().schedule(reachabilityCheckTask, 0, interval);
    }

    public Location getLocation() {
        return location.get();
    }

    public void setLocation(final Location location) {
        this.location.set(location);

        if (location.getType().equals(Location.Type.NORMAL)) {
            root.layoutXProperty().bind(location.xProperty());
            root.layoutYProperty().bind(location.yProperty());
        } else {
            location.xProperty().bind(root.layoutXProperty());
            location.yProperty().bind(root.layoutYProperty());
            isPlaced = true;
        }
    }

    public ObjectProperty<Location> locationProperty() {
        return location;
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(final Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    @FXML
    private void mouseEntered() {
        circle.setCursor(Cursor.HAND);

        ((LocationPresentation) root).animateHoverEntered();

        // Keybind for making location urgent
        KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_URGENT, new Keybind(new KeyCodeCombination(KeyCode.U), () -> {
            final Location.Urgency previousUrgency = location.get().getUrgency();

            if (previousUrgency.equals(Location.Urgency.URGENT)) {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.NORMAL);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getName() + " urgent", "hourglass-full");
            } else {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.URGENT);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getName() + " normal (back form urgent)", "hourglass-empty");
            }
        }));

        // Keybind for making location committed
        KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED, new Keybind(new KeyCodeCombination(KeyCode.C), () -> {
            final Location.Urgency previousUrgency = location.get().getUrgency();

            if (previousUrgency.equals(Location.Urgency.COMMITTED)) {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.NORMAL);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getName() + " committed", "hourglass-full");
            } else {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.COMMITTED);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getName() + " normal (back from committed)", "hourglass-empty");
            }

        }));
    }

    @FXML
    private void mouseExited() {
        circle.setCursor(Cursor.DEFAULT);

        ((LocationPresentation) root).animateHoverExited();

        KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_URGENT);
        KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED);
    }

    @FXML
    private void mousePressed(final MouseEvent event) {
        final Component component = getComponent();

        event.consume();
        if (isPlaced) {
            final Edge unfinishedEdge = component.getUnfinishedEdge();

            if (unfinishedEdge != null) {
                unfinishedEdge.setTargetLocation(getLocation());
            } else {
                // If shift is being held down, start drawing a new edge
                if (event.isShiftDown()) {
                    final Edge newEdge = new Edge(getLocation());

                    UndoRedoStack.push(() -> { // Perform
                        component.addEdge(newEdge);
                    }, () -> { // Undo
                        component.removeEdge(newEdge);
                    }, "Created edge starting from location " + getLocation().getName(), "add-circle");
                }
                // Otherwise, select the location
                else {
                    SelectHelper.select(this);
                }

                // Double clicking the location opens the properties pane
                if (lastPress + DOUBLE_PRESS_SHOW_PROPERTIES_DELAY >= System.currentTimeMillis()) {
                    propertiesPane.setVisible(true);
                    // Place the location in front (so that the properties pane is above edges etc)
                    root.toFront();
                } else {
                    lastPress = System.currentTimeMillis();
                }

                // Update position for undo dragging
                previousX = root.getLayoutX();
                previousY = root.getLayoutY();
            }
        } else {

            // Unbind presentation root x and y coordinates (bind the view properly to enable dragging)
            root.layoutXProperty().unbind();
            root.layoutYProperty().unbind();

            // Bind the location to the presentation root x and y
            getLocation().xProperty().bind(root.layoutXProperty());
            getLocation().yProperty().bind(root.layoutYProperty());

            isPlaced = true;
        }
    }

    @FXML
    private void mouseDragged(final MouseEvent event) {
        // If the location is not a normal location (not initial/final) make it draggable
        if (getLocation().getType() == Location.Type.NORMAL) {

            // Calculate the potential new x alongside min and max values
            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue();
            final double minX = LocationPresentation.RADIUS + CanvasPresentation.GRID_SIZE;
            final double maxX = getComponent().getWidth() - LocationPresentation.RADIUS - CanvasPresentation.GRID_SIZE;

            // Drag according to min and max
            if (newX < minX) {
                root.setLayoutX(minX);
            } else if (newX > maxX) {
                root.setLayoutX(maxX);
            } else {
                root.setLayoutX(newX);
            }

            // Calculate the potential new y alongside min and max values
            final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue();
            final double minY = LocationPresentation.RADIUS + ComponentPresentation.TOOL_BAR_HEIGHT + CanvasPresentation.GRID_SIZE;
            final double maxY = getComponent().getHeight() - LocationPresentation.RADIUS - CanvasPresentation.GRID_SIZE;

            // Drag according to min and max
            if (newY < minY) {
                root.setLayoutY(minY);
            } else if (newY > maxY) {
                root.setLayoutY(maxY);
            } else {
                root.setLayoutY(newY);
            }

            // Tell the mouse release action that we can store an update
            wasDragged = true;
        }
    }

    @FXML
    private void mouseReleased(final MouseEvent event) {
        if (wasDragged) {
            // Add to undo redo stack
            final double currentX = root.getLayoutX();
            final double currentY = root.getLayoutY();
            final double storePreviousX = previousX;
            final double storePreviousY = previousY;
            UndoRedoStack.push(() -> {
                        root.setLayoutX(currentX);
                        root.setLayoutY(currentY);
            }, () -> {
                root.setLayoutX(storePreviousX);
                root.setLayoutY(storePreviousY);
            }, String.format("Moved location from (%.0f,%.0f) to (%.0f,%.0f)", currentX, currentY, storePreviousX, storePreviousY), "pin-drop");

            // Reset the was dragged boolean
            wasDragged = false;
        }
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {
        final Location location = getLocation();

        // Set the color of the location
        location.setColorIntensity(intensity);
        location.setColor(color);
    }

    @Override
    public Color getColor() {
        return getLocation().getColor();
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return getLocation().getColorIntensity();
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }
}
