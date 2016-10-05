package SW9.model_canvas.edges;

import SW9.model_canvas.Parent;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class Properties extends Parent {

    public final DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    public final DoubleProperty yProperty = new SimpleDoubleProperty(0d);

    private static final double ICON_WIDTH = 20d;
    private static final double VALUE_WIDTH = 120d;

    public Properties(final ObservableDoubleValue x, final ObservableDoubleValue y) {

        // Bind the x and y properties
        xProperty.bind(x);
        yProperty.bind(y);

        this.getStyleClass().add("edge-properties");

        VBox propertiesBox = new VBox();
        propertiesBox.getChildren().addAll(
                generatePropertyBox(":", ":idt\n[asidjha]"),
                generatePropertyBox("<", ":idt[asidjha]"),
                generatePropertyBox("!?", ":idt[as\nidjha]"),
                generatePropertyBox("=", ":idt[asi\ndjha]")

        );
        propertiesBox.layoutXProperty().bind(xProperty);
        propertiesBox.layoutYProperty().bind(yProperty);

        getChildren().add(propertiesBox);
    }

    private StackPane generateValueStackPane(final String value, final DoubleProperty sharedHeightProperty) {

        // The label for the value of the given property
        final Label label = new Label(value);
        label.getStyleClass().addAll("subhead", "value-label");
        label.setPrefWidth(VALUE_WIDTH);

        // Container for the stack pane containing the value
        final Rectangle box = new Rectangle(VALUE_WIDTH, 0);
        box.getStyleClass().add("value-background");
        box.heightProperty().bind(label.heightProperty());
        box.translateYProperty().bind(label.heightProperty().divide(-2));
        sharedHeightProperty.bind(box.heightProperty());

        // Add a stack-pane with both the container and the value label (will center label)
        final StackPane stackPane = new StackPane();
        stackPane.getStyleClass().add("value-container");
        stackPane.getChildren().addAll(box, label);

        box.heightProperty().addListener((observable, oldValue, newValue) -> System.out.println("value box" + newValue));

        // Add the children to the view
        return stackPane;
    }

    private StackPane generateIconStackPane(final String iconString, final DoubleProperty sharedHeightProperty) {
        // Container for the stack pane containing an icon
        final Rectangle box = new Rectangle(ICON_WIDTH, 0);
        box.getStyleClass().add("icon-background");
        box.heightProperty().bind(sharedHeightProperty);
        box.translateYProperty().bind(box.heightProperty().divide(-2));

        // The label for the icon string
        final Label label = new Label(iconString);
        label.getStyleClass().addAll("subhead", "icon-label");

        // Add a stack-pane with both the container and the icon label (will center label)
        final StackPane stackPane = new StackPane();
        stackPane.getStyleClass().add("icon-container");
        stackPane.getChildren().addAll(box, label);

        box.heightProperty().addListener((observable, oldValue, newValue) -> System.out.println("icon box" + newValue));

        // Add the children to the view
        return stackPane;
    }

    private HBox generatePropertyBox(final String iconString, final String value) {
        final HBox propertyBox = new HBox();

        final DoubleProperty sharedHeightProperty = new SimpleDoubleProperty(2);
        final StackPane valuePane = generateValueStackPane(value, sharedHeightProperty);
        final StackPane iconPane = generateIconStackPane(iconString, sharedHeightProperty);
        propertyBox.getChildren().addAll(iconPane, valuePane);

        return propertyBox;
    }


}
