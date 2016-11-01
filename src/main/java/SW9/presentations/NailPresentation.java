package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Nail;
import SW9.controllers.NailController;
import SW9.utility.colors.Color;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;

import java.io.IOException;
import java.net.URL;

public class NailPresentation extends Group {

    public static final double COLLAPSED_RADIUS = 2d;
    public static final double HOVERED_RADIUS = 7d;

    private final NailController controller;

    public NailPresentation(final Nail nail, final Component component) {
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

            // Bind the component with the one of the controller
            controller.setNail(nail);

            initializeNailCircle();
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
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
}
