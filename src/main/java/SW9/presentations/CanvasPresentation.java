package SW9.presentations;

import SW9.controllers.CanvasController;
import SW9.utility.UndoRedoStack;
import SW9.utility.helpers.CanvasDragHelper;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
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

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    public static class Grid extends Parent {

        private final ArrayList<Line> horizontalLines = new ArrayList<>();
        private final ArrayList<Line> verticalLines = new ArrayList<>();

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
                    int i = -1;
                    while (i * gridSize - gridSize < newWidth.doubleValue()) {
                        final Line line = new Line(i * gridSize, 200, i * gridSize, 300);
                        line.getStyleClass().add("grid-line");

                        final DoubleBinding parentXBinding = new DoubleBinding() {
                            {
                                super.bind(getParent().translateXProperty());
                            }

                            @Override
                            protected double computeValue() {
                                final int moveFactor = (int) (getParent().getTranslateX() / gridSize);
                                return -1 * moveFactor * gridSize + 0.5 * gridSize;
                            }
                        };

                        line.layoutXProperty().bind(parentXBinding);
                        line.startYProperty().bind(getParent().layoutYProperty().subtract(getParent().translateYProperty()).subtract(50)); // the 50 is a fix
                        line.endYProperty().bind(getParent().layoutYProperty().subtract(getParent().translateYProperty()).add(getScene().heightProperty()));

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
                    int i = -1;
                    while (i * gridSize - gridSize < newHeight.doubleValue()) {
                        final Line line = new Line(200, i * gridSize, 300, i * gridSize);
                        line.getStyleClass().add("grid-line");

                        final DoubleBinding parentYBinding = new DoubleBinding() {
                            {
                                super.bind(getParent().translateYProperty());
                            }

                            @Override
                            protected double computeValue() {
                                final int moveFactor = (int) (getParent().getTranslateY() / gridSize);
                                return -1 * moveFactor * gridSize + 0.5 * gridSize;
                            }
                        };

                        line.layoutYProperty().bind(parentYBinding);
                        line.startXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()));
                        line.endXProperty().bind(getParent().layoutXProperty().subtract(getParent().translateXProperty()).add(getScene().widthProperty()));

                        horizontalLines.add(line);
                        i++;
                    }
                    horizontalLines.forEach(line -> getChildren().add(line));
                });
            });

        }

    }
}
