package dk.cs.aau.huppaal.logging;

import java.util.regex.Pattern;

// location regex:      !location:ComponentName/LocationId
// edge regex:          !edge:ComponentName/EdgeId
// subcomponent regex:  !subcomponent:ComponentName/SubcomponentId
// jork regex:          !jork:ComponentName/JorkId
// tag regex:           !tag:ComponentName/TagId
// query regex:         !query:QueryId
// component regex:     !component:ComponentName
// file:                !file:filename (open OS default app)
public class LogRegex {
    public static Pattern PATTERN = getPattern();
    private static Pattern getPattern() {
        var sb = new StringBuilder();
        var sep = "";
        for(var quantifier : LogRegexQuantifiers.values()) {
            sb.append(sep).append(quantifier.name());
            sep = "|";
            sb.append(sep).append(quantifier.name().toLowerCase());
        }
        var x = "!(?<quantifier>"+ sb +"):(?<reference>\\S+(/\\S+)?)";
        return Pattern.compile(x);
    }
}
