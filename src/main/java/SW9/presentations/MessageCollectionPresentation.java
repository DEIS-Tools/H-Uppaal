package SW9.presentations;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;

import java.io.IOException;
import java.net.URL;

public class MessageCollectionPresentation {

    public MessageCollectionPresentation() {
        final URL location = this.getClass().getResource("MessageCollectionPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());
            // Initialize here
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

}
