package SW9.utility;

import SW9.MouseTracker;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.function.Function;

public class DragHelper {

    public static <T extends Node & MouseTracker.hasMouseTracker> void makeDraggable(final T subject, final Function<MouseEvent, Boolean> conditional) {
        final MouseTracker mouseTracker = subject.getMouseTracker();

        final double[] dragXOffset = {0d};
        final double[] dragYOffset = {0d};

        final EventHandler<MouseEvent> onMouseDragged = event -> {
            subject.xProperty().setValue(event.getX() + dragXOffset[0]);
            subject.yProperty().setValue(event.getY() + dragYOffset[0]);

            subject.setCursor(Cursor.CLOSED_HAND);
        };

        // Register the onMouseDragged event listener if the provided conditional returns true
        mouseTracker.registerOnMousePressedEventHandler(event -> {
            // Check if we should enable dragging based on the initial conditional
            if (!conditional.apply(event)) return;

            dragXOffset[0] = subject.xProperty().get() - event.getX();
            dragYOffset[0] = subject.yProperty().get() - event.getY();

            mouseTracker.registerOnMouseDraggedEventHandler(onMouseDragged);

            subject.setCursor(Cursor.CLOSED_HAND);
        });

        // Un-register the onMouseDragged event listener when we stop dragging
        mouseTracker.registerOnMouseReleasedEventHandler(event -> {
            mouseTracker.unregisterOnMouseDraggedEventHandler(onMouseDragged);

            subject.setCursor(Cursor.OPEN_HAND);
        });

        // Make the cursor look draggable when the provided conditional returns true
        mouseTracker.registerOnMouseMovedEventHandler(event -> {
            if (conditional.apply(event)) {
                subject.setCursor(Cursor.OPEN_HAND);
            }
            else {
                subject.setCursor(Cursor.DEFAULT);
            }
        });

        // The cursor should look normal when we are no longer dragging the subject
        mouseTracker.registerOnMouseExitedEventHandler(event -> {
           subject.setCursor(Cursor.DEFAULT);
        });

    }

}
