package SW9.utility.helpers;

import SW9.presentations.ComponentPresentation;
import SW9.utility.UndoRedoStack;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class NewDragHelper {

    public static void makeDraggable(final Node dragSubject,
                                     final Supplier<Double> newX,
                                     final Supplier<Double> newY) {
        NewDragHelper.makeDraggable(dragSubject, dragSubject, newX, newY, (event) -> {
        }, () -> {
        }, () -> {
        });
    }


    public static void makeDraggable(final Node dragSubject,
                                     final Node mouseSubject,
                                     final Supplier<Double> newX,
                                     final Supplier<Double> newY,
                                     final Consumer<MouseEvent> pressed,
                                     final Runnable dragged,
                                     final Runnable released) {
        final DoubleProperty previousX = new SimpleDoubleProperty();
        final DoubleProperty previousY = new SimpleDoubleProperty();
        final BooleanProperty wasDragged = new SimpleBooleanProperty();

        final DoubleProperty xDiff = new SimpleDoubleProperty();
        final DoubleProperty yDiff = new SimpleDoubleProperty();

        mouseSubject.setOnMousePressed(event -> {
            previousX.set(dragSubject.getLayoutX());
            previousY.set(dragSubject.getLayoutY());
            xDiff.set(event.getX());
            yDiff.set(event.getY());
            pressed.accept(event);
        });

        mouseSubject.setOnMouseDragged(event -> {
            final double unRoundedX = newX.get() - xDiff.get();
            final double unRoundedY = newY.get() - yDiff.get();
            double finalNewX = unRoundedX - unRoundedX % GRID_SIZE;
            double finalNewY = unRoundedY - unRoundedY % GRID_SIZE;
            if(dragSubject instanceof ComponentPresentation) {
                finalNewX -= 0.5 * GRID_SIZE;
                finalNewY -= 0.5 * GRID_SIZE;
            }
            dragSubject.setLayoutX(finalNewX);
            dragSubject.setLayoutY(finalNewY);

            wasDragged.set(true);
            dragged.run();
        });

        mouseSubject.setOnMouseReleased(event -> {
            final double currentX = dragSubject.getLayoutX();
            final double currentY = dragSubject.getLayoutY();
            final double storePreviousX = previousX.get();
            final double storePreviousY = previousY.get();
            UndoRedoStack.push(
                    () -> {
                        dragSubject.setLayoutX(currentX);
                        dragSubject.setLayoutY(currentY);
                    },
                    () -> {
                        dragSubject.setLayoutX(storePreviousX);
                        dragSubject.setLayoutY(storePreviousY);
                    },
                    String.format("Moved " + dragSubject.getClass() +" from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                    "pin-drop"
            );

            // Reset the was dragged boolean
            wasDragged.set(false);
            released.run();
        });
    }

}
