package SW9.presentations;

import SW9.abstractions.Location;
import SW9.controllers.LocationController;
import SW9.utility.colors.Color;
import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class LocationPresentation extends StackPane {

    private final LocationController controller;
    private final Location location;

    public LocationPresentation() {
        final URL location = this.getClass().getResource("LocationPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            this.controller = fxmlLoader.getController();
            this.location = controller.getLocation();
            initializeCircle();
            initializeLabel();
            initializeTypeGraphics();

            // TODO introduce change of name and invariant
            // TODO make location draggable within a component
            // TODO make creation of location possible from the mouse


        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeCircle() {
        final Circle circle = controller.circle;
        final ObjectProperty<Color> color = location.colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.colorIntensityProperty();

        // Delegate to style the label based on the color of the location
        final Consumer<Color> updateColor = (newColor) -> {
            circle.setFill(newColor.getColor(colorIntensity.get()));
            circle.setStroke(newColor.getColor(colorIntensity.get().next(2)));
        };


        // Set the initial color
        updateColor.accept(color.get());

        // Update the color of the circle when the color of the location is updated
        color.addListener((observable, oldValue, newValue) -> updateColor.accept(newValue));
    }

    private void initializeLabel() {
        final Label label = controller.label;
        final ObjectProperty<Location.Urgency> urgency = location.urgencyProperty();
        final ObjectProperty<Color> color = location.colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.colorIntensityProperty();


        // Delegate to style the label based on the color of the location
        final Consumer<Color> updateColor = (newColor) -> {
            label.setTextFill(newColor.getTextColor(colorIntensity.get()));
        };

        // Set the initial color
        updateColor.accept(color.get());

        // Update the color of the label when the color of the location is updated
        color.addListener((observable, oldValue, newValue) -> updateColor.accept(newValue));

        // Delegate to update te text of the label depending of the urgency
        final Consumer<Location.Urgency> updateText = (newType) -> {
            if (newType.equals(Location.Urgency.URGENT)) {
                label.setText("U");
            } else if (newType.equals(Location.Urgency.COMMITTED)) {
                label.setText("C");
            } else {
                label.setText("");
            }
        };

        // Set the initial text
        updateText.accept(urgency.get());

        // Update the text whenever the urgency changes
        urgency.addListener((observable, oldValue, newValue) -> updateText.accept(newValue));
    }

    private void initializeTypeGraphics() {
        final Circle initialIndicator = controller.initialIndicator;
        final StackPane finalIndicator = controller.finalIndicator;

        initialIndicator.visibleProperty().bind(new When(location.typeProperty().isEqualTo(Location.Type.INITIAL)).then(true).otherwise(false));
        finalIndicator.visibleProperty().bind(new When(location.typeProperty().isEqualTo(Location.Type.FINAl)).then(true).otherwise(false));

    }
}
