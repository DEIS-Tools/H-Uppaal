package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.LogTabController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.logging.LogTextField;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.StyleClassedTextArea;

public class LogTabPresentation extends HBox {
    public LogTabController controller;
    public boolean autoscroll;
    private final StyleClassedTextArea logArea;
    private final VirtualizedScrollPane<StyleClassedTextArea> scrollPane;
    public LogTabPresentation() {
        controller = PresentationFxmlLoader.loadSetRoot("LogTabPresentation.fxml", this);
        autoscroll = true;

        logArea = new StyleClassedTextArea();
        logArea.getStyleClass().add("log-text");
        logArea.setWrapText(true);
        logArea.setEditable(false);
        scrollPane = new VirtualizedScrollPane<>(logArea);
        controller.a.getChildren().add(scrollPane);

        initializeButtons();
        setupAutoscroll();
        Log.addOnLogAddedListener(this::onLogAdded);
    }

    private void onLogAdded(Log log) {
        if(!log.level().equals(controller.level))
            return;
        // TODO: Highlighting, hyperlinking, check if it already ends in a newline etc.
        logArea.append(log.message() + "\n", "log-text-entry");
    }

    private void initializeButtons() {
        controller.clearLogsButton.setOnMouseClicked(e -> {
            controller.logBox.getChildren().clear();
            // This is just to be polite to the logging framework (and your RAM)
            Log.clearLogsForLevel(controller.level);
            logArea.clear();
        });
        Tooltip.install(controller.clearLogsButton, new Tooltip("Clear log"));

        controller.filterLogsButton.setOnMouseClicked(e -> {
            Log.addError("Not implemented yet!");
            HUPPAAL.showToast("Not implemented yet!");
        });
        Tooltip.install(controller.filterLogsButton, new Tooltip("Filter logs"));

        controller.autoscrollLogButton.setOnMouseClicked(e -> toggleAutoScroll());
        Tooltip.install(controller.autoscrollLogButton, new Tooltip("Toggle autoscroll"));
        scrollPane.setOnScrollStarted(e -> {
            if(autoscroll)
                toggleAutoScroll();
        });
    }

    private void toggleAutoScroll() {
        autoscroll = !autoscroll;
        Log.addError("Not implemented yet!");
        HUPPAAL.showToast("Not implemented yet!");
        setupAutoscroll();
    }

    private void setupAutoscroll() {
        if(autoscroll) {
            // TODO: how to autoscroll a virtualized scroll pane?
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-add-check");
            controller.autoscrollLogButton.setStyle("-fx-background-color: rgba(255,255,255,0.1)");
        } else {
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-play");
            controller.autoscrollLogButton.setStyle("");
        }
    }
}
