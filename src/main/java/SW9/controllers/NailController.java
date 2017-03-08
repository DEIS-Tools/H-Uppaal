package SW9.controllers;

import SW9.Debug;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.presentations.DropDownMenu;
import SW9.presentations.TagPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.SelectHelper;
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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ResourceBundle;

public class NailController implements Initializable, SelectHelper.ItemSelectable, Nudgeable {

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

        initializeMouseControls();
    }

    private void showContextMenu() {

        final DropDownMenu contextMenu = new DropDownMenu(((Pane) root.getParent().getParent().getParent().getParent()), root, 230, true);

        contextMenu.addClickableListElement("Delete", (mouseEvent -> {
            final Nail nail = getNail();
            final Edge edge = getEdge();
            final Component component = getComponent();
            final int index = edge.getNails().indexOf(nail);

            final String restoreProperty = edge.getProperty(nail.getPropertyType());

            // If the last nail on a self loop for a location or join/fork delete the edge also
            final boolean shouldDeleteEdgeAlso = edge.isSelfLoop() && edge.getNails().size() == 1 && edge.getSourceSubComponent() == null;

            // Create an undo redo description based, add extra comment if edge is also deleted
            String message =  String.format("Deleted %s", nail.toString());
            if(shouldDeleteEdgeAlso) {
                message += String.format("(Was last Nail on self loop edge --> %s also deleted)", toString());
            }

            UndoRedoStack.push(
                    () -> {
                        edge.removeNail(nail);
                        edge.setProperty(nail.getPropertyType(), "");
                        if(shouldDeleteEdgeAlso) {
                            component.removeEdge(edge);
                        }
                    },
                    () -> {
                        if(shouldDeleteEdgeAlso) {
                            component.addEdge(edge);
                        }
                        edge.setProperty(nail.getPropertyType(), restoreProperty);
                        edge.insertNailAt(nail, index);
                    },
                    message,
                    "delete"
            );
            contextMenu.close();
        }));

        contextMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 0.5,0.5);
    }

    private void initializeMouseControls() {

        root.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            event.consume();
            if (event.isShortcutDown()) {
                SelectHelper.addToSelection(this);
            } else if(event.isSecondaryButtonDown()) {
                showContextMenu();
            } else {
                SelectHelper.select(this);
            }
        });

        root.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> nailBeingDragged = true);
        root.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> nailBeingDragged = false);

        ItemDragHelper.makeDraggable(root, this::getDragBounds);
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
    public ItemDragHelper.DragBounds getDragBounds() {
        final ObservableDoubleValue minX = new SimpleDoubleProperty(CanvasPresentation.GRID_SIZE);
        final ObservableDoubleValue maxX = getComponent().widthProperty().subtract(CanvasPresentation.GRID_SIZE);
        final ObservableDoubleValue minY = new SimpleDoubleProperty(ComponentPresentation.TOOL_BAR_HEIGHT + CanvasPresentation.GRID_SIZE);
        final ObservableDoubleValue maxY = getComponent().heightProperty().subtract(CanvasPresentation.GRID_SIZE);

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
}
