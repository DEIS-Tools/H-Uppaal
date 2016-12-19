package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Jork;
import SW9.controllers.JorkController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.SelectHelper;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.shape.StrokeType;

import java.io.IOException;
import java.net.URL;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class JorkPresentation extends Group implements SelectHelper.Selectable {

    public static final double JORK_WIDTH = GRID_SIZE * 6;
    public static final double JORK_HEIGHT = GRID_SIZE;
    public static final double JORK_Y_TRANSLATE = 5;

    private final JorkController controller;

    public JorkPresentation(final Jork newJork, final Component component) {
        final URL url = this.getClass().getResource("JorkPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(url);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(url.openStream());

            controller = fxmlLoader.getController();
            controller.setComponent(component);
            controller.setJork(newJork);

            setLayoutX(newJork.getX());
            setLayoutY(newJork.getY());
            newJork.xProperty().bind(layoutXProperty());
            newJork.yProperty().bind(layoutYProperty());

            setTranslateY(JORK_Y_TRANSLATE);

            initializeColor();
            initializeShape();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeColor() {
        controller.rectangle.setFill(Color.GREY.getColor(Color.Intensity.I700));
        controller.rectangle.setStroke(Color.GREY.getColor(Color.Intensity.I900));
    }

    private void initializeShape() {
        controller.rectangle.setHeight(JORK_HEIGHT);
        controller.rectangle.setWidth(JORK_WIDTH);
        controller.rectangle.setStrokeType(StrokeType.INSIDE);
    }

    @Override
    public void select() {
        // Set the color
        controller.rectangle.setFill(SelectHelper.getNormalColor());
        controller.rectangle.setStroke(SelectHelper.getBorderColor());
    }

    @Override
    public void deselect() {
        // Set the color
        initializeColor();
    }
}
