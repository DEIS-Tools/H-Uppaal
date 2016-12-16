package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Jork;
import SW9.presentations.CanvasPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.NailHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class JorkController implements Initializable, SelectHelper.ColorSelectable {

    public Group root;
    public Rectangle rectangle;

    private final ObjectProperty<Jork> jork = new SimpleObjectProperty<>();

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

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

        ItemDragHelper.makeDraggable(
                root,
                root,
                () -> CanvasPresentation.mouseTracker.getGridX(),
                () -> CanvasPresentation.mouseTracker.getGridY(),
                (event) -> {
                    event.consume();
                    SelectHelper.select(this);
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
                },
                () -> {
                },
                () -> {
                }
        );
    }

    public Jork getJork() {
        return jork.get();
    }

    public ObjectProperty<Jork> jorkProperty() {
        return jork;
    }

    public void setJork(Jork jork) {
        this.jork.set(jork);
    }

    public Component getComponent() {
        return component.get();
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    public void setComponent(Component component) {
        this.component.set(component);
    }
}
