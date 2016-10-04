package SW9.model_canvas.edges;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.model_canvas.IParent;
import SW9.model_canvas.ModelCanvas;
import SW9.model_canvas.Removable;
import SW9.model_canvas.arrow_heads.SimpleArrowHead;
import SW9.model_canvas.locations.Location;
import SW9.utility.BindingHelper;
import SW9.utility.DragHelper;
import SW9.utility.SelectHelper;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;

public class Edge extends Parent implements Removable {

    private final Location sourceLocation;
    private Location targetLocation = null;
    public BooleanProperty targetLocationIsSet = new SimpleBooleanProperty(false);

    private final SimpleArrowHead arrowHead = new SimpleArrowHead();

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

        lineCue.getStyleClass().add("link");

        // Bind the lineCue from the source location to the mouse (will be rebound when nails are created)
        BindingHelper.bind(lineCue, sourceLocation.circle, canvasMouseTracker);
        // Bind arrowhead to the mouse
        BindingHelper.bind(arrowHead, sourceLocation.circle, canvasMouseTracker);
        // Bind the lineCue to the arrowhead
        BindingHelper.bind(lineCue, arrowHead);

        // Add the lineCue to the canvas
        getChildren().add(lineCue);
        getChildren().add(arrowHead);

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
            // Do not turn the nails invisible if we are selected
            if(SelectHelper.isSelected(this)) return;

            for (Nail nail : nails) {
                if (nail.isBeingDragged) return;
            }
            nails.forEach(nail -> nail.setVisible(false));
        });
    }

    public Location getSourceLocation() {
        return sourceLocation;
    }

    private final EventHandler<MouseEvent> drawEdgeStepWhenCanvasPressed = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (skipLine) {
                skipLine = false;
                return;
            } else if (getTargetLocation() != null) {
                return;
            }

            // Create a new nail and a link that will connect to it
            final DragHelper.Draggable parent = (DragHelper.Draggable) getParent();

            final Nail nail = new Nail(parent.xProperty().add(canvasMouseTracker.getXProperty().get() - parent.xProperty().get()), parent.yProperty().add(canvasMouseTracker.getYProperty().get() - parent.yProperty().get()));
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
                setTargetLocation(ModelCanvas.getHoveredLocation());
                final Nail previousNail = nails.get(nails.size() - 1);

                BindingHelper.bind(link.line, previousNail, getTargetLocation().circle);
                BindingHelper.bind(arrowHead, previousNail, getTargetLocation().circle);
                BindingHelper.bind(link.line, arrowHead);
            }

            // We have no nails, i.e. we are creating an edge directly from a source location to a target location
            else if (ModelCanvas.mouseIsHoveringLocation() && nailsIsEmpty.get()) {
                setTargetLocation(ModelCanvas.getHoveredLocation());

                // If the target location is the same as the source, add some nails to make the view readable
                if (sourceLocation.equals(getTargetLocation())) {

                    setTargetLocation(sourceLocation);
                    // Create two nails outside the source locations
                    Nail firstNail = new Nail(sourceLocation.circle.centerXProperty().add(Location.RADIUS * 3), sourceLocation.circle.centerYProperty());
                    Nail secondNail = new Nail(sourceLocation.circle.centerXProperty(), sourceLocation.circle.centerYProperty().add(Location.RADIUS * 3));

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
                    BindingHelper.bind(link.line, secondNail, getTargetLocation().circle);
                    BindingHelper.bind(arrowHead, secondNail, getTargetLocation().circle);
                    BindingHelper.bind(link.line, arrowHead);

                } else {
                    BindingHelper.bind(link.line, sourceLocation.circle, getTargetLocation().circle);
                    BindingHelper.bind(arrowHead, sourceLocation.circle, getTargetLocation().circle);
                    BindingHelper.bind(link.line, arrowHead);
                }
            }

            // We did finish the edge by pressing a location, add the new nail
            if (getTargetLocation() == null) {
                Edge.this.getChildren().add(nail);
                nails.add(nail);
            }
            // We did finish the edge, remove the cue and bind thee last link and arrow head accordingly
            else {
                Edge.this.getChildren().remove(lineCue);

                // We no longer wish to discard the edge when pressing the esc button
                KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_EDGE);

                // Make the edge visible to the mouse (so that we can show nails when we hover the links)
                Edge.this.setMouseTransparent(false);

                // Tell the canvas that this edge is no longer being drawn
                ModelCanvas.setEdgeBeingDrawn(null);
            }

            // If the line cue is present rebind it and line cue to start from newest nail
            if (getChildren().contains(lineCue)) {

                BindingHelper.bind(lineCue, nail, canvasMouseTracker);
                BindingHelper.bind(arrowHead, nail, canvasMouseTracker);
                BindingHelper.bind(lineCue, arrowHead);
            }

            // Add the link and nail to the canvas
            Edge.this.getChildren().add(0, link);
            links.add(link);
        }
    };

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(final Location targetLocation) {
        this.targetLocation = targetLocation;

        SelectHelper.makeSelectable(this);

        targetLocationIsSet.setValue(targetLocation != null);
    }

    @Override
    public IParent getIParent() {
        return sourceLocation.getIParent();
    }

    @Override
    public boolean select() {
        if(!targetLocationIsSet.get()) return false;

        nails.forEach(nail -> nail.getStyleClass().add("selected"));
        links.forEach(link -> link.line.getStyleClass().add("selected"));
        lineCue.getStyleClass().add("selected");
        arrowHead.mark();

        // Make nails visible
        nails.forEach(nail -> nail.setVisible(true));

        return true;
    }

    @Override
    public void deselect() {
        nails.forEach(nail -> nail.getStyleClass().remove("selected"));
        links.forEach(link -> link.line.getStyleClass().remove("selected"));
        lineCue.getStyleClass().remove("selected");
        arrowHead.unmark();

        // Make nails invisible
        nails.forEach(nail -> nail.setVisible(false));
    }

    @Override
    public void remove() {
        sourceLocation.getModelContainer().remove(this);
    }

    @Override
    public void reAdd() {
        sourceLocation.getModelContainer().add(this);
    }

    @Override
    public MouseTracker getMouseTracker() {
        return localMouseTracker;
    }

    @Override
    public DoubleProperty xProperty() {
        return sourceLocation.getModelContainer().xProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return sourceLocation.getModelContainer().yProperty();
    }
}
