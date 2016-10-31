package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.model_canvas.arrow_heads.SimpleArrowHead;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.NailPresentation;
import SW9.utility.helpers.BindingHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EdgeController implements Initializable {

    private final ArrayList<Line> lines = new ArrayList<>();
    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    private final SimpleArrowHead simpleArrowHead = new SimpleArrowHead();
    public Pane edgeRoot;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        edge.addListener((obsEdge, oldEdge, newEdge) -> {

            // When the target location is set, finish drawing the edge
            newEdge.targetLocationProperty().addListener((obsTargetLocation, oldTargetLocation, newTargetLocation) -> {
                // TODO: Check if the source location is the same as the target location

                // If the nails list is empty, directly connect the source and target locations
                // otherwise, bind the line from the last nail to the target location
                final Line lastLine = lines.get(lines.size() - 1);
                final ObservableList<Nail> nails = getEdge().getNails();
                if (nails.size() == 0) {
                    // Check if the source and target locations are the same, if they are, add two new helper nails
                    if (newEdge.getSourceLocation().equals(newTargetLocation)) {
                        final Nail nail1 = new Nail(newTargetLocation.xProperty().add(5 * CanvasPresentation.GRID_SIZE), newTargetLocation.yProperty().add(3 * CanvasPresentation.GRID_SIZE));
                        final Nail nail2 = new Nail(newTargetLocation.xProperty().add(3 * CanvasPresentation.GRID_SIZE), newTargetLocation.yProperty().add(5 * CanvasPresentation.GRID_SIZE));

                        // Add the nails to the nails collection (will draw lines between them)
                        nails.addAll(nail1, nail2);

                        // Find the new last line (updated by adding nails to the collection) and bind it from the last nail to the target location
                        final Line newLastLine = lines.get(lines.size() - 1);
                        BindingHelper.bind(newLastLine, simpleArrowHead, nail2, newTargetLocation);
                    } else {
                        BindingHelper.bind(lastLine, simpleArrowHead, newEdge.getSourceLocation(), newTargetLocation);
                    }
                } else {
                    final Nail lastNail = nails.get(nails.size() - 1);
                    BindingHelper.bind(lastLine, simpleArrowHead, lastNail, newTargetLocation);
                }
            });

            component.addListener((obsComponent, oldComponent, newComponent) -> {
                if (newEdge.getNails().isEmpty()) {
                    final Line line = new Line();
                    lines.add(line);

                    // Add the line and its arrowhead to the view
                    edgeRoot.getChildren().addAll(line, simpleArrowHead);

                    // Bind the first line and the arrowhead from the source location to the mouse
                    BindingHelper.bind(line, simpleArrowHead, newEdge.getSourceLocation(), newComponent.xProperty(), newComponent.yProperty());
                }

                // Changes are made to the nails list
                newEdge.getNails().addListener(new ListChangeListener<Nail>() {
                    @Override
                    public void onChanged(final Change<? extends Nail> change) {
                        while (change.next()) {
                            // There were added some nails
                            change.getAddedSubList().forEach(nail -> {
                                // Create a new nail presentation based on the abstraction added to the list
                                final NailPresentation newNail = new NailPresentation(nail, newComponent);
                                edgeRoot.getChildren().addAll(newNail);

                                // The previous last line must end in the new nail
                                final Line lastLine = lines.get(lines.size() - 1);

                                // If the nail is the first in the list, bind it to the source location
                                // otherwise, bind it the the previous nail
                                final int nailIndex = edge.get().getNails().indexOf(nail);
                                if (nailIndex == 0) {
                                    BindingHelper.bind(lastLine, newEdge.getSourceLocation(), nail);
                                } else {
                                    final Nail previousNail = edge.get().getNails().get(nailIndex - 1);
                                    BindingHelper.bind(lastLine, previousNail, nail);
                                }

                                // Create a new line that will bind from the new nail to the mouse
                                final Line newLine = new Line();
                                lines.add(newLine);
                                BindingHelper.bind(newLine, simpleArrowHead, nail, newComponent.xProperty(), newComponent.yProperty());
                                edgeRoot.getChildren().add(newLine);
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
