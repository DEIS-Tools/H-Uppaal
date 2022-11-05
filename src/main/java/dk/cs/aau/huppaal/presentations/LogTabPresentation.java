package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.controllers.LogTabController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import javafx.scene.layout.VBox;

public class LogTabPresentation extends VBox {
    public LogTabController controller;
    public LogTabPresentation() {
        controller = PresentationFxmlLoader.loadSetRoot("LogTabPresentation.fxml", this);
        Log.addOnLogAddedListener(this::OnLogAdded);
    }

    private void OnLogAdded(Log log) {
        if(!log.level().equals(controller.level))
            return;
        getChildren().add(new LogPresentation(log));
    }
}
