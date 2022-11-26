package dk.cs.aau.huppaal.logging;

import java.util.regex.Pattern;

// location regex:      [text](location:ComponentName/LocationId)
// edge regex:          [text](edge:ComponentName/EdgeId)
// subcomponent regex:  [text](subcomponent:ComponentName/SubcomponentId)
// jork regex:          [text](jork:ComponentName/JorkId)
// tag regex:           [text](tag:ComponentName/TagId)
// query regex:         [text](query:QueryId)
// component regex:     [text](component:ComponentName)
// file:                [text](generic:filename) (open OS default app)
public class LogRegex {
    public static Pattern PATTERN = getPattern();
    private static Pattern getPattern() {
        var sb = new StringBuilder();
        var sep = "";
        for(var quantifier : LogLinkQuantifier.values()) {
            sb.append(sep).append(quantifier.name());
            sep = "|";
            sb.append(sep).append(quantifier.name().toLowerCase());
        }
        // For humans:
        // [<display>](<quantifier>:<ref1>/<ref2>?)
        var x = "\\[(?<display>[^]]+)]\\((?<quantifier>"+sb+"):(?<link>(?<component>[^/]+)/?(?<identifier>\\S+)?)\\)";
        return Pattern.compile(x);
    }
}
