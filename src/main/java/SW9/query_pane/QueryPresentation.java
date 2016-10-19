package SW9.query_pane;

import SW9.abstractions.Query;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

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

            setStyle("-fx-background-color: red;");

            Label label = new Label();
            label.textProperty().bind(query.queryProperty());
            getChildren().add(label);

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
