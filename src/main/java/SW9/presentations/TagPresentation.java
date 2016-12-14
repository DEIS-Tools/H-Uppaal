package SW9.presentations;

import SW9.abstractions.Component;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.LocationAware;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.PauseTransition;
import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
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

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
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

        final Insets insets = new Insets(-1, 2, 0, 2);
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

            textField.setMinHeight(GRID_SIZE * 1.5);
            textField.setMaxHeight(GRID_SIZE * 1.5);

            textField.focusedProperty().addListener((observable, oldFocused, newFocused) -> {
                if (newFocused) {
                    shape.setTranslateY(0);
                    textField.setTranslateY(0);
                }
            });

            if (getWidth() >= 1000) {
                setWidth(newWidth);
                setHeight(GRID_SIZE * 2);
                shape.setTranslateY(-2);
                textField.setTranslateY(-2);
            }

            // Fixes the jumping of the shape when the text field is empty
            if (textField.getText().isEmpty()) {
                shape.setLayoutX(0);
            }
        });

        label.textProperty().bind(new When(textField.textProperty().isNotEmpty()).then(textField.textProperty()).otherwise(textField.promptTextProperty()));

        textField.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (!newFocused && textField.getText().isEmpty()) {
                setOpacity(0);
            }
        });

        // Show and hide the tag according to focus of the text field
        opacityProperty().addListener((obs, oldOpacity, newOpacity) -> {
            if (newOpacity.doubleValue() < 1) {
                if (textFieldIsFocused()) {
                    setOpacity(1);
                }
            }
        });
    }

    private void initializeShape() {
        final int WIDTH = 5000;
        final double HEIGHT = GRID_SIZE * 1.5;

        final Path shape = (Path) lookup("#shape");

        final MoveTo start = new MoveTo(0, 0);

        l2 = new LineTo(WIDTH, 0);
        l3 = new LineTo(WIDTH, HEIGHT);
        final LineTo l4 = new LineTo(0, HEIGHT);
        final LineTo l6 = new LineTo(0, 0);

        shape.getElements().addAll(start, l2, l3, l4, l6);

        shape.setFill(backgroundColor.getColor(backgroundColorIntensity));
        shape.setStroke(backgroundColor.getColor(backgroundColorIntensity.next(2)));

        final JFXTextField textField = (JFXTextField) lookup("#textField");
        shape.setCursor(Cursor.OPEN_HAND);

        shape.setOnMousePressed(event -> {
            previousX = getTranslateX();
            previousY = getTranslateY();
        });

        shape.setOnMouseDragged(event -> {
            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).subtract(getLocationAware().xProperty()).doubleValue() - getMinWidth() / 2;
            setTranslateX(newX);

            final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).subtract(getLocationAware().yProperty()).doubleValue() - getHeight() / 2;
            setTranslateY(newY);

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
            } else {
                textField.setMouseTransparent(false);
                shape.setCursor(Cursor.TEXT);

                final PauseTransition wait = new PauseTransition(Duration.seconds(3));
                wait.setOnFinished((e) -> {
                    textField.setMouseTransparent(true);
                    shape.setCursor(Cursor.OPEN_HAND);
                });
                wait.play();
            }

        });
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

        // If the content of the tag is going to be empty, start out by hiding it
        if(string.get().equals("")) {
            setOpacity(0);
        } else {
            setOpacity(1);
        }

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

    public boolean textFieldIsFocused() {
        final JFXTextField textField = (JFXTextField) lookup("#textField");

        return textField.isFocused();
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
