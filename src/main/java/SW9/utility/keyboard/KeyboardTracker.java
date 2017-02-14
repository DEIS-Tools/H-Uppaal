package SW9.utility.keyboard;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public class KeyboardTracker {
    public static final String ADD_NEW_LOCATION = "ADD_NEW_LOCATION";
    public static final String DISCARD_NEW_LOCATION = "DISCARD_NEW_LOCATION";
    public static final String DISCARD_NEW_EDGE = "DISCARD_NEW_EDGE";
    public static final String MAKE_LOCATION_URGENT = "MAKE_LOCATION_URGENT";
    public static final String MAKE_LOCATION_COMMITTED = "MAKE_LOCATION_COMMITTED";
    public static final String CREATE_COMPONENT = "CREATE_COMPONENT";
    public static final String UNDO = "UNDO";
    public static final String REDO = "REDO";
    public static final String DELETE_SELECTED = "DELETE_SELECTED";
    public static final String ADD_CHANNEL_BOX = "ADD_CHANNEL_BOX";
    public static final String COLOR_0 = "COLOR_0";
    public static final String COLOR_1 = "COLOR_1";
    public static final String COLOR_2 = "COLOR_2";
    public static final String COLOR_3 = "COLOR_3";
    public static final String COLOR_4 = "COLOR_4";
    public static final String COLOR_5 = "COLOR_5";
    public static final String COLOR_6 = "COLOR_6";
    public static final String COLOR_7 = "COLOR_7";
    public static final String COLOR_8 = "COLOR_8";
    public static final String COLOR_9 = "COLOR_9";
    public static final String COMPONENT_HAS_DEADLOCK = "COMPONENT_HAS_DEADLOCK";
    public static final String TESTING_BIND = "TESTING_BIND";
    public static final String TOGGLE_QUERY_PANE = "TOGGLE_QUERY_PANE";
    public static final String TOGGLE_FILE_PANE = "TOGGLE_FILE_PANE";
    public static final String HIDE_LOCATION_PROPERTY_PANE = "HIDE_LOCATION_PROPERTY_PANE";
    public static final String ADD_NEW_COMPONENT = "ADD_NEW_COMPONENT";
    public static final String COLOR_SELECTED = "COLOR_SELECTED";
    public static final String ABANDON_LOCATION = "ABANDON_LOCATION";
    public static final String ABANDON_EDGE = "ABANDON_EDGE";
    public static final String NUDGE_UP = "NUDGE_UP";
    public static final String NUDGE_DOWN = "NUDGE_DOWN";
    public static final String NUDGE_LEFT = "NUDGE_LEFT";
    public static final String NUDGE_RIGHT = "NUDGE_RIGHT";
    public static final String NUDGE_W = "NUDGE_W";
    public static final String NUDGE_A = "NUDGE_A";
    public static final String NUDGE_S = "NUDGE_S";
    public static final String NUDGE_D = "NUDGE_D";
    public static final String DESELECT = "DESELECT";

    private static final Map<String, Keybind> keyMap = new HashMap<>();
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
