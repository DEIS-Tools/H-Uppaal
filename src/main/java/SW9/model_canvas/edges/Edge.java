package SW9.model_canvas.edges;

import SW9.model_canvas.IParent;
import SW9.model_canvas.ModelCanvas;
import SW9.model_canvas.Parent;
import SW9.model_canvas.Removable;
import SW9.model_canvas.arrow_heads.SimpleArrowHead;
import SW9.model_canvas.locations.Location;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.LocationAware;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class Edge extends Parent implements Removable, Colorable {

    private Color color = null;
    private Color.Intensity intensity = null;
    private boolean colorIsSet = false;

    private final Location sourceLocation;
    private Location targetLocation = null;
    public final BooleanProperty targetLocationIsSet = new SimpleBooleanProperty(false);

    private final SimpleArrowHead arrowHead = new SimpleArrowHead();
    private final Line lineCue = new Line();
    private Nail hoveredNail;

    private boolean skipLine = true;

    final ObservableList<Link> links = FXCollections.observableArrayList();
    private final BooleanBinding linesIsEmpty = new BooleanBinding() {
        {
            super.bind(links);
        }

        @Override
        protected boolean computeValue() {
            return links.isEmpty();
        }
    };

    final ObservableList<Nail> nails = FXCollections.observableArrayList();
    private final BooleanBinding nailsIsEmpty = new BooleanBinding() {
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

        // Bind the lineCue with arrow head from the source location to the mouse (will be rebound when nails are created)
        BindingHelper.bind(lineCue, arrowHead, sourceLocation.circle, canvasMouseTracker);

        // Add the lineCue to the canvas
        getChildren().add(lineCue);
        getChildren().add(arrowHead);

        this.canvasMouseTracker.registerOnMousePressedEventHandler(drawEdgeStepWhenCanvasPressed);

        // Add keybind to discard the nails and links if ESC is pressed
        Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
            // Get the parent from the source location
            IParent parent = (IParent) this.getParent();
            if (parent == null) return;

            // Remove this edge from the canvas and unregister its draw edge handler
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
            if (SelectHelper.isSelected(this)) return;

            for (Nail nail : nails) {
                if (nail.isBeingDragged) return;
            }
            nails.forEach(nail -> nail.setVisible(false));
        });

        // Will color the edge (and its nails)
        resetColor();

        nails.addListener(new ListChangeListener<Nail>() {
            @Override
            public void onChanged(final Change<? extends Nail> c) {
                resetColor(getColor(), getColorIntensity());
            }
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
            final LocationAware parent = (LocationAware) getParent();

            final Nail nail = new Nail(parent.xProperty().add(canvasMouseTracker.xProperty().get() - parent.xProperty().get()), parent.yProperty().add(canvasMouseTracker.yProperty().get() - parent.yProperty().get()));

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

                BindingHelper.bind(link.line, arrowHead, previousNail, getTargetLocation().circle);
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
                    BindingHelper.bind(link.line, arrowHead, secondNail, getTargetLocation().circle);

                } else {
                    BindingHelper.bind(link.line, arrowHead, sourceLocation.circle, getTargetLocation().circle);
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
                BindingHelper.bind(lineCue, arrowHead, nail, canvasMouseTracker);
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
        if (this.targetLocation == null) {
            SelectHelper.makeSelectable(this);
        }

        this.targetLocation = targetLocation;
        targetLocationIsSet.setValue(targetLocation != null);
    }

    public void remove(final Nail nail) {
        final int indexOfNail = nails.indexOf(nail);
        nail.restoreIndex = indexOfNail;
        final Link a = links.get(indexOfNail);
        final Link b = links.get(indexOfNail + 1);
        final Circle startCircle;
        final Circle endCircle;

        if (indexOfNail == 0) {
            startCircle = sourceLocation.circle;
        } else {
            startCircle = nails.get(indexOfNail - 1);
        }

        if (indexOfNail == nails.size() - 1) {
            endCircle = targetLocation.circle;
        } else {
            endCircle = nails.get(indexOfNail + 1);
        }

        BindingHelper.bind(b.line, startCircle, endCircle);

        removeChild(nail);
        nails.remove(nail);
        removeChild(a);
        links.remove(a);

        if (nails.size() == 0) {
            // TODO lave en snub
            BindingHelper.bind(links.get(0).line, arrowHead, sourceLocation.circle, targetLocation.circle);
        } else {
            BindingHelper.bind(links.get(links.size() - 1).line, arrowHead, nails.get(nails.size() - 1), targetLocation.circle);
        }
    }

    public void add(final Nail nail, final int position) {
        nails.add(position, nail);

        final Link newLink = new Link();
        links.add(position, newLink);

        final Circle startCircle;
        final Circle endCircle;

        if (position == 0) {
            startCircle = sourceLocation.circle;
        } else {
            startCircle = nails.get(position - 1);
        }

        if (position == nails.size() - 1) {
            endCircle = targetLocation.circle;
        } else {
            endCircle = nails.get(position + 1);
        }

        addChildren(nail, newLink);

        BindingHelper.bind(newLink.line, startCircle, nail);
        BindingHelper.bind(links.get(position + 1).line, nail, endCircle);

        if (position == nails.size() - 1) {
            BindingHelper.bind(links.get(position + 1).line, arrowHead, nail, endCircle);
        }
    }

    @Override
    public boolean select() {
        if (!targetLocationIsSet.get()) return false;

        if (getHoveredNail() != null) {
            SelectHelper.select(getHoveredNail());

            // Make nails visible
            nails.forEach(nail -> nail.setVisible(true));

            return false; // Do not add us to the list of selected elements
        } else {
            nails.forEach(nail -> nail.getStyleClass().add("selected"));
            links.forEach(link -> link.line.getStyleClass().add("selected"));
            lineCue.getStyleClass().add("selected");
            arrowHead.mark();

            // Make nails visible
            nails.forEach(nail -> nail.setVisible(true));
        }

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

    public Nail getHoveredNail() {
        return hoveredNail;
    }

    public void setHoveredNail(Nail hoveredNail) {
        this.hoveredNail = hoveredNail;
    }

    @Override
    public boolean isColored() {
        return colorIsSet;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return intensity;
    }

    @Override
    public boolean color(final Color color, final Color.Intensity intensity) {
        colorIsSet = true;

        this.color = color;
        this.intensity = intensity;

        // Color all nails
        nails.forEach(nail -> {
            nail.setFill(color.getColor(intensity));
            nail.setStroke(color.getColor(intensity.next(2)));
        });

        return true;
    }

    @Override
    public void resetColor() {
        resetColor(Color.GREY_BLUE, Color.Intensity.I700); // default color
    }

    @Override
    public void resetColor(final Color color, final Color.Intensity intensity) {
        color(color, intensity);
        colorIsSet = false;
    }
}
