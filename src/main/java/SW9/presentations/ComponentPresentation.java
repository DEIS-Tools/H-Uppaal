package SW9.presentations;

import SW9.abstractions.Component;
import SW9.controllers.ComponentController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.DragHelper;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.mouse.MouseTracker;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class ComponentPresentation extends StackPane implements MouseTrackable {

    private final static double CORNER_SIZE = 50;

    private final ComponentController controller;
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();

    private final MouseTracker mouseTracker = new MouseTracker(this);

    public ComponentPresentation() {
        this(new Component("Component" + new Random().nextInt(5000))); // todo: find a new unique component name
    }

    public ComponentPresentation(final Component component) {
        final URL location = this.getClass().getResource("ComponentPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            initializeToolbar();
            initializeFrame();
            initializeInitialLocation();
            initializeFinalLocation();
            initializeBackground();
            initializeName();

            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            controller = fxmlLoader.getController();
            controller.setComponent(component);
            this.component.bind(controller.componentProperty());

            controller.frame.setOnMouseEntered(event -> {
                new Thread(() -> {
                    Platform.runLater(() -> controller.initialLocation.animateIn());

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // do nothing
                    }

                    Platform.runLater(() -> controller.finalLocation.animateIn());
                }).start();
            });

            controller.finalLocation.shakeAnimation();

            // Find the x and y coordinates to the values in the model
            layoutXProperty().bind(component.xProperty());
            layoutYProperty().bind(component.yProperty());

            // Bind the width and the height of the view to the values in the model
            minWidthProperty().bind(component.widthProperty());
            maxWidthProperty().bind(component.widthProperty());
            minHeightProperty().bind(component.heightProperty());
            maxHeightProperty().bind(component.heightProperty());

            DragHelper.makeDraggable(this);

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeName() {
        component.addListener((observable, oldValue, component) -> {
            // Set the text field to the name in the model, and bind the model to the text field
            controller.name.setText(component.getName());
            component.nameProperty().bind(controller.name.textProperty());

            final Color color = component.getColor();
            final Color.Intensity colorIntensity = component.getColorIntensity();

            // Set the text color for the label
            controller.name.setStyle("-fx-text-fill: " + color.toHexTextColor(colorIntensity) + ";");
            controller.name.setFocusColor(color.getTextColor(colorIntensity));
            controller.name.setUnFocusColor(javafx.scene.paint.Color.TRANSPARENT);

            // Add a left margin of CORNER_SIZE
            controller.name.setPadding(new Insets(0, 0, 0, CORNER_SIZE));
        });
    }

    private void initializeInitialLocation() {
        component.addListener((observable, oldValue, component) -> {
            controller.initialLocation.setLocation(component.getInitialLocation());
            controller.initialLocation.setTranslateX(7);
            controller.initialLocation.setTranslateY(7);
            controller.initialLocation.toFront();

            StackPane.setAlignment(controller.initialLocation, Pos.TOP_LEFT);
        });
    }

    private void initializeFinalLocation() {
        component.addListener((observable, oldValue, component) -> {
            controller.finalLocation.setLocation(component.getFinalLocation());
            controller.finalLocation.setTranslateX(-7);
            controller.finalLocation.setTranslateY(-7);
            controller.finalLocation.toFront();

            StackPane.setAlignment(controller.finalLocation, Pos.BOTTOM_RIGHT);
        });
    }

    private void initializeToolbar() {
        component.addListener((observable, oldValue, component) -> {
            final Color color = component.getColor();
            final Color.Intensity colorIntensity = component.getColorIntensity();

            // Set the background of the toolbar
            controller.toolbar.setBackground(new Background(new BackgroundFill(
                    color.getColor(colorIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            // Set the icon color and rippler color of the toggleDeclarationButton
            controller.toggleDeclarationButton.setRipplerFill(color.getTextColor(colorIntensity));
            ((FontIcon) controller.toggleDeclarationButton.getGraphic()).setFill(color.getTextColor(colorIntensity));

            // Set a hover effect for the controller.toggleDeclarationButton
            controller.toggleDeclarationButton.setOnMouseEntered(event -> controller.toggleDeclarationButton.setCursor(Cursor.HAND));
            controller.toggleDeclarationButton.setOnMouseExited(event -> controller.toggleDeclarationButton.setCursor(Cursor.DEFAULT));
        });
    }

    private void initializeFrame() {
        component.addListener((observable, oldValue, component) -> {
            Shape mask;
            final Rectangle rectangle = new Rectangle(component.getWidth(), component.getHeight());

            // Generate first corner (to subtract)
            final Polygon corner1 = new Polygon(
                    0, 0,
                    CORNER_SIZE + 2, 0,
                    0, CORNER_SIZE + 2
            );

            // Generate second corner (to subtract)
            final Polygon corner2 = new Polygon(
                    component.getWidth(), component.getHeight(),
                    component.getWidth() - CORNER_SIZE - 2, component.getHeight(),
                    component.getWidth(), component.getHeight() - CORNER_SIZE - 2
            );

            // Mask the parent of the frame (will also mask the background)
            mask = Path.subtract(rectangle, corner1);
            mask = Path.subtract(mask, corner2);
            controller.frame.setClip(mask);
            controller.background.setClip(Path.union(mask, mask));
            controller.background.setOpacity(0.5);

            // Add the missing lines that we cropped away
            final Line line1 = new Line(CORNER_SIZE, 0, 0, CORNER_SIZE);
            line1.setFill(component.getColor().getColor(component.getColorIntensity().next(2)));
            line1.setStrokeWidth(2);
            StackPane.setAlignment(line1, Pos.TOP_LEFT);
            getChildren().add(line1);

            final Line line2 = new Line(CORNER_SIZE, 0, 0, CORNER_SIZE);
            line2.setFill(component.getColor().getColor(component.getColorIntensity().next(2)));
            line2.setStrokeWidth(2);
            StackPane.setAlignment(line2, Pos.BOTTOM_RIGHT);
            getChildren().add(line2);

            // Set the stroke color to two shades darker
            controller.frame.setBorder(new Border(new BorderStroke(
                    component.getColor().getColor(component.getColorIntensity().next(2)),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(2),
                    Insets.EMPTY
            )));
        });
    }

    private void initializeBackground() {
        component.addListener((observable, oldValue, component) -> {
            // Bind the background width and height to the values in the model
            controller.background.widthProperty().bind(component.widthProperty());
            controller.background.heightProperty().bind(component.heightProperty());

            // Set the background color to the lightest possible version of the color
            controller.background.setFill(component.getColor().getColor(component.getColorIntensity()));
        });
    }

    @Override
    public DoubleProperty xProperty() {
        return component.get().xProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return component.get().yProperty();
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }
}
