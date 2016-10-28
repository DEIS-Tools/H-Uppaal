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
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class ComponentPresentation extends StackPane implements MouseTrackable {

    private final static double CORNER_SIZE = 60;

    private final ComponentController controller;
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    private LocationPresentation initialLocationPresentation = null;
    private LocationPresentation finalLocationPresentation = null;

    public ComponentPresentation() {
        this(new Component("Component" + new Random().nextInt(5000))); // todo: find a new unique component name
    }

    public ComponentPresentation(final Component component) {
        final URL location = this.getClass().getResource("ComponentPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {

            initializeDefaultLocationsContainer();
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

    private void initializeDefaultLocationsContainer() {
        // Instantiate new initial and final location presentations when the component is updated
        this.component.addListener((observable, oldValue, newComponent) -> {

            if (initialLocationPresentation != null) {
                controller.defaultLocationsContainer.getChildren().remove(initialLocationPresentation);
            }

            if (finalLocationPresentation != null) {
                controller.defaultLocationsContainer.getChildren().remove(finalLocationPresentation);
            }

            // Instantiate views for the initial and final location
            initialLocationPresentation = new LocationPresentation(newComponent.getInitialLocation(), newComponent);
            finalLocationPresentation = new LocationPresentation(newComponent.getFinalLocation(), newComponent);

            // Add the locations to the view
            controller.defaultLocationsContainer.getChildren().addAll(initialLocationPresentation, finalLocationPresentation);

            ComponentPresentation.this.controller.frame.setOnMouseEntered(event -> {
                new Thread(() -> {
                    Platform.runLater(initialLocationPresentation::animateIn);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // do nothing
                    }

                    Platform.runLater(finalLocationPresentation::animateIn);
                }).start();
            });

            finalLocationPresentation.shakeAnimation();
        });
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
            initialLocationPresentation.setLocation(component.getInitialLocation());
            initialLocationPresentation.setLayoutX(CORNER_SIZE / 2);
            initialLocationPresentation.setLayoutY(CORNER_SIZE / 2);

            StackPane.setAlignment(initialLocationPresentation, Pos.TOP_LEFT);
        });
    }

    private void initializeFinalLocation() {
        component.addListener((observable, oldValue, component) -> {
            finalLocationPresentation.setLocation(component.getFinalLocation());
            finalLocationPresentation.setLayoutX(component.getWidth() - CORNER_SIZE / 2);
            finalLocationPresentation.setLayoutY(component.getHeight() - CORNER_SIZE / 2);

            StackPane.setAlignment(finalLocationPresentation, Pos.BOTTOM_RIGHT);
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

            // Bind the missing lines that we cropped away
            controller.line1.setStartX(CORNER_SIZE);
            controller.line1.setStartY(0);
            controller.line1.setEndX(0);
            controller.line1.setEndY(CORNER_SIZE);

            controller.line1.setFill(component.getColor().getColor(component.getColorIntensity().next(2)));
            controller.line1.setStrokeWidth(2);
            StackPane.setAlignment(controller.line1, Pos.TOP_LEFT);

            controller.line2.setStartX(CORNER_SIZE);
            controller.line2.setStartY(0);
            controller.line2.setEndX(0);
            controller.line2.setEndY(CORNER_SIZE);
            controller.line2.setFill(component.getColor().getColor(component.getColorIntensity().next(2)));
            controller.line2.setStrokeWidth(2);
            StackPane.setAlignment(controller.line2, Pos.BOTTOM_RIGHT);

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
        return layoutXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return layoutYProperty();
    }

    @Override
    public MouseTracker getMouseTracker() {
        return controller.getMouseTracker();
    }
}
