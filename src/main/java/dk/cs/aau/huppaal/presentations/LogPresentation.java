package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.controllers.LogController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.logging.MonoTextLabel;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LogPresentation extends VBox {
    public LogController controller;
    public Log log;
    public LogPresentation(Log log) {
        this.log = log;
        controller = PresentationFxmlLoader.loadSetRoot("LogPresentation.fxml", this);
        initializeLabels();
    }

    public void initializeLabels() {
        for(var s : log.message().split("\n"))
            controller.messagesBox.getChildren().add(new MonoTextLabel(s));
    }
}
