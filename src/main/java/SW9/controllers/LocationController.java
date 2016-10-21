package SW9.controllers;

import SW9.abstractions.Location;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class LocationController implements Initializable {

    public Circle circle;
    public Label label;
    public Circle initialIndicator;
    public StackPane finalIndicator;
    private Location location;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.location = new Location();
    }

    public Location getLocation() {
        return location;
    }
}
