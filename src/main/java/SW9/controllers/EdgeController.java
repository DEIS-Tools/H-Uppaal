package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.utility.helpers.BindingHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EdgeController implements Initializable {

    final ArrayList<Line> lines = new ArrayList<>();
    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    public Pane edgeRoot;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        edge.addListener((obsEdge, oldEdge, newEdge) -> {
            component.addListener((obsComponent, oldComponent, newComponent) -> {

                // When the target location is set, finish drawing the edge
                newEdge.targetLocationProperty().addListener((obsTargetLocation, oldTargetLocation, newTargetLocation) -> {
                    // TODO: Check if the source location is the same as the target location

                    BindingHelper.bind(lines.get(lines.size() - 1), newEdge.getSourceLocation(), newTargetLocation);
                });

                // Remove all previous presentation elements from the root of the edge presentation
                while (edgeRoot.getChildren().size() > 0) {
                    edgeRoot.getChildren().remove(0);
                }

                if (newEdge.getNails().isEmpty()) {
                    final Line line = new Line();
                    lines.add(line);
                    Circle c1 = new Circle(24, -6, 0);
                    edgeRoot.getChildren().add(c1);
                    BindingHelper.bind(line, c1, newComponent.xProperty(), newComponent.yProperty());
                    edgeRoot.getChildren().add(line);
                }
            });
        });

    }

    public Edge getEdge() {
        return edge.get();
    }

    public void setEdge(final Edge edge) {
        this.edge.set(edge);
    }

    public ObjectProperty<Edge> edgeProperty() {
        return edge;
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(final Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

}
