package SW9.utility.helpers;

import javafx.beans.property.DoubleProperty;

public interface LocationAware {
    DoubleProperty xProperty();

    DoubleProperty yProperty();

    double getX();

    double getY();
}
