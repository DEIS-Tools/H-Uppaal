package SW9.model_canvas.edges;

import SW9.model_canvas.Parent;
import SW9.utility.helpers.LocationAware;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class Properties extends Parent implements LocationAware {

    private final DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    private final DoubleProperty yProperty = new SimpleDoubleProperty(0d);

    private static final double ICON_WIDTH = 20d;
    private static final double VALUE_WIDTH = 120d;

    public Properties(final ObservableDoubleValue x, final ObservableDoubleValue y) {

        // Bind the x and y properties
        xProperty().bind(x);
        yProperty().bind(y);

        this.getStyleClass().add("edge-properties");

        VBox propertiesBox = new VBox();
        propertiesBox.getChildren().addAll(
                generatePropertyBox(":", "id : id_t"),
                generatePropertyBox("<", "guard < value"),
                generatePropertyBox("!?", "channel!"),
                generatePropertyBox("=", "var = 42")

        );
        propertiesBox.layoutXProperty().bind(xProperty());
        propertiesBox.layoutYProperty().bind(yProperty());

        getChildren().add(propertiesBox);
    }

    private Parent generateValueBox(final String value, final DoubleProperty sharedHeightProperty) {
        // The textField for the value of the given property
        final JFXTextField textField = new JFXTextField(value);
        textField.getStyleClass().addAll("body1", "value-text-field");
        textField.setPrefWidth(VALUE_WIDTH);

        // Container for the stack pane containing the value
        final Rectangle box = new Rectangle(VALUE_WIDTH, 0);
        box.getStyleClass().add("value-background");
        box.heightProperty().bind(textField.heightProperty());

        // Bind the shared height to the height of the box
        sharedHeightProperty.bind(box.heightProperty());

        // Add a parent that should contain both the container and the value textField
        final Parent parent = new Parent();
        parent.getStyleClass().add("value-container");
        parent.addChildren(box, textField);

        return parent;
    }

    private StackPane generateIconBox(final String iconString, final DoubleProperty height) {
        // Container for the background of the stack pane containing an icon
        final Rectangle box = new Rectangle(ICON_WIDTH, 0);
        box.getStyleClass().add("icon-background");
        box.heightProperty().bind(height);

        // The label representing an icon
        final Label label = new Label(iconString);
        label.getStyleClass().addAll("subhead", "icon-label");

        // Add a stack-pane with both the container and the icon label (will center label)
        final StackPane stackPane = new StackPane();
        stackPane.getStyleClass().add("icon-container");
        stackPane.getChildren().addAll(box, label);

        // Align the box in the top left corner
        StackPane.setAlignment(box, Pos.TOP_LEFT);

        return stackPane;
    }

    private HBox generatePropertyBox(final String iconString, final String value) {
        final HBox propertyBox = new HBox();

        // A shared property to ensure that the icon box and the value box is consistent in height
        final DoubleProperty sharedHeightProperty = new SimpleDoubleProperty();

        // Generate the value and the icon for the property box
        final Parent valueBox = generateValueBox(value, sharedHeightProperty);
        final StackPane iconBox = generateIconBox(iconString, sharedHeightProperty);

        // Add the boxes to this property box
        propertyBox.getChildren().addAll(iconBox, valueBox);

        return propertyBox;
    }


    public DoubleProperty xProperty() {
        return xProperty;
    }

    public DoubleProperty yProperty() {
        return yProperty;
    }
}
