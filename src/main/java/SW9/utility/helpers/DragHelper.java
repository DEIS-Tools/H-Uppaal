package SW9.utility.helpers;

import SW9.utility.mouse.MouseTracker;
import SW9.model_canvas.ModelCanvas;
import SW9.utility.UndoRedoStack;
import javafx.beans.binding.NumberBinding;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.function.Function;

public class DragHelper {

    public static <T extends Node & MouseTrackable> void makeDraggable(final T subject) {
        makeDraggable(subject, mouseEvent -> true);
    }

    public static <T extends Node & MouseTrackable> void makeDraggable(final T subject, final Function<MouseEvent, Boolean> conditional) {
        final MouseTracker mouseTracker = subject.getMouseTracker();

        // The offset from the mouse
        final double[] dragXOffset = {0d};
        final double[] dragYOffset = {0d};

        // The offset from the draggable parent, used to undo
        final double[] parentXOffset = {0d};
        final double[] parentYOffset = {0d};

        // Variables to redo the drag later on
        final NumberBinding[] newXBinding = {null};
        final NumberBinding[] newYBinding = {null};
        final double[] newXValue = {0d};
        final double[] newYValue = {0d};

        final boolean[] hasDragged = {false};

        final EventHandler<MouseEvent> onMouseDragged = event -> {
            // Stop propagation of this event
            event.consume();

            // Check if we are allowed to drag in the first place
            if (!conditional.apply(event) || ModelCanvas.edgeIsBeingDrawn()) return;

            hasDragged[0] = true;

            // The location of the mouse (added with the relative to the subject)
            double x = event.getX() + dragXOffset[0];
            double y = event.getY() + dragYOffset[0];

            // Make coordinates snap to the grip on the canvas
            x -= x % ModelCanvas.GRID_SIZE - (ModelCanvas.GRID_SIZE / 2);
            y -= y % ModelCanvas.GRID_SIZE - (ModelCanvas.GRID_SIZE / 2);

            // If the subject has its x property bound have a parent where we can get the xProperty as well
            if (subject.xProperty().isBound()) {
                final LocationAware parent = findAncestor(subject);
                // Bind the x property of the subject to the value of the mouse event relative to the x property of the parent
                newXBinding[0] = parent.xProperty().add(x - parent.xProperty().get());
                subject.xProperty().bind(newXBinding[0]);
            } else {
                // Update the x property value to the value of the mouse
                newXValue[0] = x;
                subject.xProperty().setValue(x);
            }
            // If the subject has its y property bound have a parent where we can get the yProperty as well
            if (subject.yProperty().isBound()) {
                final LocationAware parent = findAncestor(subject);
                // Bind the y property of the subject to the value of the mouse event relative to the y property of the parent
                newYBinding[0] = parent.yProperty().add(y - parent.yProperty().get());
                subject.yProperty().bind(newYBinding[0]);
            } else {
                // Update the y property value to the value of the mouse
                newYValue[0] = y;
                subject.yProperty().setValue(y);
            }

            subject.setCursor(Cursor.CLOSED_HAND);
        };

        // Register the onMouseDragged event listener if the provided conditional returns true
        mouseTracker.registerOnMousePressedEventHandler(event -> {
            // Check if we are allowed to drag in the first place
            if (!conditional.apply(event) || ModelCanvas.edgeIsBeingDrawn()) return;

            hasDragged[0] = false;

            dragXOffset[0] = subject.xProperty().get() - event.getX();
            dragYOffset[0] = subject.yProperty().get() - event.getY();

            // For children of a draggable parent, update the offset from the parent
            final LocationAware parent = findAncestor(subject);
            if (parent != null) {
                parentXOffset[0] = subject.xProperty().get() - parent.xProperty().get();
                parentYOffset[0] = subject.yProperty().get() - parent.yProperty().get();
            }

            mouseTracker.registerOnMouseDraggedEventHandler(onMouseDragged);

            subject.setCursor(Cursor.CLOSED_HAND);
        });

        // Un-register the onMouseDragged event listener when we stop dragging
        mouseTracker.registerOnMouseReleasedEventHandler(event -> {
            // Stop propagation of this event
            event.consume();

            // Check if we are allowed to drag in the first place
            if (!conditional.apply(event) || ModelCanvas.edgeIsBeingDrawn() || !hasDragged[0]) return;

            hasDragged[0] = false;

            mouseTracker.unregisterOnMouseDraggedEventHandler(onMouseDragged);

            UndoRedoStack.push(() -> { // Perform

                // If the x property is bound bind the x property correctly, else update the value
                if (subject.xProperty().isBound()) {
                    subject.xProperty().bind(newXBinding[0]);
                } else {
                    subject.xProperty().set(newXValue[0]);
                }
                // If the x property is bound bind the x property correctly, else update the value
                if (subject.yProperty().isBound()) {
                    subject.yProperty().bind(newYBinding[0]);
                } else {
                    subject.yProperty().set(newYValue[0]);
                }

            }, () -> { // Undo
                final LocationAware parent = findAncestor(subject);

                // If the x property is bound bind it to the original parent offset, else update the value
                if (subject.xProperty().isBound()) {
                    subject.xProperty().bind(parent.xProperty().add(parentXOffset[0]));
                } else {
                    subject.xProperty().set(parent.xProperty().get() + parentXOffset[0]);
                }

                // If the y property is bound bind it to the original parent offset, else update the value
                if (subject.yProperty().isBound()) {
                    subject.yProperty().bind(parent.yProperty().add(parentYOffset[0]));
                } else {
                    subject.yProperty().set(parent.yProperty().get() + parentYOffset[0]);
                }

            });

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
        mouseTracker.registerOnMouseExitedEventHandler(event -> subject.setCursor(Cursor.DEFAULT));
    }

    public static <T extends Pane & MouseTrackable> void makeDraggable(final T subject) {
        makeDraggable(subject, mouseEvent -> true);
    }

    public static <T extends Pane & MouseTrackable> void makeDraggable(final T subject, final Function<MouseEvent, Boolean> conditional) {
        final MouseTracker mouseTracker = subject.getMouseTracker();

        final double[] dragXOffset = {0d};
        final double[] dragYOffset = {0d};

        final double[] previousXTranslation = {0d};
        final double[] previousYTranslation = {0d};

        mouseTracker.registerOnMousePressedEventHandler(event -> {
            if (!conditional.apply(event) || ModelCanvas.edgeIsBeingDrawn()) return;

            dragXOffset[0] = subject.xProperty().get() - event.getScreenX();
            dragYOffset[0] = subject.yProperty().get() - event.getScreenY();

            previousXTranslation[0] = subject.getTranslateX();
            previousYTranslation[0] = subject.getTranslateY();

            subject.setCursor(Cursor.MOVE);

            event.consume();
        });

        mouseTracker.registerOnMouseDraggedEventHandler(event -> {
            if (!conditional.apply(event) || ModelCanvas.edgeIsBeingDrawn()) return;

            subject.translateXProperty().setValue(previousXTranslation[0] + event.getScreenX() + dragXOffset[0]);
            subject.translateYProperty().setValue(previousYTranslation[0] + event.getScreenY() + dragYOffset[0]);

            subject.setCursor(Cursor.MOVE);

            event.consume();
        });

        mouseTracker.registerOnMouseReleasedEventHandler(event -> subject.setCursor(Cursor.DEFAULT));
    }

    // Finds the nearest ancestor which implements location aware
    private static <T extends Node & MouseTrackable> LocationAware findAncestor(T subject) {
        LocationAware parent = null;

        Node descendant = subject;
        while(parent == null && descendant != null) {
            if(descendant.getParent() instanceof MouseTrackable) {
                parent = (LocationAware) descendant.getParent();
            }
            descendant = descendant.getParent();
        }

        return parent;
    }
}
