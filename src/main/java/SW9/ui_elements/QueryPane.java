package SW9.ui_elements;

import SW9.utility.colors.Color;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.stage.Screen;

public class QueryPane extends VBox {


    public QueryPane() {
        // FXMLLoader.load(getClass().getResource("main.fxml"))

        initialize();
    }

    @FXML
    public void initialize() {
        StackPane.setAlignment(this, Pos.CENTER_RIGHT);

        setBackground(new Background(new BackgroundFill(
                Color.GREY_BLUE.getColor(Color.Intensity.I500),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        // Set the width of the query box to 20% of the screen size
        final double width = Screen.getPrimary().getVisualBounds().getWidth() * 0.2;
        setMaxWidth(width);

        // Hide the query pane
        translateXProperty().set(width);
    }

}
