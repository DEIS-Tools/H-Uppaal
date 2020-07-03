package dk.cs.aau.huppaal.utility.helpers;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.CanvasController;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ZoomHelper {
    private static Pane canvas;

    public static void setCanvas(Pane newCanvas) {
        canvas = newCanvas;
    }

    public static void zoomIn() {
        double newScale = canvas.getScaleX();
        double delta = 1.2;

        newScale *= delta;

        //Limit for zooming in
        if(newScale > 8){
            return;
        }

        canvas.setScaleX(newScale);
        canvas.setScaleY(newScale);
    }

    public static void zoomOut() {
        double newScale = canvas.getScaleX();
        double delta = 1.2;

        newScale /= delta;

        //Limit for zooming out
        if(newScale < 0.4){
            return;
        }

        canvas.setScaleX(newScale);
        canvas.setScaleY(newScale);
    }

    public static void resetZoom() {
        canvas.setScaleX(1);
        canvas.setScaleY(1);
    }

    public static void zoomToFit() {
        double newScale = (CanvasController.getActiveComponent().getWidth() / canvas.getWidth() * (Stage.getWindows().get(0).getWidth() / Screen.getPrimary().getBounds().getWidth()));

        canvas.setScaleX(newScale);
        canvas.setScaleY(newScale);
    }
}
