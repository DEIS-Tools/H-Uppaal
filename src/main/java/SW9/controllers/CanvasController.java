package SW9.controllers;

import SW9.abstractions.Component;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.utility.helpers.SelectHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

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
    }

    public static ObjectProperty<Component> activeComponentProperty() {
        return activeComponent;
    }

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
                root.setTranslateX(GRID_SIZE * 5 + GRID_SIZE / 2 + 4);
                root.setTranslateY(GRID_SIZE * 10 + GRID_SIZE / 2);
            }

            final ComponentPresentation newComponentPresentation = new ComponentPresentation(newComponent);
            root.getChildren().add(newComponentPresentation);
        });
    }

}
