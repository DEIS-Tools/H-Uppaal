package SW9.presentations;

import SW9.NewMain;
import SW9.abstractions.Component;
import SW9.controllers.ComponentController;
import SW9.controllers.NailController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.DragHelper;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.helpers.SelectHelperNew;
import SW9.utility.mouse.MouseTracker;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class ComponentPresentation extends StackPane implements MouseTrackable, SelectHelperNew.Selectable {

    private final static double CORNER_SIZE = 60;

    private final ComponentController controller;
    private final List<BiConsumer<Color, Color.Intensity>> updateColorDelegates = new ArrayList<>();
    private LocationPresentation initialLocationPresentation = null;
    private LocationPresentation finalLocationPresentation = null;

    public ComponentPresentation() {
        this(new Component("Component" + new Random().nextInt(5000))); // todo: find a new unique component name
    }

    public ComponentPresentation(final Component component) {
        NewMain.getProject().getComponents().addAll(component);

        final URL location = this.getClass().getResource("ComponentPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            // Set the width and the height of the view to the values in the abstraction
            setMinWidth(component.getWidth());
            setMaxWidth(component.getWidth());
            setMinHeight(component.getHeight());
            setMaxHeight(component.getHeight());

            controller = fxmlLoader.getController();
            controller.setComponent(component);

            initializeDefaultLocationsContainer();
            initializeToolbar();
            initializeFrame();
            initializeInitialLocation();
            initializeFinalLocation();
            initializeBackground();
            initializeName();

            // Make the component draggable
            DragHelper.makeDraggable(this, event -> {
                // We can only drag using left click
                if (!event.getButton().equals(MouseButton.PRIMARY)) return false;

                if(NailController.nailBeingDragged) return false;

                // We cant drag if we have an unfinished edge
                if (controller.getComponent().getUnfinishedEdge() != null) return false;

                return true;
            });
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeDefaultLocationsContainer() {
        if (initialLocationPresentation != null) {
            controller.defaultLocationsContainer.getChildren().remove(initialLocationPresentation);
        }

        if (finalLocationPresentation != null) {
            controller.defaultLocationsContainer.getChildren().remove(finalLocationPresentation);
        }

        // Instantiate views for the initial and final location
        final Component component = controller.getComponent();
        initialLocationPresentation = new LocationPresentation(component.getInitialLocation(), component);
        finalLocationPresentation = new LocationPresentation(component.getFinalLocation(), component);

        // Add the locations to the view
        controller.defaultLocationsContainer.getChildren().addAll(initialLocationPresentation, finalLocationPresentation);

        ComponentPresentation.this.controller.frame.setOnMouseEntered(event -> new Thread(() -> {
            Platform.runLater(initialLocationPresentation::animateIn);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // do nothing
            }

            Platform.runLater(finalLocationPresentation::animateIn);
        }).start());
    }

    private void initializeName() {
        final Component component = controller.getComponent();
        final BooleanProperty initialized = new SimpleBooleanProperty(false);

        controller.name.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !initialized.get()) {
                controller.root.requestFocus();
                initialized.setValue(true);
            }
        });


        // Set the text field to the name in the model, and bind the model to the text field
        controller.name.setText(component.getName());
        component.nameProperty().bind(controller.name.textProperty());

        final Runnable updateColor = () -> {
            final Color color = component.getColor();
            final Color.Intensity colorIntensity = component.getColorIntensity();

            // Set the text color for the label
            controller.name.setStyle("-fx-text-fill: " + color.toHexTextColor(colorIntensity) + ";");
            controller.name.setFocusColor(color.getTextColor(colorIntensity));
            controller.name.setUnFocusColor(javafx.scene.paint.Color.TRANSPARENT);
        };

        controller.getComponent().colorProperty().addListener(observable -> updateColor.run());
        updateColor.run();

        // Add a left margin of CORNER_SIZE
        controller.name.setPadding(new Insets(0, 0, 0, CORNER_SIZE));
    }

    private void initializeInitialLocation() {
        initialLocationPresentation.setLocation(controller.getComponent().getInitialLocation());
        initialLocationPresentation.setLayoutX(CORNER_SIZE / 2 + 1.5);
        initialLocationPresentation.setLayoutY(CORNER_SIZE / 2 + 1.5);

        StackPane.setAlignment(initialLocationPresentation, Pos.TOP_LEFT);
    }

    private void initializeFinalLocation() {
        final Component component = controller.getComponent();

        finalLocationPresentation.setLocation(component.getFinalLocation());
        finalLocationPresentation.setLayoutX(component.getWidth() - CORNER_SIZE / 2 - 1.5);
        finalLocationPresentation.setLayoutY(component.getHeight() - CORNER_SIZE / 2 - 1.5);

        StackPane.setAlignment(finalLocationPresentation, Pos.BOTTOM_RIGHT);
    }

    private void initializeToolbar() {
        final Component component = controller.getComponent();

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Set the background of the toolbar
            controller.toolbar.setBackground(new Background(new BackgroundFill(
                    newColor.getColor(newIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            // Set the icon color and rippler color of the toggleDeclarationButton
            controller.toggleDeclarationButton.setRipplerFill(newColor.getTextColor(newIntensity));
            ((FontIcon) controller.toggleDeclarationButton.getGraphic()).setFill(newColor.getTextColor(newIntensity));
        };

        updateColorDelegates.add(updateColor);

        controller.getComponent().colorProperty().addListener(observable -> updateColor.accept(component.getColor(), component.getColorIntensity()));

        updateColor.accept(component.getColor(), component.getColorIntensity());

        // Set a hover effect for the controller.toggleDeclarationButton
        controller.toggleDeclarationButton.setOnMouseEntered(event -> controller.toggleDeclarationButton.setCursor(Cursor.HAND));
        controller.toggleDeclarationButton.setOnMouseExited(event -> controller.toggleDeclarationButton.setCursor(Cursor.DEFAULT));
    }

    private void initializeFrame() {
        final Component component = controller.getComponent();

        final Shape[] mask = new Shape[1];
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

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Mask the parent of the frame (will also mask the background)
            mask[0] = Path.subtract(rectangle, corner1);
            mask[0] = Path.subtract(mask[0], corner2);
            controller.frame.setClip(mask[0]);
            controller.background.setClip(Path.union(mask[0], mask[0]));
            controller.background.setOpacity(0.5);

            // Bind the missing lines that we cropped away
            controller.line1.setStartX(CORNER_SIZE);
            controller.line1.setStartY(0);
            controller.line1.setEndX(0);
            controller.line1.setEndY(CORNER_SIZE);
            controller.line1.setStroke(newColor.getColor(newIntensity));
            controller.line1.setStrokeWidth(2);
            StackPane.setAlignment(controller.line1, Pos.TOP_LEFT);

            controller.line2.setStartX(CORNER_SIZE);
            controller.line2.setStartY(0);
            controller.line2.setEndX(0);
            controller.line2.setEndY(CORNER_SIZE);
            controller.line2.setStroke(newColor.getColor(newIntensity));
            controller.line2.setStrokeWidth(2);
            StackPane.setAlignment(controller.line2, Pos.BOTTOM_RIGHT);

            // Set the stroke color to two shades darker
            controller.frame.setBorder(new Border(new BorderStroke(
                    newColor.getColor(newIntensity),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(2),
                    Insets.EMPTY
            )));
        };

        updateColorDelegates.add(updateColor);

        component.colorProperty().addListener(observable -> {
            updateColor.accept(component.getColor(), component.getColorIntensity());
        });

        updateColor.accept(component.getColor(), component.getColorIntensity());
    }

    private void initializeBackground() {
        final Component component = controller.getComponent();

        // Bind the background width and height to the values in the model
        controller.background.widthProperty().bind(component.widthProperty());
        controller.background.heightProperty().bind(component.heightProperty());

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Set the background color to the lightest possible version of the color
            controller.background.setFill(newColor.getColor(newIntensity));
        };

        updateColorDelegates.add(updateColor);

        component.colorProperty().addListener(observable -> {
            updateColor.accept(component.getColor(), component.getColorIntensity());
        });

        updateColor.accept(component.getColor(), component.getColorIntensity());
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

    @Override
    public void select() {
        updateColorDelegates.forEach(colorConsumer -> colorConsumer.accept(Color.DEEP_ORANGE, Color.Intensity.I500));
    }

    @Override
    public void deselect() {
        updateColorDelegates.forEach(colorConsumer -> {
            final Component component = controller.getComponent();

            colorConsumer.accept(component.getColor(), component.getColorIntensity());
        });
    }
}
