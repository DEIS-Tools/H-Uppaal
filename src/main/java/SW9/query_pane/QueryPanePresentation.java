package SW9.query_pane;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class QueryPanePresentation extends AnchorPane {

    public QueryPanePresentation() {
        initialize(new QueryPanePAC(this));
    }

    public QueryPanePresentation(final QueryPanePAC pac) {
        initialize(pac);
    }

    private void initialize(final QueryPanePAC pac) {
        final URL location = this.getClass().getResource("QueryPanePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            final QueryPaneController controller = fxmlLoader.getController();
            pac.setController(controller);
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

}
