package SW9.code_analysis;

import SW9.abstractions.Component;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CodeAnalysis {

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
        ObservableList<Message> list = componentWarningsMap.get(component);
        if (list == null) {
            list = FXCollections.observableArrayList();
            componentWarningsMap.put(component, list);
        }

        list.add(message);
        warnings.add(message);
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
        ObservableList<Message> list = componentErrorsMap.get(component);
        if (list == null) {
            list = FXCollections.observableArrayList();
            componentErrorsMap.put(component, list);
        }

        list.add(message);
        errors.add(message);
    }

    public static void addMessage(final Component component, final Message message) {
        if (message.getMessageType().equals(MessageType.WARNING)) {
            addToWarnings(component, message);
        } else if (message.getMessageType().equals(MessageType.ERROR)) {
            addToErrors(component, message);
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

    public enum MessageType {WARNING, ERROR}

    public static class Message {

        private final String message;
        private final MessageType messageType;

        public Message(final String message, final MessageType messageType) {
            this.message = message;
            this.messageType = messageType;
        }

        public String getMessage() {
            return message;
        }

        public MessageType getMessageType() {
            return messageType;
        }
    }


}
