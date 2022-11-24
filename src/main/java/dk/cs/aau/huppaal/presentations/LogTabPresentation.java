package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.BuildConfig;
import dk.cs.aau.huppaal.HUPPAAL;
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Optional;

public class LogTabPresentation extends HBox {
    public final LogTabController controller;
    private boolean autoscroll, wordwrap;
    private final HyperlinkTextArea logArea;
    private final VirtualizedScrollPane<HyperlinkTextArea> scrollPane;

    public LogTabPresentation(@NamedArg("textColor") String textColor) {
        controller = PresentationFxmlLoader.loadSetRoot("LogTabPresentation.fxml", this);
        wordwrap = true;
        autoscroll = true;
        logArea = new HyperlinkTextArea(this::onLinkClick);
        scrollPane = new VirtualizedScrollPane<>(logArea);
        logArea.textProperty().addListener((e, o, n) -> {
            if (!autoscroll)
                return;
            Platform.runLater(this::scrollToLastLine);
        });
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
                throw new RuntimeException("Not a valid %s link pattern: '%s'".formatted(BuildConfig.NAME,link.getLink()));
            // TODO: When syntactic elements have UUIDs to identify them, we should look at those IDs (instead/aswell)
            var regexLink = matcher.group("link");
            var regexComponent = matcher.group("component");
            var regexIdentifier = Optional.ofNullable(matcher.group("identifier"));
            var notValidLink = "Not a valid %s link: %s".formatted(link.getQuantifier().name().toLowerCase(), link.getLink());
            switch (link.getQuantifier()) {
                case LOCATION -> {
                    if(regexIdentifier.isEmpty())
                        Log.addWarning(notValidLink);
                    var component = SelectHelper.selectComponent(regexComponent);
                    regexIdentifier.ifPresent(s -> SelectHelper.selectLocation(component, s));
                }
                case COMPONENT -> SelectHelper.selectComponent(regexComponent);
                case SUBCOMPONENT -> {
                    if(regexIdentifier.isEmpty())
                        Log.addWarning(notValidLink);
                    var component = SelectHelper.selectComponent(regexComponent);
                    regexIdentifier.ifPresent(s -> SelectHelper.selectSubComponent(component, s));
                }
                case JORK -> {
                    if(regexIdentifier.isEmpty())
                        Log.addWarning(notValidLink);
                    var component = SelectHelper.selectComponent(regexComponent);
                    regexIdentifier.ifPresent(s -> SelectHelper.selectJork(component, s));
                }
                case EDGE -> {
                    if(regexIdentifier.isEmpty())
                        Log.addWarning(notValidLink);
                    var component = SelectHelper.selectComponent(regexComponent);
                    regexIdentifier.ifPresent(s -> SelectHelper.selectEdge(component, s));
                }
                case NAIL -> Log.addInfo("Edges and nails are not selectable yet");
                // Try to open the link
                default -> {
                    var f = new File(regexLink);
                    if(f.exists())
                        Desktop.getDesktop().open(f);
                    else
                        Desktop.getDesktop().browse(new URI(regexLink));
                }
            }
        } catch (Exception e) {
            HUPPAAL.showToast(e.getMessage());
            Log.addError(e.getMessage());
        }
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
        controller.wrapTextButton.setOnMouseClicked(e -> toggleWordwrap());

        Tooltip.install(controller.autoscrollLogButton, new Tooltip("Toggle autoscroll"));
        controller.autoscrollLogButton.setOnMouseClicked(e -> toggleAutoScroll());
    }

    private void toggleWordwrap() {
        setWordwrap(!wordwrap);
    }

    private void setWordwrap(boolean value) {
        wordwrap = value;
        setupWordWrap();
    }

    private void toggleAutoScroll() {
        setAutoscroll(!autoscroll);
    }

    private void setAutoscroll(boolean value) {
        autoscroll = value;
        setupAutoscroll();
    }

    private void setupAutoscroll() {
        if(autoscroll) {
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-add-check");
            controller.autoscrollLogButton.setStyle("-fx-background-color: rgba(255,255,255,0.1)");
            scrollToLastLine();
        } else {
            controller.autoscrollLogButtonIcon.setIconLiteral("gmi-playlist-play");
            controller.autoscrollLogButton.setStyle("");
            scrollPane.estimatedScrollYProperty().unbind();
        }
    }

    private void scrollToLastLine() {
        Platform.runLater(() -> scrollPane.scrollYBy(logArea.totalHeightEstimateProperty().getValue()));
    }

    private void setupWordWrap() {
        logArea.setWrapText(wordwrap);
        if(wordwrap)
            controller.wrapTextButton.setStyle("-fx-background-color: rgba(255,255,255,0.1)");
        else
            controller.wrapTextButton.setStyle("");
    }
}
