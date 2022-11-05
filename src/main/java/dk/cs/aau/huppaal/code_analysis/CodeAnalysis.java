package dk.cs.aau.huppaal.code_analysis;

import dk.cs.aau.huppaal.abstractions.Component;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class CodeAnalysis {

    private static boolean ENABLED = true;

    private static final ObservableList<Message> backendErrors = FXCollections.observableArrayList();
    private static final ObservableList<Message> warnings = FXCollections.observableArrayList();
    private static final ObservableList<Message> errors = FXCollections.observableArrayList();

    private static final Map<Component, ObservableList<Message>> componentWarningsMap = new HashMap<>();
    private static final Map<Component, ObservableList<Message>> componentErrorsMap = new HashMap<>();

    public static ObservableList<Message> getWarnings(final Component component) {
        ObservableList<Message> list = componentWarningsMap.get(component);
        if (list == null) {
            list = FXCollections.observableArrayList();
            componentWarningsMap.put(component, list);
        }
        return list;
    }

    private static void addToWarnings(final Component component, final Message message) {
        if(!ENABLED) return;

        final ObservableList<Message> list = getWarnings(component);

        list.add(message);
        warnings.add(message);
    }

    private static void removeFromWarnings(final Component component, final Message message) {
        if(!ENABLED) return;

        final ObservableList<Message> list = getWarnings(component);

        list.remove(message);
        warnings.remove(message);
    }

    public static ObservableList<Message> getErrors(final Component component) {
        ObservableList<Message> list = componentErrorsMap.get(component);
        if (list == null) {
            list = FXCollections.observableArrayList();
            componentErrorsMap.put(component, list);
        }

        return list;
    }

    private static void addToErrors(final Component component, final Message message) {
        if(!ENABLED) return;

        final ObservableList<Message> list = getErrors(component);

        if (list.contains(message)) return;

        list.add(message);
        errors.add(message);
    }

    private static void removeFromErrors(final Component component, final Message message) {
        if(!ENABLED) return;

        final ObservableList<Message> list = getErrors(component);

        list.remove(message);
        errors.remove(message);
    }

    public static void addMessage(Message message) {
        addMessage(null, message);
    }

    public static void addMessage(String message) {
        addMessage(new Message(message));
    }

    public static void addMessage(final Component component, final Message message) {
        if(!ENABLED)
            return;
        switch (message.getMessageType()) {
            case ERROR -> addToErrors(component, message);
            case WARNING -> addToWarnings(component, message);
        }
    }

    public static void removeMessage(final Component component, final Message message) {
        if(!ENABLED)
            return;
        switch (message.getMessageType()) {
            case ERROR -> removeFromErrors(component, message);
            case WARNING -> removeFromWarnings(component, message);
        }
    }

    public static void clearWarnings(final Component component) {
        getWarnings(component).clear();
    }

    public static void clearErrors(final Component component) {
        getErrors(component).clear();
    }

    public static ObservableList<Message> getWarnings() {
        return warnings;
    }

    public static ObservableList<Message> getErrors() {
        return errors;
    }

    public static void addBackendError(final Message message) {
        backendErrors.add(message);
    }

    public static void clearBackendErrors() {
        backendErrors.clear();
    }

    public static ObservableList<Message> getBackendErrors() {
        return backendErrors;
    }

    public enum MessageType {WARNING, ERROR}

    public static class Message {

        private final MessageType messageType;
        private final StringProperty message;

        private ObservableList<Nearable> nearables = FXCollections.observableArrayList();

        public Message(String message) {
            this(message, MessageType.WARNING);
        }

        public Message(String message, MessageType type) {
            this(message, type, new ArrayList<>());
        }

        public Message(String message, MessageType messageType, Nearable... nearables) {
            this(message, messageType, Arrays.stream(nearables).toList());
        }

        public Message(String message, MessageType messageType, List<Nearable> nearables) {
            this.message = new SimpleStringProperty(message);
            this.messageType = messageType;
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
    }

    public static void enable() {
        ENABLED = true;
    }

    public static void disable() {
        ENABLED = false;
    }

}
