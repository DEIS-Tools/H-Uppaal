package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.PreloaderController;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

public class PreloaderPresentation extends StackPane {
    private final PreloaderController controller;
    public PreloaderPresentation() {
        controller = PresentationFxmlLoader.loadSetRoot("PreloaderPresentation.fxml", this);
        controller.logo.setImage(new Image(HUPPAAL.class.getResource("ic_launcher/mipmap-xxxhdpi/ic_launcher.png").toExternalForm()));
    }

    public PreloaderController getController() {
        return controller;
    }
}
