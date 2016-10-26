package SW9.controllers;

import SW9.NewMain;
import SW9.abstractions.Edge;
import SW9.presentations.CanvasPresentation;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ResourceBundle;

public class EdgeController implements Initializable {

    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();

    public Pane edgeRoot;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        edge.addListener((obsEdge, oldEdge, newEdge) -> {

            // Remove all previous presentation elements from the root of the edge presentation
            while (edgeRoot.getChildren().size() > 0) {
                edgeRoot.getChildren().remove(0);
            }

            if (newEdge.getNails().isEmpty()) {
                final Line line = new Line();
                BindingHelper.bind(line, newEdge.getSourceLocation(), new MouseTracker(edgeRoot));
                edgeRoot.getChildren().add(line);
            }
        });

    }

    public Edge getEdge() {
        return edge.get();
    }

    public ObjectProperty<Edge> edgeProperty() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge.set(edge);
    }

}
