package SW9.utility.helpers;

import SW9.controllers.CanvasController;
import SW9.controllers.ComponentController;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.utility.UndoRedoStack;
import javafx.beans.property.*;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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

    private static class DragStatus {
        final double previousX;
        final double previousY;
        final double offsetX;
        final double offsetY;
        boolean wasDragged;

        public DragStatus(final double previousX, final double previousY, final double offsetX, final double offsetY) {
            this.previousX = previousX;
            this.previousY = previousY;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.wasDragged = false;
        }
    }

    public static void makeDraggablePisseLigeGlad(final Node mouseSubject) {

        final SimpleObjectProperty<List<SelectHelper.ItemSelectable>> selectedElements = new SimpleObjectProperty<>(SelectHelper.getSelectedElements());
        final Map<SelectHelper.ItemSelectable, DragStatus> dragStatusMap = new HashMap<>();

        final DragBounds[] dragBounds = {null};

        mouseSubject.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            selectedElements.set(SelectHelper.getSelectedElements());

            final List<DragBounds> selectElementsBounds = new ArrayList<>();

            selectedElements.get().forEach(colorSelectable -> {

                final double previousX = colorSelectable.xProperty().doubleValue();
                final double previousY = colorSelectable.yProperty().doubleValue();
                final double offsetX = previousX - CanvasPresentation.mouseTracker.getGridX() - CanvasController.getActiveComponent().getX();
                final double offsetY = previousY - CanvasPresentation.mouseTracker.getGridY() - CanvasController.getActiveComponent().getY();

                dragStatusMap.put(colorSelectable, new DragStatus(previousX, previousY, offsetX, offsetY));
                selectElementsBounds.add(colorSelectable.getDragBounds());
            });

            dragBounds[0] = new DragBounds(selectElementsBounds);
        });

        mouseSubject.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {

            if(dragBounds[0] == null) return;

            final double newX = CanvasPresentation.mouseTracker.getGridX() - CanvasController.getActiveComponent().getX();
            final double newY = CanvasPresentation.mouseTracker.getGridY() - CanvasController.getActiveComponent().getY();

            selectedElements.get().forEach(colorSelectable -> {
                final double unRoundedX = dragBounds[0].trimX(newX + dragStatusMap.get(colorSelectable).offsetX);
                final double unRoundedY = dragBounds[0].trimY(newY + dragStatusMap.get(colorSelectable).offsetY);
                double finalNewX = unRoundedX - unRoundedX % GRID_SIZE;
                double finalNewY = unRoundedY - unRoundedY % GRID_SIZE;
                if(colorSelectable instanceof ComponentController) {
                    finalNewX -= 0.5 * GRID_SIZE;
                    finalNewY -= 0.5 * GRID_SIZE;
                }
                colorSelectable.xProperty().set(finalNewX);
                colorSelectable.yProperty().set(finalNewY);

                dragStatusMap.get(colorSelectable).wasDragged = true;
            });
        });

        mouseSubject.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            selectedElements.get().forEach(colorSelectable -> {
                final double currentX = colorSelectable.xProperty().doubleValue();
                final double currentY = colorSelectable.yProperty().doubleValue();
                final double storePreviousX = dragStatusMap.get(colorSelectable).previousX;
                final double storePreviousY = dragStatusMap.get(colorSelectable).previousY;

                if(currentX != storePreviousX || currentY != storePreviousY) {
                    UndoRedoStack.push(
                            () -> {
                                colorSelectable.xProperty().set(currentX);
                                colorSelectable.yProperty().set(currentY);
                            },
                            () -> {
                                colorSelectable.xProperty().set(storePreviousX);
                                colorSelectable.yProperty().set(storePreviousY);
                            },
                            String.format("Moved " + colorSelectable.getClass() + " from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                            "pin-drop"
                    );
                }

                // Reset the was dragged boolean
                //wasDragged.set(false);
            });
        });
    }

    public static void makeDraggableWithBounds(final Node dragSubject,
                                               final Node mouseSubject,
                                               final DragBounds dragBounds,
                                               final ObservableDoubleValue newXProperty,
                                               final ObservableDoubleValue newYProperty,
                                               final Consumer<MouseEvent> pressed,
                                               final Runnable dragged,
                                               final Runnable released) {
        final DoubleProperty previousX = new SimpleDoubleProperty();
        final DoubleProperty previousY = new SimpleDoubleProperty();
        final BooleanProperty wasDragged = new SimpleBooleanProperty();

        final DoubleProperty xDiff = new SimpleDoubleProperty();
        final DoubleProperty yDiff = new SimpleDoubleProperty();

        mouseSubject.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            pressed.accept(event);
            previousX.set(dragSubject.getLayoutX());
            previousY.set(dragSubject.getLayoutY());
            xDiff.set(event.getX());
            yDiff.set(event.getY());
        });

        mouseSubject.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            final double unRoundedX = dragBounds.trimX(newXProperty.get() - xDiff.get());
            final double unRoundedY = dragBounds.trimY(newYProperty.get() - yDiff.get());
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

        mouseSubject.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            final double currentX = dragSubject.getLayoutX();
            final double currentY = dragSubject.getLayoutY();
            final double storePreviousX = previousX.get();
            final double storePreviousY = previousY.get();

            if(currentX != storePreviousX || currentY != storePreviousY) {
                UndoRedoStack.push(
                        () -> {
                            dragSubject.setLayoutX(currentX);
                            dragSubject.setLayoutY(currentY);
                        },
                        () -> {
                            dragSubject.setLayoutX(storePreviousX);
                            dragSubject.setLayoutY(storePreviousY);
                        },
                        String.format("Moved " + dragSubject.getClass() + " from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                        "pin-drop"
                );
            }

            // Reset the was dragged boolean
            wasDragged.set(false);
            released.run();
        });

    };



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
            pressed.accept(event);
            previousX.set(dragSubject.getLayoutX());
            previousY.set(dragSubject.getLayoutY());
            xDiff.set(event.getX());
            yDiff.set(event.getY());
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

            if(currentX != storePreviousX || currentY != storePreviousY) {
                UndoRedoStack.push(
                        () -> {
                            dragSubject.setLayoutX(currentX);
                            dragSubject.setLayoutY(currentY);
                        },
                        () -> {
                            dragSubject.setLayoutX(storePreviousX);
                            dragSubject.setLayoutY(storePreviousY);
                        },
                        String.format("Moved " + dragSubject.getClass() + " from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                        "pin-drop"
                );
            }

            // Reset the was dragged boolean
            wasDragged.set(false);
            released.run();
        });
    }

}
