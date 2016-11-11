package SW9.presentations;

import SW9.utility.colors.Color;
import SW9.utility.colors.EnabledColor;
import com.jfoenix.controls.JFXRippler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.function.BiConsumer;

public class FilePresentation extends AnchorPane {

    private File file;

    public FilePresentation(final File file) {
        final URL location = this.getClass().getResource("FilePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            this.file = file;

            initializeIcon();
            initializeFileName();
            initializeHoverEffect();
            initializeRippler();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
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

        label.setText(file.getName());
    }

    private void initializeIcon() {
        final Random rng = new Random();

        final Circle circle = (Circle) lookup("#iconBackground");
        final FontIcon icon = (FontIcon) lookup("#icon");

        final EnabledColor componentColor = EnabledColor.enabledColors.get(rng.nextInt(EnabledColor.enabledColors.size()));

        final Color color = componentColor.color;
        final Color.Intensity colorIntensity = componentColor.intensity;

        circle.setFill(color.getColor(colorIntensity));
        icon.setFill(color.getTextColor(colorIntensity));
    }

    private void initializeHoverEffect() {
        final FontIcon openIndicator = (FontIcon) lookup("#openIndicator");

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

            openIndicator.setFill(newColor.getColor(newIntensity.next(5)));
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
