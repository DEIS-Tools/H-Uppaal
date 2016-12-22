package SW9.presentations;

import SW9.code_analysis.CodeAnalysis;
import SW9.code_analysis.Nearable;
import SW9.utility.colors.Color;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;

import static javafx.scene.paint.Color.TRANSPARENT;

public class MessagePresentation extends HBox {

    private final CodeAnalysis.Message message;

    public MessagePresentation(final CodeAnalysis.Message message) {
        this.message = message;

        final URL location = this.getClass().getResource("MessagePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            // Initialize here
            initializeHover();
            initializeMessage();
            initializeNearLabel();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeMessage() {
        final Label messageLabel = (Label) lookup("#messageLabel");
        messageLabel.textProperty().bind(message.messageProperty());
    }

    private void initializeNearLabel() {
        final InvalidationListener listener = observable -> {
            String result = "Near: ";

            if (message.getNearables().size() == 0) {
                result = ""; // Do not display any "near"
            } else {
                // Add all "near" strings
                for (final Nearable nearable : message.getNearables()) {
                    result += nearable.generateNearString();
                }
            }

            final Label nearLabel = (Label) lookup("#nearLabel");
            nearLabel.setText(result);
        };

        // Run the listener now
        listener.invalidated(null);

        // Whenever the list is updated
        message.getNearables().addListener(listener);
    }

    private void initializeHover() {
        setOnMouseEntered(event -> {
            setBackground(new Background(new BackgroundFill(
                    Color.GREY.getColor(Color.Intensity.I300),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        });

        setOnMouseExited(event -> {
            setBackground(new Background(new BackgroundFill(
                    TRANSPARENT,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        });
    }

}
