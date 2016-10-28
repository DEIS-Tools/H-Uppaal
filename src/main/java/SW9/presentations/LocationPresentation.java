package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.controllers.LocationController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class LocationPresentation extends Group implements MouseTrackable {

    public static final double RADIUS = 20;
    private final LocationController controller;
    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    private final MouseTracker mouseTracker = new MouseTracker(this);

    public LocationPresentation(final Location location, final Component component) {
        final URL url = this.getClass().getResource("LocationPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            initializeCircle();
            initializeTypeGraphics();
            initializeInvariantCircle();
            initializeUrgencyCircle();
            initializeNameLabel();

            fxmlLoader.setRoot(this);
            fxmlLoader.load(url.openStream());

            controller = fxmlLoader.getController();

            // Bind the location with the one of the controller
            controller.setLocation(location);
            this.location.bind(controller.locationProperty());

            // Bind the component with the one of the controller
            controller.setComponent(component);
            this.component.bind(controller.componentProperty());

            // TODO introduce change of name and invariant
            // TODO make location draggable within a component
            // TODO make creation of location possible from the mouse

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeNameLabel() {
        this.location.addListener((observable, oldValue, newLocation) -> {
            final Label nameLabel = controller.nameLabel;

            nameLabel.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> {
                nameLabel.translateXProperty().set(newWidth.doubleValue() / -2);
            });

            nameLabel.heightProperty().addListener((obsHeight, oldHeight, newHeight) -> {
                nameLabel.translateYProperty().set(newHeight.doubleValue() / -2);
            });

            final Color color = newLocation.getColor();
            final Color.Intensity colorIntensity = newLocation.getColorIntensity();
            nameLabel.setTextFill(color.getTextColor(colorIntensity));

            nameLabel.textProperty().bind(newLocation.nameProperty());
        });
    }

    private void initializeUrgencyCircle() {
        this.location.addListener((observable, oldValue, newLocation) -> {
            final Color color = newLocation.getColor();
            final Color.Intensity colorIntensity = newLocation.getColorIntensity();

            final StackPane urgencyContainer = controller.urgencyContainer;
            final Circle urgencyCircle = controller.urgencyCircle;
            final Label urgencyLabel = controller.urgencyLabel;

            urgencyContainer.visibleProperty().bind(newLocation.urgencyProperty().isNotEqualTo(Location.Urgency.NORMAL));
            urgencyCircle.setFill(color.getColor(colorIntensity));
            urgencyCircle.setStroke(color.getColor(colorIntensity.next(2)));
            urgencyLabel.setTextFill(color.getTextColor(colorIntensity));

            urgencyLabel.textProperty().bind(
                    new When(newLocation.urgencyProperty().isEqualTo(Location.Urgency.URGENT)).
                            then("U").
                            otherwise("C")
            );
        });
    }

    private void initializeInvariantCircle() {
        this.location.addListener((observable, oldValue, newLocation) -> {
            final Color color = newLocation.getColor();
            final Color.Intensity colorIntensity = newLocation.getColorIntensity();

            final StackPane invariantContainer = controller.invariantContainer;
            final Circle invariantCircle = controller.invariantCircle;
            final Label invariantLabel = controller.invariantLabel;

            invariantContainer.visibleProperty().bind(newLocation.invariantProperty().isNotEmpty());
            invariantCircle.setFill(color.getColor(colorIntensity));
            invariantCircle.setStroke(color.getColor(colorIntensity.next(2)));
            invariantLabel.setTextFill(color.getTextColor(colorIntensity));

        });
    }

    private void initializeCircle() {
        location.addListener((observable, oldValue, newLocation) -> {
            final Circle circle = controller.circle;
            final ObjectProperty<Color> color = newLocation.colorProperty();
            final ObjectProperty<Color.Intensity> colorIntensity = newLocation.colorIntensityProperty();

            // Delegate to style the label based on the color of the location
            final Consumer<Color> updateColor = (newColor) -> {
                circle.setFill(newColor.getColor(colorIntensity.get()));
                circle.setStroke(newColor.getColor(colorIntensity.get().next(2)));
            };

            // Set the initial color
            updateColor.accept(color.get());

            // Update the color of the circle when the color of the location is updated
            color.addListener((obs, old, newValue) -> updateColor.accept(newValue));
        });
    }

    private void initializeTypeGraphics() {
        location.addListener((observable, oldValue, newLocation) -> {
            final Circle initialIndicator = controller.initialIndicator;
            final StackPane finalIndicator = controller.finalIndicator;

            initialIndicator.visibleProperty().bind(new When(newLocation.typeProperty().isEqualTo(Location.Type.INITIAL)).then(true).otherwise(false));
            finalIndicator.visibleProperty().bind(new When(newLocation.typeProperty().isEqualTo(Location.Type.FINAl)).then(true).otherwise(false));
        });

    }

    public void setLocation(final Location location) {
        controller.setLocation(location);
    }

    public void animateIn() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final Timeline initialAnimation = new Timeline();

        final KeyValue scale0x = new KeyValue(scaleXProperty(), 0, interpolator);
        final KeyValue scale0y = new KeyValue(scaleYProperty(), 0, interpolator);

        final KeyValue scale2x = new KeyValue(scaleXProperty(), 1.1, interpolator);
        final KeyValue scale2y = new KeyValue(scaleYProperty(), 1.1, interpolator);

        final KeyValue scale1x = new KeyValue(scaleXProperty(), 1, interpolator);
        final KeyValue scale1y = new KeyValue(scaleYProperty(), 1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x, scale0y);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), scale2x, scale2y);
        final KeyFrame kf3 = new KeyFrame(Duration.millis(250), scale1x, scale1y);

        initialAnimation.getKeyFrames().addAll(kf1, kf2, kf3);

        initialAnimation.play();
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

    public void shakeAnimation() {

        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final Timeline initialAnimation = new Timeline();
        final Timeline shakeContentAnimation = new Timeline();

        final KeyValue scale0x = new KeyValue(scaleXProperty(), 1, interpolator);
        final KeyValue scale0y = new KeyValue(scaleYProperty(), 1, interpolator);
        final KeyValue radius0 = new KeyValue(controller.shakeIndicator.radiusProperty(), 0, interpolator);
        final KeyValue opacity0 = new KeyValue(controller.shakeIndicator.opacityProperty(), 0, interpolator);

        final KeyValue scale1x = new KeyValue(scaleXProperty(), 1.3, interpolator);
        final KeyValue scale1y = new KeyValue(scaleYProperty(), 1.3, interpolator);
        final KeyValue radius1 = new KeyValue(controller.shakeIndicator.radiusProperty(), controller.circle.getRadius() * 0.85, interpolator);
        final KeyValue opacity1 = new KeyValue(controller.shakeIndicator.opacityProperty(), 0.2, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x, scale0y, radius0, opacity0);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(2500), scale1x, scale1y, radius1, opacity1);
        final KeyFrame kf3 = new KeyFrame(Duration.millis(3300), radius0, opacity0);
        final KeyFrame kf4 = new KeyFrame(Duration.millis(3500), scale0x, scale0y);
        final KeyFrame kfEnd = new KeyFrame(Duration.millis(8000), null);

        initialAnimation.getKeyFrames().addAll(kf1, kf2, kf3, kf4, kfEnd);

        final KeyValue noShakeX = new KeyValue(controller.shakeContent.translateXProperty(), 0, interpolator);
        final KeyValue shakeLeftX = new KeyValue(controller.shakeContent.translateXProperty(), -1, interpolator);
        final KeyValue shakeRightX = new KeyValue(controller.shakeContent.translateXProperty(), 1, interpolator);

        final KeyFrame[] shakeFrames = {
                new KeyFrame(Duration.millis(0), noShakeX),
                new KeyFrame(Duration.millis(1450), noShakeX),

                new KeyFrame(Duration.millis(1500), shakeLeftX),
                new KeyFrame(Duration.millis(1550), shakeRightX),
                new KeyFrame(Duration.millis(1600), shakeLeftX),
                new KeyFrame(Duration.millis(1650), shakeRightX),
                new KeyFrame(Duration.millis(1700), shakeLeftX),
                new KeyFrame(Duration.millis(1750), shakeRightX),
                new KeyFrame(Duration.millis(1800), shakeLeftX),
                new KeyFrame(Duration.millis(1850), shakeRightX),
                new KeyFrame(Duration.millis(1900), shakeLeftX),
                new KeyFrame(Duration.millis(1950), shakeRightX),
                new KeyFrame(Duration.millis(2000), shakeLeftX),
                new KeyFrame(Duration.millis(2050), shakeRightX),
                new KeyFrame(Duration.millis(2100), shakeLeftX),
                new KeyFrame(Duration.millis(2150), shakeRightX),
                new KeyFrame(Duration.millis(2200), shakeLeftX),
                new KeyFrame(Duration.millis(2250), shakeRightX),

                new KeyFrame(Duration.millis(2300), noShakeX),
                new KeyFrame(Duration.millis(8000), null)
        };

        shakeContentAnimation.getKeyFrames().addAll(shakeFrames);

        shakeContentAnimation.setCycleCount(1000);
        initialAnimation.setCycleCount(1000);

        shakeContentAnimation.play();
        initialAnimation.play();

    }
}
