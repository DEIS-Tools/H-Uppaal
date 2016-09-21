package SW9;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class MouseTracker {

    private DoubleProperty x = new SimpleDoubleProperty(0);
    private DoubleProperty y = new SimpleDoubleProperty(0);

    private final ArrayList<EventHandler<MouseEvent>> onMouseMovedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseClickedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseEnteredEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseExitedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseDraggedEventHandlers = new ArrayList<>();


    private final EventHandler<MouseEvent> onMouseMovedEventHandler = event -> {

        // Purge the list for unregistered handlers
        onMouseMovedEventHandlers.removeIf(handler -> handler == null);

        for (EventHandler<MouseEvent> onMouseMovedEventHandler : onMouseMovedEventHandlers) {
            onMouseMovedEventHandler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseClickedEventHandler = event -> {

        // Purge the list for unregistered handlers
        onMouseClickedEventHandlers.removeIf(handler -> handler == null);

        for (EventHandler<MouseEvent> onMouseClickedEventHandler : onMouseClickedEventHandlers) {
            onMouseClickedEventHandler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseEnteredEventHandler = event -> {

        // Purge the list for unregistered handlers
        onMouseEnteredEventHandlers.removeIf(handler -> handler == null);

        for (EventHandler<MouseEvent> onMouseEnteredEventHandler : onMouseEnteredEventHandlers) {
            onMouseEnteredEventHandler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseExitedEventHandler = event -> {

        // Purge the list for unregistered handlers
        onMouseExitedEventHandlers.removeIf(handler -> handler == null);

        for (EventHandler<MouseEvent> onMouseExitedEventHandler : onMouseExitedEventHandlers) {
            onMouseExitedEventHandler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseDraggesEventHandler = event -> {

        // Purge the list for unregistered handlers
        onMouseDraggedEventHandlers.removeIf(handler -> handler == null);

        for (EventHandler<MouseEvent> onMouseDraggedEventHandler : onMouseDraggedEventHandlers) {
            onMouseDraggedEventHandler.handle(event);
        }
    };

    public MouseTracker(final Node owner) {
        owner.setOnMouseMoved(this.onMouseMovedEventHandler);
        owner.setOnMouseClicked(this.onMouseClickedEventHandler);
        owner.setOnMouseEntered(this.onMouseEnteredEventHandler);
        owner.setOnMouseExited(this.onMouseExitedEventHandler);
        owner.setOnMouseDragged(this.onMouseDraggesEventHandler);

        // Register our own event handler to register mouse placement at all times
        registerOnMouseMovedEventHandler(event -> {
            x.set(event.getX());
            y.set(event.getY());
        });
    }

    public DoubleProperty getXProperty() {
        return x;
    }

    public DoubleProperty getYProperty() {
        return y;
    }

    public boolean registerOnMouseMovedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseMovedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseMovedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if(!onMouseMovedEventHandlers.contains(eventHandler)) return false;
        onMouseMovedEventHandlers.set(onMouseMovedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public boolean registerOnMouseClickedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseClickedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseClickedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseClickedEventHandlers.contains(eventHandler)) return false;
        onMouseClickedEventHandlers.set(onMouseClickedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public boolean registerOnMouseEnteredEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseEnteredEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseEnteredEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseEnteredEventHandlers.contains(eventHandler)) return false;
        onMouseEnteredEventHandlers.set(onMouseEnteredEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public boolean registerOnMouseExitedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseExitedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseExitedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseExitedEventHandlers.contains(eventHandler)) return false;
        onMouseExitedEventHandlers.set(onMouseExitedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public boolean registerOnMouseDraggedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseDraggedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseDraggedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseDraggedEventHandlers.contains(eventHandler)) return false;
        onMouseExitedEventHandlers.set(onMouseExitedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

}
