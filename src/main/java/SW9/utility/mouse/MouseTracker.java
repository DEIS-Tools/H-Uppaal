package SW9.utility.mouse;

import SW9.presentations.CanvasPresentation;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class MouseTracker {

    private final DoubleProperty xProperty = new SimpleDoubleProperty(0);
    private final DoubleProperty yProperty = new SimpleDoubleProperty(0);
    private final ArrayList<EventHandler<MouseEvent>> onMouseMovedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseClickedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseEnteredEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseExitedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseDraggedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMousePressedEventHandlers = new ArrayList<>();
    private final ArrayList<EventHandler<MouseEvent>> onMouseReleasedEventHandlers = new ArrayList<>();
    private boolean isActive = true;

    private final DoubleBinding gridX = new DoubleBinding() {
        {
            super.bind(xProperty());
        }

        @Override
        protected double computeValue() {
            return xProperty().get() - (xProperty().get() % CanvasPresentation.GRID_SIZE) + CanvasPresentation.GRID_SIZE * 0.5;
        }
    };
    private final DoubleBinding gridY = new DoubleBinding() {
        {
            super.bind(yProperty());
        }

        @Override
        protected double computeValue() {
            return yProperty().get() - (yProperty().get() % CanvasPresentation.GRID_SIZE) + CanvasPresentation.GRID_SIZE * 0.5;
        }
    };

    private final EventHandler<MouseEvent> onMouseMovedEventHandler = event -> {
        if (!isActive) return;
        onMouseMovedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseMovedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseClickedEventHandler = event -> {
        if (!isActive) return;
        onMouseClickedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseClickedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseEnteredEventHandler = event -> {
        if (!isActive) return;
        onMouseEnteredEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseEnteredEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseExitedEventHandler = event -> {
        if (!isActive) return;
        onMouseExitedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseExitedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {
        if (!isActive) return;
        onMouseDraggedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMouseDraggedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMousePressedEventHandler = event -> {
        if (!isActive) return;
        onMousePressedEventHandlers.removeIf(handler -> handler == null);
        for (EventHandler<MouseEvent> handler : onMousePressedEventHandlers) {
            handler.handle(event);
        }
    };

    private final EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {
        if (!isActive) return;
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
        owner.addEventFilter(MouseEvent.ANY, event -> {
            if(!Double.isNaN(event.getX())) {
                xProperty.set(event.getX());
            }
            if(!Double.isNaN(event.getY())) {
                yProperty.set(event.getY());
            }
        });
    }

    public DoubleProperty xProperty() {
        return xProperty;
    }

    public DoubleProperty yProperty() {
        return yProperty;
    }

    public boolean registerOnMouseMovedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseMovedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseMovedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseMovedEventHandlers.contains(eventHandler)) return false;
        onMouseMovedEventHandlers.set(onMouseMovedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public void unregisterMouseMovedEventHandlers() {
        onMouseMovedEventHandlers.forEach(this::unregisterOnMouseMovedEventHandler);
    }

    public boolean registerOnMouseClickedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseClickedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseClickedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseClickedEventHandlers.contains(eventHandler)) return false;
        onMouseClickedEventHandlers.set(onMouseClickedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public void unregisterMouseClickedEventHandlers() {
        onMouseClickedEventHandlers.forEach(this::unregisterOnMouseClickedEventHandler);
    }

    public boolean registerOnMouseEnteredEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseEnteredEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseEnteredEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseEnteredEventHandlers.contains(eventHandler)) return false;
        onMouseEnteredEventHandlers.set(onMouseEnteredEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public void unregisterMouseEnteredEventHandlers() {
        onMouseEnteredEventHandlers.forEach(this::unregisterOnMouseEnteredEventHandler);
    }

    public boolean registerOnMouseExitedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseExitedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseExitedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseExitedEventHandlers.contains(eventHandler)) return false;
        onMouseExitedEventHandlers.set(onMouseExitedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public void unregisterMouseExitedEventHandlers() {
        onMouseExitedEventHandlers.forEach(this::unregisterOnMouseExitedEventHandler);
    }

    public boolean registerOnMouseDraggedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseDraggedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseDraggedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseDraggedEventHandlers.contains(eventHandler)) return false;
        onMouseDraggedEventHandlers.set(onMouseDraggedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public void unregisterMouseDraggedEventHandlers() {
        onMouseDraggedEventHandlers.forEach(this::unregisterOnMouseDraggedEventHandler);
    }

    public boolean registerOnMousePressedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMousePressedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMousePressedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMousePressedEventHandlers.contains(eventHandler)) return false;
        onMousePressedEventHandlers.set(onMousePressedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public void unregisterMousePressedEventHandlers() {
        onMousePressedEventHandlers.forEach(this::unregisterOnMousePressedEventHandler);
    }

    public boolean registerOnMouseReleasedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        return onMouseReleasedEventHandlers.add(eventHandler);
    }

    public boolean unregisterOnMouseReleasedEventHandler(final EventHandler<MouseEvent> eventHandler) {
        if (!onMouseReleasedEventHandlers.contains(eventHandler)) return false;
        onMouseReleasedEventHandlers.set(onMouseReleasedEventHandlers.indexOf(eventHandler), null);
        return true;
    }

    public void unregisterMouseReleasedEventHandlers() {
        onMouseReleasedEventHandlers.forEach(this::unregisterOnMouseReleasedEventHandler);
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

    public double getGridX() {
        return gridX.get();
    }

    public DoubleBinding gridXProperty() {
        return gridX;
    }

    public double getGridY() {
        return gridY.get();
    }

    public DoubleBinding gridYProperty() {
        return gridY;
    }
}
