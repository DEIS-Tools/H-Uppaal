package SW9.presentations;

import SW9.controllers.QueryPaneController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.DropShadowHelper;
import com.jfoenix.controls.JFXRippler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

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

            initializeLeftBorder();
            initializeToolbar();
            initializeBackground();
            initializeAddQueryButton();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeLeftBorder() {
        controller.toolbar.setBorder(new Border(new BorderStroke(
                Color.GREY_BLUE.getColor(Color.Intensity.I900),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 0, 1)
        )));

        controller.scrollPane.setBorder(new Border(new BorderStroke(
                Color.GREY.getColor(Color.Intensity.I400),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 0, 1)
        )));
    }

    private void initializeBackground() {
        controller.queriesList.setBackground(new Background(new BackgroundFill(
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

        controller.runAllQueriesButton.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.TRANSPARENT,
                new CornerRadii(100),
                Insets.EMPTY)));

        controller.runAllQueriesButton.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        controller.runAllQueriesButton.setRipplerFill(color.getTextColor(colorIntensity));

        controller.clearAllQueriesButton.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        controller.clearAllQueriesButton.setRipplerFill(color.getTextColor(colorIntensity));

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

        // Set the font color of the button
        controller.addQueryButton.setRipplerFill(color.getTextColor(intensity));
        ((FontIcon) controller.addQueryButton.getGraphic()).setFill(color.getTextColor(intensity));
    }

}
