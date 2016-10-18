package SW9.model_canvas.component;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class NewComponentView extends VBox {

    public NewComponentView(final NewComponentModel model) {
        if (model != null) {
            final URL location = NewComponentController.class.getResource("ComponentView.fxml");

            final FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

            try {
                fxmlLoader.setRoot(this);
                fxmlLoader.load(location.openStream());

                final NewComponentController controller = fxmlLoader.getController();
                controller.setModel(model);
            } catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        }
    }
}
