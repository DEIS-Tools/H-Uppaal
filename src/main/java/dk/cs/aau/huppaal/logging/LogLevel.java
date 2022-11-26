package dk.cs.aau.huppaal.logging;

public enum LogLevel {
    Information,
    Warning,
    Error;

    public static LogLevel parseLogLevel(String s) {
        return LogLevel.valueOf(s.trim().toLowerCase());
    }
}
