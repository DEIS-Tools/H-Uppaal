package dk.cs.aau.huppaal.logging;

public class ContextLabel extends MonoTextLabel {
    public ContextLabel(String text) {
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
        super(text);
    }
}
