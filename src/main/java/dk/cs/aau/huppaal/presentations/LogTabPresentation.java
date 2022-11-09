package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.LogTabController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.logging.LogRegex;
import dk.cs.aau.huppaal.logging.LogRegexQuantifiers;
import dk.cs.aau.huppaal.presentations.logging.HyperlinkTextArea;
import dk.cs.aau.huppaal.presentations.logging.TextStyle;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import javafx.beans.NamedArg;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class LogTabPresentation extends HBox {
    public LogTabController controller;
    public boolean autoscroll;
    private final HyperlinkTextArea logArea;
    private final VirtualizedScrollPane<HyperlinkTextArea> scrollPane;
    public LogTabPresentation(@NamedArg("textColor") String textColor) {
        controller = PresentationFxmlLoader.loadSetRoot("LogTabPresentation.fxml", this);
        autoscroll = true;
        logArea = new HyperlinkTextArea(this::onLinkClick);
        scrollPane = new VirtualizedScrollPane<>(logArea);
        initializeLogArea(textColor);
        initializeButtons();
        setupAutoscroll();
        Log.addOnLogAddedListener(this::onLogAdded);
    }

    private void onLinkClick(String linkString) {
        // TODO: Implement clickable links like so:
        //       location regex:      !location:ComponentName/LocationId
        //       edge regex:          !edge:ComponentName/EdgeId
        //       subcomponent regex:  !subcomponent:ComponentName/SubcomponentId
        //       jork regex:          !jork:ComponentName/JorkId
        //       tag regex:           !tag:ComponentName/TagId
        //       component regex:     !component:ComponentName
        //       file:                !file:filename (open OS default app)
        try {
            Desktop.getDesktop().browse(new URI(linkString));
        } catch (IOException | URISyntaxException e) {
            HUPPAAL.showToast(e.getMessage());
            Log.addError(e.getMessage());
        }
    }

    private synchronized void onLogAdded(Log log) {
        if(!log.level().equals(controller.level))
            return;
        var logMessage = log.message();
        var matcher = LogRegex.PATTERN.matcher(log.message());
        var index = 0;
        while(matcher.find()) {
            logArea.appendText(logMessage.substring(index, matcher.start()));
            index = matcher.end();
            logArea.appendWithLink(matcher.group("reference"), "http://www.google.com",
                    LogRegexQuantifiers.valueOf(matcher.group("quantifier").toUpperCase()).getStyle());
        }
        logArea.appendText(logMessage.substring(index) + "\n");
    }

    private void initializeLogArea(String textColor) {
        logArea.setTextInsertionStyle(new TextStyle().updateTextColorWeb(textColor));
        logArea.getStyleClass().add("log-text");
        logArea.setEditable(false);
        logArea.setWrapText(true);
        controller.logAreaInsertionPoint.getChildren().add(scrollPane);
    }

    private void initializeButtons() {
        Tooltip.install(controller.clearLogsButton, new Tooltip("Clear log"));
        controller.clearLogsButton.setOnMouseClicked(e -> {
            Log.clearLogsForLevel(controller.level); // This is just to be polite to the logging framework, and your RAM
            logArea.clear();
        });

        controller.filterLogsButton.setOnMouseClicked(e -> {
            Log.addError("Not implemented yet!");
            HUPPAAL.showToast("Not implemented yet!");
        });
        Tooltip.install(controller.filterLogsButton, new Tooltip("Filter logs"));

        Tooltip.install(controller.autoscrollLogButton, new Tooltip("Toggle autoscroll"));
        controller.autoscrollLogButton.setOnMouseClicked(e -> toggleAutoScroll());
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
