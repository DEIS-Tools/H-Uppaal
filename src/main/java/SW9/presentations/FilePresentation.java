package SW9.presentations;

import SW9.abstractions.Component;
import SW9.controllers.CanvasController;
import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXRippler;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;

public class FilePresentation extends AnchorPane {

    private final SimpleObjectProperty<Component> component = new SimpleObjectProperty<>(null);

    public FilePresentation(final Component component) {
        final URL location = this.getClass().getResource("FilePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            this.component.set(component);

            initializeIcon();
            initializeFileName();
            initializeHoverEffect();
            initializeRippler();
            initializeMoreInformationButton();
            initializeActiveIndicator();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeActiveIndicator() {
        final Rectangle activeIndicator = (Rectangle) lookup("#activeIndicator");

        component.get().colorProperty().addListener((obs, oldColor, newColor) -> {
            activeIndicator.setFill(newColor.getColor(component.get().getColorIntensity()));
        });
        activeIndicator.setFill(component.get().getColor().getColor(component.get().getColorIntensity().next(-3)));

        final TranslateTransition animateLeft = new TranslateTransition(Duration.millis(80), activeIndicator);
        animateLeft.setFromX(0);
        animateLeft.setToX(-1 * activeIndicator.getWidth());

        final TranslateTransition animateRight = new TranslateTransition(Duration.millis(80), activeIndicator);
        animateRight.setFromX(-1 * activeIndicator.getWidth());
        animateRight.setToX(0);

        CanvasController.activeComponentProperty().addListener((obs, oldActiveComponent, newActiveComponent) -> {
            if (newActiveComponent != null && component.get().equals(newActiveComponent)) {
                animateRight.play();
            } else if (oldActiveComponent != null && component.get().equals(oldActiveComponent)) {
                animateLeft.play();
            }
        });
    }

    private void initializeMoreInformationButton() {
        final JFXRippler moreInformation = (JFXRippler) lookup("#moreInformation");

        moreInformation.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        moreInformation.setPosition(JFXRippler.RipplerPos.BACK);
        moreInformation.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I500));

        moreInformation.setOnMousePressed(Event::consume); // Todo: show more information. Rename etc.
    }

    private void initializeRippler() {
        final JFXRippler rippler = (JFXRippler) lookup("#rippler");

        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I400;

        rippler.setMaskType(JFXRippler.RipplerMask.RECT);
        rippler.setRipplerFill(color.getColor(colorIntensity));
        rippler.setPosition(JFXRippler.RipplerPos.BACK);
    }

    private void initializeFileName() {
        final Label label = (Label) lookup("#fileName");

        component.get().nameProperty().addListener((obs, oldName, newName) -> label.setText(newName));
        label.setText(component.get().getName());
    }

    private void initializeIcon() {
        final Circle circle = (Circle) lookup("#iconBackground");
        final FontIcon icon = (FontIcon) lookup("#icon");

        component.get().colorProperty().addListener((obs, oldColor, newColor) -> {
            circle.setFill(newColor.getColor(component.get().getColorIntensity()));
            icon.setFill(newColor.getTextColor(component.get().getColorIntensity()));
        });

        circle.setFill(component.get().getColor().getColor(component.get().getColorIntensity()));
        icon.setFill(component.get().getColor().getTextColor(component.get().getColorIntensity()));
    }

    private void initializeHoverEffect() {
        final FontIcon moreInformationIcon = (FontIcon) lookup("#moreInformationIcon");

        final Color color = Color.GREY;
        final Color colorHovered = Color.GREY_BLUE;

        final Color.Intensity colorIntensity = Color.Intensity.I200;
        final Color.Intensity colorIntensityHovered = Color.Intensity.I100;

        final BiConsumer<Color, Color.Intensity> setBackground = (newColor, newIntensity) -> {
            setBackground(new Background(new BackgroundFill(
                    newColor.getColor(newIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            setBorder(new Border(new BorderStroke(
                    newColor.getColor(newIntensity.next(2)),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(0, 0, 1, 0)
            )));

            moreInformationIcon.setFill(newColor.getColor(newIntensity.next(5)));
        };

        // Update the background when hovered
        setOnMouseEntered(event -> {
            setBackground.accept(colorHovered, colorIntensityHovered);
            setCursor(Cursor.HAND);
        });
        setOnMouseExited(event -> {
            setBackground.accept(color, colorIntensity);
            setCursor(Cursor.DEFAULT);
        });

        // Update the background initially
        setBackground.accept(color, colorIntensity);
    }

}
