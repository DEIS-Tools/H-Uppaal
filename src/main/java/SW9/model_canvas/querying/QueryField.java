package SW9.model_canvas.querying;

import SW9.backend.UPPAALDriver;
import SW9.model_canvas.ModelContainer;
import SW9.model_canvas.Parent;
import SW9.utility.colors.Color;
import SW9.utility.helpers.LocationAware;
import com.uppaal.engine.EngineException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;

public class QueryField extends Parent implements LocationAware {

    private final DoubleProperty xProperty = new SimpleDoubleProperty();
    private final DoubleProperty yProperty = new SimpleDoubleProperty();
    private final TextField textField = new TextField();
    private static final double FIELD_WIDTH = 500;

    public QueryField(final double x, final double y, final ModelContainer modelContainer) {
        xProperty().setValue(x);
        yProperty().setValue(y);

        textField.layoutXProperty().bind(xProperty());
        textField.layoutYProperty().bind(yProperty());
        textField.setPrefWidth(FIELD_WIDTH);
        this.textField.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        addChild(textField);
        textField.setOnKeyPressed(event -> {

            if (event.getCode() != KeyCode.ENTER) return;

            final String query = textField.getText();

            try {
                final Color color;
                if (UPPAALDriver.verify(query, modelContainer)) {
                    color = Color.GREEN;
                } else {
                    color = Color.RED;
                }

                textField.setBackground(new Background(new BackgroundFill(color.getColor(Color.Intensity.I500), CornerRadii.EMPTY, Insets.EMPTY)));
                textField.setStyle("-fx-text-fill: #ffffff;");
            } catch (EngineException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public DoubleProperty xProperty() {
        return xProperty;
    }

    @Override
    public DoubleProperty yProperty() {
        return yProperty;
    }

}
