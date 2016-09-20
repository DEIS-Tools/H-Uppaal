package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import javafx.beans.binding.Bindings;
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

    public Edge(final Location sourceLocation, final MouseTracker mouseTracker) {
        // Add the mouseTracker
        this.praentMouseTracker = mouseTracker;

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

        lines.get(0).endXProperty().bind(Bindings.add(this.praentMouseTracker.getXProperty(), 1d));
        lines.get(0).endYProperty().bind(Bindings.add(this.praentMouseTracker.getYProperty(), 1d));
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
    }

    public Pane getParentPane() {
        return (Pane) sourceLocation.getParent();
    }

    public void eraseFromParent() {
        Pane parrent = getParentPane();
        for (Line line : lines) {
            parrent.getChildren().remove(line);
        }
    }

    private void addLine() {
        Line line = new Line();
        this.lines.add(line);
        getParentPane().getChildren().add(line);
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
                        double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) - Math.toRadians(180);
                        return startX.get() + Location.RADIUS * Math.cos(angle);
                    }
                },
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {
                        double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) - Math.toRadians(180);
                        return startY.get() + Location.RADIUS * Math.sin(angle);
                    }
                }
        );

    }


    // Bindings for ending in a location
    private Pair<DoubleBinding, DoubleBinding> getEndBindings(final Location sourceLocation, final Location targetLocation) {
        return getEndBindings(sourceLocation.centerXProperty(), sourceLocation.centerYProperty(), targetLocation.centerXProperty(), targetLocation.centerYProperty());
    }
    private Pair<DoubleBinding, DoubleBinding> getEndBindings(final DoubleProperty startX, final DoubleProperty startY, final DoubleProperty endX, final DoubleProperty endY) {

        return new Pair<>(
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {
                        double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get());
                        return endX.get() + Location.RADIUS * Math.cos(angle);
                    }
                },
                new DoubleBinding() {
                    {
                        super.bind(startX, startY, endX, endY);
                    }

                    @Override
                    protected double computeValue() {
                        double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get());
                        return endY.get() + Location.RADIUS * Math.sin(angle);
                    }
                }
        );

    }
}
