package SW9.controllers;

import SW9.abstractions.Edge;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.Group;

import java.net.URL;
import java.util.ResourceBundle;

public class EdgeController implements Initializable {

    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();

    public Group root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
