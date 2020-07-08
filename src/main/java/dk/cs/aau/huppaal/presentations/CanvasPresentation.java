package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.utility.UndoRedoStack;
import dk.cs.aau.huppaal.utility.helpers.CanvasDragHelper;
import dk.cs.aau.huppaal.utility.helpers.MouseTrackable;
import dk.cs.aau.huppaal.utility.helpers.ZoomHelper;
import dk.cs.aau.huppaal.utility.keyboard.Keybind;
import dk.cs.aau.huppaal.utility.keyboard.KeyboardTracker;
import dk.cs.aau.huppaal.utility.mouse.MouseTracker;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class CanvasPresentation extends Pane implements MouseTrackable {

    public static final int GRID_SIZE = 10;
    public static MouseTracker mouseTracker;

    private final DoubleProperty x = new SimpleDoubleProperty(0);
    private final DoubleProperty y = new SimpleDoubleProperty(0);

    private final CanvasController controller;

    public CanvasPresentation() {
        final URL location = this.getClass().getResource("CanvasPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        mouseTracker = new MouseTracker(this);

        KeyboardTracker.registerKeybind(KeyboardTracker.UNDO, new Keybind(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), UndoRedoStack::undo));
        KeyboardTracker.registerKeybind(KeyboardTracker.REDO, new Keybind(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), UndoRedoStack::redo));

        //Add keybindings for zoom functionality
        KeyboardTracker.registerKeybind(KeyboardTracker.ZOOM_IN, new Keybind(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.SHORTCUT_DOWN), ZoomHelper::zoomIn));
        KeyboardTracker.registerKeybind(KeyboardTracker.ZOOM_OUT, new Keybind(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN), ZoomHelper::zoomOut));
        KeyboardTracker.registerKeybind(KeyboardTracker.RESET_ZOOM, new Keybind(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN), ZoomHelper::resetZoom));
        KeyboardTracker.registerKeybind(KeyboardTracker.ZOOM_TO_FIT, new Keybind(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN), ZoomHelper::zoomToFit));


        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());
            controller = fxmlLoader.getController();

            initializeGrid();

            /*

            // Center on the component
            controller.component.heightProperty().addListener(observable -> {
                setTranslateY(getHeight() / 2 - controller.component.getHeight() / 2);
                setTranslateX(getWidth() / 2 - controller.component.getWidth() / 2);
            });

            // Move the component half a grid size to align it to the grid
            controller.component.setLayoutX(GRID_SIZE / 2);
            controller.component.setLayoutY(GRID_SIZE / 2);

            */
            CanvasDragHelper.makeDraggable(this, mouseEvent -> mouseEvent.getButton().equals(MouseButton.SECONDARY));
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeGrid() {
        final Grid grid = new Grid(GRID_SIZE);

        this.addEventFilter(MouseEvent.MOUSE_CLICKED, (mouseEvent) -> {
            if(mouseEvent.getButton().equals(MouseButton.SECONDARY)){
                grid.setInitialDragPosition(mouseEvent.getX(), mouseEvent.getY());
            }
        });

        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, (mouseEvent) -> {
            if(mouseEvent.getButton().equals(MouseButton.SECONDARY)){
                grid.reactToDrag(mouseEvent);
            }
        });

        getChildren().add(grid);
        grid.toBack();
    }

    @Override
    public DoubleProperty xProperty() {
        return x;
    }

    @Override
    public DoubleProperty yProperty() {
        return y;
    }

    @Override
    public double getX() {
        return xProperty().get();
    }

    @Override
    public double getY() {
        return yProperty().get();
    }

    public CanvasController getController() {
        return controller;
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    public static class Grid extends Parent {

        private final ArrayList<Line> horizontalLines = new ArrayList<>();
        private final ArrayList<Line> verticalLines = new ArrayList<>();
        private double lastLineAddedX = 0;
        private double lastLineAddedY = 0;

        public Grid(final int gridSize) {

            // When the scene changes (goes from null to something)
            sceneProperty().addListener((observable, oldScene, newScene) -> {
                // When the width of this scene is being updated
                newScene.widthProperty().addListener((observable1, oldWidth, newWidth) -> {
                    // Remove old lines
                    while (!verticalLines.isEmpty()) {
                        final Line removeLine = verticalLines.get(0);
                        getChildren().remove(removeLine);
                        verticalLines.remove(removeLine);
                    }

                    // Add new lines (to cover the screen, with 1 line in margin in both ends)
                    int i = -1500;
                    while (i * gridSize - gridSize < newWidth.doubleValue() * 10) {
                        final Line line = new Line(i * gridSize, 200, i * gridSize, 300);
                        line.getStyleClass().add("grid-line");

                        final DoubleBinding parentXBinding = new DoubleBinding() {
                            {
                                super.bind(translateXProperty());
                            }

                            @Override
                            protected double computeValue() {
                                final int moveFactor = (int) (getParent().getTranslateX() / gridSize);
                                return -1 * moveFactor * gridSize + 0.5 * gridSize;
                            }
                        };

                        line.layoutXProperty().bind(parentXBinding);
                        line.startYProperty().bind(getParent().layoutYProperty().subtract(getParent().translateYProperty()).subtract(1500));
                        line.endYProperty().bind(getParent().layoutYProperty().subtract(getParent().translateYProperty()).add(getScene().heightProperty()).add(1500));

                        verticalLines.add(line);
                        i++;
                    }
                    verticalLines.forEach(line -> getChildren().add(line));
                });

                // When the height of this scene is being updated
                newScene.heightProperty().addListener((observable1, oldHeight, newHeight) -> {
                    // Remove old lines
                    while (!horizontalLines.isEmpty()) {
                        final Line removeLine = horizontalLines.get(0);
                        getChildren().remove(removeLine);
                        horizontalLines.remove(removeLine);
                    }

                    // Add new lines (to cover the screen, with 1 line in margin in both ends)
                    int i = -1500;
                    while (i * gridSize - gridSize < newHeight.doubleValue() * 10) {
                        final Line line = new Line(200, i * gridSize, 300, i * gridSize);
                        line.getStyleClass().add("grid-line");

                        final DoubleBinding parentYBinding = new DoubleBinding() {
                            {
                                super.bind(translateYProperty());
                            }

                            @Override
                            protected double computeValue() {
                                final int moveFactor = (int) (getParent().getTranslateY() / gridSize);
                                return -1 * moveFactor * gridSize + 0.5 * gridSize;
                            }
                        };

                        line.layoutYProperty().bind(parentYBinding);
                        line.startXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()).subtract(1500));
                        line.endXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()).add(getScene().widthProperty()).add(1500));

                        horizontalLines.add(line);
                        i++;
                    }
                    horizontalLines.forEach(line -> getChildren().add(line));
                });
            });
        }

        public void reactToDrag(MouseEvent mouseEvent) {
            if(mouseEvent.getY() - lastLineAddedY > GRID_SIZE){
                double lineY = horizontalLines.get(horizontalLines.size() - 1).getStartY();

                final Line line = new Line(200, lineY + GRID_SIZE, 300, lineY + GRID_SIZE);
                line.getStyleClass().add("grid-line");

                final DoubleBinding parentYBinding = new DoubleBinding() {
                    {
                        super.bind(translateYProperty());
                    }

                    @Override
                    protected double computeValue() {
                        final int moveFactor = (int) (getParent().getTranslateY() / GRID_SIZE);
                        return -1 * moveFactor * GRID_SIZE + 0.5 * GRID_SIZE;
                    }
                };

                line.layoutYProperty().bind(parentYBinding);
                line.startXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()).subtract(1500));
                line.endXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()).add(getScene().widthProperty()).add(1500));

                Line oldLine = horizontalLines.get(0);

                horizontalLines.add(line);
                horizontalLines.remove(0);

                lastLineAddedY -= GRID_SIZE;

                getChildren().remove(oldLine);
                getChildren().add(line);
            } else if(mouseEvent.getY() - lastLineAddedY < -GRID_SIZE) {
                double lineY = horizontalLines.get(0).getStartY();

                final Line line = new Line(200, lineY - GRID_SIZE, 300, lineY - GRID_SIZE);
                line.getStyleClass().add("grid-line");
                line.setStroke(new LinearGradient(0d, -5d, 0d, 5d, false,
                        CycleMethod.NO_CYCLE, new Stop(0, Color.BLACK),
                        new Stop(0.199,Color.BLACK),
                        new Stop(0.2,Color.RED),
                        new Stop(0.799,Color.RED),
                        new Stop(0.8,Color.BLACK)));

                final DoubleBinding parentYBinding = new DoubleBinding() {
                    {
                        super.bind(translateYProperty());
                    }

                    @Override
                    protected double computeValue() {
                        final int moveFactor = (int) (getParent().getTranslateY() / GRID_SIZE);
                        return -1 * moveFactor * GRID_SIZE + 0.5 * GRID_SIZE;
                    }
                };

                line.layoutYProperty().bind(parentYBinding);
                line.startXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()).subtract(1500));
                line.endXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()).add(getScene().widthProperty()).add(1500));

                Line oldLine = horizontalLines.get(horizontalLines.size() - 1);

                horizontalLines.add(0, line);
                horizontalLines.remove(horizontalLines.size() - 1);

                lastLineAddedY += GRID_SIZE;

                getChildren().remove(oldLine);
                getChildren().add(line);
            }
        }

        public void setInitialDragPosition(double x, double y) {
            lastLineAddedX = x;
            lastLineAddedY = y;
        }
    }
}
