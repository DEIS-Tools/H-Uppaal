package dk.cs.aau.huppaal.logging;

import dk.cs.aau.huppaal.BuildConfig;
import dk.cs.aau.huppaal.utility.FuncInterfaces.Runnable1;

import javax.swing.*;
import java.util.*;

public record Log(
        UUID id,
        String service,         // service that produced the service
        String message,         // information about what happened
        List<String> contexts,  // e.g. links to locations, components, edges etc.
        LogLevel level          // buffer to put the message into
) {
    public static final String DEFAULT_SERVICE = BuildConfig.NAME;
    public static final Map<String, List<Log>> logs = new HashMap<>();
    private static final List<Runnable1<Log>> onLogAddedSubscribers = new ArrayList<>();
    private static final List<Runnable> onLogRemovedSubscribers = new ArrayList<>();

    public static void addError(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, Collections.emptyList(), LogLevel.Error));
    }
    public static void addWarning(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, Collections.emptyList(), LogLevel.Warning));
    }
    public static void addInfo(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, Collections.emptyList(), LogLevel.Information));
    }
    public static void addError(String service, String message) {
        addLog(new Log(UUID.randomUUID(), service, message, Collections.emptyList(), LogLevel.Error));
    }
    public static void addWarning(String service, String message) {
        addLog(new Log(UUID.randomUUID(), service, message, Collections.emptyList(), LogLevel.Warning));
    }
    public static void addInfo(String service, String message) {
        addLog(new Log(UUID.randomUUID(), service, message, Collections.emptyList(), LogLevel.Information));
    }
    public static void addLog(String message) {
        addLog(new Log(UUID.randomUUID(), DEFAULT_SERVICE, message, Collections.emptyList(), LogLevel.Information));
    }
    public static void addLog(Log message) {
        System.out.println(message.service + ": " + message.message);
        if(!logs.containsKey(message.service())) {
            var l = new ArrayList<Log>();
            l.add(message);
            logs.put(message.service(), l);
            return;
        }
        logs.get(message.service()).add(message);
        onLogAddedSubscribers.forEach(r -> r.run(message));
    }
    public static void clearAllLogs() {
        for(var l : logs.entrySet())
            clearLogForService(l.getKey());
    }
    public static void clearLogForService(String service) {
        if(!logs.containsKey(service))
            return;
        logs.get(service).clear();
        onLogRemovedSubscribers.forEach(Runnable::run);
    }
    public static void addOnLogAddedListener(Runnable1<Log> r) {
        onLogAddedSubscribers.add(r);
    }
    public static void addOnLogRemovedListener(Runnable r) {
        onLogRemovedSubscribers.add(r);
    }
}
