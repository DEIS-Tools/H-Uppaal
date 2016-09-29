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

    // The initial and final locations
    private final Location initialLocation;
    private final Location finalLocation;

    // The frame of the component
    private final Path frame = new Path();

    // The name of the component
    private final Label label;
    private final Rectangle labelContainer;
    private final Polygon labelTriangle;

    private static final double CORNER_SIZE = 50;

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

        // Initialize the frame of the component
        initializeFrame(frame);

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
                frame,
                labelTriangle,
                labelContainer,
                label,
                initialLocation,
                finalLocation
        );
    }


    private void initializeFrame(final Path frame) {
        MoveTo p1 = new MoveTo();
        LineTo p2 = new LineTo();
        LineTo p3 = new LineTo();
        LineTo p4 = new LineTo();
        LineTo p5 = new LineTo();
        LineTo p6 = new LineTo();
        LineTo p7 = new LineTo();

        p1.xProperty().bind(xProperty);
        p1.yProperty().bind(yProperty.add(CORNER_SIZE));

        p2.xProperty().bind(xProperty.add(CORNER_SIZE));
        p2.yProperty().bind(yProperty);

        p3.xProperty().bind(xProperty.add(widthProperty));
        p3.yProperty().bind(yProperty);

        p4.xProperty().bind(xProperty.add(widthProperty));
        p4.yProperty().bind(yProperty.add(heightProperty).subtract(CORNER_SIZE));

        p5.xProperty().bind(xProperty.add(widthProperty).subtract(CORNER_SIZE));
        p5.yProperty().bind(yProperty.add(heightProperty));

        p6.xProperty().bind(xProperty);
        p6.yProperty().bind(yProperty.add(heightProperty));

        p7.xProperty().bind(p1.xProperty());
        p7.yProperty().bind(p1.yProperty());

        frame.getElements().addAll(p1, p2, p3, p4, p5, p6, p7);

        frame.setStroke(Color.BLACK);
        frame.setStrokeWidth(2d);
        frame.setFill(Color.TRANSPARENT);
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
