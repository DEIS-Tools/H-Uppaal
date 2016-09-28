package SW9.utility;

import SW9.MouseTracker;
import SW9.model_canvas.ModelCanvas;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.function.Function;

public class DragHelper {

    public static <T extends Node & Draggable> void makeDraggable(final T subject) {
        makeDraggable(subject, mouseEvent -> true);
    }

    public static <T extends Node & Draggable> void makeDraggable(final T subject, final Function<MouseEvent, Boolean> conditional) {
        final MouseTracker mouseTracker = subject.getMouseTracker();

        final double[] dragXOffset = {0d};
        final double[] dragYOffset = {0d};

        final EventHandler<MouseEvent> onMouseDragged = event -> {
            // Stop propagation of this event
            event.consume();

            if (!conditional.apply(event)) return;
            // The location of the mouse (added with the relative to the subject)
            double x = event.getX() + dragXOffset[0];
            double y = event.getY() + dragYOffset[0];

            // Make coordinates snap to the grip on the canvas
            x -= x % ModelCanvas.GRID_SIZE - (ModelCanvas.GRID_SIZE / 2);
            y -= y % ModelCanvas.GRID_SIZE - (ModelCanvas.GRID_SIZE / 2);

            // If the subject has its x property bound have a parent where we can get the xProperty as well
            if(subject.xProperty().isBound() && subject.getParent() instanceof Draggable) {
                Draggable parent = (Draggable) subject.getParent();
                // Bind the x property of the subject to the value of the mouse event relative to the x property of the parent
                subject.xProperty().bind(parent.xProperty().add(x - parent.xProperty().get()));
            } else {
                // Update the x property value to the value of the mouse
                subject.xProperty().setValue(x);
            }
            // If the subject has its y property bound have a parent where we can get the yProperty as well
            if(subject.yProperty().isBound() && subject.getParent() instanceof Draggable) {
                Draggable parent = (Draggable) subject.getParent();
                // Bind the y property of the subject to the value of the mouse event relative to the y property of the parent
                subject.yProperty().bind(parent.yProperty().add(y - parent.yProperty().get()));
            } else {
                // Update the y property value to the value of the mouse
                subject.yProperty().setValue(y);
            }

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
            } else {
                subject.setCursor(Cursor.DEFAULT);
            }
        });

        // The cursor should look normal when we are no longer dragging the subject
        mouseTracker.registerOnMouseExitedEventHandler(event -> {
            subject.setCursor(Cursor.DEFAULT);
        });

    }

    public static <T extends Pane & Draggable> void makeDraggable(final T subject) {
        makeDraggable(subject, mouseEvent -> true);
    }

    public static <T extends Pane & Draggable> void makeDraggable(final T subject, final Function<MouseEvent, Boolean> conditional) {
        final MouseTracker mouseTracker = subject.getMouseTracker();

        final double[] dragXOffset = {0d};
        final double[] dragYOffset = {0d};

        final double[] previousXTranslation = {0d};
        final double[] previousYTranslation = {0d};

        mouseTracker.registerOnMousePressedEventHandler(event -> {
            if (!conditional.apply(event)) return;

            dragXOffset[0] = subject.xProperty().get() - event.getScreenX();
            dragYOffset[0] = subject.yProperty().get() - event.getScreenY();

            previousXTranslation[0] = subject.getTranslateX();
            previousYTranslation[0] = subject.getTranslateY();

            subject.setCursor(Cursor.MOVE);

            event.consume();
        });

        mouseTracker.registerOnMouseDraggedEventHandler(event -> {
            if (!conditional.apply(event)) return;

            subject.translateXProperty().setValue(previousXTranslation[0] + event.getScreenX() + dragXOffset[0]);
            subject.translateYProperty().setValue(previousYTranslation[0] + event.getScreenY() + dragYOffset[0]);

            subject.setCursor(Cursor.MOVE);

            event.consume();
        });

        mouseTracker.registerOnMouseReleasedEventHandler(event -> subject.setCursor(Cursor.DEFAULT));
    }

    public interface Draggable {
        MouseTracker getMouseTracker();

        DoubleProperty xProperty();

        DoubleProperty yProperty();
    }
}
