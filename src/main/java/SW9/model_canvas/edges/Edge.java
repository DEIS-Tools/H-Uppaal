package SW9.model_canvas.edges;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.model_canvas.IParent;
import SW9.model_canvas.ModelCanvas;
import SW9.model_canvas.arrow_heads.SimpleArrow;
import SW9.model_canvas.locations.Location;
import SW9.utility.BindingHelper;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;

public class Edge extends Parent {

    private final Location sourceLocation;
    private Location targetLocation = null;

    private final SimpleArrow simpleArrow = new SimpleArrow();

    private Line lineCue = new Line();

    private boolean skipLine = true;

    ObservableList<Link> links = FXCollections.observableArrayList();
    private BooleanBinding linesIsEmpty = new BooleanBinding() {
        {
            super.bind(links);
        }

        @Override
        protected boolean computeValue() {
            return links.isEmpty();
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
        ModelCanvas.setEdgeBeingDrawn(this);

        // Make the edge click-through until it is placed
        this.setMouseTransparent(true);

        getChildren().add(new SimpleArrow());

        // Bind the lineCue from the source location to the mouse (will be rebound when nails are created)
        BindingHelper.bind(lineCue, sourceLocation.circle, canvasMouseTracker);

        // Add the lineCue to the canvas
        getChildren().add(lineCue);

        getChildren().add(simpleArrow);

        // Bind arrowhead to the line cue
        BindingHelper.bind(simpleArrow, lineCue);

        this.canvasMouseTracker.registerOnMousePressedEventHandler(drawEdgeStepWhenCanvasPressed);

        // Add keybind to discard the nails and links if ESC is pressed
        Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
            // Get the parent from the source location
            IParent parent = (IParent) this.getParent();
            if (parent == null) return;

            // Remove this edge from the canvas and unregister its draw ednge handler
            parent.removeChild(this);
            this.canvasMouseTracker.unregisterOnMousePressedEventHandler(drawEdgeStepWhenCanvasPressed);

            // Tell the canvas that this edge is no longer being drawn
            ModelCanvas.setEdgeBeingDrawn(null);
        });
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_EDGE, removeOnEscape);

        // Make nails visible when we hover the edge
        localMouseTracker.registerOnMouseEnteredEventHandler(event -> nails.forEach(nail -> nail.setVisible(true)));

        // If no nail is being dragged and we are not hovering the edge, make the nails invisible
        localMouseTracker.registerOnMouseExitedEventHandler(event -> {
            for (Nail nail : nails) {
                if (nail.isBeingDragged) return;
            }
            nails.forEach(nail -> nail.setVisible(false));
        });
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

            // Create a new nail and a link that will connect to it
            final Nail nail = new Nail(canvasMouseTracker.getXProperty().get(), canvasMouseTracker.getYProperty().get());
            final Link link = new Link();

            // If we are creating the first nail and link
            // Bind the link from the source location to the location we clicked
            // Bind the nail to the coordinates we clicked
            if (!ModelCanvas.mouseIsHoveringLocation() && linesIsEmpty.get() && nailsIsEmpty.get()) {
                BindingHelper.bind(link.line, sourceLocation.circle, nail);
            }

            // If we are creating the n'th nail and link
            // Bind the link from the n-1 nail to the n nail
            // Bind the nail to the coordinates we clicked
            else if (!ModelCanvas.mouseIsHoveringLocation() && !linesIsEmpty.get() && !nailsIsEmpty.get()) {
                final Nail previousNail = nails.get(nails.size() - 1);

                BindingHelper.bind(link.line, previousNail, nail);
            }

            // We have at least one nail and one link, and are now finishing the edge by pressing a location
            // Bind the link from the last nail, to the target location
            else if (ModelCanvas.mouseIsHoveringLocation() && !nailsIsEmpty.get()) {
                Edge.this.targetLocation = ModelCanvas.getHoveredLocation();
                final Nail previousNail = nails.get(nails.size() - 1);

                BindingHelper.bind(link.line, previousNail, targetLocation.circle);
            }

            // We have no nails, i.e. we are creating an edge directly from a source location to a target location
            else if (ModelCanvas.mouseIsHoveringLocation() && nailsIsEmpty.get()) {
                Edge.this.targetLocation = ModelCanvas.getHoveredLocation();

                // If the target location is the same as the source, add some nails to make the view readable
                if (sourceLocation.equals(targetLocation)) {

                    targetLocation = sourceLocation;
                    // Create two nails outside the source locations
                    Nail firstNail = new Nail(sourceLocation.circle.getCenterX() + Location.RADIUS * 3, sourceLocation.circle.getCenterY());
                    Nail secondNail = new Nail(sourceLocation.circle.getCenterX(), sourceLocation.circle.getCenterY() + Location.RADIUS * 3);

                    // Create two links for connecting the edge (the link created before is the third link in the chain)
                    Link firstLink = new Link();
                    Link secondLink = new Link();

                    // Add them to the view
                    Edge.this.getChildren().addAll(firstLink, secondLink, firstNail, secondNail);

                    // Add links and edges to the collections
                    links.addAll(firstLink, secondLink);
                    nails.addAll(firstNail, secondNail);

                    // Bind the links between the nails and source locations
                    BindingHelper.bind(firstLink.line, sourceLocation.circle, firstNail);
                    BindingHelper.bind(secondLink.line, firstNail, secondNail);
                    BindingHelper.bind(link.line, secondNail, targetLocation.circle);

                } else {
                    BindingHelper.bind(link.line, sourceLocation.circle, targetLocation.circle);
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
                BindingHelper.bind(simpleArrow, link.line);

                // We no longer wish to discard the edge when pressing the esc button
                KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_EDGE);

                // Make the edge visible to the mouse (so that we can show nails when we hover the links)
                Edge.this.setMouseTransparent(false);

                // Tell the canvas that this edge is no longer being drawn
                ModelCanvas.setEdgeBeingDrawn(null);
            }

            BindingHelper.bind(lineCue, nail, canvasMouseTracker);

            // Add the link and nail to the canvas
            Edge.this.getChildren().add(0, link);
            links.add(link);
        }
    };

}
