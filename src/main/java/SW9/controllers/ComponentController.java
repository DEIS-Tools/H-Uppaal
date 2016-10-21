package SW9.controllers;

import SW9.abstractions.Component;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ComponentController implements Initializable {

    private Component component;

    public StackPane container;
    public Rectangle background;
    public TextArea declaration;

    public ComponentController() {
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(final Component component) {
        this.component = component;
    }

    public void toggleDeclaration() {
        declaration.setVisible(true);

        final Circle circle = new Circle(0);
        circle.layoutXProperty().bind(component.widthProperty());

        final ObjectProperty<Node> clip = new SimpleObjectProperty<>(circle);
        declaration.clipProperty().bind(clip);

        final Transition rippleEffect = new Transition() {
            private final double maxRadius = Math.sqrt(Math.pow(component.getWidth(), 2) + Math.pow(component.getHeight(), 2));

            {
                setCycleDuration(Duration.millis(500));
            }

            protected void interpolate(final double fraction) {
                if(component.isDeclarationOpen()) {
                    circle.setRadius(fraction * maxRadius);
                } else {
                    circle.setRadius(maxRadius - fraction * maxRadius);
                }
                clip.set(circle);
            }
        };

        final Interpolator interpolator = Interpolator.SPLINE(0.785, 0.135, 0.15, 0.86);
        rippleEffect.setInterpolator(interpolator);

        rippleEffect.play();
        component.declarationOpenProperty().set(!component.isDeclarationOpen());
    }

}
