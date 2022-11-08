package dk.cs.aau.huppaal.logging;

import javafx.scene.control.TextField;

// TODO: Having a TextField for each log entry makes only one log entry
//       selectable at a time. The official solution to this is to use
//       a TextArea, however when I tried, there were a lot of scrollpane
//       issues. At the time of writing there are more important features
//       to implement, but if you want to solve this take a look at the
//       TextArea solution again. It will very likely work.
public class LogTextField extends TextField {
    private final Log log;
    public LogTextField(Log log) {
        // TODO: Implement clickable links like so:
        //       location regex:      !location:ComponentName/LocationId
        //       edge regex:          !edge:ComponentName/EdgeId
        //       subcomponent regex:  !subcomponent:ComponentName/SubcomponentId
        //       jork regex:          !jork:ComponentName/JorkId
        //       tag regex:           !tag:ComponentName/TagId
        //       component regex:     !component:ComponentName
        //        - If a regex is recognized, but the link doesn't work, highlight the link with red
        //          and make it un-clickable
        //        - Make sure to subscribe the link to "on deleted" of the referenced things
        super(log.message());
        this.log = log;
        setEditable(false);
        getStyleClass().add("copyable-label");
        getStyleClass().add("body2-mono");
        setStyle("-fx-text-fill: white");
    }
}
