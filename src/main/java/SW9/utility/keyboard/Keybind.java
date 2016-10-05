package SW9.utility.keyboard;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

public class Keybind {

    public final KeyCodeCombination keyCombo;
    private final Runnable action;

    public Keybind(final KeyCodeCombination keyCombo, final Runnable action) {

        this.keyCombo = keyCombo;
        this.action = action;
    }

    public boolean matches(KeyEvent keyEvent) {
        return keyCombo.match(keyEvent);
    }

    public void fire(KeyEvent keyEvent) {
        if (matches(keyEvent)) {
            action.run();
        }
    }

}
