package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Edge {

    private Location sourceLocation;
    private Location targetLocation;
    private final MouseTracker praentMouseTracker;
    private final List<Line> lines = new ArrayList<>();
    private final Line arrowHeadLeft = new Line();
    private final Line arrowHeadRight = new Line();

    private static final double ARROW_HEAD_ANGLE = 15;
    private static final double ARROW_HEAD_LENGTH = 15;

    public Edge(final Location sourceLocation, final MouseTracker parentMouseTracker) {
        // Add the parentMouseTracker
        this.praentMouseTracker = parentMouseTracker;

        // After the mousetracker and line have been added, add the sorucelocation
        setSourceLocation(sourceLocation);

        // Enable escape key to discard the edge
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_EDGE, removeOnEscape);
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        Pane parent = getParentPane();
        if (parent == null) return;
        eraseFromParent();

        // Notify the canvas that we not longer are creating an edge
        ModelCanvas.edgeOnMouse = null;
    });

    public void setSourceLocation(final Location sourceLocation) {
        this.sourceLocation = sourceLocation;

        // Create the initial line
        addLine();

        Pair<DoubleBinding, DoubleBinding> startBindings = getStartBindings(this.sourceLocation, this.praentMouseTracker);
        lines.get(0).startXProperty().bind(startBindings.getKey());
        lines.get(0).startYProperty().bind(startBindings.getValue());

        Pair<DoubleBinding, DoubleBinding> endBindings = getEndBindings(this.sourceLocation, this.praentMouseTracker);
        lines.get(0).endXProperty().bind(endBindings.getKey());
        lines.get(0).endYProperty().bind(endBindings.getValue());
    }

    public void setTargetLocation(final Location targetLocation) {
        this.targetLocation = targetLocation;
        KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_EDGE);

        Pair<DoubleBinding, DoubleBinding> startBindings = getStartBindings(sourceLocation, this.targetLocation);
        lines.get(0).startXProperty().bind(startBindings.getKey());
        lines.get(0).startYProperty().bind(startBindings.getValue());

        Pair<DoubleBinding, DoubleBinding> endBindings = getEndBindings(sourceLocation, this.targetLocation);
        lines.get(lines.size() - 1).endXProperty().bind(endBindings.getKey());
        lines.get(lines.size() - 1).endYProperty().bind(endBindings.getValue());

        activateMouseEvents();
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

        if(lines.size() == 0) {
            arrowHeadLeft.setMouseTransparent(true);
            arrowHeadRight.setMouseTransparent(true);
            getParentPane().getChildren().add(arrowHeadRight);
            getParentPane().getChildren().add(arrowHeadLeft);
        }

        Line line = new Line();
        line.setMouseTransparent(true); // Sets the transparency of the mouse to true
        this.lines.add(line);
        getParentPane().getChildren().add(line);

        updateArrow();
    }

    private void activateMouseEvents() {
        for(final Line line : this.lines) {
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

    // Bindings for starting in a location
    private Pair<DoubleBinding, DoubleBinding> getStartBindings(final Location sourceLocation, final MouseTracker mouseTracker) {
        return getStartBindings(sourceLocation.centerXProperty(), sourceLocation.centerYProperty(), mouseTracker.getXProperty(), mouseTracker.getYProperty());
    }

    private Pair<DoubleBinding, DoubleBinding> getStartBindings(final Location sourceLocation, final Location targetLocation) {
        return getStartBindings(sourceLocation.centerXProperty(), sourceLocation.centerYProperty(), targetLocation.centerXProperty(), targetLocation.centerYProperty());
    }

    private Pair<DoubleBinding, DoubleBinding> getStartBindings(final DoubleProperty startX, final DoubleProperty startY, final DoubleProperty endX, final DoubleProperty endY) {


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

                        if(ModelCanvas.locationIsHovered()) {
                            endXValue = ModelCanvas.getHoveredLocation().getCenterX();
                            endYValue = ModelCanvas.getHoveredLocation().getCenterY();
                        }

                        double angle = Math.atan2(startY.get() - endYValue, startX.get() - endXValue) - Math.toRadians(180);
                        return startY.get() + Location.RADIUS * Math.sin(angle);
                    }
                }
        );

    }

    // Bindings for ending in a location
    private Pair<DoubleBinding, DoubleBinding> getEndBindings(final Location sourceLocation, final Location targetLocation) {
        return getEndBindings(sourceLocation.centerXProperty(), sourceLocation.centerYProperty(), targetLocation.centerXProperty(), targetLocation.centerYProperty());
    }

    // Bindings for ending in a location
    private Pair<DoubleBinding, DoubleBinding> getEndBindings(final Location sourceLocation, final MouseTracker mouseTracker) {
        return getEndBindings(sourceLocation.centerXProperty(), sourceLocation.centerYProperty(), mouseTracker.getXProperty(), mouseTracker.getYProperty());
    }
    private Pair<DoubleBinding, DoubleBinding> getEndBindings(final DoubleProperty startX, final DoubleProperty startY, final DoubleProperty endX, final DoubleProperty endY) {

        return new Pair<>(
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {

                        double endXValue, endYValue;

                        if(ModelCanvas.locationIsHovered()) {
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

                        if(ModelCanvas.locationIsHovered()) {
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
