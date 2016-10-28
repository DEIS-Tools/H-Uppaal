package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Nail;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class NailController implements Initializable {

    private final ObjectProperty<Nail> nail = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    public Group root;
    public Circle nailCircle;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        nail.addListener((observable, oldValue, newValue) -> {

            // The radius property on the abstraction must reflect the radius in the view
            newValue.radiusProperty().bind(nailCircle.radiusProperty());

            // Draw the presentation based on the initial value from the abstraction
            root.setLayoutX(newValue.getX());
            root.setLayoutY(newValue.getY());

            // Reflect future updates from the presentation into the abstraction
            newValue.xProperty().bind(root.layoutXProperty());
            newValue.yProperty().bind(root.layoutYProperty());
        });
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
}
