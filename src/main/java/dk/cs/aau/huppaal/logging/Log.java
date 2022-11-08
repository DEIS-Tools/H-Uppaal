package dk.cs.aau.huppaal.logging;

import dk.cs.aau.huppaal.BuildConfig;
import dk.cs.aau.huppaal.utility.FuncInterfaces.Runnable1;
import javafx.application.Platform;

import java.util.*;

public record Log(
        UUID id,
        String service,         // service that produced the service
        String message,         // information about what happened
        LogLevel level          // buffer to put the message into
) {
    public static final String DEFAULT_SERVICE = BuildConfig.NAME;
    public static final Map<String, List<Log>> logs = new HashMap<>();
    private static final List<Runnable1<Log>> onLogAddedSubscribers = new ArrayList<>();
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
        System.out.println(message.service + ": " + message.message);
        if(!logs.containsKey(message.service()))
            logs.put(message.service(), new ArrayList<>());
        logs.get(message.service()).add(message);
        // Use runLater because the addLog might've been called from a thread that's not the main thread
        onLogAddedSubscribers.forEach(r -> Platform.runLater(() -> r.run(message)));
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
        onLogRemovedSubscribers.forEach(Runnable::run);
    }
    public static synchronized void addOnLogAddedListener(Runnable1<Log> r) {
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
