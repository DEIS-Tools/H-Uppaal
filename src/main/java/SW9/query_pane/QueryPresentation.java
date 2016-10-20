package SW9.query_pane;

import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import javafx.beans.binding.When;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class QueryPresentation extends AnchorPane {

    private final Query query;

    public QueryPresentation(final Query query) {
        final URL location = this.getClass().getResource("QueryPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            this.query = query;

            initializeStateIndicator();

            initializeProgressIndicator();

            initializeActionIcon();


        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeActionIcon() {
        // Find the action icon
        final JFXButton actionButton = (JFXButton) lookup("#actionButton");
        final FontIcon actionIcon = (FontIcon) actionButton.getGraphic();


        int a = 2;
        // Delegate that based on the query state updated the action icon
        final Consumer<QueryState> updateIcon = (queryState) -> {
            if (queryState.equals(QueryState.RUNNING)) {
                actionIcon.setIconLiteral("gmi-stop");
            } else {
                actionIcon.setIconLiteral("gmi-play-arrow");
            }
        };

        // Update the icon initially
        updateIcon.accept(query.getQueryState());

        // Update the icon when ever the query state is updated
        query.queryStateProperty().addListener((observable, oldValue, newValue) -> updateIcon.accept(newValue));
    }

    private void initializeProgressIndicator() {
        // Find the progress indicator
        final JFXSpinner progressIndicator = (JFXSpinner) lookup("#progressIndicator");

        // If the query is running show the indicator, otherwise hide it
        progressIndicator.visibleProperty().bind(new When(query.queryStateProperty().isEqualTo(QueryState.RUNNING)).then(true).otherwise(false));
    }

    private void initializeStateIndicator() {
        // Find the state indicator from the inflated xml
        final StackPane stateIndicator = (StackPane) lookup("#stateIndicator");

        // Delegate that based on a query state updates the color of the state indicator
        final Consumer<QueryState> updateColor = (queryState) -> {
            final Color color = queryState.getColor();
            final Color.Intensity colorIntensity = queryState.getColorIntensity();

            stateIndicator.setBackground(new Background(new BackgroundFill(color.getColor(colorIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY)
            ));
        };

        // Update the initial color
        updateColor.accept(query.getQueryState());

        // Ensure that the color is updated when ever the query state is updated
        query.queryStateProperty().addListener((observable, oldValue, newValue) -> updateColor.accept(newValue));
    }
}
