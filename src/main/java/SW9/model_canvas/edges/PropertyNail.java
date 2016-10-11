package SW9.model_canvas.edges;

import SW9.utility.colors.Color;
import javafx.beans.value.ObservableDoubleValue;

public class PropertyNail extends Nail {

    private final Properties properties = new Properties();

    public PropertyNail(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY) {
        super(centerX, centerY);

        properties.xProperty().bind(xProperty());
        properties.yProperty().bind(yProperty());

        addChildren(properties);
    }

    @Override
    public boolean color(final Color color, final Color.Intensity intensity) {
        properties.color(color, intensity);

        return super.color(color, intensity);
    }
}
