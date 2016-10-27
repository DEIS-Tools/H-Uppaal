package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.controllers.EdgeController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

public class EdgePresentation extends Pane {

    private final EdgeController controller;

    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    public EdgePresentation(final Edge edge, final Component component) {
        final URL url = this.getClass().getResource("EdgePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(url.openStream());

            controller = fxmlLoader.getController();

            controller.setEdge(edge);
            this.edge.bind(controller.edgeProperty());

            controller.setComponent(component);
            this.component.bind(controller.componentProperty());


        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
