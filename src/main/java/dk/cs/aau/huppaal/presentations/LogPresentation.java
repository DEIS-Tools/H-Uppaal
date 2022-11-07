package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.controllers.LogController;
import dk.cs.aau.huppaal.logging.ContextLabel;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.logging.MonoTextLabel;
import dk.cs.aau.huppaal.presentations.util.PresentationFxmlLoader;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class LogPresentation extends VBox {
    public LogController controller;
    public Log log;
    public LogPresentation(Log log) {
        this.log = log;
        controller = PresentationFxmlLoader.loadSetRoot("LogPresentation.fxml", this);
        initializeLabels();
        initializeReferenceCollection();
        initializeHeadline();
        initializeLogIcon();
        initializeLogIconColor();
    }

    public LogPresentation(Log log, String icon) {
        this.log = log;
        controller = PresentationFxmlLoader.loadSetRoot("LogPresentation.fxml", this);
        initializeLabels();
        initializeReferenceCollection();
        initializeHeadline();
        setLogIcon(icon);
        initializeLogIconColor();
    }

    public void initializeLabels() {
        if(!log.contexts().isEmpty())
            controller.messagesBox.getChildren().add(new MonoTextLabel("ref: "));
        for(var s : log.message().split("\n"))
            controller.messagesBox.getChildren().add(new MonoTextLabel(s));
    }

    public void initializeReferenceCollection() {
        for(var r : log.contexts())
            controller.referencesBox.getChildren().add(new ContextLabel(r));
    }

    public void initializeHeadline() {
        controller.headline.setText(String.format("[%s]", log.service()));
    }

    public void setLogIcon(String gmiIcon) {
        controller.logIcon.setIconLiteral(gmiIcon);
    }

    public void setLogIconColor(Color color) {
        controller.logIcon.setIconColor(color);
    }

    public void initializeLogIcon() {
        switch (log.level()) {
            case Information -> setLogIcon("gmi-info");
            case Warning -> setLogIcon("gmi-warning");
            case Error -> setLogIcon("gmi-error");
        }
    }

    public void initializeLogIconColor() {
        switch (log.level()) {
            case Information -> setLogIconColor(Color.WHITE);
            case Warning -> setLogIconColor(Color.YELLOW);
            case Error -> setLogIconColor(Color.RED);
        }
    }
}
