package SW9;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class HUPPAALView extends AnchorPane {

    public HUPPAALView(final WindowModel model) {
        if (model != null) {
            final URL location = HUPPAALController.class.getResource("HUPPAALView.fxml");

            final FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

            try {
                fxmlLoader.setRoot(this);
                fxmlLoader.load(location.openStream());

                final HUPPAALController controller = fxmlLoader.getController();
                controller.setModel(model);
            } catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        }
    }

}
