package SW9.model_canvas.arrow_heads;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public abstract class ChannelSenderArrowHead extends ArrowHead {

    private static final double TRIANGLE_LENGTH = 25d;
    private static final double TRIANGLE_WIDTH = 25d;

    // Properties for drawing the triangle and binding the label in the centroid of it
    private ObservableDoubleValue ax = xProperty();
    private ObservableDoubleValue ay = yProperty().subtract(getHeadHeight() - TRIANGLE_LENGTH);
    private ObservableDoubleValue bx = Bindings.subtract(ax, TRIANGLE_WIDTH / 2);
    private ObservableDoubleValue by = Bindings.subtract(ay, TRIANGLE_LENGTH);
    private ObservableDoubleValue cx = Bindings.add(ax, TRIANGLE_WIDTH / 2);
    private ObservableDoubleValue cy = Bindings.subtract(ay, TRIANGLE_LENGTH);

    private BooleanProperty isUrgent = new SimpleBooleanProperty();

    public ChannelSenderArrowHead() {
        super();
        getChildren().addAll(initializeTriangle(), initializeLabel());
    }

    private Path initializeTriangle() {
        final Path triangle = new Path();

        MoveTo start = new MoveTo();
        LineTo l1 = new LineTo();
        LineTo l2 = new LineTo();
        LineTo l3 = new LineTo();

        start.xProperty().bind(ax);
        start.yProperty().bind(ay);

        l1.xProperty().bind(bx);
        l1.yProperty().bind(by);

        l2.xProperty().bind(cx);
        l2.yProperty().bind(cy);

        l3.xProperty().bind(ax);
        l3.yProperty().bind(ay);

        triangle.setFill(Color.BLACK);
        triangle.getElements().addAll(start, l1, l2, l3);

        return triangle;
    }


    private Label initializeLabel() {
        final Label label = new Label();

        // Add the caption text-size class, and make the text white
        label.getStyleClass().addAll("caption", "white-text");

        DoubleBinding lx = new DoubleBinding() {
            {
                super.bind(ax, bx, cx, label.widthProperty());
            }

            @Override
            protected double computeValue() {
                return (ax.get() + bx.get() + cx.get()) / 3 - label.widthProperty().get() / 2;
            }
        };

        DoubleBinding ly = new DoubleBinding() {
            {
                super.bind(ay, by, cy, label.heightProperty());
            }

            @Override
            protected double computeValue() {
                return (ay.get() + by.get() + cy.get()) / 3 - label.heightProperty().get() / 2;
            }
        };

        // Bind the label to the centroid of the triangle
        label.layoutXProperty().bind(lx);
        label.layoutYProperty().bind(ly);

        // Display the label U - for urgent
        label.setText("U");

        // Bind the isUrgent stringBinder to hide and show the label
        label.opacityProperty().bind(new When(isUrgent).then(1d).otherwise(0d));

        // Rotate the label back so that it is always displayed as U
        label.rotateProperty().bind(this.rotateProperty().multiply(-1));

        return label;
    }

    @Override
    public double getHeadHeight() {
        return TRIANGLE_LENGTH;
    }

    @Override
    public double getHeadWidth() {
        return TRIANGLE_WIDTH;
    }

    public BooleanProperty isUrgentProperty() {
        return isUrgent;
    }

}
