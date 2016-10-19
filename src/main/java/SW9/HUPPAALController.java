package SW9;

import SW9.query_pane.QueryPanePresentation;
import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

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

    private WindowAbstraction abstraction;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Create new abstraction
        this.abstraction = new WindowAbstraction();
    }

    public WindowAbstraction getAbstraction() {
        return abstraction;
    }

    public void setAbstraction(final WindowAbstraction abstraction) {
        this.abstraction = abstraction;
    }

    @FXML
    private void minimizeWindowButtonClicked() {
        ((Stage) minimizeWindowButton.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void maximizeWindowButtonClicked() {
        final Stage stage = ((Stage) minimizeWindowButton.getScene().getWindow());
        final WindowAbstraction abstraction = getAbstraction();

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
