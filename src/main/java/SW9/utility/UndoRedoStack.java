package SW9.utility;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.function.BiConsumer;

public class UndoRedoStack {

    private static final Stack<Command> undoStack = new Stack<>();
    private static final Stack<Command> redoStack = new Stack<>();

    private static final SimpleBooleanProperty canUndo = new SimpleBooleanProperty(false);
    private static final SimpleBooleanProperty canRedo = new SimpleBooleanProperty(false);

    private static BiConsumer<Stack<Command>, Stack<Command>> debugRunnable = (c1, c2) -> {
    };

    public static void setDebugRunnable(final BiConsumer<Stack<Command>, Stack<Command>> debugRunnable) {
        UndoRedoStack.debugRunnable = debugRunnable;
    }

    public static Command push(final Runnable perform, final Runnable undo, final String description, final String icon) {
        final Command item = new Command(perform, undo, description, icon);

        // Empty the redo stack (new changes may be conflicting with redoing)
        while (!redoStack.isEmpty()) {
            redoStack.pop();
        }

        final Command command = undoStack.push(item);
        command.perform();

        updateState();

        return command;
    }

    public static void undo() {
        try {
            final Command command = undoStack.pop();

            redoStack.push(command);
            command.undo();
        } catch (EmptyStackException e) {
            // The stack is empty, nothing left to undo. Ignore.
        }

        updateState();
    }

    public static void redo() {
        try {
            final Command command = redoStack.pop();

            undoStack.push(command);
            command.perform();
        } catch (EmptyStackException e) {
            // The stack is empty, nothing left to redo. Ignore.
        }

        updateState();
    }

    public static void forgetLast() {
        try {
            undoStack.pop();
        } catch (EmptyStackException e) {
            // The stack is empty, nothing left to undo. Ignore.
        }

        updateState();
    }

    private static void updateState() {
        if (undoStack.isEmpty()) {
            canUndo.set(false);
        } else {
            canUndo.set(true);
        }

        if (redoStack.isEmpty()) {
            canRedo.set(false);
        } else {
            canRedo.set(true);
        }

        debugRunnable.accept(undoStack, redoStack);
    }

    public static boolean canUndo() {
        return canUndo.get();
    }

    public static SimpleBooleanProperty canUndoProperty() {
        return canUndo;
    }

    public static boolean canRedo() {
        return canRedo.get();
    }

    public static SimpleBooleanProperty canRedoProperty() {
        return canRedo;
    }

    public static class Command {

        private final Runnable perform;
        private final Runnable undo;
        private final String description;
        private final String icon;

        public Command(final Runnable perform, final Runnable undo, final String description, final String icon) {
            this.perform = perform;
            this.undo = undo;
            this.description = description;
            this.icon = icon;
        }

        public void perform() {
            perform.run();
        }

        public void undo() {
            undo.run();
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }
}
