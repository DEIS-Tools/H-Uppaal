package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.presentations.LocationPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.SelectHelperNew;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class LocationController implements Initializable, SelectHelperNew.Selectable {

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
    public Path hexagon;
    public Path hexagonShakeIndicator;
    private boolean isPlaced;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.location.addListener((obsLocation, oldLocation, newLocation) -> {
            // The radius property on the abstraction must reflect the radius in the view
            newLocation.radiusProperty().bind(circle.radiusProperty());

            // The scale property on the abstraction must reflect the radius in the view
            newLocation.scaleProperty().bind(root.scaleXProperty());
        });

        // Scale x and y 1:1 (based on the x-scale)
        root.scaleYProperty().bind(root.scaleXProperty());
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

            BindingHelper.place(getLocation(), getComponent().xProperty(), getComponent().yProperty());
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
    public void styleSelected() {
        ((SelectHelperNew.SelectStyleable) root).styleSelected();
    }

    @Override
    public void styleDeselected() {
        ((SelectHelperNew.SelectStyleable) root).styleDeselected();
    }
}
