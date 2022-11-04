package dk.cs.aau.huppaal.utility.helpers.circular;

import dk.cs.aau.huppaal.abstractions.SubComponent;
import dk.cs.aau.huppaal.utility.helpers.Circular;
import javafx.beans.property.SimpleDoubleProperty;

import static dk.cs.aau.huppaal.presentations.CanvasPresentation.GRID_SIZE;

public class SourceSubComponentCircular extends AbstractCircular implements Circular {
    public SourceSubComponentCircular(SubComponent sourceSubComponent) {
        super(new SimpleDoubleProperty(), new SimpleDoubleProperty(), sourceSubComponent.getComponent().getInitialLocation().radiusProperty(), sourceSubComponent.getComponent().getInitialLocation().scaleProperty());
        this.x.bind(sourceSubComponent.xProperty().add(sourceSubComponent.widthProperty()).subtract(GRID_SIZE * 2));
        this.y.bind(sourceSubComponent.yProperty().add(sourceSubComponent.heightProperty()).subtract(GRID_SIZE * 2));
    }
}
