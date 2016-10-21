package SW9.controllers;

import SW9.abstractions.WindowPlacement;
import SW9.presentations.QueryPanePresentation;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HUPPAALController implements Initializable {

    public BorderPane topStatusBar;
    public BorderPane bottomStatusBar;

    public JFXButton minimizeWindowButton;
    public JFXButton maximizeWindowButton;
    public JFXButton closeWindowButton;
    public Label applicationTitle;
    public QueryPanePresentation queryPane;

    private WindowPlacement windowPlacement;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Create new windowPlacement
        this.windowPlacement = new WindowPlacement();
    }

    public WindowPlacement getWindowPlacement() {
        return windowPlacement;
    }

    public void setWindowPlacement(final WindowPlacement windowPlacement) {
        this.windowPlacement = windowPlacement;
    }

    @FXML
    private void minimizeWindowButtonClicked() {
        ((Stage) minimizeWindowButton.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void maximizeWindowButtonClicked() {
        final Stage stage = ((Stage) minimizeWindowButton.getScene().getWindow());
        final WindowPlacement abstraction = getWindowPlacement();

        if (abstraction.isIsMaximized()) {
            // Undo maximized again
            stage.setX(abstraction.getNotMaximizedX());
            stage.setY(abstraction.getNotMaximizedY());
            stage.setWidth(abstraction.getNotMaximizedWidth());
            stage.setHeight(abstraction.getNotMaximizedHeight());

            abstraction.setIsMaximized(false);
        } else {
            abstraction.setNotMaximizedX(stage.getX());
            abstraction.setNotMaximizedY(stage.getY());
            abstraction.setNotMaximizedWidth(stage.getWidth());
            abstraction.setNotMaximizedHeight(stage.getHeight());

            stage.setX(0d);
            stage.setY(0d);

            // Maximize the window
            final Screen screen1 = Screen.getPrimary();
            final Rectangle2D bounds = screen1.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            abstraction.setIsMaximized(true);
        }
    }

    @FXML
    private void closeWindowButtonClicked() {
        // TODO: Prompt the user to save project or just do it
        System.exit(0);
    }
}
