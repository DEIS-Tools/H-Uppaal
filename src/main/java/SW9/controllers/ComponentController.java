package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.presentations.LocationPresentation;
import SW9.utility.colors.Color;
import SW9.utility.helpers.DragHelper;
import com.jfoenix.controls.JFXButton;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class ComponentController implements Initializable {

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);

    public BorderPane toolbar;
    public StackPane container;
    public Rectangle background;
    public TextArea declaration;
    public JFXButton toggleDeclarationButton;
    public StackPane locationContainer;
    public LocationPresentation initialLocation;
    public LocationPresentation finalLocation;
    public BorderPane frame;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        component.addListener((observable, oldComponent, newComponent) -> {
            initializeToolbar();

            // Whenever the initial location updates in the model, also update it in the view
            component.get().initialLocationProperty().addListener((observable1, oldInitialLocation, newInitialLocation) -> {
                initializeInitialLocation(newInitialLocation);
            });

            // Whenever the initial location updates in the model, also update it in the view
            component.get().finalLocationProperty().addListener((observable1, oldFinalLocation, newFinalLocation) -> {
                initializeFinalLocation(newFinalLocation);
            });

            initializeInitialLocation(newComponent.getInitialLocation());
            initializeFinalLocation(newComponent.getFinalLocation());
        });

        initializeFrame();

    }

    private void initializeInitialLocation(final Location newInitialLocation) {
        initialLocation.setLocation(newInitialLocation);

        StackPane.setAlignment(initialLocation, Pos.TOP_LEFT);
    }

    private void initializeFinalLocation(final Location newFinalLocation) {
        finalLocation.setLocation(newFinalLocation);

        StackPane.setAlignment(finalLocation, Pos.BOTTOM_RIGHT);
    }

    private void initializeToolbar() {
        final Color color = getComponent().getColor();
        final Color.Intensity colorIntensity = getComponent().getColorIntensity();

        // Set the background of the toolbar
        toolbar.setBackground(new Background(new BackgroundFill(
                color.getColor(colorIntensity),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        // Set the border of the toolbar
        toolbar.setBorder(new Border(new BorderStroke(
                color.getColor(colorIntensity.next(2)),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(1)
        )));

        // Set the icon color and rippler color of the toggleDeclarationButton
        toggleDeclarationButton.setRipplerFill(color.getTextColor(colorIntensity));
        ((FontIcon) toggleDeclarationButton.getGraphic()).setFill(color.getTextColor(colorIntensity));

        // Set a hover effect for the toggleDeclarationButton
        toggleDeclarationButton.setOnMouseEntered(event -> toggleDeclarationButton.setCursor(Cursor.HAND));
        toggleDeclarationButton.setOnMouseExited(event -> toggleDeclarationButton.setCursor(Cursor.DEFAULT));
    }

    private void initializeFrame() {
        // TODO: Crop corners
    }

    public void toggleDeclaration(final MouseEvent mouseEvent) {
        declaration.setVisible(true);

        final Circle circle = new Circle(0);
        circle.setCenterX(component.get().getWidth() - (toggleDeclarationButton.getWidth() - mouseEvent.getX()));
        circle.setCenterY(-1 * mouseEvent.getY());

        final ObjectProperty<Node> clip = new SimpleObjectProperty<>(circle);
        declaration.clipProperty().bind(clip);

        final Transition rippleEffect = new Transition() {
            private final double maxRadius = Math.sqrt(Math.pow(getComponent().getWidth(), 2) + Math.pow(getComponent().getHeight(), 2));

            {
                setCycleDuration(Duration.millis(500));
            }

            protected void interpolate(final double fraction) {
                if(getComponent().isDeclarationOpen()) {
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
        getComponent().declarationOpenProperty().set(!getComponent().isDeclarationOpen());
    }

    public Component getComponent() {
        return component.get();
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    public void setComponent(final Component component) {
        this.component.set(component);
    }

}
