package dk.cs.aau.huppaal.controllers;

import com.jfoenix.controls.JFXRippler;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.logging.LogLevel;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class LogTabController implements Initializable {
    public LogLevel level;
    public JFXRippler autoscrollLogButton, clearLogsButton, filterLogsButton;
    public FontIcon autoscrollLogButtonIcon;
    public StackPane logAreaInsertionPoint;

    public LogTabController() {
        level = LogLevel.Information;
    }

    public LogTabController(LogLevel level) {
        this.level = level;
    }

    public String getLevel() {
        return level.name();
    }

    public void setLevel(String level) {
        try {
            this.level = LogLevel.parseLogLevel(level);
        } catch (Exception e) {
            e.printStackTrace();
            HUPPAAL.showToast(level + " is not a log level");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: Maybe grab the loglevel from resources?
    }
}
