package SW9.presentations;

import SW9.abstractions.Component;
import SW9.controllers.SubComponentController;
import SW9.utility.colors.Color;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static SW9.presentations.ComponentPresentation.CORNER_SIZE;
import static SW9.presentations.ComponentPresentation.TOOL_BAR_HEIGHT;

public class SubComponentPresentation extends StackPane {

    private final SubComponentController controller;
    private final List<BiConsumer<Color, Color.Intensity>> updateColorDelegates = new ArrayList<>();
    private LocationPresentation initialLocationPresentation = null;
    private LocationPresentation finalLocationPresentation = null;

    public SubComponentPresentation(final Component component, final Component parentComponent) {
        final URL location = this.getClass().getResource("SubComponentPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            // Todo: Set height and width of the sub component
            setMinWidth(CORNER_SIZE * 5);
            setMaxWidth(CORNER_SIZE * 5);
            setMinHeight(CORNER_SIZE * 2);
            setMaxHeight(CORNER_SIZE * 2);

            controller = fxmlLoader.getController();
            controller.setComponent(component);
            controller.setParentComponent(parentComponent);

            initializeDefaultLocationsContainer();
            initializeToolbar();
            initializeFrame();
            initializeInitialLocation();
            initializeFinalLocation();
            initializeBackground();
            initializeName();

            // todo: make draggable

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
        initialLocationPresentation = new LocationPresentation(component.getInitialLocation(), component, false);
        finalLocationPresentation = new LocationPresentation(component.getFinalLocation(), component, false);

        // Add the locations to the view
        controller.defaultLocationsContainer.getChildren().addAll(initialLocationPresentation, finalLocationPresentation);
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
            controller.name.setStyle("-fx-text-fill: " + color.getTextColorRgbaString(colorIntensity) + ";");
        };

        controller.getComponent().colorProperty().addListener(observable -> updateColor.run());
        updateColor.run();

        // Center the text vertically and aff a left padding of CORNER_SIZE
        controller.name.setPadding(new Insets(2, 0, 0, CORNER_SIZE));
    }

    private void initializeInitialLocation() {
        initialLocationPresentation.setLocation(controller.getComponent().getInitialLocation());
        initialLocationPresentation.layoutXProperty().unbind();
        initialLocationPresentation.layoutYProperty().unbind();
        initialLocationPresentation.setLayoutX(CORNER_SIZE / 2 + 1);
        initialLocationPresentation.setLayoutY(CORNER_SIZE / 2 + 1);

        StackPane.setAlignment(initialLocationPresentation, Pos.TOP_LEFT);
    }

    private void initializeFinalLocation() {
        final Component component = controller.getComponent();

        finalLocationPresentation.setLocation(component.getFinalLocation());
        finalLocationPresentation.layoutXProperty().unbind();
        finalLocationPresentation.layoutYProperty().unbind();
        finalLocationPresentation.setLayoutX(getMinWidth() - CORNER_SIZE / 2 - 1);
        finalLocationPresentation.setLayoutY(getMinHeight() - CORNER_SIZE / 2 - 1);

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

            controller.toolbar.setPrefHeight(TOOL_BAR_HEIGHT);
        };

        controller.getComponent().colorProperty().addListener(observable -> updateColor.accept(component.getColor(), component.getColorIntensity()));

        updateColor.accept(component.getColor(), component.getColorIntensity());
    }

    private void initializeFrame() {
        final Component component = controller.getComponent();

        final Shape[] mask = new Shape[1];
        final Rectangle rectangle = new Rectangle(getMinWidth(), getMinHeight());

        // Generate first corner (to subtract)
        final Polygon corner1 = new Polygon(
                0, 0,
                CORNER_SIZE + 2, 0,
                0, CORNER_SIZE + 2
        );

        // Generate second corner (to subtract)
        final Polygon corner2 = new Polygon(
                getMinWidth(), getMinHeight(),
                getMinWidth() - CORNER_SIZE - 2, getMinHeight(),
                getMinWidth(), getMinHeight() - CORNER_SIZE - 2
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
            controller.line1.setStroke(newColor.getColor(newIntensity.next(2)));
            controller.line1.setStrokeWidth(1.25);
            StackPane.setAlignment(controller.line1, Pos.TOP_LEFT);

            controller.line2.setStartX(CORNER_SIZE);
            controller.line2.setStartY(0);
            controller.line2.setEndX(0);
            controller.line2.setEndY(CORNER_SIZE);
            controller.line2.setStroke(newColor.getColor(newIntensity.next(2)));
            controller.line2.setStrokeWidth(1.25);
            StackPane.setAlignment(controller.line2, Pos.BOTTOM_RIGHT);

            // Set the stroke color to two shades darker
            controller.frame.setBorder(new Border(new BorderStroke(
                    newColor.getColor(newIntensity.next(2)),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(1),
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
        controller.background.widthProperty().bind(minWidthProperty());
        controller.background.heightProperty().bind(minHeightProperty());

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
}
