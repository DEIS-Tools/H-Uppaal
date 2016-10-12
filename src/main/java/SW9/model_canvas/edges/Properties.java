package SW9.model_canvas.edges;

import SW9.model_canvas.ModelCanvas;
import SW9.model_canvas.Parent;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.LocationAware;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;

public class Properties extends Parent implements LocationAware, MouseTrackable, Colorable {

    private Color color = null;
    private Color.Intensity intensity = null;
    private boolean colorIsSet = false;

    private final DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    private final DoubleProperty yProperty = new SimpleDoubleProperty(0d);

    private static final double ICON_WIDTH = 20d;
    private static final double VALUE_WIDTH = 120d;

    private final MouseTracker localMouseTracker = new MouseTracker(this);

    private final ArrayList<Node> hiddenElements = new ArrayList<>();
    private final ArrayList<Shape> iconBoxes = new ArrayList<>();

    public enum Type {
        EDGE_SELECT(":"), EDGE_GUARD("<"), EDGE_UPDATE("="), EDGE_SYNC("!?"), LOCATION_NAME("@"), LOCATION_INVARIANT("I");

        public final String icon;

        Type (final String icon) {
            this.icon = icon;
        }
    }

    public static class Entry {
        public Properties.Type type;
        public StringProperty stringBinder;
        public Entry(final Properties.Type type, final StringProperty stringBinder) {
            this.type = type;
            this.stringBinder = stringBinder;
        }
    }

    public Properties(final Entry ... entries) {
        this.getStyleClass().add("edge-properties");

        final VBox propertiesBox = new VBox();

        for(final Entry entry : entries) {
            propertiesBox.getChildren().add(generatePropertyBox(entry));
        }

        propertiesBox.layoutXProperty().bind(xProperty());
        propertiesBox.layoutYProperty().bind(yProperty());

        getChildren().add(propertiesBox);

        // Hide the elements in hiddenElements (input fields) when we are not hovering the properties
        localMouseTracker.registerOnMouseEnteredEventHandler(event -> {
            // Do not snow if we have a location on the mouse
            if(ModelCanvas.mouseHasLocation()) {
                hiddenElements.forEach(node -> node.setVisible(false));
            } else {
                hiddenElements.forEach(node -> node.setVisible(true));
            }
        });
        localMouseTracker.registerOnMouseExitedEventHandler(event -> hiddenElements.forEach(node -> node.setVisible(false)));
        hiddenElements.forEach(node -> node.setVisible(false));
    }

    private Parent generateValueBox(final String value, final DoubleProperty sharedHeightProperty, final StringProperty binder) {
        // The textField for the value of the given stringBinder
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

        hiddenElements.add(parent);

        binder.bind(textField.textProperty());

        return parent;
    }

    private StackPane generateIconBox(final String iconString, final DoubleProperty height) {
        // Container for the background of the stack pane containing an icon
        final Rectangle box = new Rectangle(ICON_WIDTH, 0);
        box.getStyleClass().add("icon-background");
        box.heightProperty().bind(height);

        iconBoxes.add(box);

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

    private HBox generatePropertyBox(final Entry propertyEntry) {
        final HBox propertyBox = new HBox();

        // A shared stringBinder to ensure that the icon box and the value box is consistent in height
        final DoubleProperty sharedHeightProperty = new SimpleDoubleProperty();

        // Generate the value and the icon for the stringBinder box
        final Parent valueBox = generateValueBox(propertyEntry.stringBinder.get(), sharedHeightProperty, propertyEntry.stringBinder);
        final StackPane iconBox = generateIconBox(propertyEntry.type.icon, sharedHeightProperty);

        // Add the boxes to this stringBinder box
        propertyBox.getChildren().addAll(iconBox, valueBox);

        return propertyBox;
    }


    @Override
    public MouseTracker getMouseTracker() {
        return localMouseTracker;
    }

    public DoubleProperty xProperty() {
        return xProperty;
    }

    public DoubleProperty yProperty() {
        return yProperty;
    }

    @Override
    public boolean isColored() {
        return colorIsSet;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return intensity;
    }

    @Override
    public boolean color(final Color color, final Color.Intensity intensity) {
        iconBoxes.forEach(node -> {
            node.setFill(color.getColor(intensity));
            node.setStroke(color.getColor(intensity.next(2)));
        });

        return true;
    }

    @Override
    public void resetColor() {
        resetColor(Color.GREY_BLUE, Color.Intensity.I700); // default color
    }

    @Override
    public void resetColor(final Color color, final Color.Intensity intensity) {
        color(color, intensity);
        colorIsSet = false;
    }
}
