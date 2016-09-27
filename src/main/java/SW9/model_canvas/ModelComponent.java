package SW9.model_canvas;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Parent;
import javafx.scene.shape.Line;

public class ModelComponent extends Parent {


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

    private final InitialLocation initialLocation;
    private final FinalLocation finalLocation;

    private static final double CORNER_SIZE = 50;

    public ModelComponent(final double x, final double y, final double width, final double height) {

        // Initialize the properties
        xProperty = new SimpleDoubleProperty(x);
        yProperty = new SimpleDoubleProperty(y);
        widthProperty = new SimpleDoubleProperty(width);
        heightProperty = new SimpleDoubleProperty(height);

        // Initialize locations
        initialLocation = new InitialLocation(
                xProperty.add(CORNER_SIZE / 2d),
                yProperty.add(CORNER_SIZE / 2d)
        );



        finalLocation = new FinalLocation(
                xProperty.add(widthProperty.subtract(CORNER_SIZE / 2d)),
                yProperty.add(heightProperty.subtract(CORNER_SIZE / 2d))
        );


        // Disable the locations mouse-trackers
        initialLocation.localMouseTracker.disable();
        finalLocation.localMouseTracker.disable();

        // Bind the line where the initial location is placed
        intialLocationLine.startXProperty().bind(xProperty);
        intialLocationLine.startYProperty().bind(yProperty.add(CORNER_SIZE));
        intialLocationLine.endXProperty().bind(xProperty.add(CORNER_SIZE));
        intialLocationLine.endYProperty().bind(yProperty);

        // Bind the top line
        topLine.startXProperty().bind(xProperty.add(CORNER_SIZE));
        topLine.startYProperty().bind(yProperty);
        topLine.endXProperty().bind(xProperty.add(widthProperty));
        topLine.endYProperty().bind(yProperty);

        // Bind the right line
        rightLine.startXProperty().bind(xProperty.add(widthProperty));
        rightLine.startYProperty().bind(yProperty);
        rightLine.endXProperty().bind(xProperty.add(widthProperty));
        rightLine.endYProperty().bind(yProperty.add(heightProperty).subtract(CORNER_SIZE));

        // Bind the line where the final location is placed
        finalLocationLine.startXProperty().bind(xProperty.add(widthProperty));
        finalLocationLine.startYProperty().bind(yProperty.add(heightProperty).subtract(CORNER_SIZE));
        finalLocationLine.endXProperty().bind(xProperty.add(widthProperty).subtract(CORNER_SIZE));
        finalLocationLine.endYProperty().bind(yProperty.add(heightProperty));

        // Bind the bottom line
        bottomLine.startXProperty().bind(xProperty.add(widthProperty).subtract(CORNER_SIZE));
        bottomLine.startYProperty().bind(yProperty.add(heightProperty));
        bottomLine.endXProperty().bind(xProperty);
        bottomLine.endYProperty().bind(yProperty.add(heightProperty));

        // Bind the left line
        leftLine.startXProperty().bind(xProperty);
        leftLine.startYProperty().bind(yProperty.add(heightProperty));
        leftLine.endXProperty().bind(xProperty);
        leftLine.endYProperty().bind(yProperty.add(CORNER_SIZE));

        getChildren().addAll(intialLocationLine, topLine, rightLine, finalLocationLine, bottomLine, leftLine, initialLocation, finalLocation);
    }


}
