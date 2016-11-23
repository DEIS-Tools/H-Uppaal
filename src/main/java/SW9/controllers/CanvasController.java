package SW9.controllers;

import SW9.abstractions.Component;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import javafx.animation.Transition;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
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

        root.getChildren().addAll(getTestLocation());

    }

    private Path getTestLocation() {
        final Path path = new Path();

        final DoubleProperty animation = new SimpleDoubleProperty(0);
        final DoubleBinding reverseAnimation = new SimpleDoubleProperty(1).subtract(animation);

        final double c = 0.551915024494;
        final double scale = 15;
        double l2lr = 0.35;

        final MoveTo moveTo = new MoveTo();
        moveTo.xProperty().bind(animation.multiply(l2lr * scale));
        moveTo.yProperty().set(scale);

        final CubicCurveTo cc1 = new CubicCurveTo();
        cc1.controlX1Property().bind(reverseAnimation.multiply(c * scale).add(animation.multiply(l2lr * scale)));
        cc1.controlY1Property().bind(reverseAnimation.multiply(scale).add(animation.multiply(scale)));
        cc1.controlX2Property().bind(reverseAnimation.multiply(scale).add(animation.multiply(scale)));
        cc1.controlY2Property().bind(reverseAnimation.multiply(c * scale).add(animation.multiply(l2lr * scale)));
        cc1.setX(scale);
        cc1.yProperty().bind(animation.multiply(l2lr * scale));


        final LineTo lineTo1 = new LineTo();
        lineTo1.xProperty().bind(cc1.xProperty());
        lineTo1.yProperty().bind(cc1.yProperty().multiply(-1));

        final CubicCurveTo cc2 = new CubicCurveTo();
        cc2.controlX1Property().bind(cc1.controlX2Property());
        cc2.controlY1Property().bind(cc1.controlY2Property().multiply(-1));
        cc2.controlX2Property().bind(cc1.controlX1Property());
        cc2.controlY2Property().bind(cc1.controlY1Property().multiply(-1));
        cc2.xProperty().bind(moveTo.xProperty());
        cc2.yProperty().bind(moveTo.yProperty().multiply(-1));


        final LineTo lineTo2 = new LineTo();
        lineTo2.xProperty().bind(cc2.xProperty().multiply(-1));
        lineTo2.yProperty().bind(cc2.yProperty());

        final CubicCurveTo cc3 = new CubicCurveTo();
        cc3.controlX1Property().bind(cc2.controlX2Property().multiply(-1));
        cc3.controlY1Property().bind(cc2.controlY2Property());
        cc3.controlX2Property().bind(cc2.controlX1Property().multiply(-1));
        cc3.controlY2Property().bind(cc2.controlY1Property());
        cc3.xProperty().bind(lineTo1.xProperty().multiply(-1));
        cc3.yProperty().bind(lineTo1.yProperty());


        final LineTo lineTo3 = new LineTo();
        lineTo3.xProperty().bind(cc3.xProperty());
        lineTo3.yProperty().bind(cc3.yProperty().multiply(-1));

        final CubicCurveTo cc4 = new CubicCurveTo();
        cc4.controlX1Property().bind(cc3.controlX2Property());
        cc4.controlY1Property().bind(cc3.controlY2Property().multiply(-1));
        cc4.controlX2Property().bind(cc3.controlX1Property());
        cc4.controlY2Property().bind(cc3.controlY1Property().multiply(-1));
        cc4.xProperty().bind(lineTo2.xProperty());
        cc4.yProperty().bind(lineTo2.yProperty().multiply(-1));


        final LineTo lineTo4 = new LineTo();
        lineTo4.xProperty().bind(moveTo.xProperty());
        lineTo4.yProperty().bind(moveTo.yProperty());


        path.getElements().add(moveTo);
        path.getElements().add(cc1);

        path.getElements().add(lineTo1);
        path.getElements().add(cc2);

        path.getElements().add(lineTo2);
        path.getElements().add(cc3);

        path.getElements().add(lineTo3);
        path.getElements().add(cc4);

        path.getElements().add(lineTo4);

        KeyboardTracker.registerKeybind("LOCATION_DAKKE_DAK", new Keybind(new KeyCodeCombination(KeyCode.N), () -> {
            final Transition a = new Transition() {
                {
                    setCycleDuration(Duration.millis(500));
                }

                @Override
                protected void interpolate(final double frac) {
                    animation.set(frac);
                }
            };
            a.play();
        }));

        path.setFill(Color.AQUA);
        path.setStroke(Color.RED);
        path.setStrokeWidth(4);

        return path;
    }

}
