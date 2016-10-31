package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.presentations.LocationPresentation;
import SW9.utility.UndoRedoStack;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class LocationController implements Initializable {

    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    public Circle circle;

    public Group root;
    public Circle initialIndicator;
    public StackPane finalIndicator;
    public Circle shakeIndicator;
    public Group shakeContent;

    public StackPane invariantContainer;
    public Circle invariantCircle;
    public Label invariantLabel;

    public StackPane urgencyContainer;
    public Circle urgencyCircle;
    public Label urgencyLabel;
    public Label nameLabel;

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
        this.location.get().xProperty().bind(root.layoutXProperty());
        this.location.get().yProperty().bind(root.layoutYProperty());
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
    }

    @FXML
    private void mouseExited() {
        circle.setCursor(Cursor.DEFAULT);

        ((LocationPresentation) root).animateHoverExited();
    }

    @FXML
    private void mousePressed() {
        final Component component = getComponent();
        final Edge unfinishedEdge = component.getUnfinishedEdge();

        if (unfinishedEdge != null) {
            unfinishedEdge.setTargetLocation(getLocation());
        } else {
            final Edge newEdge = new Edge(getLocation());

            UndoRedoStack.push(() -> { // Perform
                component.addEdge(newEdge);
            }, () -> { // Undo
                component.removeEdge(newEdge);
            });
        }
    }

}
