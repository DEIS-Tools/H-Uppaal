package dk.cs.aau.huppaal.logging;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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
public class LogTextField extends TextFlow {
    private class NormalText extends Text {
        public NormalText(String text) {
            this(text, Color.WHITE);
        }
        public NormalText(String text, Color c) {
            super(text);
            setFill(c);
            getStyleClass().add("copyable-label");
            getStyleClass().add("body2-mono");
        }
    }

    private final Log log;

    public LogTextField(Log log) {
        this.log = log;
        initializeTextFields();
        setLineSpacing(0);
        setPadding(new Insets(0,0,0,0));
    }

    private void initializeTextFields() {
        getChildren().add(new Hyperlink(String.format("[%s]: ", this.log.service())));
        getChildren().add(new NormalText(this.log.message()));
    }
}
