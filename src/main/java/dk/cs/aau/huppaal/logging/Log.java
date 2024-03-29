package dk.cs.aau.huppaal.logging;

import dk.cs.aau.huppaal.BuildConfig;
import javafx.application.Platform;

import java.util.*;
import java.util.function.Consumer;

/**
 * Logging framework for all your messaging needs. A log entry consists of:
 * <ul>
 *  <li>{@link #id} an (autogenerated) uuid</li>
 *  <li>{@link #service} a string of the name of the service that produced the log</li>
 *  <li>{@link #message} the message to log. See below for formatting</li>
 *  <li>{@link #level} what severity the log is</li>
 * </ul>
 * */
public record Log(
        UUID id,
        String service,         // service that produced the service
        String message,         // information about what happened
        LogLevel level          // buffer to put the message into
) {
    public static final String DEFAULT_SERVICE = BuildConfig.NAME;
    public static final Map<String, List<Log>> logs = new HashMap<>();
    private static final List<Consumer<Log>> onLogAddedSubscribers = new ArrayList<>();
    private static final List<Runnable> onLogRemovedSubscribers = new ArrayList<>();

    public static void addError(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, LogLevel.Error));
    }
    public static void addWarning(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, LogLevel.Warning));
    }
    public static void addInfo(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, LogLevel.Information));
    }
    public static void addError(String service, String message) {
        addLog(new Log(UUID.randomUUID(), service, message, LogLevel.Error));
    }
    public static void addWarning(String service, String message) {
        addLog(new Log(UUID.randomUUID(), service, message, LogLevel.Warning));
    }
    public static void addInfo(String service, String message) {
        addLog(new Log(UUID.randomUUID(), service, message, LogLevel.Information));
    }
    public static void addLog(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, LogLevel.Information));
    }
    public static synchronized void addLog(Log message) {
        if(!logs.containsKey(message.service()))
            logs.put(message.service(), new ArrayList<>());
        logs.get(message.service()).add(message);
        // Use runLater because the addLog might've been called from a thread that's not the main thread
        onLogAddedSubscribers.forEach(r -> Platform.runLater(() -> r.accept(message)));
    }
    public static synchronized void clearAllLogs() {
        for(var l : logs.entrySet())
            clearLogsForService(l.getKey());
    }
    public static synchronized void clearLogsForLevel(LogLevel level) {
        for(var e : logs.entrySet()) {
            var removeLogs = new ArrayList<Log>();
            for(var l : e.getValue()) {
                if(l.level.equals(level))
                    removeLogs.add(l);
            }
            e.getValue().removeAll(removeLogs);
        }
    }
    public static synchronized void clearLogsForService(String service) {
        if(!logs.containsKey(service))
            return;
        logs.get(service).clear();
        // Use runLater because the clearLogs might've been called from a thread that's not the main thread
        onLogRemovedSubscribers.forEach(Platform::runLater);
    }
    public static synchronized void addOnLogAddedListener(Consumer<Log> r) {
        onLogAddedSubscribers.add(r);
    }
    public static synchronized void addOnLogRemovedListener(Runnable r) {
        onLogRemovedSubscribers.add(r);
    }
    public static synchronized void removeUuid(UUID logId) {
        removeUuid(DEFAULT_SERVICE, logId);
    }
    public static synchronized void removeUuid(String service, UUID logId) {
        if(!logs.containsKey(service))
            return;
        var ls = logs.get(service);
        Log x = null;
        for(var l : ls)
            if(l.id().equals(logId))
                x = l;
        if(x != null)
            ls.remove(x);
    }
}
