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
import SW9.utility.keyboard.NudgeDirection;
import SW9.utility.keyboard.Nudgeable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Path;

import java.net.URL;
import java.util.ResourceBundle;

public class JorkController implements Initializable, SelectHelper.ItemSelectable, Nudgeable {

    private final ObjectProperty<Jork> jork = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    public Group root;
    public Path shape;
    public Label id;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeMouseControls();
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
    public ItemDragHelper.DragBounds getDragBounds() {
        final ObservableDoubleValue minX = new SimpleDoubleProperty(CanvasPresentation.GRID_SIZE);
        final ObservableDoubleValue maxX = getComponent().widthProperty().subtract(JorkPresentation.JORK_WIDTH + CanvasPresentation.GRID_SIZE);
        final ObservableDoubleValue minY = new SimpleDoubleProperty(ComponentPresentation.TOOL_BAR_HEIGHT + CanvasPresentation.GRID_SIZE);
        final ObservableDoubleValue maxY = getComponent().heightProperty().subtract(JorkPresentation.JORK_HEIGHT + CanvasPresentation.GRID_SIZE);
        return new ItemDragHelper.DragBounds(minX, maxX, minY, maxY);
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }

    private void initializeMouseControls() {

        root.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {

            event.consume();

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
                    UndoRedoStack.forgetLast();
                }));

                UndoRedoStack.push(() -> { // Perform
                    component.addEdge(newEdge);
                }, () -> { // Undo
                    component.removeEdge(newEdge);
                }, "Created edge starting from jork " + getJork().getId(), "add-circle");
            }
        });

        ItemDragHelper.makeDraggable(root, this::getDragBounds);
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

    @Override
    public boolean nudge(final NudgeDirection direction) {

        final double oldX = root.getLayoutX();
        final double newX = getDragBounds().trimX(root.getLayoutX() + direction.getXOffset());
        root.layoutXProperty().set(newX);

        final double oldY = root.getLayoutY();
        final double newY = getDragBounds().trimY(root.getLayoutY() + direction.getYOffset());
        root.layoutYProperty().set(newY);

        return oldX != newX || oldY != newY;
    }

    private enum JorkType {
        LOCATION,
        SUB_COMPONENT,
        UNKNOWN
    }

    @Override
    public DoubleProperty xProperty() {
        return root.layoutXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return root.layoutYProperty();
    }

    @Override
    public double getX() {
        return xProperty().get();
    }

    @Override
    public double getY() {
        return yProperty().get();
    }
}
