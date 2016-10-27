package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Nail;
import SW9.controllers.NailController;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;

import java.io.IOException;
import java.net.URL;

public class NailPresentation extends Group implements MouseTrackable {

    private final NailController controller;
    private final ObjectProperty<Nail> nail = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    public NailPresentation(final Nail nail, final Component component) {
        final URL url = this.getClass().getResource("NailPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {

            initializeNailCircle();

            fxmlLoader.setRoot(this);
            fxmlLoader.load(url.openStream());

            controller = fxmlLoader.getController();

            // Bind the component with the one of the controller
            controller.setComponent(component);
            this.component.bind(controller.componentProperty());

            // Bind the component with the one of the controller
            controller.setNail(nail);
            this.nail.bind(controller.nailProperty());

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeNailCircle() {
        component.addListener((obsComponent, oldComponent, newComponent) -> {

            // When the color of the component updates, update the nail indicator as well
            newComponent.colorProperty().addListener((obsColor, oldColor, newColor) -> {
                controller.nailCircle.setFill(newColor.getColor(newComponent.getColorIntensity()));
                controller.nailCircle.setStroke(newColor.getColor(newComponent.getColorIntensity().next(2)));
            });

            // When the color intensity of the component updates, update the nail indicator as well
            newComponent.colorIntensityProperty().addListener((obsColorIntensity, oldColorIntensity, newColorIntensity) -> {
                controller.nailCircle.setFill(newComponent.getColor().getColor(newColorIntensity));
                controller.nailCircle.setStroke(newComponent.getColor().getColor(newColorIntensity.next(2)));
            });
        });
    }

    @Override
    public DoubleProperty xProperty() {
        return null;
    }

    @Override
    public DoubleProperty yProperty() {
        return null;
    }

    @Override
    public MouseTracker getMouseTracker() {
        return null;
    }
}
