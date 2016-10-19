package SW9.query_pane;

import SW9.utility.colors.Color;
import SW9.utility.helpers.DropShadowHelper;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class QueryPanePresentation extends StackPane {

    private final QueryPaneController controller;

    public QueryPanePresentation() {
        final URL location = this.getClass().getResource("QueryPanePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            controller = fxmlLoader.getController();

            initializeToolbar();
            initializeAddQueryButton();
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeToolbar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        // Set the background of the toolbar
        controller.toolbar.setBackground(new Background(new BackgroundFill(
                color.getColor(colorIntensity),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        // Set the elevation of the toolbar
        controller.toolbar.setEffect(DropShadowHelper.generateElevationShadow(8));
    }

    private void initializeAddQueryButton() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I700;

        // Set the background of the add query button (will make it look like a floating action button)
        controller.addQueryButton.setBackground(new Background(new BackgroundFill(
                color.getColor(intensity),
                new CornerRadii(100),
                Insets.EMPTY
        )));

        // Add a drop shadow to the add query button
        controller.addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(6));

        controller.addQueryButton.setRipplerFill(color.getTextColor(intensity));
    }

}
