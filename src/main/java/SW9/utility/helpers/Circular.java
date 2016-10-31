package SW9.utility.helpers;

import javafx.beans.property.DoubleProperty;

public interface Circular extends LocationAware {
    DoubleProperty radiusProperty();

    DoubleProperty scaleProperty();
}
