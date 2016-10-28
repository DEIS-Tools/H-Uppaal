package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.presentations.NailPresentation;
import SW9.utility.helpers.BindingHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
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

            // When the target location is set, finish drawing the edge
            newEdge.targetLocationProperty().addListener((obsTargetLocation, oldTargetLocation, newTargetLocation) -> {
                // TODO: Check if the source location is the same as the target location

                BindingHelper.bind(lines.get(lines.size() - 1), newEdge.getSourceLocation(), newTargetLocation);
            });

            // Remove all previous presentation elements from the root of the edge presentation
            while (edgeRoot.getChildren().size() > 0) {
                edgeRoot.getChildren().remove(0);
            }

            //
            component.addListener((obsComponent, oldComponent, newComponent) -> {
                if (newEdge.getNails().isEmpty()) {
                    final Line line = new Line();
                    lines.add(line);
                    BindingHelper.bind(line, newEdge.getSourceLocation(), newComponent.xProperty(), newComponent.yProperty());
                    edgeRoot.getChildren().add(line);
                }

                newEdge.getNails().addListener(new ListChangeListener<Nail>() {
                    @Override
                    public void onChanged(final Change<? extends Nail> change) {
                        while (change.next()) {
                            // There were added some nails
                            change.getAddedSubList().forEach(nail -> {
                                edgeRoot.getChildren().addAll(new NailPresentation(nail, newComponent));
                            });
                        }
                    }
                });

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
