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
    public static final String MAKE_LOCATION_URGENT = "MAKE_LOCATION_URGENT";
    public static final String MAKE_LOCATION_COMMITTED = "MAKE_LOCATION_COMMITTED";
    public static final String CREATE_COMPONENT = "CREATE_COMPONENT";

    public static final String TEST_ARROW_ONE = "TEST_ARROW_ONE";

    public static final EventHandler<KeyEvent> handleKeyPress = event -> {

        Map<String, Keybind> copy = new HashMap<>(keyMap);

        for (final Keybind keybind : copy.values()) {
            if (keybind == null) continue;
            if (keybind.matches(event)) {
                keybind.fire(event);
            }
        }
    };

    public synchronized static void registerKeybind(final String id, final Keybind keybind) {
        keyMap.put(id, keybind);
    }

    public synchronized static void unregisterKeybind(final String id) {
        keyMap.put(id, null);
    }
}
