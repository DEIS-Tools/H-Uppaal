package dk.cs.aau.huppaal.utility.helpers;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.presentations.CanvasPresentation;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.rmi.MarshalException;

public class ZoomHelper {
    private static CanvasPresentation canvasPresentation;
    private static CanvasPresentation.Grid grid;

    public static void setCanvas(CanvasPresentation newCanvasPresentation) {
        canvasPresentation = newCanvasPresentation;
    }

    public static void setGrid(CanvasPresentation.Grid newGrid) {
        grid = newGrid;
    }

    public static void zoomIn() {
        double newScale = canvasPresentation.getScaleX();
        double delta = 1.2;

        newScale *= delta;

        //Limit for zooming in
        if(newScale > 8){
            return;
        }

        canvasPresentation.setScaleX(newScale);
        canvasPresentation.setScaleY(newScale);
    }

    public static void zoomOut() {
        double newScale = canvasPresentation.getScaleX();
        double delta = 1.2;

        newScale /= delta;

        //Limit for zooming out
        if(newScale < 0.4){
            return;
        }

        canvasPresentation.setScaleX(newScale);
        canvasPresentation.setScaleY(newScale);
    }

    public static void resetZoom() {
        canvasPresentation.setScaleX(1);
        canvasPresentation.setScaleY(1);
    }

    public static void zoomToFit() {
        double newScale = Math.min(canvasPresentation.getWidth() / CanvasController.getActiveComponent().getWidth() - 0.1, canvasPresentation.getHeight() / CanvasController.getActiveComponent().getHeight() - 0.1); //0.1 added as margin
        final double gridSize = CanvasPresentation.GRID_SIZE / newScale;
        double x = CanvasController.getActiveComponent().getWidth() * newScale;
        double y = CanvasController.getActiveComponent().getHeight() * newScale;

        canvasPresentation.setScaleX(newScale);
        canvasPresentation.setScaleY(newScale);

        canvasPresentation.setLayoutX(0);
        canvasPresentation.setLayoutY(0);

        canvasPresentation.setTranslateX(gridSize * 0.5);
        canvasPresentation.setTranslateY(gridSize * 0.5);

        grid.setTranslateX(gridSize * 0.5);
        grid.setTranslateY(gridSize * 0.5);
    }
}
