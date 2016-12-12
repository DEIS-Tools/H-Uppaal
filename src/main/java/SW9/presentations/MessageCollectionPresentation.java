package SW9.presentations;

import SW9.abstractions.Component;
import SW9.utility.colors.Color;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;

public class MessageCollectionPresentation extends VBox {

    public MessageCollectionPresentation(final Component component) {
        final URL location = this.getClass().getResource("MessageCollectionPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            initializeHeadline(component);
            initializeLine();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeLine() {
        final VBox children = (VBox) lookup("#children");
        final Line line = (Line) lookup("#line");

        children.getChildren().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                line.setEndY(children.getChildren().size() * 20 + 8);
            }
        });
    }

    private void initializeHeadline(final Component component) {
        final Label headline = (Label) lookup("#headline");
        final Circle indicator = (Circle) lookup("#indicator");
        final Line line = (Line) lookup("#line");

        headline.setText(component.getName());

        final BiConsumer<Color, Color.Intensity> updateColor = (color, intensity) -> {
            indicator.setFill(color.getColor(component.getColorIntensity()));
        };

        updateColor.accept(component.getColor(), component.getColorIntensity());
        component.colorProperty().addListener((observable, oldColor, newColor) -> updateColor.accept(newColor, component.getColorIntensity()));

        line.setStroke(Color.GREY.getColor(Color.Intensity.I400));
    }

    public void addChild() {
        final VBox children = (VBox) lookup("#children");

        children.getChildren().add(new MessagePresentation());
    }

}
