package SW9.controllers;

import SW9.abstractions.Location;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class LocationController implements Initializable {

    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();

    public Circle circle;

    public Label label;
    public Circle initialIndicator;
    public StackPane finalIndicator;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

    }

    public Location getLocation() {
        return location.get();
    }

    public ObjectProperty<Location> locationProperty() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location.set(location);
    }



}
