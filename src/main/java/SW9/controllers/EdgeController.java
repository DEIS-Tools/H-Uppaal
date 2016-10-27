package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.utility.helpers.BindingHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ResourceBundle;

public class EdgeController implements Initializable {

    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    public Pane edgeRoot;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        edge.addListener((obsEdge, oldEdge, newEdge) -> {
            component.addListener((obsComponent, oldComponent, newComponent) -> {

                // Remove all previous presentation elements from the root of the edge presentation
                while (edgeRoot.getChildren().size() > 0) {
                    edgeRoot.getChildren().remove(0);
                }

                if (newEdge.getNails().isEmpty()) {
                    final Line line = new Line();
                    BindingHelper.bind(line, newEdge.getSourceLocation(), newComponent.xProperty(), newComponent.yProperty());
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
