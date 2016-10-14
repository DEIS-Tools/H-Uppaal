package SW9.ui_elements;

import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.stage.Screen;

import java.io.IOException;

public class QueryPane extends VBox {

    private HBox toolbar;
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox scrollPaneContent = new VBox();

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

        // Initialize the toolbar
        initializeToolbar();

        // Initialize the scroll pane (will be used to handle the query children)
        initializeScrollPane();

        // Populate the list with queries
        for (int i = 0; i < 20; i++) {
            final StringProperty query = new SimpleStringProperty("A[] not deadlock");
            final StringProperty comment = new SimpleStringProperty("The model does not contain a deadlock");

            final Query child = new Query(query, comment);
            scrollPaneContent.getChildren().add(child.getView());
        }
    }

    private void initializeScrollPane() {
        getChildren().add(scrollPane);

        // Will make the scroll pane larger
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Will set the content of the scroll pane (just a VBox)
        scrollPane.setContent(scrollPaneContent);

        // Styling
        scrollPane.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.TRANSPARENT,
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
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

    private class Query {

        private final HBox root;
        private final JFXTextField queryField;
        private final JFXTextField commentField;

        private final Color inputColor = Color.GREY_BLUE;
        private final Color.Intensity inputColorIntensity = Color.Intensity.I700;

        Query(final StringProperty query, final StringProperty comment) throws IOException {
            // Load the element from fxml
            root = FXMLLoader.load(getClass().getResource("/SW9/fxml/query_pane/query.fxml"));

            final Color color = Color.GREY;
            final Color.Intensity colorIntensity = Color.Intensity.I50;

            // Set the background color
            setBackground(new Background(new BackgroundFill(
                    color.getColor(colorIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY)));

            // Initialize the query field
            queryField = (JFXTextField) root.lookup("#query-text-field");
            initializeTextInputField(queryField, query, color.toHexColor(inputColorIntensity));

            // Initialize the comment field
            commentField = (JFXTextField) root.lookup("#comment-text-field");
            initializeTextInputField(commentField, comment, Color.GREY.toHexColor(Color.Intensity.I600));

            // Hover effect
            root.setOnMouseEntered(event -> {
                root.setBackground(new Background(new BackgroundFill(
                        Color.GREY_BLUE.getColor(colorIntensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)));
            });
            root.setOnMouseExited(event -> {
                root.setBackground(new Background(new BackgroundFill(
                        color.getColor(colorIntensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)));
            });
        }

        private void initializeTextInputField(final JFXTextField queryField, final StringProperty query, final String color) {
            // Set the text to whatever the text property we got, is
            queryField.setText(query.get());

            // Bind the property we got to the property of the input field (will notify original listeners when user updated query)
            query.bind(queryField.textProperty());

            // Set the focus and unfocused colors
            queryField.setFocusColor(inputColor.getColor(inputColorIntensity));
            queryField.setUnFocusColor(javafx.scene.paint.Color.TRANSPARENT);

            // Set the text color
            queryField.setStyle("-fx-text-fill: " + color + ";");
        }

        HBox getView() {
            return root;
        }

    }

}
