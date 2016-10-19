package SW9;

import SW9.utility.colors.Color;
import SW9.utility.helpers.DropShadowHelper;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class HUPPAALPresentation extends BorderPane {

    private final HUPPAALController controller;

    public HUPPAALPresentation() {
        final URL location = this.getClass().getResource("HUPPAALPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            controller = fxmlLoader.getController();

            initializeTopStatusBar();
            initializeBottomStatusBar();

            // Align the query pane to the bottom of the top bar, and the top of the bottom bar
            controller.topStatusBar.heightProperty().addListener((observable, oldValue, newValue) -> AnchorPane.setTopAnchor(controller.queryPane, (Double) newValue));
            controller.bottomStatusBar.heightProperty().addListener((observable, oldValue, newValue) -> AnchorPane.setBottomAnchor(controller.queryPane, (Double) newValue));

            controller.queryPane.setEffect(DropShadowHelper.generateElevationShadow(8));
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeTopStatusBar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I900;

        // Set the background for the top status bar
        controller.topStatusBar.setBackground(
                new Background(new BackgroundFill(color.getColor(intensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));

        // Set the font color for the buttons
        ((FontIcon) controller.minimizeWindowButton.getGraphic()).setFill(color.getTextColor(intensity));
        controller.minimizeWindowButton.setRipplerFill(color.getTextColor(intensity));
        ((FontIcon) controller.maximizeWindowButton.getGraphic()).setFill(color.getTextColor(intensity));
        controller.maximizeWindowButton.setRipplerFill(color.getTextColor(intensity));
        ((FontIcon) controller.closeWindowButton.getGraphic()).setFill(color.getTextColor(intensity));
        controller.closeWindowButton.setRipplerFill(color.getTextColor(intensity));

        // Set the font color for the application title
        ((FontIcon) controller.applicationTitle.getGraphic()).setFill(color.getTextColor(intensity));
        controller.applicationTitle.setTextFill(color.getTextColor(intensity));
    }

    private void initializeBottomStatusBar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I200;

        // Set the background for the bottom status bar
        controller.bottomStatusBar.setBackground(
                new Background(new BackgroundFill(color.getColor(intensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));
    }

}
