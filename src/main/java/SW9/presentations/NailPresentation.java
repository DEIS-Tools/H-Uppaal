package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.controllers.NailController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.SelectHelper;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorInput;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import static javafx.util.Duration.millis;

public class NailPresentation extends Group implements SelectHelper.Selectable {

    public static final double COLLAPSED_RADIUS = 2d;
    public static final double HOVERED_RADIUS = 7d;

    private final NailController controller;
    private final Timeline shakeAnimation = new Timeline();

    public NailPresentation(final Nail nail, final Edge edge,final Component component) {
        final URL url = this.getClass().getResource("NailPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(url.openStream());

            controller = fxmlLoader.getController();

            // Bind the component with the one of the controller
            controller.setComponent(component);

            // Bind the edge with the one of the controller
            controller.setEdge(edge);

            // Bind the nail with the one of the controller
            controller.setNail(nail);

            initializeNailCircleColor();
            initializePropertyTag();
            initializeRadius();
            initializeShakeAnimation();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeRadius() {

        final Consumer<Edge.PropertyType> radiusUpdater = (propertyType) -> {
            if(!propertyType.equals(Edge.PropertyType.NONE)) {
                controller.getNail().setRadius(NailPresentation.HOVERED_RADIUS);
            }
        };

        controller.getNail().propertyTypeProperty().addListener((observable, oldValue, newValue) -> {
            radiusUpdater.accept(newValue);
        });

        radiusUpdater.accept(controller.getNail().getPropertyType());
    }

    private void initializePropertyTag() {

        final TagPresentation propertyTag = controller.propertyTag;
        final Line propertyTagLine = controller.propertyTagLine;
        propertyTag.setComponent(controller.getComponent());
        propertyTag.setLocationAware(controller.getNail());

        // Bind the line to the tag
        BindingHelper.bind(propertyTagLine, propertyTag);

        // Bind the color of the tag to the color of the component
        propertyTag.bindToColor(controller.getComponent().colorProperty(), controller.getComponent().colorIntensityProperty());

        // Updates visibility and placeholder of the tag depending on the type of nail
        final Consumer<Edge.PropertyType> updatePropertyType = (propertyType) -> {

            // If it is not a property nail hide the tag otherwise show it and write proper placeholder
            if(propertyType.equals(Edge.PropertyType.NONE)) {
                propertyTag.setVisible(false);
            } else {

                // Show the property tag since the nail is a property nail
                propertyTag.setVisible(true);

                // Set and bind the location of the property tag
                if((controller.getNail().getPropertyX() != 0) && (controller.getNail().getPropertyY() != 0)) {
                    propertyTag.setTranslateX(controller.getNail().getPropertyX());
                    propertyTag.setTranslateY(controller.getNail().getPropertyY());
                }
                controller.getNail().propertyXProperty().bind(propertyTag.translateXProperty());
                controller.getNail().propertyYProperty().bind(propertyTag.translateYProperty());

                Label propertyLabel = controller.propertyLabel;

                if(propertyType.equals(Edge.PropertyType.SELECTION)) {
                    propertyTag.setPlaceholder("Select");
                    propertyLabel.setText(":");
                    propertyLabel.setTranslateX(-3);
                    propertyLabel.setTranslateY(-8);
                    propertyTag.setAndBindString(controller.getEdge().selectProperty());
                } else if(propertyType.equals(Edge.PropertyType.GUARD)) {
                    propertyTag.setPlaceholder("Guard");
                    propertyLabel.setText("<");
                    propertyLabel.setTranslateX(-3);
                    propertyLabel.setTranslateY(-7);
                    propertyTag.setAndBindString(controller.getEdge().guardProperty());
                } else if(propertyType.equals(Edge.PropertyType.SYNCHRONIZATION)) {
                    propertyTag.setPlaceholder("Sync");
                    propertyLabel.setText("!?");
                    propertyLabel.setTranslateX(-6);
                    propertyLabel.setTranslateY(-7);
                    propertyTag.setAndBindString(controller.getEdge().syncProperty());
                } else if(propertyType.equals(Edge.PropertyType.UPDATE)) {
                    propertyTag.setPlaceholder("Update");
                    propertyLabel.setText("=");
                    propertyLabel.setTranslateX(-3);
                    propertyLabel.setTranslateY(-7);
                    propertyTag.setAndBindString(controller.getEdge().updateProperty());
                }

                propertyTag.requestTextFieldFocus();
                propertyTag.requestTextFieldFocus(); // Requesting it twice is needed for some reason
            }
        };

        // Whenever the property type updates update the tag
        controller.getNail().propertyTypeProperty().addListener((obs, oldPropertyType, newPropertyType) -> {
            updatePropertyType.accept(newPropertyType);
        });

        // Update the tag initially
        updatePropertyType.accept(controller.getNail().getPropertyType());
    }

    private void initializeNailCircleColor() {
        final Runnable updateNailColor = () -> {
            final Color color = controller.getComponent().getColor();
            final Color.Intensity colorIntensity = controller.getComponent().getColorIntensity();

            if(!controller.getNail().getPropertyType().equals(Edge.PropertyType.NONE)) {
                controller.nailCircle.setFill(color.getColor(colorIntensity));
                controller.nailCircle.setStroke(color.getColor(colorIntensity.next(2)));
            } else {
                controller.nailCircle.setFill(Color.GREY_BLUE.getColor(Color.Intensity.I800));
                controller.nailCircle.setStroke(Color.GREY_BLUE.getColor(Color.Intensity.I900));
            }
        };

        // When the color of the component updates, update the nail indicator as well
        controller.getComponent().colorProperty().addListener((observable) -> updateNailColor.run());

        // When the color intensity of the component updates, update the nail indicator
        controller.getComponent().colorIntensityProperty().addListener((observable) -> updateNailColor.run());

        // Initialize the color of the nail
        updateNailColor.run();
    }

    private void initializeShakeAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final double startX = controller.root.getTranslateX();
        final KeyValue kv1 = new KeyValue(controller.root.translateXProperty(), startX - 3, interpolator);
        final KeyValue kv2 = new KeyValue(controller.root.translateXProperty(), startX + 3, interpolator);
        final KeyValue kv3 = new KeyValue(controller.root.translateXProperty(), startX, interpolator);

        final KeyFrame kf1 = new KeyFrame(millis(50), kv1);
        final KeyFrame kf2 = new KeyFrame(millis(100), kv2);
        final KeyFrame kf3 = new KeyFrame(millis(150), kv1);
        final KeyFrame kf4 = new KeyFrame(millis(200), kv2);
        final KeyFrame kf5 = new KeyFrame(millis(250), kv3);

        shakeAnimation.getKeyFrames().addAll(kf1, kf2, kf3, kf4, kf5);
    }

    public void shake() {
        shakeAnimation.play();
    }

    @Override
    public void select() {
        final Color color = Color.DEEP_ORANGE;
        final Color.Intensity intensity = Color.Intensity.I500;

        // Set the color
        controller.nailCircle.setFill(color.getColor(intensity));
        controller.nailCircle.setStroke(color.getColor(intensity.next(2)));
    }

    @Override
    public void deselect() {
        Color color = Color.GREY_BLUE;
        Color.Intensity intensity = Color.Intensity.I800;

        // Set the color
        if(!controller.getNail().getPropertyType().equals(Edge.PropertyType.NONE)) {
            color = controller.getComponent().getColor();
            intensity = controller.getComponent().getColorIntensity();
        }

        controller.nailCircle.setFill(color.getColor(intensity));
        controller.nailCircle.setStroke(color.getColor(intensity.next(2)));

    }
}
