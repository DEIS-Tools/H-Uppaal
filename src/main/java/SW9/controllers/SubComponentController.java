package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Jork;
import SW9.abstractions.SubComponent;
import SW9.code_analysis.CodeAnalysis;
import SW9.code_analysis.Nearable;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.LocationPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.NailHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SubComponentController implements Initializable, SelectHelper.ColorSelectable {

    private static final Map<SubComponent, Boolean> initializedInconsistentEdgeError = new HashMap<>();

    private final ObjectProperty<SubComponent> subComponent = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Component> parentComponent = new SimpleObjectProperty<>(null);

    public BorderPane toolbar;
    public Rectangle background;
    public BorderPane frame;
    public JFXTextField identifier;
    public Label originalComponent;
    public StackPane root;
    public Line line1;
    public Line line2;
    public Pane defaultLocationsContainer;
    public Label description;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        makeDraggable();

        initializeSelectListener();
    }

    private void initializeSelectListener() {
        SelectHelper.elementsToBeSelected.addListener(new ListChangeListener<Nearable>() {
            @Override
            public void onChanged(final Change<? extends Nearable> c) {
                while (c.next()) {
                    if (c.getAddedSize() == 0) return;

                    for (final Nearable nearable : SelectHelper.elementsToBeSelected) {
                        if (nearable instanceof SubComponent) {
                            if (nearable.equals(getSubComponent())) {
                                SelectHelper.addToSelection(SubComponentController.this);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void initializeInconsistentEdgeError() {
        if (initializedInconsistentEdgeError.containsKey(getSubComponent())) return; // Already initialized
        initializedInconsistentEdgeError.put(getSubComponent(), true); // Set initialized

        final CodeAnalysis.Message onlyOneTypeOfStarters = new CodeAnalysis.Message("Sub components can not be started both in parallel and sequentially", CodeAnalysis.MessageType.ERROR, getSubComponent());
        final CodeAnalysis.Message onlyOneFork = new CodeAnalysis.Message("Sub components can only be started by once, and only by a single fork", CodeAnalysis.MessageType.ERROR, getSubComponent());

        final CodeAnalysis.Message onlyOneTypeOfFinishers = new CodeAnalysis.Message("Sub components can not end in both a join and locations", CodeAnalysis.MessageType.ERROR, getSubComponent());
        final CodeAnalysis.Message onlyOneJoin = new CodeAnalysis.Message("Sub components can only be joined once, and only by a single join", CodeAnalysis.MessageType.ERROR, getSubComponent());


        final Consumer<SubComponent> checkForInconsistentIncoming = (subComponent) -> {
            if (subComponent != null) { // The subComponent is not null

                // Get all incoming edges for the sub component
                final List<Edge> incomingEdges = getParentComponent().getIncomingEdges(subComponent);

                // Count the amount of forks to this sub component
                int forks = 0;
                for (final Edge edge : incomingEdges) {
                    if (edge.getSourceJork() != null && edge.getSourceJork().getType().equals(Jork.Type.FORK)) {
                        forks++;
                    }
                }

                // If the component is started by multiple forks
                if (forks > 1) {
                    // Add the message to the UI
                    CodeAnalysis.addMessage(getParentComponent(), onlyOneFork);
                } else {
                    // Remove the message from the UI
                    CodeAnalysis.removeMessage(getParentComponent(), onlyOneFork);
                }

                // If there are inconsistent edges (eg from fork and a location)
                if (incomingEdges.size() > forks && forks != 0) {
                    // Add the message to the UI
                    CodeAnalysis.addMessage(getParentComponent(), onlyOneTypeOfStarters);
                } else {
                    // Remove the message from the UI
                    CodeAnalysis.removeMessage(getParentComponent(), onlyOneTypeOfStarters);
                }

            } else {
                // Remove the messages
                CodeAnalysis.removeMessage(getParentComponent(), onlyOneTypeOfStarters);
                CodeAnalysis.removeMessage(getParentComponent(), onlyOneFork);
            }
        };

        final Consumer<SubComponent> checkForInconsistentOutgoingEdges = (subComponent) -> {
            if (subComponent != null) { // The subComponent is not null

                // Get all outgoing edges for the sub component
                final List<Edge> outGoingEdges = getParentComponent().getOutGoingEdges(subComponent);

                // Count the amount of joins to this sub component
                int joins = 0;
                for (final Edge edge : outGoingEdges) {
                    if (edge.getTargetJork() != null && edge.getTargetJork().getType().equals(Jork.Type.JOIN)) {
                        joins++;
                    }
                }

                // If the component is started by multiple joins
                if (joins > 1) {
                    // Add the message to the UI
                    CodeAnalysis.addMessage(getParentComponent(), onlyOneJoin);
                } else {
                    // Remove the message from the UI
                    CodeAnalysis.removeMessage(getParentComponent(), onlyOneJoin);
                }

                // If there are inconsistent edges (eg to join and a location)
                if (outGoingEdges.size() > joins && joins != 0) {
                    // Add the message to the UI
                    CodeAnalysis.addMessage(getParentComponent(), onlyOneTypeOfFinishers);
                } else {
                    // Remove the message from the UI
                    CodeAnalysis.removeMessage(getParentComponent(), onlyOneTypeOfFinishers);
                }

            } else {
                // Remove the messages
                CodeAnalysis.removeMessage(getParentComponent(), onlyOneTypeOfFinishers);
                CodeAnalysis.removeMessage(getParentComponent(), onlyOneJoin);
            }
        };

        final Consumer<SubComponent> checkIfErrorIsPresent = (subComponent) -> {
            checkForInconsistentIncoming.accept(subComponent);
            checkForInconsistentOutgoingEdges.accept(subComponent);
        };

        // When the list of edges are updated
        final InvalidationListener listener = observable -> checkIfErrorIsPresent.accept(getSubComponent());
        getParentComponent().getEdges().addListener(listener);

        // Check if the error is present right now
        checkIfErrorIsPresent.accept(getSubComponent());
    }

    private void makeDraggable() {

        final DoubleProperty mouseXDiff = new SimpleDoubleProperty(0);
        final DoubleProperty mouseYDiff = new SimpleDoubleProperty(0);

        final Consumer<MouseEvent> startEdgeFromSubComponent = (event) -> {

            // Store where in the subcomponent the mouse is dragging
            mouseXDiff.set(event.getX());
            mouseYDiff.set(event.getY());

            event.consume();

            final Edge unfinishedEdge = getParentComponent().getUnfinishedEdge();

            if (unfinishedEdge != null) {
                unfinishedEdge.setTargetSubComponent(getSubComponent());
                NailHelper.addMissingNails(unfinishedEdge);
            } else if (event.isShiftDown()) {
                event.consume();
                final Edge newEdge = new Edge(getSubComponent());

                KeyboardTracker.registerKeybind(KeyboardTracker.ABANDON_EDGE, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                    getParentComponent().removeEdge(newEdge);
                    UndoRedoStack.forget();
                }));

                UndoRedoStack.push(() -> { // Perform
                    getParentComponent().addEdge(newEdge);
                }, () -> { // Undo
                    getParentComponent().removeEdge(newEdge);
                }, "Created edge starting from sub component " + getSubComponent().getIdentifier(), "add-circle");
            } else {
                // If the sub component is pressed twice open its corresponding component in the canvas
                if(event.getClickCount() > 1) {
                    CanvasController.setActiveComponent(getSubComponent().getComponent());
                } else {
                    SelectHelper.select(this);
                }
            }
        };

        final Supplier<Double> supplyX = () -> {
            // Calculate the potential new x alongside min and max values
            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getParentComponent().xProperty()).get();
            final double minX = mouseXDiff.get() + CanvasPresentation.GRID_SIZE;
            final double maxX = getParentComponent().getWidth() - getSubComponent().getWidth() - CanvasPresentation.GRID_SIZE + mouseXDiff.get();

            // Drag according to min and max
            if (newX < minX) {
                return minX;
            } else if (newX > maxX) {
                return maxX;
            } else {
                return newX;
            }
        };

        final Supplier<Double> supplyY = () -> {
            // Calculate the potential new y alongside min and max values
            final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getParentComponent().yProperty()).doubleValue();
            final double minY = mouseYDiff.get() + ComponentPresentation.TOOL_BAR_HEIGHT + CanvasPresentation.GRID_SIZE;
            final double maxY = getParentComponent().getHeight() - getSubComponent().getHeight() - CanvasPresentation.GRID_SIZE + mouseYDiff.get();

            // Drag according to min and max
            if (newY < minY) {
                return minY;
            } else if (newY > maxY) {
                return maxY;
            } else {
                return newY;
            }
        };

        ItemDragHelper.makeDraggable(
                root,
                root,
                supplyX,
                supplyY,
                startEdgeFromSubComponent,
                () -> {},
                () -> {}
        );
    }

    public SubComponent getSubComponent() {
        return subComponent.get();
    }

    public void setSubComponent(final SubComponent subComponent) {
        this.subComponent.set(subComponent);
    }

    public ObjectProperty<SubComponent> subComponentProperty() {
        return subComponent;
    }

    public Component getParentComponent() {
        return parentComponent.get();
    }

    public void setParentComponent(final Component parentComponent) {
        this.parentComponent.set(parentComponent);
    }

    public ObjectProperty<Component> parentComponentProperty() {
        return parentComponent;
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {
        // Cannot be colored
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return null;
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();

        defaultLocationsContainer.getChildren().forEach(node -> ((LocationPresentation) node).select());
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();

        defaultLocationsContainer.getChildren().forEach(node -> ((LocationPresentation) node).deselect());
    }
}
