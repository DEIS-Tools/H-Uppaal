package SW9;

import SW9.model_canvas.ModelCanvas;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class MouseTracker {

    private DoubleProperty x = new SimpleDoubleProperty(0);
    private DoubleProperty y = new SimpleDoubleProperty(0);
    private boolean isActive = true;

    private final ArrayList<EventHandler<MouseEvent>> onMouseMovedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseClickedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseEnteredEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseExitedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseDraggedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMousePressedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseReleasedEventHandlers = new ArrayList<>();

    private final EventHandler<MouseEvent> onMouseMovedEventHandler = event -> {
        if(!isActive) return;
        onMouseMovedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseMovedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseClickedEventHandler = event -> {
        if(!isActive) return;
        onMouseClickedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseClickedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseEnteredEventHandler = event -> {
        if(!isActive) return;
        onMouseEnteredEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseEnteredEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseExitedEventHandler = event -> {
        if(!isActive) return;
        onMouseExitedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseExitedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {
        if(!isActive) return;
        onMouseDraggedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseDraggedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMousePressedEventHandler = event -> {
        if(!isActive) return;
        onMousePressedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMousePressedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
        if(!isActive) return;
        onMouseReleasedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseReleasedEventHandlers) {
            handler.handle(event);
        }
    };

    public MouseTracker(final Node owner) {
        owner.setOnMouseMoved(this.onMouseMovedEventHandler);
        owner.setOnMouseClicked(this.onMouseClickedEventHandler);
        owner.setOnMouseEntered(this.onMouseEnteredEventHandler);
        owner.setOnMouseExited(this.onMouseExitedEventHandler);
        owner.setOnMouseDragged(this.onMouseDraggedEventHandler);
        owner.setOnMousePressed(this.onMousePressedEventHandler);
        owner.setOnMouseReleased(this.onMouseReleasedEventHandler);

        // Register our own event handler to register mouse placement at all times
        registerOnMouseMovedEventHandler(event -> {
            x.set(event.getX() - (event.getX() % ModelCanvas.GRID_SIZE) + (ModelCanvas.GRID_SIZE / 2));
            y.set(event.getY() - (event.getY() % ModelCanvas.GRID_SIZE) + (ModelCanvas.GRID_SIZE / 2));
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
        if (!onMouseMovedEventHandlers.contains(eventHandler)) return false;
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
        onMouseDraggedEventHandlers.set(onMouseDraggedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public boolean registerOnMousePressedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMousePressedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMousePressedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMousePressedEventHandlers.contains(eventHandler)) return false;
        onMousePressedEventHandlers.set(onMousePressedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public boolean registerOnMouseReleasedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseReleasedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseReleasedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseReleasedEventHandlers.contains(eventHandler)) return false;
        onMouseReleasedEventHandlers.set(onMouseReleasedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    /**
     * Disables the mouse tracker, stopping all events from being handled
     */
    public void disable() {
        isActive = false;
    }

    /**
     * Enables the mouse tracker, all events will now be handled
     */
    public void enable() {
        isActive = true;
    }

}
