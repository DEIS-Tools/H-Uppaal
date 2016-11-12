package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.model_canvas.arrow_heads.SimpleArrowHead;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.Link;
import SW9.presentations.NailPresentation;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.Circular;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class EdgeController implements Initializable {
    private final ArrayList<Link> links = new ArrayList<>();
    private final ObjectProperty<Edge> edge = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    private final SimpleArrowHead simpleArrowHead = new SimpleArrowHead();
    private final SimpleBooleanProperty isHoveringEdge = new SimpleBooleanProperty(false);
    private final SimpleIntegerProperty timeHoveringEdge = new SimpleIntegerProperty(0);
    public Group edgeRoot;
    private Runnable collapseNail;
    private Thread runningThread;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
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
                            getEdge().getNails().forEach(nail -> {
                                final Timeline animation = new Timeline();

                                final KeyValue radius0 = new KeyValue(nail.radiusProperty(), NailPresentation.COLLAPSED_RADIUS);
                                final KeyValue radius1 = new KeyValue(nail.radiusProperty(), NailPresentation.HOVERED_RADIUS);

                                final KeyFrame kf1 = new KeyFrame(Duration.millis(0), radius1);
                                final KeyFrame kf2 = new KeyFrame(Duration.millis(100), radius0);

                                animation.getKeyFrames().addAll(kf1, kf2);

                                animation.play();
                            });
                        });

                        break;
                    }

                    previousValue = timeHoveringEdge.get();
                }

            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        };

        edge.addListener((obsEdge, oldEdge, newEdge) -> {

            // When the target location is set, finish drawing the edge
            newEdge.targetLocationProperty().addListener((obsTargetLocation, oldTargetLocation, newTargetLocation) -> {
                // TODO: Check if the source location is the same as the target location

                // If the nails list is empty, directly connect the source and target locations
                // otherwise, bind the line from the last nail to the target location
                final Link lastLink = links.get(links.size() - 1);
                final ObservableList<Nail> nails = getEdge().getNails();
                if (nails.size() == 0) {
                    // Check if the source and target locations are the same, if they are, add two new helper nails
                    if (newEdge.getSourceLocation().equals(newTargetLocation)) {
                        final Nail nail1 = new Nail(newTargetLocation.xProperty().add(5 * CanvasPresentation.GRID_SIZE), newTargetLocation.yProperty().add(3 * CanvasPresentation.GRID_SIZE));
                        final Nail nail2 = new Nail(newTargetLocation.xProperty().add(3 * CanvasPresentation.GRID_SIZE), newTargetLocation.yProperty().add(5 * CanvasPresentation.GRID_SIZE));

                        // Add the nails to the nails collection (will draw links between them)
                        nails.addAll(nail1, nail2);

                        // Find the new last link (updated by adding nails to the collection) and bind it from the last nail to the target location
                        final Link newLastLink = links.get(links.size() - 1);
                        BindingHelper.bind(newLastLink, simpleArrowHead, nail2, newTargetLocation);
                    } else {
                        BindingHelper.bind(lastLink, simpleArrowHead, newEdge.getSourceLocation(), newTargetLocation);
                    }
                } else {
                    final Nail lastNail = nails.get(nails.size() - 1);
                    BindingHelper.bind(lastLink, simpleArrowHead, lastNail, newTargetLocation);
                }

                // When the target location is set the
                edgeRoot.setMouseTransparent(false);
            });

            component.addListener((obsComponent, oldComponent, newComponent) -> {
                if (newEdge.getNails().isEmpty() && newEdge.getTargetLocation() == null) {
                    final Link link = new Link();
                    links.add(link);

                    // Add the link and its arrowhead to the view
                    edgeRoot.getChildren().addAll(link, simpleArrowHead);

                    // Bind the first link and the arrowhead from the source location to the mouse
                    BindingHelper.bind(link, simpleArrowHead, newEdge.getSourceLocation(), newComponent.xProperty(), newComponent.yProperty());
                } else if (newEdge.getTargetLocation() != null) {

                    edgeRoot.getChildren().add(simpleArrowHead);

                    final Circular[] previous = {newEdge.getSourceLocation()};

                    newEdge.getNails().forEach(nail -> {
                        final Link link = new Link();
                        links.add(link);

                        edgeRoot.getChildren().addAll(link, new NailPresentation(nail, getComponent()));
                        BindingHelper.bind(link, previous[0], nail);

                        previous[0] = nail;
                    });

                    final Link link = new Link();
                    links.add(link);

                    edgeRoot.getChildren().add(link);
                    BindingHelper.bind(link, simpleArrowHead, previous[0], newEdge.getTargetLocation());
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

                                // The previous last link must end in the new nail
                                final Link lastLink = links.get(links.size() - 1);

                                // If the nail is the first in the list, bind it to the source location
                                // otherwise, bind it the the previous nail
                                final int nailIndex = edge.get().getNails().indexOf(nail);
                                if (nailIndex == 0) {
                                    BindingHelper.bind(lastLink, newEdge.getSourceLocation(), nail);
                                } else {
                                    final Nail previousNail = edge.get().getNails().get(nailIndex - 1);
                                    BindingHelper.bind(lastLink, previousNail, nail);
                                }

                                // Create a new link that will bind from the new nail to the mouse
                                final Link newLink = new Link();
                                links.add(newLink);
                                BindingHelper.bind(newLink, simpleArrowHead, nail, newComponent.xProperty(), newComponent.yProperty());
                                edgeRoot.getChildren().add(newLink);
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

    public void edgeEntered() {
        isHoveringEdge.set(true);

        if ((runningThread != null && runningThread.isAlive())) return; // Do not re-animate

        timeHoveringEdge.set(500);
        runningThread = new Thread(collapseNail);
        runningThread.start();

        getEdge().getNails().forEach(nail -> {
            final Timeline animation = new Timeline();

            final KeyValue radius0 = new KeyValue(nail.radiusProperty(), NailPresentation.COLLAPSED_RADIUS);
            final KeyValue radius2 = new KeyValue(nail.radiusProperty(), NailPresentation.HOVERED_RADIUS * 1.2);
            final KeyValue radius1 = new KeyValue(nail.radiusProperty(), NailPresentation.HOVERED_RADIUS);

            final KeyFrame kf1 = new KeyFrame(Duration.millis(0), radius0);
            final KeyFrame kf2 = new KeyFrame(Duration.millis(80), radius2);
            final KeyFrame kf3 = new KeyFrame(Duration.millis(100), radius1);

            animation.getKeyFrames().addAll(kf1, kf2, kf3);

            animation.play();
        });
    }

    public void edgeExited() {
        isHoveringEdge.set(false);
    }

}
