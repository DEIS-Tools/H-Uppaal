package SW9.presentations;

import SW9.utility.colors.Color;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.*;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;

import static javafx.scene.paint.Color.TRANSPARENT;

public class TagPresentation extends StackPane {

    private final static Color backgroundColor = Color.GREY;
    private final static Color.Intensity backgroundColorIntensity = Color.Intensity.I50;
    private LineTo l2;
    private LineTo l3;

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
            initializeHole();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeHole() {
        final Path shape = (Path) lookup("#shape");
        final Circle hole = (Circle) lookup("#hole");

        hole.setFill(TRANSPARENT);
        hole.setStrokeWidth(2);

        final Circle innerHole = new Circle(4 + hole.getRadius(), l3.getY() / 2, hole.getRadius());

        final Shape mask = Path.subtract(shape, innerHole);
        shape.setClip(mask);
    }

    private void initializeLabel() {
        final Label label = (Label) lookup("#label");

        label.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            final double newWidth = newBounds.getWidth() + 16 + 8;

            setMinWidth(newWidth);
            setMaxWidth(newWidth);

            l2.setX(newWidth);
            l3.setX(newWidth);
        });
    }

    private void initializeShape() {
        final int CORNER_SIZE = 8;

        final int WIDTH = 80;
        final int HEIGHT = 30;

        final Path shape = (Path) lookup("#shape");

        final MoveTo start = new MoveTo(0, CORNER_SIZE);

        final LineTo l1 = new LineTo(CORNER_SIZE, 0);
        l2 = new LineTo(WIDTH, 0);
        l3 = new LineTo(WIDTH, HEIGHT);
        final LineTo l4 = new LineTo(CORNER_SIZE, HEIGHT);
        final LineTo l5 = new LineTo(0, HEIGHT - CORNER_SIZE);
        final LineTo l6 = new LineTo(0, CORNER_SIZE);

        shape.getElements().addAll(start, l1, l2, l3, l4, l5, l6);

        shape.setFill(backgroundColor.getColor(backgroundColorIntensity));
        shape.setStroke(backgroundColor.getColor(backgroundColorIntensity.next(5)));
    }

    public void bindToColor(final ObjectProperty<Color> color, final ObjectProperty<Color.Intensity> intensity) {
        final BiConsumer<Color, Color.Intensity> recolor = (newColor, newIntensity) -> {

            final Circle hole = (Circle) lookup("#hole");
            hole.setStroke(newColor.getColor(newIntensity.next(-20).next(3)));

        };

        color.addListener(observable -> recolor.accept(color.get(), intensity.get()));
        intensity.addListener(observable -> recolor.accept(color.get(), intensity.get()));

        recolor.accept(color.get(), intensity.get());
    }

    public void bindToString(final StringProperty string) {
        final Label label = (Label) lookup("#label");

        label.textProperty().bind(string);
    }

}
