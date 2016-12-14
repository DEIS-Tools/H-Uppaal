package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.controllers.NailController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.SelectHelper;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class NailPresentation extends Group implements SelectHelper.Selectable {

    public static final double COLLAPSED_RADIUS = 2d;
    public static final double HOVERED_RADIUS = 7d;

    private final NailController controller;

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

            initializeNailCircle();

            initializePropertyTag();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
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
                propertyTagLine.setVisible(false);
            } else {

                // Show the property tag since the nail is a property nail
                propertyTag.setVisible(true);
                propertyTagLine.setVisible(true);

                // Set and bind the location of the property tag
                if((controller.getNail().getPropertyX() != 0) && (controller.getNail().getPropertyY() != 0)) {
                    propertyTag.setTranslateX(controller.getNail().getPropertyX());
                    propertyTag.setTranslateY(controller.getNail().getPropertyY());
                }
                controller.getNail().propertyXProperty().bind(propertyTag.translateXProperty());
                controller.getNail().propertyYProperty().bind(propertyTag.translateYProperty());

                if(propertyType.equals(Edge.PropertyType.SELECTION)) {
                    propertyTag.setPlaceholder("Select");
                    propertyTag.setAndBindString(controller.getEdge().selectProperty());
                } else if(propertyType.equals(Edge.PropertyType.GUARD)) {
                    propertyTag.setPlaceholder("Guard");
                    propertyTag.setAndBindString(controller.getEdge().guardProperty());
                } else if(propertyType.equals(Edge.PropertyType.SYNCHRONIZATION)) {
                    propertyTag.setPlaceholder("Sync");
                    propertyTag.setAndBindString(controller.getEdge().syncProperty());
                } else if(propertyType.equals(Edge.PropertyType.UPDATE)) {
                    propertyTag.setPlaceholder("Update");
                    propertyTag.setAndBindString(controller.getEdge().updateProperty());
                }
            }
        };

        // Whenever the property type updates update the tag
        controller.getNail().propertyTypeProperty().addListener((obs, oldPropertyType, newPropertyType) -> {
            updatePropertyType.accept(newPropertyType);
        });

        // Update the tag initially
        updatePropertyType.accept(controller.getNail().getPropertyType());
    }

    private void initializeNailCircle() {
        final Runnable updateNailColor = () -> {
            final Color color = controller.getComponent().getColor();
            final Color.Intensity colorIntensity = controller.getComponent().getColorIntensity();

            controller.nailCircle.setFill(color.getColor(colorIntensity));
            controller.nailCircle.setStroke(color.getColor(colorIntensity.next(2)));
        };

        // When the color of the component updates, update the nail indicator as well
        controller.getComponent().colorProperty().addListener((observable) -> updateNailColor.run());

        // When the color intensity of the component updates, update the nail indicator
        controller.getComponent().colorIntensityProperty().addListener((observable) -> updateNailColor.run());

        // Initialize the color of the nail
        updateNailColor.run();
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
        final Component component = controller.getComponent();

        // Set the color
        controller.nailCircle.setFill(component.getColor().getColor(component.getColorIntensity()));
        controller.nailCircle.setStroke(component.getColor().getColor(component.getColorIntensity().next(2)));
    }
}
