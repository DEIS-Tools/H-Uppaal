package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.utility.helpers.SelectHelper;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class CanvasController implements Initializable {

    private final static ObjectProperty<Component> activeComponent = new SimpleObjectProperty<>(null);

    private final static HashMap<Component, Pair<Double, Double>> componentTranslateMap = new HashMap<>();

    public Pane root;

    public static Component getActiveComponent() {
        return activeComponent.get();
    }

    public static void setActiveComponent(final Component component) {
        CanvasController.activeComponent.set(component);
        Platform.runLater(CanvasController::leaveTextAreas);
    }

    public static ObjectProperty<Component> activeComponentProperty() {
        return activeComponent;
    }

    public static void leaveTextAreas() {
        leaveTextAreas.run();
    }

    public static EventHandler<KeyEvent> getLeaveTextAreaKeyHandler() {
        return getLeaveTextAreaKeyHandler(keyEvent -> {});
    }

    public static EventHandler<KeyEvent> getLeaveTextAreaKeyHandler(final Consumer<KeyEvent> afterEnter) {
        return (keyEvent) -> {
            leaveOnEnterPressed.accept(keyEvent);
            afterEnter.accept(keyEvent);
        };
    }

    private static Consumer<KeyEvent> leaveOnEnterPressed;
    private static Runnable leaveTextAreas;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        CanvasPresentation.mouseTracker.registerOnMousePressedEventHandler(event -> {
            // Deselect all elements
            SelectHelper.clearSelectedElements();
        });

        activeComponent.addListener((obs, oldComponent, newComponent) -> {
            if (oldComponent != null) {
                componentTranslateMap.put(oldComponent, new Pair<>(root.getTranslateX(), root.getTranslateY()));
            }

            root.getChildren().removeIf(node -> node instanceof ComponentPresentation);

            if (newComponent == null) return; // We should not add the new component since it is null (clear the view)

            if (componentTranslateMap.containsKey(newComponent)) {
                final Pair<Double, Double> restoreCoordinates = componentTranslateMap.get(newComponent);
                root.setTranslateX(restoreCoordinates.getKey());
                root.setTranslateY(restoreCoordinates.getValue());
            } else {
                root.setTranslateX(GRID_SIZE * 3);
                root.setTranslateY(GRID_SIZE * 8);
            }

            final ComponentPresentation newComponentPresentation = new ComponentPresentation(newComponent);
            root.getChildren().add(newComponentPresentation);
            root.requestFocus();
        });

        leaveTextAreas = () -> {root.requestFocus();};

        leaveOnEnterPressed = (keyEvent) -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                leaveTextAreas();
            }
        };

    }

}
