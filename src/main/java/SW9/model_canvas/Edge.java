package SW9.model_canvas;

import SW9.MouseTracker;
import SW9.utility.BindingHelper;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Pair;
import org.apache.xpath.operations.Mod;

import java.util.ArrayList;
import java.util.List;

public class Edge {

    private final Location sourceLocation;
    private Location targetLocation = null;

    private final Line arrowHeadLeft = new Line();
    private final Line arrowHeadRight = new Line();
    private static final double ARROW_HEAD_ANGLE = 15;
    private static final double ARROW_HEAD_LENGTH = 15;

    private Line lineCue = new Line();

    private boolean skipLine = true;

    ObservableList<Line> lines = FXCollections.observableArrayList();
    private BooleanBinding linesIsEmpty = new BooleanBinding() {
        {
            super.bind(lines);
        }

        @Override
        protected boolean computeValue() {
            return lines.isEmpty();
        }
    };

    ObservableList<Nail> nails = FXCollections.observableArrayList();
    private BooleanBinding nailsIsEmpty = new BooleanBinding() {
        {
            super.bind(nails);
        }

        @Override
        protected boolean computeValue() {
            return nails.isEmpty();
        }
    };


    // Mouse trackers
    private final MouseTracker canvasMouseTracker;

    public Edge(final Location sourceLocation, final MouseTracker canvasMouseTracker) {
        this.sourceLocation = sourceLocation;
        this.canvasMouseTracker = canvasMouseTracker;

        // Bind the lineCue from the source location to the mouse (will be rebound when nails are created)
        BindingHelper.bind(lineCue, sourceLocation, canvasMouseTracker);

        // Add the lineCue to the canvas and make it click-through
        lineCue.setMouseTransparent(true);
        addChildToParent(lineCue);

        // Add the arrowhead to the canvas
        arrowHeadRight.setMouseTransparent(true);
        arrowHeadLeft.setMouseTransparent(true);
        addChildToParent(arrowHeadRight);
        addChildToParent(arrowHeadLeft);

        // Bind arrowhead to the line cue
        bindArrowHeadToLine(lineCue);

        this.canvasMouseTracker.registerOnMousePressedEventHandler(event -> {
            if (skipLine) {
                skipLine = false;
                return;
            } else if (this.targetLocation != null) {
                return;
            }

            // Create a new nail and a line that will connect to it
            final Nail nail = new Nail(lineCue.getEndX(), lineCue.getEndY());
            nail.setMouseTransparent(true);
            final Line line = new Line();
            line.setMouseTransparent(true);

            // If we are creating the first nail and line
            // Bind the line from the source location to the location we clicked
            // Bind the nail to the coordinates we clicked
            if(!ModelCanvas.mouseIsHoveringLocation() && linesIsEmpty.get() && nailsIsEmpty.get()) {
                BindingHelper.bind(nail, event);
                BindingHelper.bind(line, sourceLocation, nail);
            }

            // If we are creating the n'th nail and line
            // Bind the line from the n-1 nail to the n nail
            // Bind the nail to the coordinates we clicked
            else if(!ModelCanvas.mouseIsHoveringLocation() && !linesIsEmpty.get() && !nailsIsEmpty.get()) {
                final Nail previousNail = nails.get(nails.size() - 1);

                BindingHelper.bind(nail, event);
                BindingHelper.bind(line, previousNail, nail);
            }

            // We have at least one nail and one line, and are now finishing the edge by pressing a location
            // Bind the line from the last nail, to the target location
            else if(ModelCanvas.mouseIsHoveringLocation() && !nailsIsEmpty.get()) {
                this.targetLocation = ModelCanvas.getHoveredLocation();
                final Nail previousNail = nails.get(nails.size() - 1);

                BindingHelper.bind(line, previousNail, targetLocation);
            }

            // We have no nails, i.e. we are creating an edge directly from a source location to a target location
            else if(ModelCanvas.mouseIsHoveringLocation() && nailsIsEmpty.get()) {
                this.targetLocation = ModelCanvas.getHoveredLocation();

                // If the target location is the same as the source, add some nails to make the view readable
                if(sourceLocation.equals(targetLocation)) {
                    System.out.println("data");
                } else {
                    BindingHelper.bind(line, sourceLocation, targetLocation);
                }
            }

            // We did finish the edge by pressing a location, add the new nail
            if(this.targetLocation == null) {
                addChildToParent(nail);
                nails.add(nail);
            }
            // We did finish the edge, remove the cue and move the arrow head
            else {
                removeChildFromParent(lineCue);
                bindArrowHeadToLine(line);
            }

            BindingHelper.bind(lineCue, nail, canvasMouseTracker);

            // Add the line and nail to the canvas
            addChildToParent(line);
            lines.add(line);
        });
    }

    private void removeChildFromParent(final Node node) {
        // Get the parent from the source location
        Pane parent = (Pane) this.sourceLocation.getParent();

        if (parent == null) return;

        parent.getChildren().remove(node);
    }

    private void addChildToParent(final Node node) {

        // Get the parent from the source location
        Pane parent = (Pane) this.sourceLocation.getParent();

        if (parent == null) return;

        parent.getChildren().add(node);
    }

    private void bindArrowHeadToLine(final Line line) {

        DoubleProperty startX = line.startXProperty();
        DoubleProperty startY = line.startYProperty();
        DoubleProperty endX = line.endXProperty();
        DoubleProperty endY = line.endYProperty();

        DoubleBinding arrowHeadLeftX = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) + Math.toRadians(ARROW_HEAD_ANGLE);
                return endX.get() + Math.cos(angle) * ARROW_HEAD_LENGTH;
            }
        };

        DoubleBinding arrowHeadLeftY = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) + Math.toRadians(ARROW_HEAD_ANGLE);
                return endY.get() + Math.sin(angle) * ARROW_HEAD_LENGTH;
            }
        };

        DoubleBinding arrowHeadRightX = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) - Math.toRadians(ARROW_HEAD_ANGLE);
                return endX.get() + Math.cos(angle) * ARROW_HEAD_LENGTH;
            }
        };

        DoubleBinding arrowHeadRightY = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) - Math.toRadians(ARROW_HEAD_ANGLE);
                return endY.get() + Math.sin(angle) * ARROW_HEAD_LENGTH;
            }
        };

        arrowHeadLeft.startXProperty().bind(arrowHeadLeftX);
        arrowHeadLeft.startYProperty().bind(arrowHeadLeftY);
        arrowHeadLeft.endXProperty().bind(endX);
        arrowHeadLeft.endYProperty().bind(endY);

        arrowHeadRight.startXProperty().bind(arrowHeadRightX);
        arrowHeadRight.startYProperty().bind(arrowHeadRightY);
        arrowHeadRight.endXProperty().bind(endX);
        arrowHeadRight.endYProperty().bind(endY);


    }
}
