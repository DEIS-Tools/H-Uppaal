package SW9.controllers;

import SW9.NewMain;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.backend.UPPAALDriver;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.LocationPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.SelectHelperNew;
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

public class LocationController implements Initializable, SelectHelperNew.ColorSelectable {

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
    private boolean isPlaced;
    private long lastPress = 0;
    private TimerTask reachabilityCheckTask;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.location.addListener((obsLocation, oldLocation, newLocation) -> {
            // The radius property on the abstraction must reflect the radius in the view
            newLocation.radiusProperty().bind(circle.radiusProperty());

            // The scale property on the abstraction must reflect the radius in the view
            newLocation.scaleProperty().bind(root.scaleXProperty());

            // initialize the name field and its bindings
            nameField.setText(newLocation.getName());
            newLocation.nameProperty().bind(nameField.textProperty());

            // initialize the invariant field and its bindings
            invariantField.setText(newLocation.getInvariant());
            newLocation.invariantProperty().bind(invariantField.textProperty());

            // If the location is not a normal location (not initial/final) make it draggable
            if(newLocation.getType() == Location.Type.NORMAL) {
                root.setOnMouseDragged(event -> {
                    root.setLayoutX(CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue());
                    root.setLayoutY(CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue());
                });
            }

        });

        // Scale x and y 1:1 (based on the x-scale)
        root.scaleYProperty().bind(root.scaleXProperty());

        // Register click listener on canvas to hide the property pane when the canvas is clicked
        CanvasPresentation.mouseTracker.registerOnMouseClickedEventHandler(event -> propertiesPane.setVisible(false));

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
                if (!getComponent().getLocations().contains(getLocation())) return;

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
                        NewMain.getProject().getComponents()
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
                });
            } else {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.URGENT);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                });
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
                });
            } else {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.COMMITTED);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                });
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
    private void mouseClicked(final MouseEvent event) {
        event.consume();
        // Double clicking the location opens the properties pane
        if(lastPress + DOUBLE_PRESS_SHOW_PROPERTIES_DELAY >= System.currentTimeMillis()) {
            propertiesPane.setVisible(true);
            // Place the location in front (so that the properties pane is above edges etc)
            root.toFront();
        } else {
            lastPress = System.currentTimeMillis();
        }
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
                    });
                }
                // Otherwise, select the location
                else {
                    SelectHelperNew.select(this);
                }
            }
        } else {

            /*subject.xProperty().unbind();
            subject.yProperty().unbind();
            subject.xProperty().set(CanvasPresentation.mouseTracker.gridXProperty().subtract(x).get());
            subject.yProperty().set(CanvasPresentation.mouseTracker.gridYProperty().subtract(y).get());*/

            // Unbind presentation root x and y coordinates (bind the view properly to enable dragging)
            root.layoutXProperty().unbind();
            root.layoutYProperty().unbind();

            // Bind the location to the presentation root x and y
            getLocation().xProperty().bind(root.layoutXProperty());
            getLocation().yProperty().bind(root.layoutYProperty());

            isPlaced = true;
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
        ((SelectHelperNew.Selectable) root).select();
    }

    @Override
    public void deselect() {
        ((SelectHelperNew.Selectable) root).deselect();
    }
}
