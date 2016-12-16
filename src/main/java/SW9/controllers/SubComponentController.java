package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.SubComponent;
import SW9.presentations.CanvasPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.NailHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class SubComponentController implements Initializable {

    private final ObjectProperty<SubComponent> subComponent = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Component> parentComponent = new SimpleObjectProperty<>(null);

    public BorderPane toolbar;
    public Rectangle background;
    public BorderPane frame;
    public JFXTextField identifier;
    public Label originalComponent;
    public StackPane root;
    public Line line1;
    public Line line2;
    public Pane defaultLocationsContainer;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        makeDraggable();
    }

    private void makeDraggable() {

        Consumer<MouseEvent> startEdgeFromSubComponent = (event) -> {

            event.consume();

            final Edge unfinishedEdge = getParentComponent().getUnfinishedEdge();

            if (unfinishedEdge != null) {
                unfinishedEdge.setTargetSubComponent(getSubComponent());
                NailHelper.addMissingNails(unfinishedEdge);
            } else if (event.isShiftDown()) {
                event.consume();
                final Edge newEdge = new Edge(getSubComponent());

                KeyboardTracker.registerKeybind(KeyboardTracker.ABANDON_EDGE, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                    getParentComponent().removeEdge(newEdge);
                    UndoRedoStack.forget();
                }));

                UndoRedoStack.push(() -> { // Perform
                    getParentComponent().addEdge(newEdge);
                }, () -> { // Undo
                    getParentComponent().removeEdge(newEdge);
                }, "Created edge starting from sub component " + getSubComponent().getIdentifier(), "add-circle");
            }
        };

        ItemDragHelper.makeDraggable(
                root,
                root,
                () -> CanvasPresentation.mouseTracker.gridXProperty().subtract(getParentComponent().xProperty()).get(),
                () -> CanvasPresentation.mouseTracker.gridYProperty().subtract(getParentComponent().yProperty()).get(),
                startEdgeFromSubComponent,
                () -> {},
                () -> {}
        );
    }

    public SubComponent getSubComponent() {
        return subComponent.get();
    }

    public void setSubComponent(final SubComponent subComponent) {
        this.subComponent.set(subComponent);
    }

    public ObjectProperty<SubComponent> subComponentProperty() {
        return subComponent;
    }

    public Component getParentComponent() {
        return parentComponent.get();
    }

    public void setParentComponent(final Component parentComponent) {
        this.parentComponent.set(parentComponent);
    }

    public ObjectProperty<Component> parentComponentProperty() {
        return parentComponent;
    }
}
