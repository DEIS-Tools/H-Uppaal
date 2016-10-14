package SW9.ui_elements;

import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.stage.Screen;

import java.io.IOException;

public class QueryPane extends VBox {

    private HBox toolbar;

    public QueryPane() throws IOException {
        initialize();
    }

    @FXML
    private void initialize() throws IOException {
        StackPane.setAlignment(this, Pos.CENTER_RIGHT);

        // Load the stylesheet for the query pane
        getStylesheets().add("SW9/query_pane.css");

        // Add a drop shadow (to make the pane look floating)
        final DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5);
        dropShadow.setHeight(200);
        this.setEffect(dropShadow);

        // Set the background color for the pane
        setBackground(new Background(new BackgroundFill(
                Color.GREY.getColor(Color.Intensity.I300),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        // Set the width of the query box to 20% of the screen size
        final double width = Screen.getPrimary().getVisualBounds().getWidth() * 0.2;
        setMaxWidth(width);

        // Hide the query pane
        translateXProperty().set(width);

        initializeToolbar();
    }

    private void initializeToolbar() throws IOException {
        // Load the toolbar fxml
        toolbar = FXMLLoader.load(getClass().getResource("/SW9/fxml/query_pane/toolbar.fxml"));
        getChildren().add(toolbar);

        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        // Set the background of the toolbar
        toolbar.setBackground(new Background(new BackgroundFill(
                color.getColor(colorIntensity),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        // Find the labels in the toolbar and style them accordingly to the background used
        ((Label) toolbar.lookup("#queries-headline")).setTextFill(color.getTextColor(colorIntensity));
        ((Label) toolbar.lookup("#queries-headline-caption")).setTextFill(color.getTextColor(colorIntensity));

        // Find the buttons in the toolbar and style them accordingly to the background used
        ((JFXButton) toolbar.lookup("#clear-button")).setTextFill(color.getTextColor(colorIntensity));
        ((JFXButton) toolbar.lookup("#run-all-button")).setTextFill(color.getTextColor(colorIntensity));

        toolbar.paddingProperty().set(new Insets(15));
    }

}
