package SW9;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class KeyboardTracker {
    private static List<Keybind> keybinds = new ArrayList<>();

    public static final EventHandler<KeyEvent> handleKeyPress = event -> {
        keybinds.removeIf(keybind -> keybind == null);

        for (final Keybind keybind : keybinds) {
            keybind.fire(event);
        }
    };

    public static void registerKeybind(final Keybind keybind) {
        keybinds.add(keybind);
    }

    public static void unregisterKeybind(final Keybind keybind) {
        keybinds.set(keybinds.indexOf(keybind), null);
    }
}
