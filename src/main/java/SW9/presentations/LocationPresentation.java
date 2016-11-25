package SW9.presentations;

import SW9.Debug;
import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.controllers.LocationController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.*;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LocationPresentation extends Group implements MouseTrackable, SelectHelper.Selectable {

    public static final double RADIUS = 15;
    public static final double INITIAL_RADIUS = RADIUS / 4 * 3;
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
    private final DoubleProperty animation = new SimpleDoubleProperty(0);
    private final DoubleBinding reverseAnimation = new SimpleDoubleProperty(1).subtract(animation);

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

            initializeIdLabel();

            initializeTypeGraphics();

            initializeCircle();
            initializeLocationShape();

            initializeNameTag();
            initializeInvariantTag();

            initializeInitialAnimation();
            initializeHoverAnimationEntered();
            initializeHoverAnimationExited();
            initializeShakeAnimation();

            initializeHiddenAreaCircle();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeHiddenAreaCircle() {
        controller.hiddenAreaCircle.opacityProperty().bind(Debug.hoverableAreaOpacity);
        controller.hiddenAreaCircle.setFill(Debug.hoverableAreaColor.getColor(Debug.hoverableAreaColorIntensity));
    }

    private void initializeIdLabel() {
        final Location location = controller.getLocation();


        final Label idLabel = controller.idLabel;

        idLabel.textProperty().bind((location.idProperty()));

        // Center align the label
        idLabel.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> idLabel.translateXProperty().set(newWidth.doubleValue() / -2));
        idLabel.heightProperty().addListener((obsHeight, oldHeight, newHeight) -> idLabel.translateYProperty().set(newHeight.doubleValue() / -2));

        final ObjectProperty<Color> color = location.colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.colorIntensityProperty();

        // Delegate to style the label based on the color of the location
        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            idLabel.setTextFill(newColor.getTextColor(newIntensity));
        };

        updateColorDelegates.add(updateColor);

        // Set the initial color
        updateColor.accept(color.get(), colorIntensity.get());

        // Update the color of the circle when the color of the location is updated
        color.addListener((obs, old, newColor) -> updateColor.accept(newColor, colorIntensity.get()));
    }

    private void initializeNameTag() {

        controller.nameTag.replaceSpace();

        final Consumer<Location> updateNameTag = location -> {
            // Update the color
            controller.nameTag.bindToColor(location.colorProperty(), location.colorIntensityProperty(), true);

            // Update the invariant
            controller.nameTag.setAndBindString(location.nicknameProperty());

            // Update the placeholder
            controller.nameTag.setPlaceholder("No name");

            // Update the position
            controller.nameTag.translateXProperty().set(controller.circle.getRadius() * 1.5);
            controller.nameTag.translateYProperty().bind(controller.invariantTag.heightProperty().divide(-2));


            final Consumer<String> updateVisibility = (nickname) -> {
                if (nickname.equals("")) {
                    controller.nameTag.setOpacity(0);
                } else {
                    controller.nameTag.setOpacity(1);
                }
            };

            location.nicknameProperty().addListener((obs, oldNickname, newNickname) -> updateVisibility.accept(newNickname));
            updateVisibility.accept(location.getNickname());
        };

        controller.nameTag.opacityProperty().addListener((obs, oldOpacity, newOpacity) -> {
            if (newOpacity.doubleValue() < 1) {
                if (controller.nameTag.textFieldIsFocused()) {
                    controller.nameTag.setOpacity(1);
                }
            }
        });

        controller.locationProperty().addListener(observable -> updateNameTag.accept(controller.getLocation()));
        updateNameTag.accept(controller.getLocation());
    }

    private void initializeInvariantTag() {
        final Consumer<Location> updateInvariantTag = location -> {
            // Update the color
            controller.invariantTag.bindToColor(location.colorProperty(), location.colorIntensityProperty());

            // Update the invariant
            controller.invariantTag.setAndBindString(location.invariantProperty());

            // Update the placeholder
            controller.invariantTag.setPlaceholder("No invariant");

            // Update the position
            controller.invariantTag.translateXProperty().set(controller.circle.getRadius() * 1.5);
            controller.invariantTag.translateYProperty().bind(controller.invariantTag.heightProperty().add(controller.invariantTag.heightProperty().divide(-2)));

            final Consumer<String> updateVisibility = (invariant) -> {
                if (invariant.equals("")) {
                    controller.invariantTag.setOpacity(0);
                } else {
                    controller.invariantTag.setOpacity(1);
                }
            };

            location.invariantProperty().addListener((obs, oldInvariant, newInvariant) -> updateVisibility.accept(newInvariant));
            updateVisibility.accept(location.getInvariant());
        };

        controller.invariantTag.opacityProperty().addListener((obs, oldOpacity, newOpacity) -> {
            if (newOpacity.doubleValue() < 1) {
                if (controller.invariantTag.textFieldIsFocused()) {
                    controller.invariantTag.setOpacity(1);
                }
            }
        });

        controller.locationProperty().addListener(observable -> updateInvariantTag.accept(controller.getLocation()));
        updateInvariantTag.accept(controller.getLocation());
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

    private void initializeCircle() {
        final Location location = controller.getLocation();

        final Circle circle = controller.circle;
        circle.setRadius(RADIUS);
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

    private void initializeLocationShape() {
        final Path path = controller.locationShape;

        initializeLocationShape(path, RADIUS);

        final Location location = controller.getLocation();


        BiConsumer<Location.Urgency, Location.Urgency> updateUrgencies = (oldUrgency, newUrgency) -> {
            final Transition toUrgent = new Transition() {
                {
                    setCycleDuration(Duration.millis(200));
                }

                @Override
                protected void interpolate(final double frac) {
                    animation.set(frac);
                }
            };

            final Transition toNormal = new Transition() {
                {
                    setCycleDuration(Duration.millis(200));
                }

                @Override
                protected void interpolate(final double frac) {
                    animation.set(1-frac);
                }
            };

            if(oldUrgency.equals(Location.Urgency.NORMAL) && !newUrgency.equals(Location.Urgency.NORMAL)) {
                toUrgent.play();

            } else if(newUrgency.equals(Location.Urgency.NORMAL)) {
                toNormal.play();
            }

            if(newUrgency.equals(Location.Urgency.COMMITTED)) {
                path.setStrokeWidth(3);
            } else {
                path.setStrokeWidth(1);
            }
        };

        location.urgencyProperty().addListener((obsUrgency, oldUrgency, newUrgency) -> {
            updateUrgencies.accept(oldUrgency, newUrgency);
        });

        updateUrgencies.accept(Location.Urgency.NORMAL, location.getUrgency());

        // Update the colors
        final ObjectProperty<Color> color = location.colorProperty();
        final ObjectProperty<Color.Intensity> colorIntensity = location.colorIntensityProperty();

        // Delegate to style the label based on the color of the location
        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            path.setFill(newColor.getColor(newIntensity));
            path.setStroke(newColor.getColor(newIntensity.next(2)));
        };

        updateColorDelegates.add(updateColor);

        // Set the initial color
        updateColor.accept(color.get(), colorIntensity.get());

        // Update the color of the circle when the color of the location is updated
        color.addListener((obs, old, newColor) -> updateColor.accept(newColor, colorIntensity.get()));
    }

    private void initializeTypeGraphics() {
        final Location location = controller.getLocation();

        final Path initialIndicator = controller.initialIndicator;
        final StackPane finalIndicator = controller.finalIndicator;

        initializeLocationShape(initialIndicator, INITIAL_RADIUS);

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

    private void initializeLocationShape(final Path locationShape, final double radius) {
        final double c = 0.551915024494;
        final double circleToOctagonLineRatio = 0.35;

        final MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(animation.multiply(circleToOctagonLineRatio * radius));
        moveTo.yProperty().set(radius);

        final CubicCurveTo cc1 = new CubicCurveTo();
        cc1.controlX1Property().bind(reverseAnimation.multiply(c * radius).add(animation.multiply(circleToOctagonLineRatio * radius)));
        cc1.controlY1Property().bind(reverseAnimation.multiply(radius).add(animation.multiply(radius)));
        cc1.controlX2Property().bind(reverseAnimation.multiply(radius).add(animation.multiply(radius)));
        cc1.controlY2Property().bind(reverseAnimation.multiply(c * radius).add(animation.multiply(circleToOctagonLineRatio * radius)));
        cc1.setX(radius);
        cc1.yProperty().bind(animation.multiply(circleToOctagonLineRatio * radius));


        final LineTo lineTo1 = new LineTo();
        lineTo1.xProperty().bind(cc1.xProperty());
        lineTo1.yProperty().bind(cc1.yProperty().multiply(-1));

        final CubicCurveTo cc2 = new CubicCurveTo();
        cc2.controlX1Property().bind(cc1.controlX2Property());
        cc2.controlY1Property().bind(cc1.controlY2Property().multiply(-1));
        cc2.controlX2Property().bind(cc1.controlX1Property());
        cc2.controlY2Property().bind(cc1.controlY1Property().multiply(-1));
        cc2.xProperty().bind(moveTo.xProperty());
        cc2.yProperty().bind(moveTo.yProperty().multiply(-1));


        final LineTo lineTo2 = new LineTo();
        lineTo2.xProperty().bind(cc2.xProperty().multiply(-1));
        lineTo2.yProperty().bind(cc2.yProperty());

        final CubicCurveTo cc3 = new CubicCurveTo();
        cc3.controlX1Property().bind(cc2.controlX2Property().multiply(-1));
        cc3.controlY1Property().bind(cc2.controlY2Property());
        cc3.controlX2Property().bind(cc2.controlX1Property().multiply(-1));
        cc3.controlY2Property().bind(cc2.controlY1Property());
        cc3.xProperty().bind(lineTo1.xProperty().multiply(-1));
        cc3.yProperty().bind(lineTo1.yProperty());


        final LineTo lineTo3 = new LineTo();
        lineTo3.xProperty().bind(cc3.xProperty());
        lineTo3.yProperty().bind(cc3.yProperty().multiply(-1));

        final CubicCurveTo cc4 = new CubicCurveTo();
        cc4.controlX1Property().bind(cc3.controlX2Property());
        cc4.controlY1Property().bind(cc3.controlY2Property().multiply(-1));
        cc4.controlX2Property().bind(cc3.controlX1Property());
        cc4.controlY2Property().bind(cc3.controlY1Property().multiply(-1));
        cc4.xProperty().bind(lineTo2.xProperty());
        cc4.yProperty().bind(lineTo2.yProperty().multiply(-1));


        final LineTo lineTo4 = new LineTo();
        lineTo4.xProperty().bind(moveTo.xProperty());
        lineTo4.yProperty().bind(moveTo.yProperty());


        locationShape.getElements().add(moveTo);
        locationShape.getElements().add(cc1);

        locationShape.getElements().add(lineTo1);
        locationShape.getElements().add(cc2);

        locationShape.getElements().add(lineTo2);
        locationShape.getElements().add(cc3);

        locationShape.getElements().add(lineTo3);
        locationShape.getElements().add(cc4);

        locationShape.getElements().add(lineTo4);
    }
}
