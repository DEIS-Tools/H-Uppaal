package SW9.controllers;

import SW9.abstractions.*;
import SW9.code_analysis.CodeAnalysis;
import SW9.code_analysis.Nearable;
import SW9.model_canvas.arrow_heads.SimpleArrowHead;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.DropDownMenu;
import SW9.presentations.Link;
import SW9.presentations.NailPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.Circular;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXPopup;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class EdgeController implements Initializable, SelectHelper.ItemSelectable {
    private static final Map<Edge, Boolean> initializedEdgeFromTargetError = new HashMap<>();
    private static final Map<Edge, Boolean> initializedEdgeToInitialError = new HashMap<>();
    private static final Map<Edge, Boolean> initializedEdgeToForkError = new HashMap<>();
    private static final Map<Edge, Boolean> initializedEdgeFromJoinError = new HashMap<>();
    private final ObservableList<Link> links = FXCollections.observableArrayList();
    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    private final SimpleArrowHead simpleArrowHead = new SimpleArrowHead();
    private final SimpleBooleanProperty isHoveringEdge = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty timeHoveringEdge = new SimpleIntegerProperty(0);
    private final Map<Nail, NailPresentation> nailNailPresentationMap = new HashMap<>();
    public Group edgeRoot;
    private Runnable collapseNail;
    private Thread runningThread;
    private Consumer<Nail> enlargeNail;
    private Consumer<Nail> shrinkNail;
    private Circle dropDownMenuHelperCircle;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeNailCollapse();

        edge.addListener((obsEdge, oldEdge, newEdge) -> {
            newEdge.targetCircularProperty().addListener(getNewTargetCircularListener(newEdge));
            component.addListener(getComponentChangeListener(newEdge));

            // Invalidate the list of edges (to update UI and errors)
            newEdge.targetCircularProperty().addListener(observable -> {
                getComponent().removeEdge(getEdge());
                getComponent().addEdge(getEdge());
            });
        });

        initializeLinksListener();

        ensureNailsInFront();

        initializeSelectListener();
    }

    private void initializeSelectListener() {
        SelectHelper.elementsToBeSelected.addListener(new ListChangeListener<Nearable>() {
            @Override
            public void onChanged(final Change<? extends Nearable> c) {
                while (c.next()) {
                    if (c.getAddedSize() == 0) return;

                    for (final Nearable nearable : SelectHelper.elementsToBeSelected) {
                        if (nearable instanceof Edge) {
                            if (nearable.equals(getEdge())) {
                                SelectHelper.addToSelection(EdgeController.this);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void initializeEdgeErrorFromTargetLocation() {
        if (initializedEdgeFromTargetError.containsKey(getEdge())) return; // Already initialized
        initializedEdgeFromTargetError.put(getEdge(), true); // Set initialized

        final CodeAnalysis.Message message = new CodeAnalysis.Message("Outgoing edges from a target location are not allowed", CodeAnalysis.MessageType.ERROR, getEdge());

        final Consumer<Location> checkIfErrorIsPresent = (sourceLocation) -> {
            if (sourceLocation != null
                    && sourceLocation.getType().equals(Location.Type.FINAl)
                    && getComponent().getEdges().contains(getEdge())
                    && getEdge().getTargetCircular() != null) {


                // Add the message to the UI
                CodeAnalysis.addMessage(getComponent(), message);
            } else {
                // Remove the message from the UI
                CodeAnalysis.removeMessage(getComponent(), message);
            }
        };

        // When the source location is updated
        getEdge().sourceLocationProperty().addListener((obs, oldSource, newSource) -> checkIfErrorIsPresent.accept(newSource));

        // When the list of edges are updated
        final InvalidationListener listener = observable -> checkIfErrorIsPresent.accept(getEdge().getSourceLocation());
        getComponent().getEdges().addListener(listener);

        // Check if the error is present right now
        checkIfErrorIsPresent.accept(getEdge().getSourceLocation());
    }

    public void initializeEdgeErrorToInitialLocation() {
        if (initializedEdgeToInitialError.containsKey(getEdge())) return; // Already initialized
        initializedEdgeToInitialError.put(getEdge(), true); // Set initialized

        final CodeAnalysis.Message message = new CodeAnalysis.Message("Incoming edges to an initial location are not allowed", CodeAnalysis.MessageType.ERROR, getEdge());

        final Consumer<Location> checkIfErrorIsPresent = (targetLocation) -> {

            if (targetLocation != null
                    && targetLocation.getType().equals(Location.Type.INITIAL)
                    && getComponent().getEdges().contains(getEdge())) {

                // Add the message to the UI
                CodeAnalysis.addMessage(getComponent(), message);
            } else {
                // Remove the message from the UI
                CodeAnalysis.removeMessage(getComponent(), message);
            }
        };

        // When the source location is updated
        getEdge().targetLocationProperty().addListener((obs, oldTarget, newTarget) -> checkIfErrorIsPresent.accept(newTarget));

        // When the list of edges are updated
        final InvalidationListener listener = observable -> checkIfErrorIsPresent.accept(getEdge().getTargetLocation());
        getComponent().getEdges().addListener(listener);

        // Check if the error is present right now
        checkIfErrorIsPresent.accept(getEdge().getTargetLocation());
    }

    public void initializeEdgeToForkError() {
        if (initializedEdgeToForkError.containsKey(getEdge())) return; // Already initialized
        initializedEdgeToForkError.put(getEdge(), true); // Set initialized

        final CodeAnalysis.Message message = new CodeAnalysis.Message("Only subcomponents can run in parallel", CodeAnalysis.MessageType.ERROR, getEdge());

        final Consumer<Edge> checkIfErrorIsPresent = (edge) -> {
            if (edge != null // The edge is not null
                    // The edge is not being drawn
                    && !edge.equals(getComponent().getUnfinishedEdge())
                    // The edge has a source jork
                    && edge.getSourceJork() != null
                    // The source jork is a fork
                    && edge.getSourceJork().getType().equals(Jork.Type.FORK)
                    // The jork does not have a sub component as its target
                    && edge.getTargetSubComponent() == null
                    // The edge is in the component (not deleted)
                    && getComponent().getEdges().contains(edge)) {
                // Add the message to the UI
                CodeAnalysis.addMessage(getComponent(), message);
            } else {
                // Add the message to the UI
                CodeAnalysis.removeMessage(getComponent(), message);
            }
        };

        // When the target location is updated
        getEdge().targetCircularProperty().addListener((obs, oldTarget, newTarget) -> checkIfErrorIsPresent.accept(getEdge()));

        // When the list of edges are updated
        final InvalidationListener listener = observable -> checkIfErrorIsPresent.accept(getEdge());
        getComponent().getEdges().addListener(listener);

        // Check if the error is present right now
        checkIfErrorIsPresent.accept(getEdge());
    }

    public void initializeEdgeFromJoinError() {
        if (initializedEdgeFromJoinError.containsKey(getEdge())) return; // Already initialized
        initializedEdgeFromJoinError.put(getEdge(), true); // Set initialized

        final CodeAnalysis.Message message = new CodeAnalysis.Message("Only subcomponents that are running in parallel can be joined", CodeAnalysis.MessageType.ERROR, getEdge());

        final Consumer<Edge> checkIfErrorIsPresent = (edge) -> {
            if (edge != null // The edge is not null
                    // The edge is not being drawn
                    && !edge.equals(getComponent().getUnfinishedEdge())
                    // The edge has a target jork
                    && edge.getTargetJork() != null
                    // The target jork is a join
                    && edge.getTargetJork().getType().equals(Jork.Type.JOIN)
                    // The jork does not have a sub component as its source
                    && edge.getSourceSubComponent() == null
                    // The edge is in the component (not deleted)
                    && getComponent().getEdges().contains(edge)) {
                // Add the message to the UI
                CodeAnalysis.addMessage(getComponent(), message);
            } else {
                // Add the message to the UI
                CodeAnalysis.removeMessage(getComponent(), message);
            }
        };

        // When the target location is updated
        getEdge().targetCircularProperty().addListener((obs, oldTarget, newTarget) -> checkIfErrorIsPresent.accept(getEdge()));

        // When the list of edges are updated
        final InvalidationListener listener = observable -> checkIfErrorIsPresent.accept(getEdge());
        getComponent().getEdges().addListener(listener);

        // Check if the error is present right now
        checkIfErrorIsPresent.accept(getEdge());
    }

    private void ensureNailsInFront() {

        // When ever changes happens to the children of the edge root force nails in front and other elements to back
        edgeRoot.getChildren().addListener((ListChangeListener<? super Node>) c -> {
            while (c.next()) {
                for (int i = 0; i < c.getAddedSize(); i++) {
                    final Node node = c.getAddedSubList().get(i);
                    if (node instanceof NailPresentation) {
                        node.toFront();
                    } else {
                        node.toBack();
                    }
                }
            }
        });
    }

    private ChangeListener<Component> getComponentChangeListener(final Edge newEdge) {
        return (obsComponent, oldComponent, newComponent) -> {
            if (newEdge.getNails().isEmpty() && newEdge.getTargetCircular() == null) {
                final Link link = new Link();
                links.add(link);

                // Add the link and its arrowhead to the view
                edgeRoot.getChildren().addAll(link, simpleArrowHead);

                // Bind the first link and the arrowhead from the source location to the mouse
                BindingHelper.bind(link, simpleArrowHead, newEdge.getSourceCircular(), newComponent.xProperty(), newComponent.yProperty());
            } else if (newEdge.getTargetCircular() != null) {

                edgeRoot.getChildren().add(simpleArrowHead);

                final Circular[] previous = {newEdge.getSourceCircular()};

                newEdge.getNails().forEach(nail -> {
                    final Link link = new Link();
                    links.add(link);

                    final NailPresentation nailPresentation = new NailPresentation(nail, newEdge, getComponent());
                    nailNailPresentationMap.put(nail, nailPresentation);

                    edgeRoot.getChildren().addAll(link, nailPresentation);
                    BindingHelper.bind(link, previous[0], nail);

                    previous[0] = nail;
                });

                final Link link = new Link();
                links.add(link);

                edgeRoot.getChildren().add(link);
                BindingHelper.bind(link, simpleArrowHead, previous[0], newEdge.getTargetCircular());
            }

            // Changes are made to the nails list
            newEdge.getNails().addListener(getNailsChangeListener(newEdge, newComponent));

        };
    }

    private ListChangeListener<Nail> getNailsChangeListener(final Edge newEdge, final Component newComponent) {
        return change -> {
            while (change.next()) {
                // There were added some nails
                change.getAddedSubList().forEach(newNail -> {
                    // Create a new nail presentation based on the abstraction added to the list
                    final NailPresentation newNailPresentation = new NailPresentation(newNail, newEdge, newComponent);
                    nailNailPresentationMap.put(newNail, newNailPresentation);

                    edgeRoot.getChildren().addAll(newNailPresentation);

                    if (newEdge.getTargetCircular() != null) {
                        final int indexOfNewNail = edge.get().getNails().indexOf(newNail);

                        final Link newLink = new Link();
                        final Link pressedLink = links.get(indexOfNewNail);
                        links.add(indexOfNewNail, newLink);

                        edgeRoot.getChildren().addAll(newLink);

                        Circular oldStart = getEdge().getSourceCircular();
                        Circular oldEnd = getEdge().getTargetCircular();

                        if (indexOfNewNail != 0) {
                            oldStart = getEdge().getNails().get(indexOfNewNail - 1);
                        }

                        if (indexOfNewNail != getEdge().getNails().size() - 1) {
                            oldEnd = getEdge().getNails().get(indexOfNewNail + 1);
                        }

                        BindingHelper.bind(newLink, oldStart, newNail);

                        if (oldEnd.equals(getEdge().getTargetCircular())) {
                            BindingHelper.bind(pressedLink, simpleArrowHead, newNail, oldEnd);
                        } else {
                            BindingHelper.bind(pressedLink, newNail, oldEnd);
                        }

                        if (isHoveringEdge.get()) {
                            enlargeNail.accept(newNail);
                        }

                    } else {
                        // The previous last link must end in the new nail
                        final Link lastLink = links.get(links.size() - 1);

                        // If the nail is the first in the list, bind it to the source location
                        // otherwise, bind it the the previous nail
                        final int nailIndex = edge.get().getNails().indexOf(newNail);
                        if (nailIndex == 0) {
                            BindingHelper.bind(lastLink, newEdge.getSourceCircular(), newNail);
                        } else {
                            final Nail previousNail = edge.get().getNails().get(nailIndex - 1);
                            BindingHelper.bind(lastLink, previousNail, newNail);
                        }

                        // Create a new link that will bind from the new nail to the mouse
                        final Link newLink = new Link();
                        links.add(newLink);
                        BindingHelper.bind(newLink, simpleArrowHead, newNail, newComponent.xProperty(), newComponent.yProperty());
                        edgeRoot.getChildren().add(newLink);
                    }
                });

                change.getRemoved().forEach(removedNail -> {
                    final int removedIndex = change.getFrom();
                    final NailPresentation removedNailPresentation = nailNailPresentationMap.remove(removedNail);
                    final Link danglingLink = links.get(removedIndex + 1);
                    edgeRoot.getChildren().remove(removedNailPresentation);
                    edgeRoot.getChildren().remove(links.get(removedIndex));

                    Circular newFrom = getEdge().getSourceCircular();
                    Circular newTo = getEdge().getTargetCircular();

                    if (removedIndex > 0) {
                        newFrom = getEdge().getNails().get(removedIndex - 1);
                    }

                    if (removedIndex - 1 != getEdge().getNails().size() - 1) {
                        newTo = getEdge().getNails().get(removedIndex);
                    }

                    if (newTo.equals(getEdge().getTargetCircular())) {
                        BindingHelper.bind(danglingLink, simpleArrowHead, newFrom, newTo);
                    } else {
                        BindingHelper.bind(danglingLink, newFrom, newTo);
                    }


                    links.remove(removedIndex);
                });
            }
        };
    }

    private ChangeListener<Circular> getNewTargetCircularListener(final Edge newEdge) {
        // When the target location is set, finish drawing the edge
        return (obsTargetLocation, oldTargetCircular, newTargetCircular) -> {
            // If the nails list is empty, directly connect the source and target locations
            // otherwise, bind the line from the last nail to the target location
            final Link lastLink = links.get(links.size() - 1);
            final ObservableList<Nail> nails = getEdge().getNails();
            if (nails.size() == 0) {
                // Check if the source and target locations are the same, if they are, add proper amount of nails
                if (newEdge.getSourceCircular().equals(newTargetCircular)) {
                    final Nail nail1 = new Nail(newTargetCircular.xProperty().add(4 * GRID_SIZE), newTargetCircular.yProperty().subtract(GRID_SIZE));
                    final Nail nail2 = new Nail(newTargetCircular.xProperty().add(4 * GRID_SIZE), newTargetCircular.yProperty().add(GRID_SIZE));

                    // Add the nails to the nails collection (will draw links between them)
                    nails.addAll(nail1, nail2);

                    // Find the new last link (updated by adding nails to the collection) and bind it from the last nail to the target location
                    final Link newLastLink = links.get(links.size() - 1);
                    BindingHelper.bind(newLastLink, simpleArrowHead, nail2, newTargetCircular);
                } else {
                    BindingHelper.bind(lastLink, simpleArrowHead, newEdge.getSourceCircular(), newEdge.getTargetCircular());
                }
            } else {
                final Nail lastNail = nails.get(nails.size() - 1);
                BindingHelper.bind(lastLink, simpleArrowHead, lastNail, newEdge.getTargetCircular());
            }

            KeyboardTracker.unregisterKeybind(KeyboardTracker.ABANDON_EDGE);

            // When the target location is set the
            edgeRoot.setMouseTransparent(false);
        };
    }

    private void initializeNailCollapse() {
        enlargeNail = nail -> {
            if (!nail.getPropertyType().equals(Edge.PropertyType.NONE)) return;
            final Timeline animation = new Timeline();

            final KeyValue radius0 = new KeyValue(nail.radiusProperty(), NailPresentation.COLLAPSED_RADIUS);
            final KeyValue radius2 = new KeyValue(nail.radiusProperty(), NailPresentation.HOVERED_RADIUS * 1.2);
            final KeyValue radius1 = new KeyValue(nail.radiusProperty(), NailPresentation.HOVERED_RADIUS);

            final KeyFrame kf1 = new KeyFrame(Duration.millis(0), radius0);
            final KeyFrame kf2 = new KeyFrame(Duration.millis(80), radius2);
            final KeyFrame kf3 = new KeyFrame(Duration.millis(100), radius1);

            animation.getKeyFrames().addAll(kf1, kf2, kf3);

            animation.play();
        };
        shrinkNail = nail -> {
            if (!nail.getPropertyType().equals(Edge.PropertyType.NONE)) return;
            final Timeline animation = new Timeline();

            final KeyValue radius0 = new KeyValue(nail.radiusProperty(), NailPresentation.COLLAPSED_RADIUS);
            final KeyValue radius1 = new KeyValue(nail.radiusProperty(), NailPresentation.HOVERED_RADIUS);

            final KeyFrame kf1 = new KeyFrame(Duration.millis(0), radius1);
            final KeyFrame kf2 = new KeyFrame(Duration.millis(100), radius0);

            animation.getKeyFrames().addAll(kf1, kf2);

            animation.play();
        };

        collapseNail = () -> {
            final int interval = 50;

            int previousValue = 1;

            try {
                while (true) {
                    Thread.sleep(interval);

                    if (isHoveringEdge.get()) {
                        // Do not let the timer go above this threshold
                        if (timeHoveringEdge.get() <= 500) {
                            timeHoveringEdge.set(timeHoveringEdge.get() + interval);
                        }
                    } else {
                        timeHoveringEdge.set(timeHoveringEdge.get() - interval);
                    }

                    if (previousValue >= 0 && timeHoveringEdge.get() < 0) {
                        // Run on UI thread
                        Platform.runLater(() -> {
                            // Collapse all nails
                            getEdge().getNails().forEach(shrinkNail);
                        });

                        break;
                    }

                    previousValue = timeHoveringEdge.get();
                }

            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    private void initializeLinksListener() {
        // Tape
        dropDownMenuHelperCircle = new Circle(5);
        dropDownMenuHelperCircle.setOpacity(0);
        dropDownMenuHelperCircle.setMouseTransparent(true);
        edgeRoot.getChildren().add(dropDownMenuHelperCircle);

        links.addListener(new ListChangeListener<Link>() {
            @Override
            public void onChanged(final Change<? extends Link> c) {
                links.forEach((link) -> link.setOnMousePressed(event -> {

                    if (event.isSecondaryButtonDown() && getComponent().getUnfinishedEdge() == null) {
                        event.consume();

                        final DropDownMenu dropDownMenu = new DropDownMenu(((Pane) edgeRoot.getParent().getParent().getParent().getParent()), dropDownMenuHelperCircle, 230, true);

                        addEdgePropertyRow(dropDownMenu, "Add Select", Edge.PropertyType.SELECTION, link);
                        addEdgePropertyRow(dropDownMenu, "Add Guard", Edge.PropertyType.GUARD, link);
                        addEdgePropertyRow(dropDownMenu, "Add Synchronization", Edge.PropertyType.SYNCHRONIZATION, link);
                        addEdgePropertyRow(dropDownMenu, "Add Update", Edge.PropertyType.UPDATE, link);

                        dropDownMenu.addSpacerElement();

                        dropDownMenu.addClickableListElement("Add Nail", mouseEvent -> {
                            final double nailX = Math.round((DropDownMenu.x - getComponent().getX()) / GRID_SIZE) * GRID_SIZE;
                            final double nailY = Math.round((DropDownMenu.y - getComponent().getY()) / GRID_SIZE) * GRID_SIZE;
                            final Nail newNail = new Nail(nailX, nailY);

                            UndoRedoStack.push(
                                    () -> getEdge().insertNailAt(newNail, links.indexOf(link)),
                                    () -> getEdge().removeNail(newNail),
                                    "Nail added",
                                    "add-circle"
                            );
                            dropDownMenu.close();
                        });
                        dropDownMenu.addSpacerElement();

                        dropDownMenu.addClickableListElement("Delete", mouseEvent -> {
                            dropDownMenu.close();
                            UndoRedoStack.push(() -> { // Perform
                                getComponent().removeEdge(getEdge());
                            }, () -> { // Undo
                                getComponent().addEdge(getEdge());
                            }, "Deleted edge " + getEdge(), "delete");
                        });

                        dropDownMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, event.getX(), event.getY());

                        DropDownMenu.x = CanvasPresentation.mouseTracker.getGridX();
                        DropDownMenu.y = CanvasPresentation.mouseTracker.getGridY();


                    } else if ((event.isShiftDown() && event.isPrimaryButtonDown()) || event.isMiddleButtonDown()) {
                        final double nailX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue();
                        final double nailY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue();

                        final Nail newNail = new Nail(nailX, nailY);

                        UndoRedoStack.push(
                                () -> getEdge().insertNailAt(newNail, links.indexOf(link)),
                                () -> getEdge().removeNail(newNail),
                                "Nail added",
                                "add-circle"
                        );

                    }
                }));
            }
        });
    }

    private void addEdgePropertyRow(final DropDownMenu dropDownMenu, final String rowTitle, final Edge.PropertyType type, final Link link) {
        final SimpleBooleanProperty isDisabled = new SimpleBooleanProperty(false);

        final int[] data = {-1, -1, -1, -1};

        int i = 0;
        for (final Nail nail : getEdge().getNails()) {
            if (nail.getPropertyType().equals(Edge.PropertyType.SELECTION)) data[Edge.PropertyType.SELECTION.getI()] = i;
            if (nail.getPropertyType().equals(Edge.PropertyType.GUARD)) data[Edge.PropertyType.GUARD.getI()] = i;
            if (nail.getPropertyType().equals(Edge.PropertyType.SYNCHRONIZATION)) data[Edge.PropertyType.SYNCHRONIZATION.getI()] = i;
            if (nail.getPropertyType().equals(Edge.PropertyType.UPDATE)) data[Edge.PropertyType.UPDATE.getI()] = i;

            if (nail.getPropertyType().equals(type)) {
                isDisabled.set(true);
            }

            i++;
        }

        final SimpleIntegerProperty insertAt = new SimpleIntegerProperty(links.indexOf(link));
        final int clickedLinkedIndex = links.indexOf(link);


        // Check the elements before me, and ensure that I am placed after these
        for (int i1 = type.getI() - 1; i1 >= 0; i1--) {

            if (data[i1] != -1 && data[i1] >= clickedLinkedIndex) {
                insertAt.set(data[i1] + 1);
            }
        }

        // Check the elements after me, and ensure that I am placed before these
        for (int i1 = type.getI() + 1; i1 < data.length; i1++) {

            if (data[i1] != -1 && data[i1] < clickedLinkedIndex) {
                insertAt.set(data[i1]);
            }
        }

        dropDownMenu.addClickableAndDisableableListElement(rowTitle, isDisabled, event -> {
            final double nailX = Math.round((DropDownMenu.x - getComponent().getX()) / GRID_SIZE) * GRID_SIZE;
            final double nailY = Math.round((DropDownMenu.y - getComponent().getY()) / GRID_SIZE) * GRID_SIZE;

            final Nail newNail = new Nail(nailX, nailY);
            newNail.setPropertyType(type);

            UndoRedoStack.push(
                    () -> getEdge().insertNailAt(newNail, insertAt.get()),
                    () -> getEdge().removeNail(newNail),
                    "Nail property added (" + type + ")",
                    "add-circle"
            );

            dropDownMenu.close();
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

    public void edgeEntered() {
        isHoveringEdge.set(true);
        if ((runningThread != null && runningThread.isAlive())) return; // Do not re-animate

        timeHoveringEdge.set(500);
        runningThread = new Thread(collapseNail);
        runningThread.start();

        getEdge().getNails().forEach(enlargeNail);
    }

    public void edgeExited() {
        isHoveringEdge.set(false);
    }

    @FXML
    public void edgePressed(final MouseEvent event) {
        if (!event.isShiftDown()) {
            event.consume();

            if (event.isShortcutDown()) {
                SelectHelper.addToSelection(this);
            } else {
                SelectHelper.select(this);
            }
        }
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {
        final Edge edge = getEdge();

        // Set the color of the edge
        edge.setColorIntensity(intensity);
        edge.setColor(color);
    }

    @Override
    public Color getColor() {
        return getEdge().getColor();
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return getEdge().getColorIntensity();
    }

    @Override
    public ItemDragHelper.DragBounds getDragBounds() {
        return ItemDragHelper.DragBounds.generateLooseDragBounds();
    }

    @Override
    public void select() {
        edgeRoot.getChildren().forEach(node -> {
            if (node instanceof SelectHelper.Selectable) {
                ((SelectHelper.Selectable) node).select();
            }
        });
    }

    @Override
    public void deselect() {
        edgeRoot.getChildren().forEach(node -> {
            if (node instanceof SelectHelper.Selectable) {
                ((SelectHelper.Selectable) node).deselect();
            }
        });
    }

    @Override
    public DoubleProperty xProperty() {
        return edgeRoot.layoutXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return edgeRoot.layoutYProperty();
    }

    @Override
    public double getX() {
        return xProperty().get();
    }

    @Override
    public double getY() {
        return yProperty().get();
    }
}
