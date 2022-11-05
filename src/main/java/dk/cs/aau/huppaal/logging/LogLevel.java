package dk.cs.aau.huppaal.logging;

import dk.cs.aau.huppaal.exceptions.InvalidFormatException;

public enum LogLevel {
    Information,
    Warning,
    Error;

    public static LogLevel parseLogLevel(String s) throws Exception {
        return switch (s.trim().toLowerCase()) {
            case "information" -> Information;
            case "warning" -> Warning;
            case "error" -> Error;
            default -> throw new InvalidFormatException("not a LogLevel: " + s);
        };
    }
}
