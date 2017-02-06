package SW9.controllers;

import SW9.Debug;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.presentations.TagPresentation;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.SelectHelper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NailController implements Initializable, SelectHelper.ColorSelectable {

    public static boolean nailBeingDragged = false;

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Nail> nail = new SimpleObjectProperty<>();

    public Group root;
    public Circle nailCircle;
    public Circle dragCircle;
    public Line propertyTagLine;
    public TagPresentation propertyTag;
    public Group dragGroup;
    public Label propertyLabel;

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

        makeDraggable();
    }

    private void makeDraggable() {

        final DoubleProperty mouseXDiff = new SimpleDoubleProperty(0);
        final DoubleProperty mouseYDiff = new SimpleDoubleProperty(0);

        final Consumer<MouseEvent> mousePressedOnNail = (event) -> {
            mouseXDiff.set(event.getX());
            mouseYDiff.set(event.getY());

            if (event.isShortcutDown()) {
                SelectHelper.addToSelection(this);
            } else {
                SelectHelper.select(this);
            }
        };

        final Supplier<Double> supplyX = () -> {
            // Calculate the potential new x alongside min and max values
            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue();
            final double minX = mouseXDiff.get() + CanvasPresentation.GRID_SIZE;
            final double maxX = getComponent().getWidth() + mouseXDiff.get() - CanvasPresentation.GRID_SIZE;

            // Drag according to min and max
            if (newX < minX) {
                return minX;
            } else if (newX > maxX) {
                return maxX;
            } else {
                return newX;
            }
        };

        final Supplier<Double> supplyY = () -> {
            // Calculate the potential new y alongside min and max values
            final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue();
            final double minY = mouseYDiff.get() + ComponentPresentation.TOOL_BAR_HEIGHT + CanvasPresentation.GRID_SIZE;
            final double maxY = getComponent().getHeight() + mouseYDiff.get() - CanvasPresentation.GRID_SIZE;

            // Drag according to min and max
            if (newY < minY) {
                return minY;
            } else if (newY > maxY) {
                return maxY;
            } else {
                return newY;
            }
        };

        ItemDragHelper.makeDraggable(
                root,
                dragGroup,
                supplyX,
                supplyY,
                mousePressedOnNail,
                () -> nailBeingDragged = true,
                () -> nailBeingDragged =false
        );
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

    public Edge getEdge() {
        return edge.get();
    }

    public void setEdge(final Edge edge) {
        this.edge.set(edge);
    }

    public ObjectProperty<Edge> edgeProperty() {
        return edge;
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

    @FXML
    private void mouseEntered() {
        propertyTag.setOpacity(1);
    }

    @FXML
    private void mouseExited() {
        if (getEdge().getProperty(getNail().getPropertyType()).equals("")) {
            propertyTag.setOpacity(0);
        } else {
            propertyTag.setOpacity(1);
        }

    }
}
