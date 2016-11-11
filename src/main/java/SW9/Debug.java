package SW9;

import SW9.utility.colors.Color;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Debug {

    public static Color draggableAreaColor = Color.PINK;
    public static Color.Intensity draggableAreaColorIntensity = Color.Intensity.I500;

    public static Color hoverableAreaColor = Color.LIGHT_BLUE;
    public static Color.Intensity hoverableAreaColorIntensity = Color.Intensity.I500;

    public static BooleanProperty debugModeEnabled = new SimpleBooleanProperty(false);

    public static DoubleBinding draggableAreaOpacity = new When(debugModeEnabled).then(0.5).otherwise(0d);
    public static DoubleBinding hoverableAreaOpacity = new When(debugModeEnabled).then(0.5).otherwise(0d);

}
