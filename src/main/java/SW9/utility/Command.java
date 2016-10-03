package SW9.utility;

public class Command {

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
