package dk.cs.aau.huppaal;

import dk.cs.aau.huppaal.presentations.PreloaderPresentation;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HUPPAALPreloader extends Preloader {
    public enum LoadStage {
        LOADING_PROJECT,
        INITALIZE_JFX,
        AFTER_INIT,
        START_JFX,
        AFTER_SHOW,
        FINISHED
    }
    public static class Notification implements PreloaderNotification {
        private final LoadStage stage;
        public Notification(LoadStage stage) {
            this.stage = stage;
        }
        public LoadStage getStage() {
            return stage;
        }
    }
    private Stage stage;
    private PreloaderPresentation presentation;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = new Stage();
        presentation = new PreloaderPresentation();
        presentation.getController().projectNameLabel.setText(HUPPAAL.projectDirectory.getValue());
        stage.setScene(new Scene(presentation));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if(info instanceof Notification notificationInfo) {
            switch (notificationInfo.getStage()) {
                case LOADING_PROJECT -> presentation.getController().statusLabel.setText("Loading Project...");
                case INITALIZE_JFX -> presentation.getController().statusLabel.setText("Initializing JFX...");
                case AFTER_INIT -> presentation.getController().statusLabel.setText("Finished init...");
                case START_JFX -> presentation.getController().statusLabel.setText("Starting JFX...");
                case AFTER_SHOW -> stage.hide();
                case FINISHED -> Platform.runLater(() -> presentation.getController().statusLabel.setText("Finished"));
            }
        }
        super.handleApplicationNotification(info);
    }
}
