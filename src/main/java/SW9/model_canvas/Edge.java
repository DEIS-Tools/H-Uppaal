package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.utility.BindingHelper;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class Edge extends Parent {

    private final Location sourceLocation;
    private Location targetLocation = null;

    private final ArrowHead arrowHead = new ArrowHead();
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
    private MouseTracker canvasMouseTracker = null;
    private final MouseTracker localMouseTracker = new MouseTracker(this);

    public Edge(final Location sourceLocation, final MouseTracker canvasMouseTracker) {
        this.sourceLocation = sourceLocation;
        this.canvasMouseTracker = canvasMouseTracker;

        getChildren().add(new ArrowHead());

        // Bind the lineCue from the source location to the mouse (will be rebound when nails are created)
        BindingHelper.bind(lineCue, sourceLocation, canvasMouseTracker);

        // Add the lineCue to the canvas and make it click-through
        lineCue.setMouseTransparent(true);
        getChildren().add(lineCue);

        getChildren().add(arrowHead);

        // Bind arrowhead to the line cue
        BindingHelper.bind(arrowHead, lineCue);

        this.canvasMouseTracker.registerOnMousePressedEventHandler(drawEdgeStepWhenCanvasPressed);

        // Add keybind to discard the nails and lines if ESC is pressed
        Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
            // Get the parent from the source location
            Pane parent = (Pane) this.getParent();
            if (parent == null) return;

            parent.getChildren().remove(this);
            this.canvasMouseTracker.unregisterOnMousePressedEventHandler(drawEdgeStepWhenCanvasPressed);
        });
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_EDGE, removeOnEscape);

        // Make nails visible when we hover the edge
        localMouseTracker.registerOnMouseEnteredEventHandler(event -> nails.forEach(nail -> nail.setVisible(true)));
        localMouseTracker.registerOnMouseExitedEventHandler(event -> nails.forEach(nail -> nail.setVisible(false)));
    }

    private final EventHandler<MouseEvent> drawEdgeStepWhenCanvasPressed = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (skipLine) {
                skipLine = false;
                return;
            } else if (Edge.this.targetLocation != null) {
                return;
            }

            // Create a new nail and a line that will connect to it
            final Nail nail = new Nail(lineCue.getEndX(), lineCue.getEndY());
            final Line line = new Line();
            line.setMouseTransparent(true);

            // If we are creating the first nail and line
            // Bind the line from the source location to the location we clicked
            // Bind the nail to the coordinates we clicked
            if (!ModelCanvas.mouseIsHoveringLocation() && linesIsEmpty.get() && nailsIsEmpty.get()) {
                BindingHelper.place(nail, event);
                BindingHelper.bind(line, sourceLocation, nail);
            }

            // If we are creating the n'th nail and line
            // Bind the line from the n-1 nail to the n nail
            // Bind the nail to the coordinates we clicked
            else if (!ModelCanvas.mouseIsHoveringLocation() && !linesIsEmpty.get() && !nailsIsEmpty.get()) {
                final Nail previousNail = nails.get(nails.size() - 1);

                BindingHelper.place(nail, event);
                BindingHelper.bind(line, previousNail, nail);
            }

            // We have at least one nail and one line, and are now finishing the edge by pressing a location
            // Bind the line from the last nail, to the target location
            else if (ModelCanvas.mouseIsHoveringLocation() && !nailsIsEmpty.get()) {
                Edge.this.targetLocation = ModelCanvas.getHoveredLocation();
                final Nail previousNail = nails.get(nails.size() - 1);

                BindingHelper.bind(line, previousNail, targetLocation);
            }

            // We have no nails, i.e. we are creating an edge directly from a source location to a target location
            else if (ModelCanvas.mouseIsHoveringLocation() && nailsIsEmpty.get()) {
                Edge.this.targetLocation = ModelCanvas.getHoveredLocation();

                // If the target location is the same as the source, add some nails to make the view readable
                if (sourceLocation.equals(targetLocation)) {
                    System.out.println("Den samme øv bøv");
                } else {
                    BindingHelper.bind(line, sourceLocation, targetLocation);
                }
            }

            // We did finish the edge by pressing a location, add the new nail
            if (Edge.this.targetLocation == null) {
                Edge.this.getChildren().add(nail);
                nails.add(nail);
            }
            // We did finish the edge, remove the cue and move the arrow head
            else {
                Edge.this.getChildren().remove(lineCue);
                BindingHelper.bind(arrowHead, line);

                // We no longer wish to discard the edge when pressing the esc button
                KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_EDGE);

                // Make all lines visible to the mouse (so that we can show nails when we hover the lines)
                lines.forEach(l -> l.setMouseTransparent(false));
                line.setMouseTransparent(false);
            }

            BindingHelper.bind(lineCue, nail, canvasMouseTracker);

            // Add the line and nail to the canvas
            Edge.this.getChildren().add(line);
            lines.add(line);
        }
    };

}
