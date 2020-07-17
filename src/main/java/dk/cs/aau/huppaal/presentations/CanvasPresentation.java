package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.utility.UndoRedoStack;
import dk.cs.aau.huppaal.utility.helpers.CanvasDragHelper;
import dk.cs.aau.huppaal.utility.helpers.MouseTrackable;
import dk.cs.aau.huppaal.utility.helpers.ZoomHelper;
import dk.cs.aau.huppaal.utility.keyboard.Keybind;
import dk.cs.aau.huppaal.utility.keyboard.KeyboardTracker;
import dk.cs.aau.huppaal.utility.mouse.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Screen;

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
        getChildren().add(grid);
        grid.toBack();

        this.translateXProperty().addListener(((observable, oldValue, newValue) -> grid.handleTranslateX(oldValue.doubleValue(), newValue.doubleValue(), this.scaleXProperty().doubleValue())));
        this.translateYProperty().addListener(((observable, oldValue, newValue) -> grid.handleTranslateY(oldValue.doubleValue(), newValue.doubleValue(), this.scaleYProperty().doubleValue())));

        ZoomHelper.setGrid(grid);
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

        public Grid(final int gridSize) {
            int screenWidthInGridSlices = (int) (Screen.getPrimary().getBounds().getWidth() - (Screen.getPrimary().getBounds().getWidth() % gridSize)) * 3;
            int screenHeightInGridSlices = (int) (Screen.getPrimary().getBounds().getHeight() - (Screen.getPrimary().getBounds().getHeight() % gridSize)) * 3;

            setTranslateX(gridSize * 0.5);
            setTranslateY(gridSize * 0.5);

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

                    // Add new lines to cover the screen with zoom accounted for
                    int i = -screenWidthInGridSlices;
                    while (i * gridSize - gridSize < screenWidthInGridSlices) {
                        Line line = new Line(i * gridSize, -screenHeightInGridSlices, i * gridSize, screenHeightInGridSlices);
                        line.getStyleClass().add("grid-line");

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

                    // Add new lines to cover the screen with zoom accounted for
                    int i = -screenHeightInGridSlices;
                    while (i * gridSize - gridSize < screenHeightInGridSlices) {
                        Line line = new Line(-screenWidthInGridSlices, i * gridSize, screenWidthInGridSlices, i * gridSize);
                        line.getStyleClass().add("grid-line");

                        horizontalLines.add(line);
                        i++;
                    }
                    horizontalLines.forEach(line -> getChildren().add(line));
                });
            });
        }

        public void handleTranslateX(double oldValue, double newValue, double scale) {
            this.setTranslateX(this.getTranslateX() + (newValue - oldValue) / -scale);
        }

        public void handleTranslateY(double oldValue, double newValue, double scale) {
            this.setTranslateY(this.getTranslateY() + (newValue - oldValue) / -scale);
        }
    }
}
