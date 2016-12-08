package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.controllers.EdgeController;
import SW9.utility.colors.Color;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class EdgePresentation extends Group {

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

            initializeEdgeProperties();


        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeEdgeProperties() {
        initializeProperty(controller.selectContainer, controller.selectCircle, controller.selectLabel, 1);
        initializeProperty(controller.guardContainer, controller.guardCircle, controller.guardLabel, 2);
        initializeProperty(controller.syncContainer, controller.syncCircle, controller.syncLabel, 3);
        initializeProperty(controller.updateContainer, controller.updateCircle, controller.updateLabel, 4);
    }

    private void initializeProperty(final StackPane container, final Circle circle, final Label label, final int index) {
        final Edge edge = controller.getEdge();

        circle.setFill(javafx.scene.paint.Color.WHITE);
        circle.setStroke(Color.GREY.getColor(Color.Intensity.I500));
        label.setTextFill(Color.GREY.getTextColor(Color.Intensity.I50));

        final Runnable updatePlacement = () -> {
            // todo: calculate the placement differently if source = target
            if (edge.getSourceCircular().equals(edge.getTargetLocation())) return;

            if (edge.getTargetLocation() == null) return;
            final double x1 = edge.getSourceCircular().getX();
            final double x2 = edge.getTargetLocation().getX();
            final double y1 = edge.getSourceCircular().getY();
            final double y2 = edge.getTargetLocation().getY();
            final double m = (y2 - y1) / (x2 - x1);
            final double angle = Math.atan(m);

            final double hype = GRID_SIZE + index * GRID_SIZE * 2;
            final double w = Math.cos(angle) * hype;
            final double h = Math.sqrt(Math.pow(hype, 2) - Math.pow(w, 2));

            if (x1 > x2) {
                container.setLayoutX(x1 + w * -1);
            } else {
                container.setLayoutX(x1 + w);
            }

            if (y1 > y2) {
                container.setLayoutY(y1 + h * -1);
            } else {
                container.setLayoutY(y1 + h);
            }
        };

        edge.sourceLocationProperty().addListener(observable -> {
            edge.getSourceCircular().xProperty().addListener(observable1 -> updatePlacement.run());
            edge.getSourceCircular().yProperty().addListener(observable1 -> updatePlacement.run());
        });

        edge.targetLocationProperty().addListener(observable -> {
            edge.getTargetLocation().xProperty().addListener(observable1 -> updatePlacement.run());
            edge.getTargetLocation().yProperty().addListener(observable1 -> updatePlacement.run());
        });

        edge.getSourceCircular().xProperty().addListener(obs -> updatePlacement.run());
        edge.getSourceCircular().yProperty().addListener(obs -> updatePlacement.run());

        if (edge.getTargetLocation() == null) return;
        edge.getTargetLocation().xProperty().addListener(obs -> updatePlacement.run());
        edge.getTargetLocation().yProperty().addListener(obs -> updatePlacement.run());

        updatePlacement.run();

    }
}
