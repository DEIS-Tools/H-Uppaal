package SW9.presentations;

import SW9.controllers.HUPPAALController;
import SW9.utility.colors.Color;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.*;

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

            initializeTopBar();
            initializeBottomStatusBar();
            initializeToolbar();

            controller.bottomStatusBar.heightProperty().addListener((observable, oldValue, newValue) -> AnchorPane.setBottomAnchor(controller.queryPane, (Double) newValue));
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeTopBar() {
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            controller.menuBar.useSystemMenuBarProperty().set(true);
            controller.menuBar.setVisible(false);
        } else {
            final Color color = Color.GREY_BLUE;
            final Color.Intensity intensity = Color.Intensity.I800;

            // Set the background for the top toolbar
            controller.menuBar.setBackground(
                    new Background(new BackgroundFill(color.getColor(intensity),
                            CornerRadii.EMPTY,
                            Insets.EMPTY)
                    ));

            // Set the bottom border
            controller.menuBar.setBorder(new Border(new BorderStroke(
                    color.getColor(intensity.next()),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(0, 0, 1, 0)
            )));
        }
    }

    private void initializeToolbar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I700;

        // Set the background for the top toolbar
        controller.toolbar.setBackground(
                new Background(new BackgroundFill(color.getColor(intensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));

        // Set the font color for the title
        controller.title.setTextFill(color.getTextColor(intensity));
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
