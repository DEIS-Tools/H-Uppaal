package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Jork;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.presentations.JorkPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.NailHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JorkController implements Initializable, SelectHelper.ColorSelectable {

    private final ObjectProperty<Jork> jork = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    public Group root;
    public Path shape;
    public Label id;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        makeDraggable();
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {

    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return null;
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }

    private void makeDraggable() {

        final DoubleProperty mouseXDiff = new SimpleDoubleProperty(0);
        final DoubleProperty mouseYDiff = new SimpleDoubleProperty(0);

        final Consumer<MouseEvent> mousePressed = (event) -> {
            event.consume();

            mouseXDiff.set(event.getX());
            mouseYDiff.set(event.getY());

            if (event.isShortcutDown()) {
                SelectHelper.addToSelection(this);
            } else {
                SelectHelper.select(this);
            }

            final Component component = getComponent();
            final Edge unfinishedEdge = component.getUnfinishedEdge();
            if (unfinishedEdge != null) {
                unfinishedEdge.setTargetJork(getJork());
                NailHelper.addMissingNails(unfinishedEdge);
            } else if (event.isShiftDown()) {
                final Edge newEdge = new Edge(getJork());

                KeyboardTracker.registerKeybind(KeyboardTracker.ABANDON_EDGE, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                    component.removeEdge(newEdge);
                    UndoRedoStack.forget();
                }));

                UndoRedoStack.push(() -> { // Perform
                    component.addEdge(newEdge);
                }, () -> { // Undo
                    component.removeEdge(newEdge);
                }, "Created edge starting from jork " + getJork().getId(), "add-circle");
            }
        };

        final Supplier<Double> supplyX = () -> {
            // Calculate the potential new x alongside min and max values
            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).get();
            final double minX = mouseXDiff.get() + CanvasPresentation.GRID_SIZE;
            final double maxX = getComponent().getWidth() - JorkPresentation.JORK_WIDTH - CanvasPresentation.GRID_SIZE + mouseXDiff.get();

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
            final double maxY = getComponent().getHeight() - JorkPresentation.JORK_HEIGHT - CanvasPresentation.GRID_SIZE + mouseYDiff.get();

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
                root,
                supplyX,
                supplyY,
                mousePressed,
                () -> {
                },
                () -> {
                }
        );
    }

    public Jork getJork() {
        return jork.get();
    }

    public void setJork(Jork jork) {
        this.jork.set(jork);
    }

    public ObjectProperty<Jork> jorkProperty() {
        return jork;
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    private enum JorkType {
        LOCATION,
        SUB_COMPONENT,
        UNKNOWN
    }
}
