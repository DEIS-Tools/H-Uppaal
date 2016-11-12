package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.controllers.LocationController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.DropShadowHelper;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.*;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LocationPresentation extends Group implements MouseTrackable, SelectHelper.Selectable {

    public static final double RADIUS = 20;
    private final LocationController controller;

    private final MouseTracker mouseTracker = new MouseTracker(this);
    private final Timeline initialAnimation = new Timeline();
    private final Timeline hoverAnimationEntered = new Timeline();
    private final Timeline hoverAnimationExited = new Timeline();
    private final Timeline scaleShakeIndicatorBackgroundAnimation = new Timeline();
    private final Timeline shakeContentAnimation = new Timeline();
    private final Timeline propertiesPaneAnimationShow = new Timeline();
    private final Timeline propertiesPaneAnimationHide = new Timeline();

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
            initializeUrgencyLabel();

            initializeCircle();

            initializeNameTag();

            initializeInitialAnimation();
            initializeHoverAnimationEntered();
            initializeHoverAnimationExited();
            initializeShakeAnimation();
            initializePropertiesPaneAnimationShow();
            initializePropertiesPaneAnimationHide();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeNameTag() {
        final Consumer<Location> updateNameTag = location -> {
            // Update the color
            controller.nameTag.bindToColor(location.colorProperty(), location.colorIntensityProperty());

            // Update the name
            controller.nameTag.setAndBindString(location.nameProperty());

            // Update the position
            controller.nameTag.translateXProperty().set(controller.circle.getRadius() * 1.5);
            controller.nameTag.translateYProperty().bind(controller.nameTag.heightProperty().divide(-2));
        };

        controller.locationProperty().addListener(observable -> updateNameTag.accept(controller.getLocation()));
        updateNameTag.accept(controller.getLocation());
    }

    private void initializeHoverAnimationEntered() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final KeyValue scale1x = new KeyValue(controller.scaleContent.scaleXProperty(), 1, interpolator);
        final KeyValue scale2x = new KeyValue(controller.scaleContent.scaleXProperty(), 1.1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale1x);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(100), scale2x);

        hoverAnimationEntered.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeHoverAnimationExited() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final KeyValue scale2x = new KeyValue(controller.scaleContent.scaleXProperty(), 1.1, interpolator);
        final KeyValue scale1x = new KeyValue(controller.scaleContent.scaleXProperty(), 1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale2x);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(100), scale1x);

        hoverAnimationExited.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeInitialAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);
        final KeyValue scale0x = new KeyValue(controller.scaleContent.scaleXProperty(), 0, interpolator);
        final KeyValue scale2x = new KeyValue(controller.scaleContent.scaleXProperty(), 1.1, interpolator);
        final KeyValue scale1x = new KeyValue(controller.scaleContent.scaleXProperty(), 1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), scale2x);
        final KeyFrame kf3 = new KeyFrame(Duration.millis(250), scale1x);

        initialAnimation.getKeyFrames().addAll(kf1, kf2, kf3);
    }

    private void initializeUrgencyLabel() {
        final Location location = controller.getLocation();

        final Label urgencyLabel = controller.urgencyLabel;

        // Center align the label
        urgencyLabel.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> urgencyLabel.translateXProperty().set(newWidth.doubleValue() / -2));
        urgencyLabel.heightProperty().addListener((obsHeight, oldHeight, newHeight) -> urgencyLabel.translateYProperty().set(newHeight.doubleValue() / -2));

        final Color color = location.getColor();
        final Color.Intensity colorIntensity = location.getColorIntensity();

        urgencyLabel.setTextFill(color.getTextColor(colorIntensity));
        location.urgencyProperty().addListener((obs, oldUrgency, newUrgency) -> {
            if (newUrgency.equals(Location.Urgency.NORMAL)) {
                urgencyLabel.setText("");
            } else if (newUrgency.equals(Location.Urgency.URGENT)) {
                urgencyLabel.setText("U");
            } else if (newUrgency.equals(Location.Urgency.COMMITTED)) {
                urgencyLabel.setText("C");
            }
        });
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
        if (shakeContentAnimation.getStatus().equals(Animation.Status.RUNNING)) return;

        hoverAnimationEntered.play();
    }

    public void animateHoverExited() {
        if (shakeContentAnimation.getStatus().equals(Animation.Status.RUNNING)) return;

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

    private void initializeShakeAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final KeyValue scale0x = new KeyValue(controller.scaleContent.scaleXProperty(), 1, interpolator);
        final KeyValue radius0 = new KeyValue(controller.circleShakeIndicator.radiusProperty(), 0, interpolator);
        final KeyValue opacity0 = new KeyValue(controller.circleShakeIndicator.opacityProperty(), 0, interpolator);

        final KeyValue scale1x = new KeyValue(controller.scaleContent.scaleXProperty(), 1.3, interpolator);
        final KeyValue radius1 = new KeyValue(controller.circleShakeIndicator.radiusProperty(), controller.circle.getRadius() * 0.85, interpolator);
        final KeyValue opacity1 = new KeyValue(controller.circleShakeIndicator.opacityProperty(), 0.2, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x, radius0, opacity0);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(2500), scale1x, radius1, opacity1);
        final KeyFrame kf3 = new KeyFrame(Duration.millis(3300), radius0, opacity0);
        final KeyFrame kf4 = new KeyFrame(Duration.millis(3500), scale0x);
        final KeyFrame kfEnd = new KeyFrame(Duration.millis(8000), null);

        scaleShakeIndicatorBackgroundAnimation.getKeyFrames().addAll(kf1, kf2, kf3, kf4, kfEnd);

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
        scaleShakeIndicatorBackgroundAnimation.setCycleCount(1000);
    }

    public void animateShakeWarning(final boolean start) {
        if (start) {
            scaleShakeIndicatorBackgroundAnimation.play();
            shakeContentAnimation.play();
        } else {
            controller.scaleContent.scaleXProperty().set(1);
            scaleShakeIndicatorBackgroundAnimation.playFromStart();
            scaleShakeIndicatorBackgroundAnimation.stop();

            controller.circleShakeIndicator.setOpacity(0);
            shakeContentAnimation.playFromStart();
            shakeContentAnimation.stop();
        }
    }

    private void initializePropertiesPaneAnimationHide() {
        controller.propertiesPane.visibleProperty().addListener((obs, oldVisibility, newVisibility) -> {
            animatePropertiesPane(newVisibility);
        });

        controller.propertiesPane.setEffect(DropShadowHelper.generateElevationShadow(8));

        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final KeyValue noScaleX = new KeyValue(controller.propertiesPane.scaleXProperty(), 0, interpolator);
        final KeyValue fullScaleX = new KeyValue(controller.propertiesPane.scaleXProperty(), 1, interpolator);
        final KeyValue noScaleY = new KeyValue(controller.propertiesPane.scaleYProperty(), 0, interpolator);
        final KeyValue fullScaleY = new KeyValue(controller.propertiesPane.scaleYProperty(), 1, interpolator);

        final KeyFrame[] shakeFrames = {
                new KeyFrame(Duration.millis(0), noScaleX, noScaleY),
                new KeyFrame(Duration.millis(100), fullScaleX, fullScaleY),
        };

        propertiesPaneAnimationShow.getKeyFrames().addAll(shakeFrames);
    }

    private void initializePropertiesPaneAnimationShow() {
    }

    private void animatePropertiesPane(final boolean show) {
        if(show) {
            propertiesPaneAnimationShow.play();
        } else {
            propertiesPaneAnimationHide.play();
        }
    }

    @Override
    public void select() {
        updateColorDelegates.forEach(colorConsumer -> colorConsumer.accept(Color.DEEP_ORANGE, Color.Intensity.I500));
    }

    @Override
    public void deselect() {
        updateColorDelegates.forEach(colorConsumer -> {
            final Location location = controller.getLocation();

            colorConsumer.accept(location.getColor(), location.getColorIntensity());
        });
    }

    public LocationController getController() {
        return controller;
    }
}
