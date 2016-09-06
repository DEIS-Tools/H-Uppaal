package SW9;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class MouseTracker {

    private double x = 0;
    private double y = 0;

    private final ArrayList<EventHandler<MouseEvent>> onMouseMovedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseClickedEventHandlers = new ArrayList<>();

    public final EventHandler<MouseEvent> onMouseMovedEventHandler = event -> {

        // Purge the list for unregistered handlers
        onMouseMovedEventHandlers.removeIf(handler -> handler == null);

        for (EventHandler<MouseEvent> onMouseMovedEventHandler1 : onMouseMovedEventHandlers) {
            onMouseMovedEventHandler1.handle(event);
        }
    };

    public final EventHandler<MouseEvent> onMouseClickedEventHandler = event -> {

        // Purge the list for unregistered handlers
        onMouseClickedEventHandlers.removeIf(handler -> handler == null);

        for (EventHandler<MouseEvent> onMouseClickedEventHandler1 : onMouseClickedEventHandlers) {
            onMouseClickedEventHandler1.handle(event);
        }
    };

    public MouseTracker() {
        // Register our own event handler to register mouse placement at all times
        registerOnMouseMovedEventHandler(event -> {
            x = event.getX();
            y = event.getY();
        });
    }

    public double getX() {
        return x;
    }

    public double getY() {
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

}
