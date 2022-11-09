package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.LogTabController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.presentations.log.HyperlinkTextArea;
import dk.cs.aau.huppaal.presentations.log.TextStyle;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import javafx.beans.NamedArg;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.awt.*;
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
        logArea.setTextInsertionStyle(new TextStyle().updateTextColorWeb(textColor));
        logArea.getStyleClass().add("log-text");
        logArea.setEditable(false);
        logArea.setWrapText(true);

        scrollPane = new VirtualizedScrollPane<>(logArea);
        controller.logAreaInsertionPoint.getChildren().add(scrollPane);

        initializeButtons();
        setupAutoscroll();
        Log.addOnLogAddedListener(this::onLogAdded);
    }

    private void onLinkClick(String linkString) {
        try {
            Desktop.getDesktop().browse(new URI(linkString));
        } catch (IOException | URISyntaxException e) {
            HUPPAAL.showToast(e.getMessage());
            Log.addError(e.getMessage());
        }
    }

    private void onLogAdded(Log log) {
        if(!log.level().equals(controller.level))
            return;
        // TODO: Highlighting, hyperlinking, check if it already ends in a newline etc.
        // TODO: Implement clickable links like so:
        //       location regex:      !location:ComponentName/LocationId
        //       edge regex:          !edge:ComponentName/EdgeId
        //       subcomponent regex:  !subcomponent:ComponentName/SubcomponentId
        //       jork regex:          !jork:ComponentName/JorkId
        //       tag regex:           !tag:ComponentName/TagId
        //       component regex:     !component:ComponentName
        //       file:                !file:filename (open OS default app)
        //       - If a regex is recognized, but the link doesn't work, highlight the link with red
        //         and make it un-clickable, or maybe clicks just generate a toast.
        //         Make sure to subscribe the link to "on deleted" of the referenced things
        //       - Update 2022-11-08:
        //         I've tried to implement this with a TextFlow container of styled Text children
        //         however there's no way of making the text "highlightable/selectable"
        //         https://stackoverflow.com/questions/33274827/how-to-make-textflow-selectable
        //         There's an open issue about this (created in 2013, updated in 2018):
        //         https://bugs.openjdk.org/browse/JDK-8092278
        //         in lue of waiting another decade to get a proper logging framework
        //         The conclusion is to use a third-party solution: RichTextArea
        //         Holy crap JavaFX really is stupid at some places.
        logArea.appendText(log.message() + "\n");
    }

    private void initializeButtons() {
        controller.clearLogsButton.setOnMouseClicked(e -> {
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
