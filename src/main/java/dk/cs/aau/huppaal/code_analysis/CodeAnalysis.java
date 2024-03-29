package dk.cs.aau.huppaal.code_analysis;

import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.logging.LogLevel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Logging system for analysis conclusions to be presented.
 * e.g. if a location has no incoming edges, that indicates an implementation error
 * @deprecated Use the new logging framework ({@link Log}) for this
 * */
@Deprecated
public class CodeAnalysis {
    private static boolean ENABLED = true;

    private static final ObservableList<Message> backendErrors = FXCollections.observableArrayList();
    private static final ObservableList<Message> warnings = FXCollections.observableArrayList();
    private static final ObservableList<Message> errors = FXCollections.observableArrayList();

    private static final Map<Component, ObservableList<Message>> componentWarningsMap = new HashMap<>();
    private static final Map<Component, ObservableList<Message>> componentErrorsMap = new HashMap<>();

    @Deprecated
    public static ObservableList<Message> getWarnings(final Component component) {
        ObservableList<Message> list = componentWarningsMap.get(component);
        if (list == null) {
            list = FXCollections.observableArrayList();
            componentWarningsMap.put(component, list);
        }
        return list;
    }

    @Deprecated
    private static void addToWarnings(final Component component, final Message message) {
        Log.addLog(message.toLog(component));
        if(!ENABLED) return;

        final ObservableList<Message> list = getWarnings(component);

        list.add(message);
        warnings.add(message);
    }

    @Deprecated
    private static void removeFromWarnings(final Component component, final Message message) {
        if(component != null)
            Log.removeUuid(component.getName(), message.id);
        else
            Log.removeUuid(message.id);
        if(!ENABLED) return;

        final ObservableList<Message> list = getWarnings(component);

        list.remove(message);
        warnings.remove(message);
    }

    @Deprecated
    public static ObservableList<Message> getErrors(final Component component) {
        ObservableList<Message> list = componentErrorsMap.get(component);
        if (list == null) {
            list = FXCollections.observableArrayList();
            componentErrorsMap.put(component, list);
        }

        return list;
    }

    @Deprecated
    private static void addToErrors(final Component component, final Message message) {
        Log.addLog(message.toLog(component));
        if(!ENABLED) return;

        final ObservableList<Message> list = getErrors(component);

        if (list.contains(message)) return;

        list.add(message);
        errors.add(message);
    }

    @Deprecated
    private static void removeFromErrors(final Component component, final Message message) {
        if(component != null)
            Log.removeUuid(component.getName(), message.id);
        else
            Log.removeUuid(message.id);
        if(!ENABLED) return;

        final ObservableList<Message> list = getErrors(component);

        list.remove(message);
        errors.remove(message);
    }

    @Deprecated
    public static void addMessage(Message message) {
        addMessage(null, message);
    }

    @Deprecated
    public static void addMessage(String message) {
        addMessage(new Message(message));
    }

    @Deprecated
    public static void addMessage(final Component component, final Message message) {
        if(!ENABLED)
            return;
        switch (message.getMessageType()) {
            case ERROR -> addToErrors(component, message);
            case WARNING -> addToWarnings(component, message);
        }
    }

    @Deprecated
    public static void removeMessage(final Component component, final Message message) {
        if(!ENABLED)
            return;
        switch (message.getMessageType()) {
            case ERROR -> removeFromErrors(component, message);
            case WARNING -> removeFromWarnings(component, message);
        }
    }

    @Deprecated
    public static void clearWarnings(final Component component) {
        Log.clearLogsForLevel(LogLevel.Warning);
        getWarnings(component).clear();
    }

    @Deprecated
    public static void clearErrors(final Component component) {
        Log.clearLogsForLevel(LogLevel.Error);
        getErrors(component).clear();
    }

    @Deprecated
    public static ObservableList<Message> getWarnings() {
        return warnings;
    }

    @Deprecated
    public static ObservableList<Message> getErrors() {
        return errors;
    }

    @Deprecated
    public static void addBackendError(final Message message) {
        backendErrors.add(message);
    }

    @Deprecated
    public static void clearBackendErrors() {
        backendErrors.clear();
    }

    @Deprecated
    public static ObservableList<Message> getBackendErrors() {
        return backendErrors;
    }

    @Deprecated
    public enum MessageType {WARNING, ERROR}

    @Deprecated
    public static class Message {
        private final UUID id;
        private final MessageType messageType;
        private final StringProperty message;

        private ObservableList<Nearable> nearables = FXCollections.observableArrayList();

        public Message(final String message, final MessageType messageType, final Nearable... nearables) {
            this.message = new SimpleStringProperty(message);
            this.messageType = messageType;
            this.id = UUID.randomUUID();
            this.nearables.addAll(nearables);
        }

        public Message(String message) {
            this(message, MessageType.WARNING);
        }

        public Message(String message, MessageType type) {
            this(message, type, new ArrayList<>());
        }

        public Message(String message, MessageType messageType, List<Nearable> nearables) {
            this.message = new SimpleStringProperty(message);
            this.messageType = messageType;
            this.id = UUID.randomUUID();
            this.nearables.addAll(nearables);
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public String getMessage() {
            return message.get();
        }

        public void setMessage(final String message) {
            this.message.set(message);
        }

        public StringProperty messageProperty() {
            return message;
        }

        public ObservableList<Nearable> getNearables() {
            return nearables;
        }

        public void setNearables(final ObservableList<Nearable> nearables) {
            this.nearables = nearables;
        }

        /**
         * Convert a Code analysis message to a Log message.
         * @deprecated Use the Log framework instead of this.
         * */
        @Deprecated()
        public Log toLog() {
            return toLog(null);
        }

        /**
         * Convert a Code analysis message to a Log message.
         * @deprecated Use the Log framework instead of this.
         * */
        @Deprecated()
        public Log toLog(Component service) {
            // Messy, super-ugly hack to circumvent issues relating to ObservableList
            var finalButNotFinalMessageWrapper = new Object() { String msg = ""; String sep = ""; };
            nearables.forEach(n -> {
                finalButNotFinalMessageWrapper.msg += finalButNotFinalMessageWrapper.sep + n.generateNearString();
                finalButNotFinalMessageWrapper.sep = ", ";
            });
            if(!nearables.isEmpty())
                finalButNotFinalMessageWrapper.msg += ": ";
            finalButNotFinalMessageWrapper.msg += message.get();
            var s = Log.DEFAULT_SERVICE;
            if(service != null)
                s = service.getName();
            return new Log(id, s, finalButNotFinalMessageWrapper.msg, switch (messageType) {
                case WARNING -> LogLevel.Warning;
                case ERROR -> LogLevel.Error;
                default -> LogLevel.Information;
            });
        }
    }

    @Deprecated
    public static void enable() {
        ENABLED = true;
    }

    @Deprecated
    public static void disable() {
        ENABLED = false;
    }

}
