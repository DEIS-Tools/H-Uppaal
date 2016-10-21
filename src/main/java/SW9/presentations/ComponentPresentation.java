package SW9.presentations;

import SW9.abstractions.Component;
import SW9.controllers.ComponentController;
import SW9.utility.colors.Color;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class ComponentPresentation extends StackPane {

    private final ComponentController controller;
    private final Component component;

    public ComponentPresentation() {
        this(new Component("Component" + new Random().nextInt(5000))); // todo: find a new unique component name
    }

    public ComponentPresentation(final Component component) {
        final URL location = this.getClass().getResource("ComponentPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            controller = fxmlLoader.getController();
            this.component = component;
            controller.setComponent(this.component);

            // Find the x and y coordinates to the values in the model
            layoutXProperty().bind(component.xProperty());
            layoutYProperty().bind(component.yProperty());

            // Bind the width and the height of the view to the values in the model
            minWidthProperty().bind(component.widthProperty());
            maxWidthProperty().bind(component.widthProperty());
            minHeightProperty().bind(component.heightProperty());
            maxHeightProperty().bind(component.heightProperty());

            initializeBackground();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeBackground() {
        // Bind the background width and height to the values in the model
        controller.background.widthProperty().bind(component.widthProperty());
        controller.background.heightProperty().bind(component.heightProperty());

        // Set the background color to the lightest possible version of the color
        controller.background.setFill(component.getColor().getColor(Color.Intensity.I50));

        // Set the stroke color to two shades darker
        controller.background.setStroke(component.getColor().getColor(component.getColorIntensity().next(2)));

        // Will let the grid show through
        controller.background.blendModeProperty().set(BlendMode.MULTIPLY);
    }
}
