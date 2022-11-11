package dk.cs.aau.huppaal.presentations;

import com.hp.hpl.jena.shared.NotFoundException;
import dk.cs.aau.huppaal.BuildConfig;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.controllers.LogTabController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.logging.LogRegex;
import dk.cs.aau.huppaal.logging.LogLinkQuantifier;
import dk.cs.aau.huppaal.presentations.logging.Hyperlink;
import dk.cs.aau.huppaal.presentations.logging.HyperlinkTextArea;
import dk.cs.aau.huppaal.presentations.logging.TextStyle;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import dk.cs.aau.huppaal.utility.helpers.SelectHelper;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;

public class LogTabPresentation extends HBox {
    public LogTabController controller;
    public boolean autoscroll, wordwrap;
    private final HyperlinkTextArea logArea;
    private VirtualizedScrollPane<HyperlinkTextArea> scrollPane;
    public LogTabPresentation(@NamedArg("textColor") String textColor) {
        controller = PresentationFxmlLoader.loadSetRoot("LogTabPresentation.fxml", this);
        wordwrap = true;
        autoscroll = true;
        logArea = new HyperlinkTextArea(this::onLinkClick);
        scrollPane = new VirtualizedScrollPane<>(logArea);
        initializeLogArea(textColor);
        initializeButtons();
        setupWordWrap();
        setupAutoscroll();
        Log.addOnLogAddedListener(this::onLogAdded);
    }

    private void onLinkClick(Hyperlink link) {
        try {
            var matcher = LogRegex.PATTERN.matcher(link.getLink());
            if(!matcher.find()) // *should* never happen
                throw new RuntimeException("Not a valid %s link pattern: %s".formatted(BuildConfig.NAME,link.getLink()));
            // TODO: When syntactic elements have UUIDs to identify them, we should look at those IDs (instead/aswell)
            var ref  = matcher.group("ref");
            var ref1 = matcher.group("ref1");
            var ref2 = Optional.ofNullable(matcher.group("ref2"));
            switch (link.getQuantifier()) {
                case LOCATION -> {
                    if(ref2.isEmpty())
                        Log.addWarning("Not a valid location link: %s - directing to the component anyway".formatted(link.getLink()));
                    selectComponent(ref1); // TODO: Select Location
                }
                case COMPONENT -> selectComponent(ref1);
                // Try to open the link
                default -> Desktop.getDesktop().browse(new URI(ref));
            }
        } catch (Exception e) {
            HUPPAAL.showToast(e.getMessage());
            Log.addError(e.getMessage());
        }
    }

    // TODO: This function should be in SelectHelper, not the LogTabPresentation!
    private void selectComponent(String componentId) throws NotFoundException {
        var component = HUPPAAL.getProject().getComponents().stream().filter(c -> c.getName().equals(componentId)).findAny();
        if(component.isEmpty())
            throw new NotFoundException("No such component %s".formatted(componentId));
        if(!CanvasController.getActiveComponent().equals(component.get())) {
            SelectHelper.elementsToBeSelected = FXCollections.observableArrayList();
            CanvasController.setActiveComponent(component.get());
        }
        SelectHelper.clearSelectedElements();
        // TODO: SelectHelper.select(nearable);
    }

    private void onLogAdded(Log log) {
        if(!log.level().equals(controller.level))
            return;
        var logMessage = log.message();
        var matcher = LogRegex.PATTERN.matcher(log.message());
        var index = 0;
        while(matcher.find()) {
            logArea.appendText(logMessage.substring(index, matcher.start()));
            index = matcher.end();
            var q = LogLinkQuantifier.valueOf(matcher.group("quantifier").toUpperCase());
            logArea.appendWithLink(matcher.group("display"), matcher.group(), q);
        }
        logArea.appendText(logMessage.substring(index) + "\n");
    }

    private void initializeLogArea(String textColor) {
        logArea.setTextInsertionStyle(new TextStyle().updateTextColorWeb(textColor));
        logArea.getStyleClass().add("log-text");
        logArea.setEditable(false);
        controller.logAreaInsertionPoint.getChildren().add(scrollPane);
    }

    private void initializeButtons() {
        Tooltip.install(controller.clearLogsButton, new Tooltip("Clear log"));
        controller.clearLogsButton.setOnMouseClicked(e -> {
            Log.clearLogsForLevel(controller.level); // This is just to be polite to the logging framework, and your RAM
            logArea.clear();
        });

        Tooltip.install(controller.wrapTextButton, new Tooltip("Toggle word wrap"));
        controller.wrapTextButton.setOnMouseClicked(e -> {
            wordwrap = !wordwrap;
            setupWordWrap();
        });

        Tooltip.install(controller.autoscrollLogButton, new Tooltip("Toggle autoscroll"));
        controller.autoscrollLogButton.setOnMouseClicked(e -> toggleAutoScroll());
    }

    private void toggleAutoScroll() {
        setAutoscroll(!autoscroll);
    }

    private void setAutoscroll(boolean value) {
        autoscroll = value;
        setupAutoscroll();
    }

    private final ChangeListener<String> logAreaChangeEventListener = (e,o,n) -> {
        if(scrollPane == null)
            return;
        Platform.runLater(this::scrollToLastLine);
    };

    private void setupAutoscroll() {
        if(autoscroll) {
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-add-check");
            controller.autoscrollLogButton.setStyle("-fx-background-color: rgba(255,255,255,0.1)");
            logArea.textProperty().addListener(logAreaChangeEventListener);
            scrollToLastLine();
        } else {
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-play");
            controller.autoscrollLogButton.setStyle("");
            logArea.textProperty().removeListener(logAreaChangeEventListener);
            scrollPane.estimatedScrollYProperty().unbind();
        }
    }

    private void scrollToLastLine() {
        scrollPane.estimatedScrollYProperty().setValue(scrollPane.getContent().totalHeightEstimateProperty().getValue());
    }

    private void setupWordWrap() {
        logArea.setWrapText(wordwrap);
        if(wordwrap)
            controller.wrapTextButton.setStyle("-fx-background-color: rgba(255,255,255,0.1)");
        else
            controller.wrapTextButton.setStyle("");
    }
}
