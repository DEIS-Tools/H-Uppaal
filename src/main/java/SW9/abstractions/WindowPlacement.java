package SW9.abstractions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class WindowPlacement {

    private final BooleanProperty isMaximized = new SimpleBooleanProperty(false);

    private final DoubleProperty notMaximizedHeight = new SimpleDoubleProperty(0);
    private final DoubleProperty notMaximizedWidth = new SimpleDoubleProperty(0);
    private final DoubleProperty notMaximizedX = new SimpleDoubleProperty(0);
    private final DoubleProperty notMaximizedY = new SimpleDoubleProperty(0);

    public double getNotMaximizedX() {
        return notMaximizedX.get();
    }

    public void setNotMaximizedX(final double notMaximizedX) {
        this.notMaximizedX.set(notMaximizedX);
    }

    public DoubleProperty notMaximizedXProperty() {
        return notMaximizedX;
    }

    public double getNotMaximizedY() {
        return notMaximizedY.get();
    }

    public void setNotMaximizedY(final double notMaximizedY) {
        this.notMaximizedY.set(notMaximizedY);
    }

    public DoubleProperty notMaximizedYProperty() {
        return notMaximizedY;
    }

    public double getNotMaximizedWidth() {
        return notMaximizedWidth.get();
    }

    public void setNotMaximizedWidth(final double notMaximizedWidth) {
        this.notMaximizedWidth.set(notMaximizedWidth);
    }

    public DoubleProperty notMaximizedWidthProperty() {
        return notMaximizedWidth;
    }

    public double getNotMaximizedHeight() {
        return notMaximizedHeight.get();
    }

    public void setNotMaximizedHeight(final double notMaximizedHeight) {
        this.notMaximizedHeight.set(notMaximizedHeight);
    }

    public DoubleProperty notMaximizedHeightProperty() {
        return notMaximizedHeight;
    }

    public boolean isIsMaximized() {
        return isMaximized.get();
    }

    public void setIsMaximized(final boolean isMaximized) {
        this.isMaximized.set(isMaximized);
    }

    public BooleanProperty isMaximizedProperty() {
        return isMaximized;
    }

}
