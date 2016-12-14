package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.model_canvas.arrow_heads.SimpleArrowHead;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.Link;
import SW9.presentations.NailPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.Circular;
import SW9.utility.helpers.SelectHelper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class EdgeController implements Initializable, SelectHelper.ColorSelectable {
    private final ObservableList<Link> links = FXCollections.observableArrayList();
    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    private final SimpleArrowHead simpleArrowHead = new SimpleArrowHead();
    private final SimpleBooleanProperty isHoveringEdge = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty timeHoveringEdge = new SimpleIntegerProperty(0);
    public Group edgeRoot;
    private Runnable collapseNail;
    private Thread runningThread;
    private Consumer<Nail> enlargeNail;

    private final Map<Nail, NailPresentation> nailNailPresentationMap = new HashMap<>();
    private Consumer<Nail> shrinkNail;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initializeNailCollapse();

        edge.addListener((obsEdge, oldEdge, newEdge) -> {
            newEdge.targetLocationProperty().addListener(getNewTargetCircularListener(newEdge));
            newEdge.targetSubComponentProperty().addListener(getNewTargetCircularListener(newEdge));
            component.addListener(getComponentChangeListener(newEdge));
        });

        initializeLinksListener();

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

                        if(isHoveringEdge.get()) {
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

                    if(removedIndex > 0) {
                        newFrom = getEdge().getNails().get(removedIndex - 1);
                    }

                    if(removedIndex -1 != getEdge().getNails().size() - 1) {
                        newTo = getEdge().getNails().get(removedIndex);
                    }

                    if(newTo.equals(getEdge().getTargetCircular())) {
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
                    final Nail nail1 = new Nail(newTargetCircular.xProperty(), newTargetCircular.yProperty().add(3 * CanvasPresentation.GRID_SIZE));
                    final Nail nail2 = new Nail(newTargetCircular.xProperty(), newTargetCircular.yProperty().add(5 * CanvasPresentation.GRID_SIZE));
                    final Nail nail3 = new Nail(newTargetCircular.xProperty(), newTargetCircular.yProperty().add(7 * CanvasPresentation.GRID_SIZE));
                    final Nail nail4 = new Nail(newTargetCircular.xProperty(), newTargetCircular.yProperty().add(9 * CanvasPresentation.GRID_SIZE));
                    final Nail nail5 = new Nail(newTargetCircular.xProperty().subtract(4 * CanvasPresentation.GRID_SIZE), newTargetCircular.yProperty().add(9 * CanvasPresentation.GRID_SIZE));
                    final Nail nail6 = new Nail(newTargetCircular.xProperty().subtract(4 * CanvasPresentation.GRID_SIZE), newTargetCircular.yProperty());

                    // Add the nails to the nails collection (will draw links between them)
                    nails.addAll(nail1, nail2, nail3, nail4, nail5, nail6);

                    // Find the new last link (updated by adding nails to the collection) and bind it from the last nail to the target location
                    final Link newLastLink = links.get(links.size() - 1);
                    BindingHelper.bind(newLastLink, simpleArrowHead, nail6, newTargetCircular);
                } else {
                    BindingHelper.bind(lastLink, simpleArrowHead, newEdge.getSourceCircular(), newEdge.getTargetCircular());
                }
            } else {
                final Nail lastNail = nails.get(nails.size() - 1);
                BindingHelper.bind(lastLink, simpleArrowHead, lastNail, newEdge.getTargetCircular());
            }

            // When the target location is set the
            edgeRoot.setMouseTransparent(false);
        };
    }

    private void initializeNailCollapse() {
        enlargeNail = nail -> {
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
                while(true) {
                    Thread.sleep(interval);

                    if(isHoveringEdge.get()) {
                        // Do not let the timer go above this threshold
                        if (timeHoveringEdge.get() <= 500) {
                            timeHoveringEdge.set(timeHoveringEdge.get() + interval);
                        }
                    } else {
                        timeHoveringEdge.set(timeHoveringEdge.get() - interval);
                    }

                    if(previousValue >= 0 && timeHoveringEdge.get() < 0) {
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
        links.addListener(new ListChangeListener<Link>() {
            @Override
            public void onChanged(Change<? extends Link> c) {
                links.forEach((link) -> link.setOnMousePressed(event -> {
                    if (event.isShiftDown()) {
                        final double nailX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue();
                        final double nailY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue();

                        final Nail newNail = new Nail(nailX, nailY);

                        UndoRedoStack.push(
                                ()-> getEdge().insertNailAt(newNail, links.indexOf(link)),
                                ()-> getEdge().removeNail(newNail),
                                "Nail added",
                                "add-circle"
                        );

                    }
                }));
            }
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
            SelectHelper.select(this);
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
}
