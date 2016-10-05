package SW9.model_canvas.synchronization;

import SW9.model_canvas.Parent;
import SW9.utility.helpers.DragHelper;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ChannelBox extends Parent implements MouseTrackable {

    // Used to register listeners, e.g. make the ChannelBox draggable
    private final MouseTracker localMouseTracker = new MouseTracker(this);

    // Used to define the location of the ChannelBox
    private DoubleProperty xProperty = new SimpleDoubleProperty();
    private DoubleProperty yProperty = new SimpleDoubleProperty();

    public ChannelBox() {
        // Container for the ChannelBox
        final Rectangle box = new Rectangle(100, 40);
        box.getStyleClass().add("channel-box");

        // Label for the synchronization
        final Label label = new Label("sync");
        label.translateXProperty().bind(label.widthProperty().divide(2));
        label.translateYProperty().bind(label.heightProperty().divide(2));
        label.setTextFill(Color.WHITE);
        label.getStyleClass().add("subhead");

        // Adjust the width of the box accordingly to the text width
        box.widthProperty().bind(label.widthProperty().add(10));
        box.heightProperty().bind(label.heightProperty().add(10));

        // Add a stack-pane with both the box and the label (will center the label)
        final StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(box, label);
        stackPane.layoutXProperty().bind(xProperty);
        stackPane.layoutYProperty().bind(yProperty);

        // Add the children to the view
        addChildren(stackPane);

        // Make us draggable
        DragHelper.makeDraggable(this);
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
    public MouseTracker getMouseTracker() {
        return localMouseTracker;
    }
}
