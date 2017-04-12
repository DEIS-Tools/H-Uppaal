package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Jork;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.presentations.DropDownMenu;
import SW9.presentations.JorkPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.keyboard.NudgeDirection;
import SW9.utility.keyboard.Nudgeable;
import com.jfoenix.controls.JFXPopup;
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
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;

import java.net.URL;
import java.util.List;
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
            if (event.isPrimaryButtonDown() && unfinishedEdge != null) {
                unfinishedEdge.setTargetJork(getJork());

            } else if (event.isSecondaryButtonDown()) {
                showContextMenu();
            } else if ((event.isShiftDown() && event.isPrimaryButtonDown()) || event.isMiddleButtonDown()) {
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

    private void showContextMenu() {

        final DropDownMenu contextMenu = new DropDownMenu(((Pane) root.getParent().getParent().getParent().getParent()), root, 230, true);

        contextMenu.addClickableListElement("Draw edge",
                (event) -> {
                    final Edge newEdge = new Edge(getJork());

                    KeyboardTracker.registerKeybind(KeyboardTracker.ABANDON_EDGE, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                        getComponent().removeEdge(newEdge);
                        UndoRedoStack.forgetLast();
                    }));

                    UndoRedoStack.push(() -> { // Perform
                        getComponent().addEdge(newEdge);
                    }, () -> { // Undo
                        getComponent().removeEdge(newEdge);
                    }, "Created edge starting from " + getJork(), "add-circle");

                    contextMenu.close();
                }
        );

        contextMenu.addSpacerElement();

        contextMenu.addClickableListElement("Delete", (mouseEvent -> {
            final Component component = CanvasController.getActiveComponent();
            final Jork jork = getJork();

            final List<Edge> relatedEdges = component.getRelatedEdges(jork);

            UndoRedoStack.push(() -> { // Perform
                // Remove the jork
                component.getJorks().remove(jork);
                relatedEdges.forEach(component::removeEdge);
            }, () -> { // Undo
                // Re-all the jork
                component.getJorks().add(jork);
                relatedEdges.forEach(component::addEdge);
            }, String.format("Deleted %s", jork), "delete");


            contextMenu.close();
        }));

        contextMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 0, 0);
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

    private enum JorkType {
        LOCATION,
        SUB_COMPONENT,
        UNKNOWN
    }
}
