package SW9.presentations;

import SW9.abstractions.Component;
import SW9.abstractions.Jork;
import SW9.controllers.JorkController;
import SW9.utility.colors.Color;
import SW9.utility.helpers.SelectHelper;
import javafx.beans.NamedArg;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Group;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;

import java.io.IOException;
import java.net.URL;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class JorkPresentation extends Group implements SelectHelper.Selectable {

    public static final double JORK_WIDTH = GRID_SIZE * 6;
    public static final double JORK_HEIGHT = GRID_SIZE;
    public static final double JORK_Y_TRANSLATE = 5;
    public static final double CORNER_SIZE = GRID_SIZE * 1.5;

    private final JorkController controller;

    public JorkPresentation(@NamedArg("type") final String type) {
        this(new Jork(type.equals("JOIN") ? Jork.Type.JOIN : Jork.Type.FORK), null);
    }

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

            initializeShape();
            initializeColor();
            initializeRotationBasedOnType();
            initializeIdLabel();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeIdLabel() {
        controller.id.textProperty().bind(controller.getJork().idProperty());

        controller.id.setLayoutX(JORK_WIDTH / 2);
        controller.id.translateXProperty().bind(controller.id.widthProperty().divide(-2));
        controller.id.setTranslateY(-2);
    }

    private void initializeColor() {
        final Color color = Color.GREY;
        final Color.Intensity intensity = Color.Intensity.I700;

        controller.shape.setFill(color.getColor(intensity));
        controller.shape.setStroke(color.getColor(intensity.next(2)));

        controller.id.setTextFill(color.getTextColor(intensity));
    }

    private void initializeShape() {
        final MoveTo p0 = new MoveTo(CORNER_SIZE, 0);
        final LineTo l0 = new LineTo(JORK_WIDTH - CORNER_SIZE, 0);
        final LineTo l1 = new LineTo(JORK_WIDTH, JORK_HEIGHT);
        final LineTo l2 = new LineTo(0, JORK_HEIGHT);
        final LineTo l3 = new LineTo(CORNER_SIZE, 0);

        controller.shape.getElements().addAll(p0, l0, l1, l2, l3);
    }

    private void initializeRotationBasedOnType() {
        if (controller.getJork().getType().equals(Jork.Type.JOIN)) {
            controller.shape.setRotate(180);
        }
    }

    @Override
    public void select() {
        // Set the color
        controller.shape.setFill(SelectHelper.getNormalColor());
        controller.shape.setStroke(SelectHelper.getBorderColor());
    }

    @Override
    public void deselect() {
        // Set the color
        initializeColor();
    }
}
