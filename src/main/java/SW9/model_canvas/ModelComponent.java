package SW9.model_canvas;

import SW9.MouseTracker;
import SW9.model_canvas.locations.Location;
import SW9.utility.DragHelper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;


public class ModelComponent extends ModelContainer {

    public final DoubleProperty xProperty;
    public final DoubleProperty yProperty;
    public final DoubleProperty widthProperty;
    public final DoubleProperty heightProperty;

    // The lines of the component
    private final Line intialLocationLine = new Line();
    private final Line topLine = new Line();
    private final Line rightLine = new Line();
    private final Line finalLocationLine = new Line();
    private final Line bottomLine = new Line();
    private final Line leftLine = new Line();

    // The initial and final locations
    private final Location initialLocation;
    private final Location finalLocation;

    // The name of the component
    private final Label label;
    private final Rectangle labelContainer;
    private final Polygon labelTriangle;

    private static final double CORNER_SIZE = 50;

    private final Rectangle hiddenFrame = new Rectangle();

    public ModelComponent(final double x, final double y, final double width, final double height, final String name, final MouseTracker canvasMouseTracker) {

        // Initialize the spacial properties
        xProperty = new SimpleDoubleProperty(x);
        yProperty = new SimpleDoubleProperty(y);
        widthProperty = new SimpleDoubleProperty(width);
        heightProperty = new SimpleDoubleProperty(height);

        DragHelper.makeDraggable(this, mouseEvent -> {
            return (mouseEvent.getX() < xProperty().get() + widthProperty.get()) &&
                    (mouseEvent.getY() < yProperty().get() + heightProperty.get());
        });

        // Initialize locations
        initialLocation = new Location(
                xProperty.add(CORNER_SIZE / 2d),
                yProperty.add(CORNER_SIZE / 2d),
                canvasMouseTracker,
                Location.Type.INITIAL
        );

        finalLocation = new Location(
                xProperty.add(widthProperty.subtract(CORNER_SIZE / 2d)),
                yProperty.add(heightProperty.subtract(CORNER_SIZE / 2d)),
                canvasMouseTracker,
                Location.Type.FINAL
        );

        // Bind the hidden frame
        hiddenFrame.xProperty().bind(xProperty);
        hiddenFrame.yProperty().bind(yProperty);
        hiddenFrame.widthProperty().bind(widthProperty);
        hiddenFrame.heightProperty().bind(heightProperty);

        // Style the hidden frame
        hiddenFrame.setStroke(Color.TRANSPARENT);
        hiddenFrame.setFill(Color.TRANSPARENT);

        // Bind the line where the initial location is placed
        intialLocationLine.startXProperty().bind(xProperty);
        intialLocationLine.startYProperty().bind(yProperty.add(CORNER_SIZE));
        intialLocationLine.endXProperty().bind(xProperty.add(CORNER_SIZE));
        intialLocationLine.endYProperty().bind(yProperty);
        intialLocationLine.setStrokeWidth(2d);

        // Bind the top line
        topLine.startXProperty().bind(xProperty.add(CORNER_SIZE));
        topLine.startYProperty().bind(yProperty);
        topLine.endXProperty().bind(xProperty.add(widthProperty));
        topLine.endYProperty().bind(yProperty);
        topLine.setStrokeWidth(2d);

        // Bind the right line
        rightLine.startXProperty().bind(xProperty.add(widthProperty));
        rightLine.startYProperty().bind(yProperty);
        rightLine.endXProperty().bind(xProperty.add(widthProperty));
        rightLine.endYProperty().bind(yProperty.add(heightProperty).subtract(CORNER_SIZE));
        rightLine.setStrokeWidth(2d);

        // Bind the line where the final location is placed
        finalLocationLine.startXProperty().bind(xProperty.add(widthProperty));
        finalLocationLine.startYProperty().bind(yProperty.add(heightProperty).subtract(CORNER_SIZE));
        finalLocationLine.endXProperty().bind(xProperty.add(widthProperty).subtract(CORNER_SIZE));
        finalLocationLine.endYProperty().bind(yProperty.add(heightProperty));
        finalLocationLine.setStrokeWidth(2d);

        // Bind the bottom line
        bottomLine.startXProperty().bind(xProperty.add(widthProperty).subtract(CORNER_SIZE));
        bottomLine.startYProperty().bind(yProperty.add(heightProperty));
        bottomLine.endXProperty().bind(xProperty);
        bottomLine.endYProperty().bind(yProperty.add(heightProperty));
        bottomLine.setStrokeWidth(2d);

        // Bind the left line
        leftLine.startXProperty().bind(xProperty);
        leftLine.startYProperty().bind(yProperty.add(heightProperty));
        leftLine.endXProperty().bind(xProperty);
        leftLine.endYProperty().bind(yProperty.add(CORNER_SIZE));
        leftLine.setStrokeWidth(2d);

        // Initialize properties for the name of the component
        labelContainer = new Rectangle();
        label = new Label(name);
        labelTriangle = new Polygon(
                xProperty.get() + CORNER_SIZE / 2, yProperty.get() + CORNER_SIZE / 2,
                xProperty.get() + CORNER_SIZE, yProperty.get(),
                xProperty.get() + CORNER_SIZE, yProperty.get() + CORNER_SIZE / 2
        );

        // Bind the properties for the name of the component
        labelContainer.xProperty().bind(xProperty.add(CORNER_SIZE));
        labelContainer.yProperty().bind(yProperty);
        labelContainer.widthProperty().bind(widthProperty.subtract(CORNER_SIZE));
        labelContainer.heightProperty().set(CORNER_SIZE / 2);

        label.layoutXProperty().bind(labelContainer.xProperty());
        label.layoutYProperty().bind(labelContainer.yProperty());


        // Style the name of the component
        Color redColor = Color.web("#D50000");
        labelContainer.fillProperty().set(redColor);
        labelContainer.setStrokeWidth(1d);
        labelContainer.setStroke(redColor);
        labelContainer.setStrokeType(StrokeType.OUTSIDE);
        labelContainer.setStrokeLineJoin(StrokeLineJoin.ROUND);
        label.textFillProperty().set(Color.WHITE);
        label.alignmentProperty().set(Pos.CENTER);
        label.setPrefHeight(CORNER_SIZE / 2);
        label.paddingProperty().set(new Insets(0, 0, 0, 10));
        label.getStyleClass().add("subhead");
        labelTriangle.setFill(redColor);
        labelTriangle.setStrokeWidth(1d);
        labelTriangle.setStroke(redColor);
        labelTriangle.setStrokeType(StrokeType.OUTSIDE);
        labelTriangle.setStrokeLineJoin(StrokeLineJoin.ROUND);
        labelTriangle.layoutXProperty().bind(xProperty.subtract(x));
        labelTriangle.layoutYProperty().bind(yProperty.subtract(y));

        addChildren(
                hiddenFrame,
                intialLocationLine,
                topLine,
                rightLine,
                finalLocationLine,
                bottomLine,
                leftLine,
                labelTriangle,
                labelContainer,
                label,
                initialLocation,
                finalLocation
        );
    }


    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    @Override
    public DoubleProperty xProperty() {
        return xProperty;
    }

    @Override
    public DoubleProperty yProperty() {
        return yProperty;
    }

    @Override
    public ObservableDoubleValue getXLimit() {
        return widthProperty;
    }

    @Override
    public ObservableDoubleValue getYLimit() {
        return heightProperty;
    }
}
