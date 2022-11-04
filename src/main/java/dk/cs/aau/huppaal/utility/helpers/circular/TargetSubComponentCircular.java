package dk.cs.aau.huppaal.utility.helpers.circular;

import dk.cs.aau.huppaal.abstractions.SubComponent;
import dk.cs.aau.huppaal.utility.helpers.Circular;
import javafx.beans.property.SimpleDoubleProperty;

import static dk.cs.aau.huppaal.presentations.CanvasPresentation.GRID_SIZE;

public class TargetSubComponentCircular extends AbstractCircular implements Circular {
    public TargetSubComponentCircular(SubComponent targetSubComponent) {
        super(new SimpleDoubleProperty(), new SimpleDoubleProperty(), targetSubComponent.getComponent().getInitialLocation().radiusProperty(), targetSubComponent.getComponent().getInitialLocation().scaleProperty());
        this.x.bind(targetSubComponent.xProperty().add(GRID_SIZE * 2));
        this.y.bind(targetSubComponent.yProperty().add(GRID_SIZE * 2));
    }
}
