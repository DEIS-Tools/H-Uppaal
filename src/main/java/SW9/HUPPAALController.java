package SW9;

import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
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

    private WindowModel model;

    public void setModel(final WindowModel model) {
        this.model = model;
    }

    public WindowModel getModel() {
        return model;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        final Color topBarBackgroundColor = Color.GREY_BLUE;
        final Color.Intensity topBarBackgroundColorIntensity = Color.Intensity.I900;

        // Set the background for the top status bar
        topStatusBar.setBackground(
                new Background(new BackgroundFill(topBarBackgroundColor.getColor(topBarBackgroundColorIntensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));

        // Set the font color for the buttons
        ((FontIcon) minimizeWindowButton.getGraphic()).setFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));
        minimizeWindowButton.setRipplerFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));
        ((FontIcon) maximizeWindowButton.getGraphic()).setFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));
        maximizeWindowButton.setRipplerFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));
        ((FontIcon) closeWindowButton.getGraphic()).setFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));
        closeWindowButton.setRipplerFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));

        // Set the font color for the application title
        ((FontIcon) applicationTitle.getGraphic()).setFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));
        applicationTitle.setTextFill(topBarBackgroundColor.getTextColor(topBarBackgroundColorIntensity));

        final Color bottomBarBackgroundColor = Color.GREY_BLUE;
        final Color.Intensity bottomBarBackgroundColorIntensity = Color.Intensity.I200;

        // Set the background for the bottom status bar
        bottomStatusBar.setBackground(
                new Background(new BackgroundFill(bottomBarBackgroundColor.getColor(bottomBarBackgroundColorIntensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));
    }

    @FXML
    private void minimizeWindowButtonClicked() {
        ((Stage) minimizeWindowButton.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void maximizeWindowButtonClicked() {
        final Stage stage = ((Stage) minimizeWindowButton.getScene().getWindow());
        final WindowModel model = getModel();

        if (getModel().isIsMaximized()) {
            // Undo maximized again
            stage.setX(model.getNotMaximizedX());
            stage.setY(model.getNotMaximizedY());
            stage.setWidth(model.getNotMaximizedWidth());
            stage.setHeight(model.getNotMaximizedHeight());

            model.setIsMaximized(false);
        } else {
            model.setNotMaximizedX(stage.getX());
            model.setNotMaximizedY(stage.getY());
            model.setNotMaximizedWidth(stage.getWidth());
            model.setNotMaximizedHeight(stage.getHeight());

            stage.setX(0d);
            stage.setY(0d);

            // Maximize the window
            final Screen screen1 = Screen.getPrimary();
            final Rectangle2D bounds = screen1.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            model.setIsMaximized(true);
        }
    }

    @FXML
    private void closeWindowButtonClicked() {
        // TODO: Prompt the user to save project or just do it
        System.exit(0);
    }
}
