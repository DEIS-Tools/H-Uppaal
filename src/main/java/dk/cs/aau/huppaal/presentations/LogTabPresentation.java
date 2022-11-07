package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.LogTabController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import javafx.scene.layout.HBox;

public class LogTabPresentation extends HBox {
    public LogTabController controller;
    public boolean autoscroll;
    public LogTabPresentation() {
        controller = PresentationFxmlLoader.loadSetRoot("LogTabPresentation.fxml", this);
        autoscroll = true;
        Log.addOnLogAddedListener(this::onLogAdded);
        initializeButtons();
        setupAutoscroll();
    }

    private void onLogAdded(Log log) {
        if(!log.level().equals(controller.level))
            return;
        controller.logBox.getChildren().add(new LogPresentation(log));
    }

    private void initializeButtons() {
        controller.clearLogsButton.setOnMouseClicked(e -> {
            controller.logBox.getChildren().clear();
            // This is just to be polite to the logging framework (and your RAM)
            Log.clearLogsForLevel(controller.level);
        });
        controller.filterLogsButton.setOnMouseClicked(e -> {
            Log.addError("Not implemented yet!");
            HUPPAAL.showToast("Not implemented yet!");
        });
        controller.autoscrollLogButton.setOnMouseClicked(e -> toggleAutoScroll());
        controller.logBoxScrollPane.setOnScrollStarted(e -> {
            if(autoscroll)
                toggleAutoScroll();
        });
    }

    private void toggleAutoScroll() {
        autoscroll = !autoscroll;
        setupAutoscroll();
    }

    private void setupAutoscroll() {
        if(autoscroll) {
            controller.logBoxScrollPane.vvalueProperty().bind(controller.logBox.heightProperty());
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-add-check");
        } else {
            controller.logBoxScrollPane.vvalueProperty().unbind();
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-play");
        }
    }
}
