package SW9.controllers;

import SW9.abstractions.Component;
import SW9.utility.mouse.MouseTracker;
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
    private MouseTracker mouseTracker;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        component.addListener((obs, oldComponent, newComponent) -> {
            // Bind the width and the height of the abstraction to the values in the view todo: reflect the height and width from the presentation into the abstraction
        });

        // The root view have been inflated, initialize the mouse tracker on it
        mouseTracker = new MouseTracker(root);
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
    private void modelContainerPressed(final MouseEvent event) {
        event.consume();

        // Todo: Select the component
    }

    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }
}
