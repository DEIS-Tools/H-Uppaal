package SW9;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class HUPPAALPresentation extends AnchorPane {

    public HUPPAALPresentation() {
        initialize(new HUPPAALPAC(this));
    }

    public HUPPAALPresentation(final HUPPAALPAC pac) {
        initialize(pac);
    }

    private void initialize(final HUPPAALPAC pac) {
        final URL location = this.getClass().getResource("HUPPAALPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            final HUPPAALController controller = fxmlLoader.getController();
            pac.setController(controller);
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

}
