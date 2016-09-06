package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.Main;
import SW9.utility.DropShadowHelper;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;

public class ModelCanvas extends Pane {

    public static boolean mouseHasLocation = false;
    public static boolean mouseHasEdge = false;

    public ModelCanvas() {
        KeyboardTracker.registerKeybind(new Keybind(new KeyCodeCombination(KeyCode.L), () -> {
            if (!mouseHasLocation) {
                mouseHasLocation = true;
                final Location newLocation = new Location(Main.mouseTracker);

                newLocation.setEffect(DropShadowHelper.generateElevationShadow(22));
                ModelCanvas.this.getChildren().add(newLocation);
            }
        }));
    }


}
