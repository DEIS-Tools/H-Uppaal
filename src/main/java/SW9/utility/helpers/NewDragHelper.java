package SW9.utility.helpers;

import SW9.presentations.CanvasPresentation;
import SW9.utility.UndoRedoStack;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;

import java.util.function.Supplier;

public class NewDragHelper {

    public static void makeDraggable(final Node node,
                                     final Supplier<Double> newX,
                                     final Supplier<Double> newY) {
        NewDragHelper.makeDraggable(node,newX, newY, () -> {},() -> {},() -> {});
    }


    public static void makeDraggable(final Node node,
                                     final Supplier<Double> newX,
                                     final Supplier<Double> newY,
                                     final Runnable pressed,
                                     final Runnable dragged,
                                     final Runnable released) {
        final DoubleProperty previousX = new SimpleDoubleProperty();
        final DoubleProperty previousY = new SimpleDoubleProperty();
        final BooleanProperty wasDragged = new SimpleBooleanProperty();

        final DoubleProperty xDiff = new SimpleDoubleProperty();
        final DoubleProperty yDiff = new SimpleDoubleProperty();

        node.setOnMousePressed(event -> {
            previousX.set(node.getLayoutX());
            previousY.set(node.getLayoutY());
            xDiff.set(event.getX());
            yDiff.set(event.getY());
            pressed.run();
        });

        node.setOnMouseDragged(event -> {
            final double unRoundedX = newX.get() - xDiff.get();
            final double unRoundedY = newY.get() - yDiff.get();
            node.setLayoutX(unRoundedX - unRoundedX % CanvasPresentation.GRID_SIZE);
            node.setLayoutY(unRoundedY - unRoundedY % CanvasPresentation.GRID_SIZE);

            wasDragged.set(true);
            dragged.run();
        });

        node.setOnMouseReleased(event -> {
            final double currentX = node.getLayoutX();
            final double currentY = node.getLayoutY();
            final double storePreviousX = previousX.get();
            final double storePreviousY = previousY.get();
            UndoRedoStack.push(
                    () -> {
                        node.setLayoutX(currentX);
                        node.setLayoutY(currentY);
                    },
                    () -> {
                        node.setLayoutX(storePreviousX);
                        node.setLayoutY(storePreviousY);
                    },
                    String.format("Moved " + node.getClass() +" from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                    "pin-drop"
            );

            // Reset the was dragged boolean
            wasDragged.set(false);
            released.run();
        });
    }

}
