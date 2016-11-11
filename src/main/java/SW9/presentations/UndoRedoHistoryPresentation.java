package SW9.presentations;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class UndoRedoHistoryPresentation extends AnchorPane {

    public UndoRedoHistoryPresentation() {
        final URL location = this.getClass().getResource("UndoRedoHistoryPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
