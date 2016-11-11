package SW9.presentations;

import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXRippler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;

public class UndoRedoHistoryEntryPresentation extends AnchorPane {

    private final UndoRedoStack.Command command;

    private final Color color;
    private final Color.Intensity colorIntensity;

    public UndoRedoHistoryEntryPresentation(final UndoRedoStack.Command command, final boolean isUndo) {
        this.command = command;

        final URL location = this.getClass().getResource("UndoRedoHistoryEntryPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            // Must be the indicator for the current state
            if (command == null) {
                color = Color.GREY_BLUE;
                colorIntensity = Color.Intensity.I500;
            } else if (isUndo) {
                color = Color.GREEN;
                colorIntensity = Color.Intensity.I600;
            } else {
                color = Color.DEEP_ORANGE;
                colorIntensity = Color.Intensity.I800;
            }

            initializeRippler();
            initializeIcon();
            initializeBackground();
            initializeLabel();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeLabel() {
        final Label label = (Label) lookup("#label");

        label.setTextFill(color.getTextColor(colorIntensity.next(-5)));

        if (command != null) {
            label.setText(command.getDescription());
        } else {
            label.setText("Current state");
        }
    }

    private void initializeRippler() {
        final JFXRippler rippler = (JFXRippler) lookup("#rippler");

        rippler.setMaskType(JFXRippler.RipplerMask.RECT);
        rippler.setRipplerFill(color.getColor(colorIntensity));
        rippler.setPosition(JFXRippler.RipplerPos.BACK);
    }

    private void initializeIcon() {
        final Circle circle = (Circle) lookup("#iconBackground");
        final FontIcon icon = (FontIcon) lookup("#icon");

        circle.setFill(color.getColor(colorIntensity));
        icon.setFill(color.getTextColor(colorIntensity));

        if (command != null) {
            icon.setIconLiteral("gmi-" + command.getIcon());
        }
        icon.setIconSize(24);
    }

    private void initializeBackground() {
        setBackground(new Background(new BackgroundFill(
                color.getColor(colorIntensity.next(-5)),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        setBorder(new Border(new BorderStroke(
                color.getColor(colorIntensity.next(-5).next(2)),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 1, 0)
        )));
    }

}
