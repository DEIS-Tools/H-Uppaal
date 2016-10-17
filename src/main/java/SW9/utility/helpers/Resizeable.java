package SW9.utility.helpers;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.layout.StackPane;

public interface Resizeable {
    ReadOnlyDoubleProperty widthProperty();

    void setWidth(final double width);

    ReadOnlyDoubleProperty heightProperty();

    void setHeight(final double height);

    void setX(final double x);

    void setY(final double y);

    StackPane getRegionContainer();
}
