package SW9.controllers;

import SW9.abstractions.Component;
import SW9.presentations.CanvasPresentation;
import SW9.utility.UndoRedoStack;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class SubComponentController implements Initializable {

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);
    public BorderPane toolbar;
    public Rectangle background;
    public BorderPane frame;
    public Label name;
    public StackPane root;
    public Pane modelContainer;
    public Line line1;
    public Line line2;
    public Label x;
    public Label y;
    public Pane defaultLocationsContainer;

    private double previousX;
    private double previousY;
    private boolean wasDragged;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        component.addListener((obs, oldComponent, newComponent) -> {
            // Bind the width and the height of the abstraction to the values in the view todo: reflect the height and width fromP the presentation into the abstraction
        });
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

        // TODO make selectable
    }

    @FXML
    private void mouseDragged(final MouseEvent event) {

        // Calculate the potential new x alongside min and max values
        final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue();

        root.setLayoutX(newX);

        // Calculate the potential new y alongside min and max values
        final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue();

        root.setLayoutY(newY);

        // Tell the mouse release action that we can store an update
        wasDragged = true;
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
        }
    }
}
