package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.controllers.LocationController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.helpers.SelectHelperNew;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class LocationPresentation extends Group implements MouseTrackable, SelectHelperNew.SelectStyleable {

    private static final double RADIUS = 20;
    private final LocationController controller;

    private final MouseTracker mouseTracker = new MouseTracker(this);
    private final Timeline initialAnimation = new Timeline();
    private final Timeline hoverAnimationEntered = new Timeline();
    private final Timeline hoverAnimationExited = new Timeline();

    private final List<BiConsumer<Color, Color.Intensity>> updateColorDelegates = new ArrayList<>();

    public LocationPresentation(final Location location, final Component component) {
        final URL url = this.getClass().getResource("LocationPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(url.openStream());

            controller = fxmlLoader.getController();

            // Bind the location with the one of the controller
            controller.setLocation(location);

            // Bind the component with the one of the controller
            controller.setComponent(component);

            // TODO introduce change of name and invariant
            // TODO make location draggable within a component
            // TODO make creation of location possible from the mouse


            initializeTypeGraphics();
            initializeNameLabel();

            initializeCircle();
            initializeRectangle();
            initializeHexagon();

            initializeInitialAnimation();
            initializeHoverAnimationEntered();
            initializeHoverAnimationExited();


        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeHexagon() {
        final MoveTo p1 = new MoveTo(0, 10);
        final LineTo p2 = new LineTo(10, 0);
        final LineTo p3 = new LineTo(30, 0);
        final LineTo p4 = new LineTo(40, 10);
        final LineTo p5 = new LineTo(40, 30);
        final LineTo p6 = new LineTo(30, 40);
        final LineTo p7 = new LineTo(10, 40);
        final LineTo p8 = new LineTo(0, 30);
        final LineTo p9 = new LineTo(0, 10);

        controller.hexagon.getElements().addAll(p1, p2, p3, p4, p5, p6, p7, p8, p9);
        controller.hexagonShakeIndicator.getElements().addAll(p1, p2, p3, p4, p5, p6, p7, p8, p9);

        final Location location = controller.getLocation();

        final ObjectProperty<Color> color = location.colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.colorIntensityProperty();

        // Delegate to style the label based on the color of the location
        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            controller.hexagon.setFill(newColor.getColor(newIntensity));
            controller.hexagon.setStroke(newColor.getColor(newIntensity.next(2)));
        };

        updateColorDelegates.add(updateColor);

        // Set the initial color
        updateColor.accept(color.get(), colorIntensity.get());

        // Update the color of the hexagon when the color of the location is updated
        color.addListener((obs, oldColor, newColor) -> updateColor.accept(newColor, colorIntensity.get()));

        // The hexagon shape should only be visible when the location is urgent
        controller.hexagon.visibleProperty().bind(location.urgencyProperty().isEqualTo(Location.Urgency.URGENT));
        controller.hexagonShakeIndicator.visibleProperty().bind(controller.hexagon.visibleProperty());
    }

    private void initializeRectangle() {
        final Location location = controller.getLocation();

        final Rectangle rectangle = controller.rectangle;
        final ObjectProperty<Color> color = location.colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.colorIntensityProperty();

        // Delegate to style the label based on the color of the location
        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            rectangle.setFill(newColor.getColor(newIntensity));
            rectangle.setStroke(newColor.getColor(newIntensity.next(2)));
        };

        updateColorDelegates.add(updateColor);

        // Set the initial color
        updateColor.accept(color.get(), colorIntensity.get());

        // Update the color of the rectangle when the color of the location is updated
        color.addListener((obs, oldColor, newColor) -> updateColor.accept(newColor, colorIntensity.get()));

        // The rectangle shape should only be visible when the location is urgent
        rectangle.visibleProperty().bind(location.urgencyProperty().isEqualTo(Location.Urgency.COMMITTED));
        controller.rectangleShakeIndicator.visibleProperty().bind(rectangle.visibleProperty());
    }

    private void initializeHoverAnimationEntered() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final KeyValue scale1x = new KeyValue(scaleXProperty(), 1, interpolator);
        final KeyValue scale2x = new KeyValue(scaleXProperty(), 1.1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale1x);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(100), scale2x);

        hoverAnimationEntered.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeHoverAnimationExited() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final KeyValue scale2x = new KeyValue(scaleXProperty(), 1.1, interpolator);
        final KeyValue scale1x = new KeyValue(scaleXProperty(), 1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale2x);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(100), scale1x);

        hoverAnimationExited.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeInitialAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);
        final KeyValue scale0x = new KeyValue(scaleXProperty(), 0, interpolator);
        final KeyValue scale2x = new KeyValue(scaleXProperty(), 1.1, interpolator);
        final KeyValue scale1x = new KeyValue(scaleXProperty(), 1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), scale2x);
        final KeyFrame kf3 = new KeyFrame(Duration.millis(250), scale1x);

        initialAnimation.getKeyFrames().addAll(kf1, kf2, kf3);
    }

    private void initializeNameLabel() {
        final Location location = controller.getLocation();

        final Label nameLabel = controller.nameLabel;

        nameLabel.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> {
            nameLabel.translateXProperty().set(newWidth.doubleValue() / -2);
        });

        nameLabel.heightProperty().addListener((obsHeight, oldHeight, newHeight) -> {
            nameLabel.translateYProperty().set(newHeight.doubleValue() / -2);
        });

        final Color color = location.getColor();
        final Color.Intensity colorIntensity = location.getColorIntensity();

        nameLabel.setTextFill(color.getTextColor(colorIntensity));
        nameLabel.textProperty().bind(location.nameProperty());
    }

    private void initializeCircle() {
        final Location location = controller.getLocation();

        final Circle circle = controller.circle;
        final ObjectProperty<Color> color = location.colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.colorIntensityProperty();

        // Delegate to style the label based on the color of the location
        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            circle.setFill(newColor.getColor(newIntensity));
            circle.setStroke(newColor.getColor(newIntensity.next(2)));
        };

        updateColorDelegates.add(updateColor);

        // Set the initial color
        updateColor.accept(color.get(), colorIntensity.get());

        // Update the color of the circle when the color of the location is updated
        color.addListener((obs, old, newColor) -> updateColor.accept(newColor, colorIntensity.get()));

        // The circle shape should only be visible when the location is urgent
        circle.visibleProperty().bind(location.urgencyProperty().isEqualTo(Location.Urgency.NORMAL));
        controller.circleShakeIndicator.visibleProperty().bind(circle.visibleProperty());
    }

    private void initializeTypeGraphics() {
        final Location location = controller.getLocation();

        final Circle initialIndicator = controller.initialIndicator;
        final StackPane finalIndicator = controller.finalIndicator;

        initialIndicator.visibleProperty().bind(new When(location.typeProperty().isEqualTo(Location.Type.INITIAL)).then(true).otherwise(false));
        finalIndicator.visibleProperty().bind(new When(location.typeProperty().isEqualTo(Location.Type.FINAl)).then(true).otherwise(false));
    }

    public void setLocation(final Location location) {
        controller.setLocation(location);
    }

    public void animateIn() {
        initialAnimation.play();
    }

    public void animateHoverEntered() {
        hoverAnimationEntered.play();
    }

    public void animateHoverExited() {
        hoverAnimationExited.play();
    }

    @Override
    public DoubleProperty xProperty() {
        return layoutXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return layoutYProperty();
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    public void shakeAnimation() {

        // Animation for rectangle shape (bind to the circle animation)
        controller.rectangleShakeIndicator.opacityProperty().bind(controller.circleShakeIndicator.opacityProperty());
        controller.circleShakeIndicator.radiusProperty().addListener((observable, oldValue, newValue) -> {
            controller.rectangleShakeIndicator.setWidth(newValue.doubleValue() * 2);
            controller.rectangleShakeIndicator.setTranslateX(newValue.doubleValue() * -1);

            controller.rectangleShakeIndicator.setHeight(newValue.doubleValue() * 2);
            controller.rectangleShakeIndicator.setTranslateY(newValue.doubleValue() * -1);
        });

        // Animation for hexagon shape (bind to the circle animation)
        controller.hexagonShakeIndicator.opacityProperty().bind(controller.circleShakeIndicator.opacityProperty());
        controller.circleShakeIndicator.radiusProperty().addListener((observable, oldValue, newValue) -> {
            controller.hexagonShakeIndicator.scaleXProperty().set(newValue.doubleValue() / LocationPresentation.RADIUS);
            controller.hexagonShakeIndicator.scaleYProperty().set(newValue.doubleValue() / LocationPresentation.RADIUS);
        });


        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final Timeline initialAnimation = new Timeline();
        final Timeline shakeContentAnimation = new Timeline();

        final KeyValue scale0x = new KeyValue(scaleXProperty(), 1, interpolator);
        final KeyValue radius0 = new KeyValue(controller.circleShakeIndicator.radiusProperty(), 0, interpolator);
        final KeyValue opacity0 = new KeyValue(controller.circleShakeIndicator.opacityProperty(), 0, interpolator);

        final KeyValue scale1x = new KeyValue(scaleXProperty(), 1.3, interpolator);
        final KeyValue radius1 = new KeyValue(controller.circleShakeIndicator.radiusProperty(), controller.circle.getRadius() * 0.85, interpolator);
        final KeyValue opacity1 = new KeyValue(controller.circleShakeIndicator.opacityProperty(), 0.2, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x, radius0, opacity0);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(2500), scale1x, radius1, opacity1);
        final KeyFrame kf3 = new KeyFrame(Duration.millis(3300), radius0, opacity0);
        final KeyFrame kf4 = new KeyFrame(Duration.millis(3500), scale0x);
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

    @Override
    public void styleSelected() {
        updateColorDelegates.forEach(colorConsumer -> colorConsumer.accept(Color.DEEP_ORANGE, Color.Intensity.I500));
    }

    @Override
    public void styleDeselected() {
        updateColorDelegates.forEach(colorConsumer -> {
            final Location location = controller.getLocation();

            colorConsumer.accept(location.getColor(), location.getColorIntensity());
        });
    }
}
