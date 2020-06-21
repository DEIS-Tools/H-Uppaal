package dk.cs.aau.huppaal.utility.helpers;

import dk.cs.aau.huppaal.abstractions.Edge;
import dk.cs.aau.huppaal.controllers.JorkController;
import dk.cs.aau.huppaal.controllers.LocationController;
import dk.cs.aau.huppaal.controllers.SubComponentController;
import dk.cs.aau.huppaal.presentations.CanvasPresentation;
import dk.cs.aau.huppaal.utility.mouse.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class MouseCircular implements Circular {
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty radius = new SimpleDoubleProperty(10);
    private final SimpleDoubleProperty scale = new SimpleDoubleProperty(1d);
    private final MouseTracker mouseTracker = CanvasPresentation.mouseTracker;

    public MouseCircular(Edge edge){
        //Set the initial x and y coordinates of the circular
        x.set(mouseTracker.getGridX());
        y.set(mouseTracker.getGridY());

        //Make sure that the circular follows the mouse after the mouse button is released
        mouseTracker.registerOnMouseMovedEventHandler(event -> {
            x.set(mouseTracker.getGridX());
            y.set(mouseTracker.getGridY());
        });

        //Make sure that the circular follows the mouse if the mouse button is not released
        mouseTracker.registerOnMouseDraggedEventHandler(event -> {
            x.set(mouseTracker.getGridX());
            y.set(mouseTracker.getGridY());
        });

        //Set the new source to the clicked circular
        EventHandler<MouseEvent> eventHandler = event -> {
            if (SelectHelper.getSelectedElements().get(0) instanceof LocationController) {
                edge.setSourceLocation(((LocationController) SelectHelper.getSelectedElements().get(0)).getLocation());
                edge.setSourceSubComponent(null);
                edge.setSourceJork(null);
            } else if (SelectHelper.getSelectedElements().get(0) instanceof SubComponentController){
                edge.setSourceLocation(null);
                edge.setSourceSubComponent(((SubComponentController) SelectHelper.getSelectedElements().get(0)).getSubComponent());
                edge.setSourceJork(null);
            } else if (SelectHelper.getSelectedElements().get(0) instanceof JorkController) {
                edge.setSourceLocation(null);
                edge.setSourceSubComponent(null);
                edge.setSourceJork(((JorkController) SelectHelper.getSelectedElements().get(0)).getJork());
            }
        };

        //Set register the eventHandler
        mouseTracker.registerOnMouseClickedEventHandler(eventHandler);

        //Unregister the eventHandler when a new source is found
        mouseTracker.registerOnMouseClickedEventHandler(event -> {
            if (SelectHelper.getSelectedElements().get(0) instanceof LocationController ||
                    SelectHelper.getSelectedElements().get(0) instanceof JorkController ||
                    SelectHelper.getSelectedElements().get(0) instanceof SubComponentController) {
                mouseTracker.unregisterOnMouseClickedEventHandler(eventHandler);
            }
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
