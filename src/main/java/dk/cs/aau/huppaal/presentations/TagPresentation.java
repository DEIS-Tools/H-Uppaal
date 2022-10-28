package dk.cs.aau.huppaal.presentations;

import com.jfoenix.controls.JFXTextArea;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.utility.UndoRedoStack;
import dk.cs.aau.huppaal.utility.colors.Color;
import dk.cs.aau.huppaal.utility.helpers.LocationAware;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;

import static dk.cs.aau.huppaal.presentations.CanvasPresentation.GRID_SIZE;
import static javafx.scene.paint.Color.TRANSPARENT;

public class TagPresentation extends StackPane {

    private final static Color backgroundColor = Color.GREY;
    private final static Color.Intensity backgroundColorIntensity = Color.Intensity.I50;

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);
    private final ObjectProperty<LocationAware> locationAware = new SimpleObjectProperty<>(null);

    private LineTo l2, l3, l4;
    private double previousX;
    private double previousY;
    private double dragOffsetX, dragOffsetY;
    private boolean wasDragged;
    private boolean hadInitialFocus = false;

    private static double TAG_HEIGHT = 1.6 * GRID_SIZE;

    public TagPresentation() {
        try {
            var location = this.getClass().getResource("TagPresentation.fxml");
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            initializeShape();
            initializeLabel();
            initializeMouseTransparency();
            initializeTextFocusHandler();
        } catch (IOException e) {
            throw new IllegalStateException(e);
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
        var textField = (JFXTextArea) lookup("#textField");

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.contains(" ")) {
                final String updatedString = newText.replace(" ", "_");
                textField.setText(updatedString);
            }
        });
    }

    private void initializeLabel() {
        var label = (Label) lookup("#label");
        var text = (JFXTextArea) lookup("#textField");
        var shape = (Path) lookup("#shape");
        var insets = new Insets(0,2,0,2);
        text.setPadding(insets);
        label.setPadding(insets);
        label.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            var newWidth = Math.max(newBounds.getWidth(), 10);
            var newHeight = Math.max(newBounds.getHeight(), TAG_HEIGHT);
            var resX = GRID_SIZE * 2 - (newWidth % (GRID_SIZE * 2));
            var resY = GRID_SIZE * 2 - (newHeight % (GRID_SIZE * 2));
            newWidth += resX;
            newHeight += resY;

            text.setMinWidth(newWidth);
            text.setPrefWidth(newWidth);
            text.setMinHeight(TAG_HEIGHT);
            text.setPrefHeight(TAG_HEIGHT);

            l2.setX(newWidth);
            l3.setX(newWidth);
            l3.setY(newHeight);
            l4.setY(newHeight);

            setMinWidth(newWidth);
            setMaxWidth(newWidth);

            text.focusedProperty().addListener((observable, oldFocused, newFocused) -> {
                if (newFocused) {
                    shape.setTranslateY(2);
                    text.setTranslateY(2);
                }
            });

            if (getWidth() >= 1000) {
                setWidth(newWidth);
                setHeight(newHeight);
                shape.setTranslateY(-1);
                text.setTranslateY(-1);
            }

            // Fixes the jumping of the shape when the text field is empty
            if (text.getText().isEmpty()) {
                shape.setLayoutX(0);
            }
        });

        label.textProperty().bind(new When(text.textProperty().isNotEmpty()).then(text.textProperty()).otherwise(text.promptTextProperty()));
    }

    private void stayWithinParent(TextField textField) {
        Node parent;
        if(getParent() instanceof NailPresentation){
            //Get the correct parent for guards, selects, synchronizations, and updates
            parent = getParent();
        } else {
            //Gte the correct parent for location names
            parent = getParent().getParent();
        }
        //Handle the horizontal placement of the tag
        if(parent.localToParent(getBoundsInParent()).getCenterX() > getComponent().widthProperty().doubleValue() - textField.getWidth()) {
            setTranslateX(getTranslateX() + getComponent().widthProperty().doubleValue() - textField.getWidth() - parent.localToParent(getBoundsInParent()).getCenterX());
        } else if (parent.localToParent(getBoundsInParent()).getCenterX() - textField.getWidth() < 0) {
            setTranslateX(getTranslateX() - (parent.localToParent(getBoundsInParent()).getCenterX() - textField.getWidth()));
        }
        //Handle the vertical placement of the tag
        if(parent.localToParent(getBoundsInParent()).getCenterY() > getComponent().heightProperty().doubleValue() - textField.getHeight()) {
            setTranslateY(getTranslateY() + getComponent().heightProperty().doubleValue() - textField.getHeight() - parent.localToParent(getBoundsInParent()).getCenterY());
        } else if (parent.localToParent(getBoundsInParent()).getCenterY() - textField.getHeight() - GRID_SIZE * 2 < 0) {
            setTranslateY(getTranslateY() - (parent.localToParent(getBoundsInParent()).getCenterY() - textField.getHeight() - GRID_SIZE * 2));
        }
    }

    private void initializeShape() {
        var width = 5000;
        var height = 5000;
        var shape = (Path) lookup("#shape");
        var start = new MoveTo(0, 0);

        l2 = new LineTo(width, 0);
        l3 = new LineTo(width, height);
        l4 = new LineTo(0, height);
        var l6 = new LineTo(0, 0);

        shape.getElements().addAll(start, l2, l3, l4, l6);
        shape.setFill(backgroundColor.getColor(backgroundColorIntensity));
        shape.setStroke(backgroundColor.getColor(backgroundColorIntensity.next(4)));
        shape.setCursor(Cursor.OPEN_HAND);

        shape.setOnMousePressed(event -> {
            previousX = getTranslateX();
            previousY = getTranslateY();
            dragOffsetX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).subtract(getLocationAware().xProperty()).subtract(previousX).doubleValue();
            dragOffsetY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).subtract(getLocationAware().yProperty()).subtract(previousY).doubleValue();
        });

        shape.setOnMouseDragged(event -> {
            event.consume();
            var newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).subtract(getLocationAware().xProperty()).doubleValue() - dragOffsetX;
            var newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).subtract(getLocationAware().yProperty()).doubleValue() - dragOffsetY;
            setTranslateX(newX);
            setTranslateY(newY);
            wasDragged = true; // Tell the mouse release action that we can store an update
        });

        var textField = (JFXTextArea) lookup("#textField");
        shape.setOnMouseReleased(event -> {
            if (wasDragged) {
                var currentX = getTranslateX();
                var currentY = getTranslateY();
                var storePreviousX = previousX;
                var storePreviousY = previousY;
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
                wasDragged = false; // Reset the was dragged boolean
            } else if(event.getClickCount() == 2){
                textField.setMouseTransparent(false);
                textField.requestFocus();
                textField.requestFocus(); // This needs to be done twice because of reasons
            }

            // agj - 2022-10-28: Disabled due to not working with very large expressions (also, it's annoying)
            //stayWithinParent(textField);
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

            var textField = (JFXTextArea) lookup("#textField");
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
        var textField = (JFXTextArea) lookup("#textField");

        textField.textProperty().unbind();
        textField.setText(string.get());
        string.bind(textField.textProperty());
    }

    public void setPlaceholder(final String placeholder) {
        var textField = (JFXTextArea) lookup("#textField");
        textField.setPromptText(placeholder);
    }

    public void replaceSpace() {
        initializeTextAid();
    }

    public void requestTextFieldFocus() {
        var textField = (JFXTextArea) lookup("#textField");
        Platform.runLater(textField::requestFocus);
    }

    public ObservableBooleanValue textFieldFocusProperty() {
        var textField = (JFXTextArea) lookup("#textField");
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
