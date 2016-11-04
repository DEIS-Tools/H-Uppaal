package SW9.presentations;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Link extends Group {

    private final DoubleProperty startX;
    private final DoubleProperty endX;
    private final DoubleProperty startY;
    private final DoubleProperty endY;
    private final static double HOVER_LINE_STROKE_WIDTH = 10d;

    public Link() {
        this(0,0,0,0);
    }

    public Link(final double startX, final double endX, final double startY, final double endY) {

        // Set the intial values
        this.startX = new SimpleDoubleProperty(startX);
        this.endX = new SimpleDoubleProperty(endX);
        this.startY = new SimpleDoubleProperty(startY);
        this.endY = new SimpleDoubleProperty(endY);

        // Create the two lines
        final Line shownLine = new Line();
        final Line hiddenHoverLine = new Line();

        // Add them
        getChildren().addAll(shownLine, hiddenHoverLine);

        // Bind the shown line
        shownLine.startXProperty().bind(this.startX);
        shownLine.endXProperty().bind(this.endX);
        shownLine.startYProperty().bind(this.startY);
        shownLine.endYProperty().bind(this.endY);

        // Bind the hidden line
        hiddenHoverLine.startXProperty().bind(shownLine.startXProperty());
        hiddenHoverLine.endXProperty().bind(shownLine.endXProperty());
        hiddenHoverLine.startYProperty().bind(shownLine.startYProperty());
        hiddenHoverLine.endYProperty().bind(shownLine.endYProperty());

        // Style the hidden line
        hiddenHoverLine.setStrokeWidth(HOVER_LINE_STROKE_WIDTH);
        hiddenHoverLine.setOpacity(0.3);
    }

    public double getStartX() {
        return startX.get();
    }

    public DoubleProperty startXProperty() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX.set(startX);
    }

    public double getEndX() {
        return endX.get();
    }

    public DoubleProperty endXProperty() {
        return endX;
    }

    public void setEndX(double endX) {
        this.endX.set(endX);
    }

    public double getStartY() {
        return startY.get();
    }

    public DoubleProperty startYProperty() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY.set(startY);
    }

    public double getEndY() {
        return endY.get();
    }

    public DoubleProperty endYProperty() {
        return endY;
    }

    public void setEndY(double endY) {
        this.endY.set(endY);
    }

}
