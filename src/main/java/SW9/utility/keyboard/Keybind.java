package SW9.utility.keyboard;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import java.util.function.Consumer;

public class Keybind {

    private final KeyCodeCombination keyCombo;
    private Runnable action = null;
    private Consumer<KeyEvent> consumer = null;

    public Keybind(final KeyCodeCombination keyCombo, final Runnable action) {
        this.keyCombo = keyCombo;
        this.action = action;
    }

    public Keybind(final KeyCodeCombination keyCombo, final Consumer<KeyEvent> consumer) {
        this.keyCombo = keyCombo;
        this.consumer = consumer;
    }

    public boolean matches(final KeyEvent keyEvent) {
        return keyCombo.match(keyEvent);
    }

    public void fire(final KeyEvent keyEvent) {
        if (matches(keyEvent)) {
            if (action != null) action.run();
            if (consumer != null) consumer.accept(keyEvent);
        }
    }

}
