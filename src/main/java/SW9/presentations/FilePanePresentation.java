package SW9.presentations;

import SW9.controllers.FilePaneController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.DropShadowHelper;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;

public class FilePanePresentation extends StackPane {

    private final FilePaneController controller;

    public FilePanePresentation() {
        final URL location = this.getClass().getResource("FilePanePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            controller = fxmlLoader.getController();

            initializeRightBorder();
            initializeBackground();
            initializeToolbar();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeRightBorder() {
        controller.toolbar.setBorder(new Border(new BorderStroke(
                Color.GREY_BLUE.getColor(Color.Intensity.I900),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 1, 0, 0)
        )));

        controller.scrollPane.setBorder(new Border(new BorderStroke(
                Color.GREY.getColor(Color.Intensity.I400),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 1, 0, 0)
        )));
    }

    private void initializeBackground() {
        controller.filesList.setBackground(new Background(new BackgroundFill(
                Color.GREY.getColor(Color.Intensity.I200),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
    }

    private void initializeToolbar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        // Set the background of the toolbar
        controller.toolbar.setBackground(new Background(new BackgroundFill(
                color.getColor(colorIntensity),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        // Set the font color of elements in the toolbar
        controller.toolbarTitle.setTextFill(color.getTextColor(colorIntensity));

        // Set the elevation of the toolbar
        controller.toolbar.setEffect(DropShadowHelper.generateElevationShadow(8));
    }

}