package SW9.controllers;

import SW9.abstractions.Jork;
import SW9.presentations.CanvasPresentation;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.SelectHelper;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class JorkController implements Initializable, SelectHelper.ColorSelectable {

    public Group root;
    public Rectangle rectangle;

    private Jork jork;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        makeDraggable();
    }

    public Jork getJork() {
        return jork;
    }

    public void setJork(final Jork jork) {
        this.jork = jork;
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {

    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return null;
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }

    private void makeDraggable() {

        ItemDragHelper.makeDraggable(
                root,
                root,
                () -> CanvasPresentation.mouseTracker.getGridX(),
                () -> CanvasPresentation.mouseTracker.getGridY(),
                (event) -> {
                    event.consume();
                    SelectHelper.select(this);
                },
                () -> {
                },
                () -> {
                }
        );
    }
}
