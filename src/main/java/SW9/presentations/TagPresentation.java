package SW9.presentations;

import SW9.abstractions.Component;
import SW9.controllers.CanvasController;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.LocationAware;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;
import static javafx.scene.paint.Color.TRANSPARENT;

public class TagPresentation extends StackPane {

    private final static Color backgroundColor = Color.GREY;
    private final static Color.Intensity backgroundColorIntensity = Color.Intensity.I50;

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);
    private final ObjectProperty<LocationAware> locationAware = new SimpleObjectProperty<>(null);

    private LineTo l2;
    private LineTo l3;
    private double previousX;
    private double previousY;
    private boolean wasDragged;
    private boolean hadInitialFocus = false;

    private static double TAG_HEIGHT = 1.6 * GRID_SIZE;

    public TagPresentation() {
        final URL location = this.getClass().getResource("TagPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            initializeShape();
            initializeLabel();
            initializeMouseTransparency();
            initializeTextFocusHandler();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeTextFocusHandler() {

        // When a label is loaded do not request focus initially
        textFieldFocusProperty().addListener((observable, oldValue, newValue) -> {
            if(!hadInitialFocus && newValue) {
                hadInitialFocus = true;
                CanvasController.leaveTextAreas();
            }
        });
    }

    private void initializeMouseTransparency() {
        mouseTransparentProperty().bind(opacityProperty().isEqualTo(0, 0.00f));
    }

    private void initializeTextAid() {
        final JFXTextField textField = (JFXTextField) lookup("#textField");

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.contains(" ")) {
                final String updatedString = newText.replace(" ", "_");
                textField.setText(updatedString);
            }
        });
    }

    private void initializeLabel() {
        final Label label = (Label) lookup("#label");
        final JFXTextField textField = (JFXTextField) lookup("#textField");
        final Path shape = (Path) lookup("#shape");

        final Insets insets = new Insets(0,2,0,2);
        textField.setPadding(insets);
        label.setPadding(insets);

        final int padding = 0;

        label.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double newWidth = Math.max(newBounds.getWidth(), 10);
            final double res = GRID_SIZE * 2 - (newWidth % (GRID_SIZE * 2));
            newWidth += res;

            textField.setMinWidth(newWidth);
            textField.setMaxWidth(newWidth);

            l2.setX(newWidth + padding);
            l3.setX(newWidth + padding);

            setMinWidth(newWidth + padding);
            setMaxWidth(newWidth + padding);

            textField.setMinHeight(TAG_HEIGHT);
            textField.setMaxHeight(TAG_HEIGHT);

            textField.focusedProperty().addListener((observable, oldFocused, newFocused) -> {
                if (newFocused) {
                    shape.setTranslateY(2);
                    textField.setTranslateY(2);
                }
            });

            if (getWidth() >= 1000) {
                setWidth(newWidth);
                setHeight(TAG_HEIGHT);
                shape.setTranslateY(-1);
                textField.setTranslateY(-1);
            }

            // Fixes the jumping of the shape when the text field is empty
            if (textField.getText().isEmpty()) {
                shape.setLayoutX(0);
            }
        });

        label.textProperty().bind(new When(textField.textProperty().isNotEmpty()).then(textField.textProperty()).otherwise(textField.promptTextProperty()));

    }

    private void initializeShape() {
        final int WIDTH = 5000;
        final double HEIGHT = TAG_HEIGHT;

        final Path shape = (Path) lookup("#shape");

        final MoveTo start = new MoveTo(0, 0);

        l2 = new LineTo(WIDTH, 0);
        l3 = new LineTo(WIDTH, HEIGHT);
        final LineTo l4 = new LineTo(0, HEIGHT);
        final LineTo l6 = new LineTo(0, 0);

        shape.getElements().addAll(start, l2, l3, l4, l6);

        shape.setFill(backgroundColor.getColor(backgroundColorIntensity));
        shape.setStroke(backgroundColor.getColor(backgroundColorIntensity.next(4)));

        final JFXTextField textField = (JFXTextField) lookup("#textField");
        shape.setCursor(Cursor.OPEN_HAND);

        shape.setOnMousePressed(event -> {
            previousX = getTranslateX();
            previousY = getTranslateY();
        });

        shape.setOnMouseDragged(event -> {
            event.consume();

            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).subtract(getLocationAware().xProperty()).doubleValue() - getMinWidth() / 2;
            setTranslateX(newX);

            final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).subtract(getLocationAware().yProperty()).doubleValue() - getHeight() / 2;
            setTranslateY(newY - 2);

            // Tell the mouse release action that we can store an update
            wasDragged = true;
        });

        shape.setOnMouseReleased(event -> {
            if (wasDragged) {
                // Add to undo redo stack
                final double currentX = getTranslateX();
                final double currentY = getTranslateY();
                final double storePreviousX = previousX;
                final double storePreviousY = previousY;
                UndoRedoStack.push(
                        () -> {
                            setTranslateX(currentX);
                            setTranslateY(currentY);
                        },
                        () -> {
                            setTranslateX(storePreviousX);
                            setTranslateY(storePreviousY);
                        },
                        String.format("Moved tag from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                        "pin-drop"
                );

                // Reset the was dragged boolean
                wasDragged = false;
            } else if(event.getClickCount() == 2){
                textField.setMouseTransparent(false);
                textField.requestFocus();
                textField.requestFocus(); // This needs to be done twice because of reasons
            }

        });

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                shape.setCursor(Cursor.TEXT);
            } else {
                textField.setMouseTransparent(true);
                shape.setCursor(Cursor.OPEN_HAND);
            }
        });

        // When enter or escape is pressed release focus
        textField.setOnKeyPressed(CanvasController.getLeaveTextAreaKeyHandler());
    }

    public void bindToColor(final ObjectProperty<Color> color, final ObjectProperty<Color.Intensity> intensity) {
        bindToColor(color, intensity, false);
    }

    public void bindToColor(final ObjectProperty<Color> color, final ObjectProperty<Color.Intensity> intensity, final boolean doColorBackground) {
        final BiConsumer<Color, Color.Intensity> recolor = (newColor, newIntensity) -> {

            final JFXTextField textField = (JFXTextField) lookup("#textField");
            textField.setUnFocusColor(TRANSPARENT);
            textField.setFocusColor(newColor.getColor(newIntensity));

            if (doColorBackground) {
                final Path shape = (Path) lookup("#shape");
                shape.setFill(newColor.getColor(newIntensity.next(-1)));
                shape.setStroke(newColor.getColor(newIntensity.next(-1).next(2)));

                textField.setStyle("-fx-prompt-text-fill: rgba(255, 255, 255, 0.6); -fx-text-fill: " + newColor.getTextColorRgbaString(newIntensity) + ";");
                textField.setFocusColor(newColor.getTextColor(newIntensity));
            } else {
                textField.setStyle("-fx-prompt-text-fill: rgba(0, 0, 0, 0.6);");
            }

        };

        color.addListener(observable -> recolor.accept(color.get(), intensity.get()));
        intensity.addListener(observable -> recolor.accept(color.get(), intensity.get()));

        recolor.accept(color.get(), intensity.get());
    }

    public void setAndBindString(final StringProperty string) {
        final JFXTextField textField = (JFXTextField) lookup("#textField");

        textField.textProperty().unbind();
        textField.setText(string.get());
        string.bind(textField.textProperty());
    }

    public void setPlaceholder(final String placeholder) {
        final JFXTextField textField = (JFXTextField) lookup("#textField");
        textField.setPromptText(placeholder);
    }

    public void replaceSpace() {
        initializeTextAid();
    }

    public void requestTextFieldFocus() {
        final JFXTextField textField = (JFXTextField) lookup("#textField");
        Platform.runLater(textField::requestFocus);
    }

    public ObservableBooleanValue textFieldFocusProperty() {
        final JFXTextField textField = (JFXTextField) lookup("#textField");
        return textField.focusedProperty();
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(final Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    public LocationAware getLocationAware() {
        return locationAware.get();
    }

    public ObjectProperty<LocationAware> locationAwareProperty() {
        return locationAware;
    }

    public void setLocationAware(LocationAware locationAware) {
        this.locationAware.set(locationAware);
    }
}
