package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.SubComponent;
import SW9.presentations.CanvasPresentation;
import SW9.utility.helpers.NewDragHelper;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class SubComponentController implements Initializable {

    private final ObjectProperty<SubComponent> subComponent = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Component> parentComponent = new SimpleObjectProperty<>(null);

    public BorderPane toolbar;
    public Rectangle background;
    public BorderPane frame;
    public JFXTextField name;
    public JFXTextField identifier;
    public Label originalComponent;
    public StackPane root;
    public Line line1;
    public Line line2;
    public Pane defaultLocationsContainer;

    private double previousX;
    private double previousY;
    private boolean wasDragged;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        subComponent.addListener((obs, oldComponent, newComponent) -> {
            // Bind the width and the height of the abstraction to the values in the view todo: reflect the height and width fromP the presentation into the abstraction
        });

        makeDraggable();
    }

    private void makeDraggable() {
        NewDragHelper.makeDraggable(
                root,
                () -> CanvasPresentation.mouseTracker.gridXProperty().subtract(getParentComponent().xProperty()).get(),
                () -> CanvasPresentation.mouseTracker.gridYProperty().subtract(getParentComponent().yProperty()).get()
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
