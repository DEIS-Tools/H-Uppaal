package SW9.model_canvas.edges;

import SW9.utility.colors.Color;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableDoubleValue;

public class PropertyNail extends Nail {

    private final Properties properties;

    public PropertyNail(final ObservableDoubleValue centerX,
                        final ObservableDoubleValue centerY,
                        final StringProperty selectProperty,
                        final StringProperty guardProperty,
                        final StringProperty updateProperty,
                        final StringProperty syncProperty) {
        super(centerX, centerY);

        // Create new properties and propagate binders from edge downwards
        properties = new Properties(
                new Properties.Entry(Properties.Type.EDGE_SELECT, selectProperty),
                new Properties.Entry(Properties.Type.EDGE_GUARD, guardProperty),
                new Properties.Entry(Properties.Type.EDGE_UPDATE, updateProperty),
                new Properties.Entry(Properties.Type.EDGE_SYNC, syncProperty)
        );

        properties.xProperty().bind(xProperty());
        properties.yProperty().bind(yProperty());

        addChildren(properties);
    }

    @Override
    public boolean color(final Color color, final Color.Intensity intensity) {
        properties.color(color, intensity);

        return super.color(color, intensity);
    }

    @Override
    public void styleSelected() {
        super.styleSelected();

        // TODO: Add .selected to elements in the properties object
    }

    @Override
    public void styleDeselected() {
        super.styleDeselected();

        // TODO: remove .selected from elements in the properties object
    }


}
