package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Edge {

    private Location sourceLocation;
    private Location targetLocation;
    private final MouseTracker parentMouseTracker;
    private final List<Line> lines = new ArrayList<>();
    private final Line arrowHeadLeft = new Line();
    private final Line arrowHeadRight = new Line();

    private static final double ARROW_HEAD_ANGLE = 15;
    private static final double ARROW_HEAD_LENGTH = 15;

    public Edge(final Location sourceLocation, final MouseTracker parentMouseTracker) {
        // Add the parentMouseTracker
        this.parentMouseTracker = parentMouseTracker;

        // After the mousetracker and line have been added, add the sorucelocation
        setSourceLocation(sourceLocation);

        // Add arrow heads
        arrowHeadLeft.setMouseTransparent(true);
        arrowHeadRight.setMouseTransparent(true);
        getParentPane().getChildren().add(arrowHeadRight);
        getParentPane().getChildren().add(arrowHeadLeft);

        // Enable escape key to discard the edge
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_EDGE, removeOnEscape);
        this.parentMouseTracker.registerOnMouseClickedEventHandler(addNailEvent);
    }

    final EventHandler<MouseEvent> addNailEvent = event -> {

        if (ModelCanvas.mouseHasEdge() && ModelCanvas.getEdgeOnMouse().equals(this)) {
            addLine();
        }
    };

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        Pane parent = getParentPane();
        if (parent == null) return;
        eraseFromParent();

        // Notify the canvas that we not longer are creating an edge
        ModelCanvas.setEdgeOnMouse(null);
    });

    public void setSourceLocation(final Location sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public void setTargetLocation(final Location targetLocation) {
        this.targetLocation = targetLocation;

        // Notify the canvas that we not longer are creating an edge
        ModelCanvas.setEdgeOnMouse(null);

        KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_EDGE);


        Line lastLink = lines.get(lines.size() - 1);

        ObservableDoubleValue startX, startY;

        if (lines.size() > 1) {
            Line secondLastLink = lines.get(lines.size() - 2);
            startX = secondLastLink.endXProperty();
            startY = secondLastLink.endYProperty();
        } else {
            Pair<DoubleBinding, DoubleBinding> startBindings = getStartBindings(sourceLocation.centerXProperty(), sourceLocation.centerYProperty(), targetLocation.centerXProperty(), targetLocation.centerXProperty());
            startX = startBindings.getKey();
            startY = startBindings.getValue();
        }


        lastLink.startXProperty().bind(startX);
        lastLink.startYProperty().bind(startY);

        Pair<DoubleBinding, DoubleBinding> endBindings = getEndBindings(startX, startY, targetLocation.centerXProperty(), targetLocation.centerXProperty());
        lastLink.endXProperty().bind(endBindings.getKey());
        lastLink.endYProperty().bind(endBindings.getValue());

        //   activateMouseEvents();
    }

    public Pane getParentPane() {
        return (Pane) sourceLocation.getParent();
    }

    public void eraseFromParent() {
        Pane parrent = getParentPane();
        for (Line line : lines) {
            parrent.getChildren().remove(line);
        }
        parrent.getChildren().remove(arrowHeadRight);
        parrent.getChildren().remove(arrowHeadLeft);

    }

    private void addLine() {
        Line newLine = new Line();
        newLine.setMouseTransparent(true); // Sets the transparency of the mouse to true
        getParentPane().getChildren().add(newLine);
        
        // No lines added already add the first one
        if (lines.isEmpty()) {

            ObservableDoubleValue mouseX, mouseY;
            mouseX = this.parentMouseTracker.getXProperty();
            mouseY = this.parentMouseTracker.getYProperty();

            Pair<DoubleBinding, DoubleBinding> startBindings = getStartBindings(this.sourceLocation.centerXProperty(), this.sourceLocation.centerYProperty(), mouseX, mouseY);

            // Bind start of the new line to point calculated of the source locations
            newLine.startXProperty().bind(startBindings.getKey());
            newLine.startYProperty().bind(startBindings.getValue());

            // Bind the new line to follow the mouse
            newLine.endXProperty().bind(mouseX);
            newLine.endYProperty().bind(mouseY);
        } else {
            ObservableDoubleValue prevLineEndX, prevLineEndY, mouseX, mouseY;
            mouseX = this.parentMouseTracker.getXProperty();
            mouseY = this.parentMouseTracker.getYProperty();


            final Line prevLine = lines.get(lines.size() - 1);

            prevLineEndX = new SimpleDoubleProperty(prevLine.getEndX());
            prevLineEndY = new SimpleDoubleProperty(prevLine.getEndY());

            // Bind the new line to start where the previous ended
            newLine.startXProperty().bind(prevLineEndX);
            newLine.startYProperty().bind(prevLineEndY);

            // Bind the new line to follow the mouse
            newLine.endXProperty().bind(mouseX);
            newLine.endYProperty().bind(mouseY);

            // Update the previous line

            // Unbind the previous line's end position
            prevLine.endXProperty().unbind();
            prevLine.endYProperty().unbind();

            // If it is the first line (connected to the source locations) stop following the mouse
            if(lines.size() == 1) {
                Pair<DoubleBinding, DoubleBinding> startBindings = getStartBindings(this.sourceLocation.centerXProperty(), this.sourceLocation.centerYProperty(), newLine.startXProperty(), newLine.startYProperty());
                prevLine.startXProperty().bind(startBindings.getKey());
                prevLine.startYProperty().bind(startBindings.getValue());

            }

        }

        this.lines.add(newLine);
        updateArrow();
    }


    private void activateMouseEvents() {
        for (final Line line : this.lines) {
            line.setMouseTransparent(false);
        }

        arrowHeadLeft.setMouseTransparent(false);
        arrowHeadRight.setMouseTransparent(false);
    }

    private void updateArrow() {
        DoubleProperty endX = lines.get(lines.size() - 1).endXProperty();
        DoubleProperty endY = lines.get(lines.size() - 1).endYProperty();
        DoubleProperty startX = lines.get(lines.size() - 1).startXProperty();
        DoubleProperty startY = lines.get(lines.size() - 1).startYProperty();

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

    // Bindings for starting in a location (handles mouse and hover location)
    private Pair<DoubleBinding, DoubleBinding> getStartBindings(final ObservableDoubleValue startX, final ObservableDoubleValue startY, final ObservableDoubleValue endX, final ObservableDoubleValue endY) {


        return new Pair<>(
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {

                        double endXValue = endX.get();
                        double endYValue = endY.get();

                        if(ModelCanvas.locationIsHovered()) {
                            endXValue = ModelCanvas.getHoveredLocation().getCenterX();
                            endYValue = ModelCanvas.getHoveredLocation().getCenterY();
                        }

                        double angle = Math.atan2(startY.get() - endYValue, startX.get() - endXValue) - Math.toRadians(180);
                        return startX.get() + Location.RADIUS * Math.cos(angle);
                    }
                },
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {

                        double endXValue = endX.get();
                        double endYValue = endY.get();

                        if (ModelCanvas.locationIsHovered()) {
                            endXValue = ModelCanvas.getHoveredLocation().getCenterX();
                            endYValue = ModelCanvas.getHoveredLocation().getCenterY();
                        }

                        double angle = Math.atan2(startY.get() - endYValue, startX.get() - endXValue) - Math.toRadians(180);
                        return startY.get() + Location.RADIUS * Math.sin(angle);
                    }
                }
        );

    }

    // Bindings for ending in a location (handles mouse and hover location)
    private Pair<DoubleBinding, DoubleBinding> getEndBindings(final ObservableDoubleValue startX, final ObservableDoubleValue startY, final ObservableDoubleValue endX, final ObservableDoubleValue endY) {

        return new Pair<>(
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {

                        double endXValue, endYValue;

                        if (ModelCanvas.locationIsHovered()) {
                            endXValue = ModelCanvas.getHoveredLocation().getCenterX();
                            endYValue = ModelCanvas.getHoveredLocation().getCenterY();
                        } else {
                            return endX.get();
                        }

                        double angle = Math.atan2(startY.get() - endYValue, startX.get() - endXValue);
                        return endXValue + Location.RADIUS * Math.cos(angle);
                    }
                },
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {

                        double endXValue, endYValue;

                        if (ModelCanvas.locationIsHovered()) {
                            endXValue = ModelCanvas.getHoveredLocation().getCenterX();
                            endYValue = ModelCanvas.getHoveredLocation().getCenterY();
                        } else {
                            return endY.get();
                        }

                        double angle = Math.atan2(startY.get() - endYValue, startX.get() - endXValue);
                        return endYValue + Location.RADIUS * Math.sin(angle);
                    }
                }
        );

    }
}
