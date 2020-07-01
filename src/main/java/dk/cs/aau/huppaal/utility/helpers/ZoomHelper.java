package dk.cs.aau.huppaal.utility.helpers;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.presentations.CanvasPresentation;
import javafx.scene.layout.Pane;

public class ZoomHelper {
    private static Pane canvas;

    public static void setCanvas(Pane newCanvas) {
        canvas = newCanvas;
    }

    public static void zoomIn() {
        double newScale = canvas.getScaleX();
        double delta = 1.2;

        newScale *= delta;

        canvas.setScaleX(newScale);
        canvas.setScaleY(newScale);
    }

    public static void zoomOut() {
        double newScale = canvas.getScaleX();
        double delta = 1.2;

        newScale /= delta;

        canvas.setScaleX(newScale);
        canvas.setScaleY(newScale);
    }

    public static void resetZoom() {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
    }

    public static void zoomToFit() {
        double newScale = (canvas.getParent().getScene().widthProperty().doubleValue() / CanvasController.getActiveComponent().getWidth());

        canvas.setScaleX(newScale);
        canvas.setScaleY(newScale);
    }
}
