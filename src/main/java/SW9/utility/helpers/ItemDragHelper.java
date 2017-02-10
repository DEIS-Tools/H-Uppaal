package SW9.utility.helpers;

import SW9.controllers.CanvasController;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.utility.UndoRedoStack;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.function.Supplier;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class ItemDragHelper {

    public static class DragBounds {
        private ObservableDoubleValue minX;
        private ObservableDoubleValue maxX;
        private ObservableDoubleValue minY;
        private ObservableDoubleValue maxY;

        public DragBounds(final ObservableDoubleValue minX, final ObservableDoubleValue maxX, final ObservableDoubleValue minY, final ObservableDoubleValue maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }

        public DragBounds(final List<DragBounds> dragBoundses) {
             minX = new SimpleDoubleProperty(Double.MIN_VALUE);
             maxX = new SimpleDoubleProperty(Double.MAX_VALUE);
             minY = new SimpleDoubleProperty(Double.MIN_VALUE);
             maxY = new SimpleDoubleProperty(Double.MAX_VALUE);

            for (final DragBounds dragBounds : dragBoundses) {
                if (dragBounds.minX.get() > minX.get()) {
                    minX = dragBounds.minX;
                }

                if (dragBounds.maxX.get() < maxX.get()) {
                    maxX = dragBounds.maxX;
                }

                if (dragBounds.minY.get() > minY.get()) {
                    minY = dragBounds.minY;
                }

                if (dragBounds.maxY.get() < maxY.get()) {
                    maxY = dragBounds.maxY;
                }
            }

        }

        public static DragBounds generateLooseDragBounds() {
            return new ItemDragHelper.DragBounds(new SimpleDoubleProperty(Double.MIN_VALUE), new SimpleDoubleProperty(Double.MAX_VALUE),new SimpleDoubleProperty(Double.MIN_VALUE), new SimpleDoubleProperty(Double.MAX_VALUE));
        }

        public double trimX(final ObservableDoubleValue x) {
            return trimX(x.get());
        }

        public double trimX(final double x) {
            return trim(x, minX.get(), maxX.get());
        }

        public double trimY(final ObservableDoubleValue y) {
            return trimY(y.get());
        }

        public double trimY(final double y) {
            return trim(y, minY.get(), maxY.get());
        }

        private double trim(final double v, final double min, final double max) {
            if(v < min) {
                return min;
            } else if(v > max) {
                return max;
            } else {
                return v;
            }
        }

    }

    public static void makeDraggable(final Node mouseSubject,
                                     final Supplier<DragBounds> getDragBounds) {
        final DoubleProperty previousX = new SimpleDoubleProperty();
        final DoubleProperty previousY = new SimpleDoubleProperty();
        final BooleanProperty wasDragged = new SimpleBooleanProperty();

        final DoubleProperty xDiff = new SimpleDoubleProperty();
        final DoubleProperty yDiff = new SimpleDoubleProperty();

        mouseSubject.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if(!event.isPrimaryButtonDown()) return;
            
            previousX.set(mouseSubject.getLayoutX());
            previousY.set(mouseSubject.getLayoutY());
            xDiff.set(event.getX());
            yDiff.set(event.getY());
        });

        mouseSubject.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if(!event.isPrimaryButtonDown()) return;

            final DragBounds dragBounds = getDragBounds.get();

            final double newX = CanvasPresentation.mouseTracker.getGridX() - CanvasController.getActiveComponent().getX();
            final double newY = CanvasPresentation.mouseTracker.getGridY() - CanvasController.getActiveComponent().getY();

            final double unRoundedX = dragBounds.trimX(newX - xDiff.get());
            final double unRoundedY = dragBounds.trimY(newY - yDiff.get());
            double finalNewX = unRoundedX - unRoundedX % GRID_SIZE;
            double finalNewY = unRoundedY - unRoundedY % GRID_SIZE;
            if (mouseSubject instanceof ComponentPresentation) {
                finalNewX -= 0.5 * GRID_SIZE;
                finalNewY -= 0.5 * GRID_SIZE;
            }
            mouseSubject.setLayoutX(finalNewX);
            mouseSubject.setLayoutY(finalNewY);

            wasDragged.set(true);
        });

        mouseSubject.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            final double currentX = mouseSubject.getLayoutX();
            final double currentY = mouseSubject.getLayoutY
                    ();
            final double storePreviousX = previousX.get();
            final double storePreviousY = previousY.get();

            if(currentX != storePreviousX || currentY != storePreviousY) {
                UndoRedoStack.push(
                        () -> {
                            mouseSubject.setLayoutX(currentX);
                            mouseSubject.setLayoutY(currentY);
                        },
                        () -> {
                            mouseSubject.setLayoutX(storePreviousX);
                            mouseSubject.setLayoutY(storePreviousY);
                        },
                        String.format("Moved " + mouseSubject.getClass() + " from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                        "pin-drop"
                );
            }

            // Reset the was dragged boolean
            wasDragged.set(false);
        });

    }

    ;


}
