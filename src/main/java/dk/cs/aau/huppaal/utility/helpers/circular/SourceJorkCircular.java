package dk.cs.aau.huppaal.utility.helpers.circular;

import dk.cs.aau.huppaal.abstractions.Jork;
import dk.cs.aau.huppaal.presentations.JorkPresentation;
import dk.cs.aau.huppaal.utility.helpers.Circular;
import javafx.beans.property.SimpleDoubleProperty;

public class SourceJorkCircular extends AbstractCircular implements Circular {
    public SourceJorkCircular(Jork jork) {
        super(new SimpleDoubleProperty(), new SimpleDoubleProperty(), new SimpleDoubleProperty(), new SimpleDoubleProperty());
        x.bind(jork.xProperty().add(JorkPresentation.JORK_WIDTH / 2));
        y.bind(jork.yProperty().add(JorkPresentation.JORK_HEIGHT + JorkPresentation.JORK_Y_TRANSLATE));
    }
}
