package SW9.presentations;

import SW9.abstractions.Component;
import SW9.controllers.CanvasController;
import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXRippler;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
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
            initializeColors();
            initializeRippler();
            initializeMoreInformationButton();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeMoreInformationButton() {
        final JFXRippler moreInformation = (JFXRippler) lookup("#moreInformation");

        moreInformation.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        moreInformation.setPosition(JFXRippler.RipplerPos.BACK);
        moreInformation.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I500));

        /*moreInformation.setOnMousePressed((mouseEvent) -> {
            mouseEvent.consume();
            component.get().setIsMain(true);
        });*/
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

        component.get().isMainProperty().addListener((obs, oldIsMain, newIsMain) -> {
            if (newIsMain) {
                icon.setIconLiteral("gmi-star");
                icon.setIconSize(22);
            } else {
                icon.setIconLiteral("gmi-description");
                icon.setIconSize(22);
            }
        });
    }

    private void initializeColors() {
        final FontIcon moreInformationIcon = (FontIcon) lookup("#moreInformationIcon");

        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I50;

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
            if(CanvasController.getActiveComponent().equals(component.get())) {
                setBackground.accept(color, colorIntensity.next(2));
            } else {
                setBackground.accept(color, colorIntensity.next());
            }
            setCursor(Cursor.HAND);
        });
        setOnMouseExited(event -> {
            if(CanvasController.getActiveComponent().equals(component.get())) {
                setBackground.accept(color, colorIntensity.next(1));
            } else {
                setBackground.accept(color, colorIntensity);
            }
            setCursor(Cursor.DEFAULT);
        });

        CanvasController.activeComponentProperty().addListener((obs, oldActiveComponent, newActiveComponent) -> {
            if (newActiveComponent == null) return;


            if (newActiveComponent.equals(component.get())) {
                setBackground.accept(color, colorIntensity.next(2));
            } else {
                setBackground.accept(color, colorIntensity);
            }
        });

        // Update the background initially
        setBackground.accept(color, colorIntensity);
    }

    public Component getComponent() {
        return component.get();
    }

    public SimpleObjectProperty<Component> componentProperty() {
        return component;
    }

}
