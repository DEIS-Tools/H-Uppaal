package dk.cs.aau.huppaal.utility.helpers;

import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.presentations.CanvasPresentation;

public class ZoomHelper {
    private static CanvasPresentation canvasPresentation;
    private static CanvasPresentation.Grid grid;

    public static void setCanvas(CanvasPresentation newCanvasPresentation) {
        canvasPresentation = newCanvasPresentation;
    }

    public static void setGrid(CanvasPresentation.Grid newGrid) {
        grid = newGrid;
    }

    public static void zoom(double delta) {
        var newScale = canvasPresentation.getScaleX() * delta;
        if(newScale > 8) //Limit for zooming in
            return;
        if(newScale < 0.4) //Limit for zooming out
            return;
        canvasPresentation.setScaleX(newScale);
        canvasPresentation.setScaleY(newScale);
        canvasPresentation.setTranslateX(canvasPresentation.getTranslateX() * delta);
        canvasPresentation.setTranslateY(canvasPresentation.getTranslateY() * delta);
    }

    public static void zoomIn() {
        zoom(1.2);
    }

    public static void zoomOut() {
        zoom(0.8);
    }

    public static void resetZoom() {
        canvasPresentation.setScaleX(1);
        canvasPresentation.setScaleY(1);
    }

    public static void zoomToFit() {
        double newScale = Math.min(canvasPresentation.getWidth() / CanvasController.getActiveComponent().getWidth() - 0.1, canvasPresentation.getHeight() / CanvasController.getActiveComponent().getHeight() - 0.2); //0.1 for width and 0.2 for height added for margin
        final double gridSize = CanvasPresentation.GRID_SIZE * newScale;
        double xOffset = newScale * canvasPresentation.getWidth() * 1.0f / 2 - newScale * CanvasController.getActiveComponent().getWidth() * 1.0f / 2;
        double yOffset = newScale * canvasPresentation.getHeight() * 1.0f / 3 - newScale * (CanvasController.getActiveComponent().getHeight() - newScale * 100) * 1.0f / 3; //The offset places the component a bit too high, so '-newScale * 100' is used to lower it a but

        canvasPresentation.setScaleX(newScale);
        canvasPresentation.setScaleY(newScale);

        canvasPresentation.setTranslateX(xOffset - (xOffset % gridSize) + gridSize * 0.5);
        canvasPresentation.setTranslateY(yOffset - (yOffset % gridSize) + gridSize * 0.5);

        grid.setTranslateX(gridSize * 0.5);
        grid.setTranslateY(gridSize * 0.5);
    }
}
