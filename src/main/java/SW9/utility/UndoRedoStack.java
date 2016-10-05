package SW9.utility;

import java.util.EmptyStackException;
import java.util.Stack;

public class UndoRedoStack {

    private static Stack<Command> undoStack = new Stack<>();
    private static Stack<Command> redoStack = new Stack<>();

    public static Command push(final Runnable perform, final Runnable undo) {
        final Command item = new Command(perform, undo);

        // Empty the redo stack (new changes may be conflicting with redoing)
        while (!redoStack.isEmpty()) {
            redoStack.pop();
        }

        final Command command = undoStack.push(item);
        command.perform();

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
    }

    public static void redo() {
        try {
            final Command command = redoStack.pop();

            undoStack.push(command);
            command.perform();
        } catch (EmptyStackException e) {
            // The stack is empty, nothing left to redo. Ignore.
        }

        System.out.println("redo");
    }

    private static class Command {

        private final Runnable perform;
        private final Runnable undo;

        public Command(final Runnable perform, final Runnable undo) {
            this.perform = perform;
            this.undo = undo;
        }

        public void perform() {
            perform.run();
        }

        public void undo() {
            undo.run();
        }
    }
}
