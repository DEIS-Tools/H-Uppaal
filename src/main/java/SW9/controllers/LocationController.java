package SW9.controllers;

import SW9.abstractions.Location;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class LocationController implements Initializable {

    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();

    public Circle circle;

    public Group root;
    public Circle initialIndicator;
    public StackPane finalIndicator;
    public Circle shakeIndicator;
    public Group shakeContent;

    public StackPane invariantContainer;
    public Circle invariantCircle;
    public Label invariantLabel;

    public StackPane urgencyContainer;
    public Circle urgencyCircle;
    public Label urgencyLabel;
    public Label nameLabel;

    private Timeline enteredAnimation;
    private Timeline existedAnimation = new Timeline();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        enteredAnimation = new Timeline();
        existedAnimation = new Timeline();

        final KeyValue scale0x = new KeyValue(root.scaleXProperty(), 1, interpolator);
        final KeyValue scale0y = new KeyValue(root.scaleYProperty(), 1, interpolator);

        final KeyValue scale1x = new KeyValue(root.scaleXProperty(), 1.1, interpolator);
        final KeyValue scale1y = new KeyValue(root.scaleYProperty(), 1.1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x, scale0y);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), scale1x, scale1y);

        final KeyFrame kf3 = new KeyFrame(Duration.millis(0), scale1x, scale1y);
        final KeyFrame kf4 = new KeyFrame(Duration.millis(200), scale0x, scale0y);

        enteredAnimation.getKeyFrames().addAll(kf1, kf2);
        existedAnimation.getKeyFrames().addAll(kf3, kf4);
    }

    public Location getLocation() {
        return location.get();
    }

    public void setLocation(final Location location) {
        this.location.set(location);
    }

    public ObjectProperty<Location> locationProperty() {
        return location;
    }

    @FXML
    private void mouseEntered() {
        circle.setCursor(Cursor.HAND);

        enteredAnimation.play();
    }

    @FXML
    private void mouseExited() {
        circle.setCursor(Cursor.DEFAULT);

        existedAnimation.play();
    }

}
