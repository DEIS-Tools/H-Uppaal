package dk.cs.aau.huppaal.utility.helpers;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Edge;
import dk.cs.aau.huppaal.abstractions.Location;
import dk.cs.aau.huppaal.controllers.EdgeController;
import dk.cs.aau.huppaal.controllers.LocationController;
import dk.cs.aau.huppaal.presentations.CanvasPresentation;
import dk.cs.aau.huppaal.utility.mouse.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class MouseCircular implements Circular {
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty radius = new SimpleDoubleProperty(10);
    private final SimpleDoubleProperty scale = new SimpleDoubleProperty(1d);

    public MouseCircular(Edge edge){
        MouseTracker mouseTracker = CanvasPresentation.mouseTracker;

        mouseTracker.registerOnMouseMovedEventHandler(event -> {
            x.set(mouseTracker.getGridX());
            y.set(mouseTracker.getGridY());
        });

        mouseTracker.registerOnMouseClickedEventHandler(event -> {
            edge.sourceCircularProperty().set(((LocationController) SelectHelper.getSelectedElements().get(0)).getLocation());
        });
    }

    @Override
    public DoubleProperty radiusProperty() {
        return radius;
    }

    @Override
    public DoubleProperty scaleProperty() {
        return scale;
    }

    @Override
    public DoubleProperty xProperty() {
        return x;
    }

    @Override
    public DoubleProperty yProperty() {
        return y;
    }

    @Override
    public double getX() {
        return x.get();
    }

    @Override
    public double getY() {
        return y.get();
    }
}
