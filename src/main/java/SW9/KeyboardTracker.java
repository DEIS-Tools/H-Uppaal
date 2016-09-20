package SW9;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public class KeyboardTracker {
    private static Map<String, Keybind> keyMap = new HashMap<>();

    public static final String ADD_NEW_LOCATION = "ADD_NEW_LOCATION";
    public static final String DISCARD_NEW_LOCATION = "DISCARD_NEW_LOCATION";
    public static final String DISCARD_NEW_EDGE = "DISCARD_NEW_EDGE";

    public static final EventHandler<KeyEvent> handleKeyPress = event -> {
        for (final Keybind keybind : keyMap.values()) {
            if (keybind == null) continue;
            if (keybind.matches(event)) {
                keybind.fire(event);
            }
        }
    };

    public static void registerKeybind(final String id, final Keybind keybind) {
        keyMap.put(id, keybind);
    }

    public static void unregisterKeybind(final String id) {
        keyMap.put(id, null);
    }
}
