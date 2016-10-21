package SW9.presentations;

import SW9.abstractions.Location;
import SW9.controllers.LocationController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class LocationPresentation extends StackPane implements MouseTrackable {

    private final LocationController controller;
    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();

    private final MouseTracker mouseTracker = new MouseTracker(this);

    public LocationPresentation() {
        this(new Location());
    }

    public LocationPresentation(final Location location) {
        final URL url = this.getClass().getResource("LocationPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(url.openStream());

            controller = fxmlLoader.getController();
            controller.setLocation(location);
            this.location.bind(controller.locationProperty());

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
        final ObjectProperty<Color> color = location.get().colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.get().colorIntensityProperty();

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
        final ObjectProperty<Location.Urgency> urgency = location.get().urgencyProperty();
        final ObjectProperty<Color> color = location.get().colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.get().colorIntensityProperty();


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

        location.addListener((observable, oldValue, newLocation) -> {
            initialIndicator.visibleProperty().bind(new When(newLocation.typeProperty().isEqualTo(Location.Type.INITIAL)).then(true).otherwise(false));
            finalIndicator.visibleProperty().bind(new When(newLocation.typeProperty().isEqualTo(Location.Type.FINAl)).then(true).otherwise(false));
        });

    }

    public void setLocation(final Location location) {
        controller.setLocation(location);
    }

    @Override
    public DoubleProperty xProperty() {
        return location.get().xProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return location.get().yProperty();
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }
}
