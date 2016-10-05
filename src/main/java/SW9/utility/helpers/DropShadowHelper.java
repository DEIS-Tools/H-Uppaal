package SW9.utility.helpers;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class DropShadowHelper {

    private DropShadowHelper() {

    }

    public static DropShadow generateElevationShadow(final double elevation) {
        final DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(elevation * 1.0f);
        dropShadow.setOffsetY(elevation / 6);

        double alpha = 0.32f + elevation / 100f;
        dropShadow.setColor(Color.web("rgba(0,0,0," + alpha + ")"));

        return dropShadow;
    }

}
