package SW9.controllers;

import SW9.Debug;
import SW9.abstractions.Component;
import SW9.abstractions.Nail;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.presentations.LocationPresentation;
import SW9.presentations.NailPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.SelectHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class NailController implements Initializable, SelectHelper.ColorSelectable {

    public static boolean nailBeingDragged = false;

    private final ObjectProperty<Nail> nail = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    public Group root;
    public Circle nailCircle;
    public Circle dragCircle;
    private double previousX;
    private double previousY;
    private boolean wasDragged;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        nail.addListener((obsNail, oldNail, newNail) -> {

            // The radius from the abstraction is the master and the view simply reflects what is in the model
            nailCircle.radiusProperty().bind(newNail.radiusProperty());

            // Draw the presentation based on the initial value from the abstraction
            root.setLayoutX(newNail.getX());
            root.setLayoutY(newNail.getY());

            // Reflect future updates from the presentation into the abstraction
            newNail.xProperty().bind(root.layoutXProperty());
            newNail.yProperty().bind(root.layoutYProperty());

        });

        // Debug visuals
        dragCircle.opacityProperty().bind(Debug.draggableAreaOpacity);
        dragCircle.setFill(Debug.draggableAreaColor.getColor(Debug.draggableAreaColorIntensity));
    }

    public Nail getNail() {
        return nail.get();
    }

    public void setNail(final Nail nail) {
        this.nail.set(nail);
    }

    public ObjectProperty<Nail> nailProperty() {
        return nail;
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
    private void mousePressed(final MouseEvent event) {
        previousX = root.getLayoutX();
        previousY = root.getLayoutY();

        SelectHelper.select(this);
    }

    @FXML
    private void mouseDragged(final MouseEvent event) {

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
            final double minY = NailPresentation.COLLAPSED_RADIUS + ComponentPresentation.TOOL_BAR_HEIGHT + CanvasPresentation.GRID_SIZE;
            final double maxY = getComponent().getHeight() - NailPresentation.COLLAPSED_RADIUS - CanvasPresentation.GRID_SIZE;

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

            nailBeingDragged = true;

    }

    @FXML
    private void mouseReleased(final MouseEvent event) {
        if (wasDragged) {
            // Add to undo redo stack
            final double currentX = root.getLayoutX();
            final double currentY = root.getLayoutY();
            final double storePreviousX = previousX;
            final double storePreviousY = previousY;
            UndoRedoStack.push(
                    () -> {
                        root.setLayoutX(currentX);
                        root.setLayoutY(currentY);
                    },
                    () -> {
                        root.setLayoutX(storePreviousX);
                        root.setLayoutY(storePreviousY);
                    },
                    String.format("Moved nail from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                    "pin-drop"
            );

            // Reset the was dragged boolean
            wasDragged = false;

            nailBeingDragged = false;
        }
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {
        // Do nothing. A nail cannot be colored, but can be colored as selected
    }

    @Override
    public Color getColor() {
        return getComponent().getColor();
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return getComponent().getColorIntensity();
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
