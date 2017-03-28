package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.SubComponent;
import SW9.controllers.CanvasController;
import SW9.controllers.SubComponentController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.SelectHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.function.Consumer;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;
import static SW9.presentations.ComponentPresentation.CORNER_SIZE;
import static SW9.presentations.ComponentPresentation.TOOL_BAR_HEIGHT;

public class SubComponentPresentation extends StackPane implements SelectHelper.Selectable {

    private final SubComponentController controller;
    private final List<BiConsumer<Color, Color.Intensity>> updateColorDelegates = new ArrayList<>();
    private LocationPresentation initialLocationPresentation = null;
    private LocationPresentation finalLocationPresentation = null;

    public SubComponentPresentation(final SubComponent subComponent, final Component parentComponent) {
        final URL location = this.getClass().getResource("SubComponentPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            controller = fxmlLoader.getController();
            controller.setSubComponent(subComponent);
            controller.setParentComponent(parentComponent);

            controller.initializeInconsistentEdgeError();

            // Todo: Set height and width of the sub component
            setMinWidth(GRID_SIZE * 24);
            setMaxWidth(GRID_SIZE * 24);
            setMinHeight(GRID_SIZE * 12);
            setMaxHeight(GRID_SIZE * 12);

            subComponent.widthProperty().bind(widthProperty());
            subComponent.heightProperty().bind(heightProperty());

            // Bind x and y
            setLayoutX(subComponent.getX());
            setLayoutY(subComponent.getY());
            subComponent.xProperty().bind(layoutXProperty());
            subComponent.yProperty().bind(layoutYProperty());

            final Runnable initialize = () -> {
                initializeDefaultLocationsContainer();
                initializeToolbar();
                initializeFrame();
                initializeInitialLocation();
                initializeFinalLocation();
                initializeBackground();
                initializeName();
                initializeDescription();
            };
            initialize.run();

            controller.getSubComponent().componentProperty().addListener((observable, oldValue, newValue) -> {
                initialize.run();
            });

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeDescription() {
        controller.description.textProperty().bind(controller.getSubComponent().getComponent().descriptionProperty());
    }

    private void initializeDefaultLocationsContainer() {
        if (initialLocationPresentation != null) {
            controller.defaultLocationsContainer.getChildren().remove(initialLocationPresentation);
        }

        if (finalLocationPresentation != null) {
            controller.defaultLocationsContainer.getChildren().remove(finalLocationPresentation);
        }

        // Instantiate views for the initial and final location
        final SubComponent subComponent = controller.getSubComponent();
        initialLocationPresentation = new LocationPresentation(subComponent.getComponent().getInitialLocation(), subComponent.getComponent(), false);
        finalLocationPresentation = new LocationPresentation(subComponent.getComponent().getFinalLocation(), subComponent.getComponent(), false);

        final Consumer<Node> tapeForMousePressed = (node) -> {
            node.setOnMousePressed((mouseEvent) -> {
                mouseEvent.consume();
                controller.root.fireEvent(mouseEvent);
            });

        };

        tapeForMousePressed.accept(initialLocationPresentation.getController().scaleContent);
        tapeForMousePressed.accept(finalLocationPresentation.getController().scaleContent);

        // Add the locations to the view
        controller.defaultLocationsContainer.getChildren().addAll(initialLocationPresentation, finalLocationPresentation);
    }

    private void initializeName() {
        final SubComponent subComponent = controller.getSubComponent();
        final BooleanProperty initialized = new SimpleBooleanProperty(false);

        controller.identifier.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !initialized.get()) {
                controller.root.requestFocus();
                initialized.setValue(true);
            }
        });

        // Set the text field to the name in the model, and bind the model to the text field
        controller.identifier.setText(subComponent.getIdentifier());
        controller.identifier.textProperty().addListener((obs, oldIdentifier, newIdentifier) -> {
            subComponent.identifierProperty().unbind();
            subComponent.setIdentifier(newIdentifier);
        });

        final Runnable updateColor = () -> {
            final Color color = subComponent.getComponent().getColor();
            final Color.Intensity colorIntensity = subComponent.getComponent().getColorIntensity();

            // Set the text color for the label
            controller.identifier.setStyle("-fx-text-fill: " + color.getTextColorRgbaString(colorIntensity) + ";");
            controller.identifier.setFocusColor(color.getTextColor(colorIntensity));
            controller.identifier.setUnFocusColor(javafx.scene.paint.Color.TRANSPARENT);

            controller.originalComponentLabel.setStyle("-fx-text-fill: " + color.getTextColorRgbaString(colorIntensity) + ";");
        };

        controller.getSubComponent().getComponent().colorProperty().addListener(observable -> updateColor.run());
        updateColor.run();

        // Center the text vertically and aff a left padding of CORNER_SIZE
        controller.identifier.setPadding(new Insets(2, 0, 0, CORNER_SIZE));
        controller.identifier.setOnKeyPressed(CanvasController.getLeaveTextAreaKeyHandler());

        controller.originalComponentLabel.setPadding(new Insets(0, 5, 0, 15));
        controller.originalComponentLabel.textProperty().bind(subComponent.getComponent().nameProperty());
    }

    private void initializeInitialLocation() {
        initialLocationPresentation.setLocation(controller.getSubComponent().getComponent().getInitialLocation());
        initialLocationPresentation.setTranslateX(CORNER_SIZE / 2 - initialLocationPresentation.getLayoutX());
        initialLocationPresentation.setTranslateY(CORNER_SIZE / 2 - initialLocationPresentation.getLayoutY());

        StackPane.setAlignment(initialLocationPresentation, Pos.TOP_LEFT);
    }

    private void initializeFinalLocation() {
        final Component component = controller.getSubComponent().getComponent();

        finalLocationPresentation.setLocation(component.getFinalLocation());
        finalLocationPresentation.setTranslateX(getMinWidth() - CORNER_SIZE / 2 - finalLocationPresentation.getLayoutX());
        finalLocationPresentation.setTranslateY(getMinHeight() - CORNER_SIZE / 2 - finalLocationPresentation.getLayoutY());

        StackPane.setAlignment(finalLocationPresentation, Pos.BOTTOM_RIGHT);
    }

    private void initializeToolbar() {
        final Component component = controller.getSubComponent().getComponent();

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Set the background of the toolbar
            controller.toolbar.setBackground(new Background(new BackgroundFill(
                    newColor.getColor(newIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            controller.toolbar.setPrefHeight(TOOL_BAR_HEIGHT);
        };

        updateColorDelegates.add(updateColor);

        component.colorProperty().addListener(observable -> updateColor.accept(component.getColor(), component.getColorIntensity()));

        updateColor.accept(component.getColor(), component.getColorIntensity());
    }

    private void initializeFrame() {
        final Component component = controller.getSubComponent().getComponent();

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
        final Component component = controller.getSubComponent().getComponent();

        // Bind the background width and height to the values in the model
        controller.background.widthProperty().bind(minWidthProperty());
        controller.background.heightProperty().bind(minHeightProperty());

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Set the background color to the lightest possible version of the color
            controller.background.setFill(newColor.getColor(newIntensity.next(-20)));
        };

        updateColorDelegates.add(updateColor);

        component.colorProperty().addListener(observable -> {
            updateColor.accept(component.getColor(), component.getColorIntensity());
        });

        updateColor.accept(component.getColor(), component.getColorIntensity());
    }

    @Override
    public void select() {
        updateColorDelegates.forEach(colorConsumer -> colorConsumer.accept(SelectHelper.SELECT_COLOR, SelectHelper.SELECT_COLOR_INTENSITY_NORMAL));
    }

    @Override
    public void deselect() {
        updateColorDelegates.forEach(colorConsumer -> {
            final Component component = controller.getSubComponent().getComponent();

            colorConsumer.accept(component.getColor(), component.getColorIntensity());
        });
    }
}
