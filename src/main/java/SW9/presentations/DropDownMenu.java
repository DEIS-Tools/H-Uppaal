package SW9.presentations;

import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

import static javafx.scene.paint.Color.TRANSPARENT;

public class DropDownMenu {

    private final int width;
    private final VBox list;
    private final JFXPopup popup;

    public DropDownMenu(final Pane container, final Node source, final int width) {
        this.width = width;

        popup = new JFXPopup();

        list = new VBox();
        list.setOnMouseExited(event -> popup.close());

        list.setStyle("-fx-background-color: white; -fx-padding: 8 0 8 0;");
        list.setMinWidth(width);
        list.setMaxWidth(width);

        popup.setContent(list);
        popup.setPopupContainer(container);
        popup.setSource(source);
    }

    public void close() {
        popup.close();
    }

    public void show(final JFXPopup.PopupVPosition vAlign, final JFXPopup.PopupHPosition hAlign, final double initOffsetX, final double initOffsetY) {
        popup.show(vAlign, hAlign, initOffsetX, initOffsetY);
    }

    public void addListElement(final String s) {
        final Label label = new Label(s);

        label.setStyle("-fx-padding: 8 16 8 16;");
        label.getStyleClass().add("body2");
        label.setMinWidth(width);

        list.getChildren().add(label);
    }

    public void addClickableListElement(final String s, final Consumer<MouseEvent> mouseEventConsumer) {
        final Label label = new Label(s);

        label.setStyle("-fx-padding: 8 16 8 16;");
        label.getStyleClass().add("body2");
        label.setMinWidth(width);

        final JFXRippler rippler = new JFXRippler(label);
        rippler.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I300));

        rippler.setOnMouseEntered(event -> {
            // Set the background to a light grey
            label.setBackground(new Background(new BackgroundFill(
                    Color.GREY.getColor(Color.Intensity.I200),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        });

        rippler.setOnMouseExited(event -> {
            // Set the background to be transparent
            label.setBackground(new Background(new BackgroundFill(
                    TRANSPARENT,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        });

        // When the rippler is pressed, run the provided consumer.
        rippler.setOnMousePressed(event -> {
            // If we do not do this, the method below will be called twice
            if (!(event.getTarget() instanceof StackPane)) return;

            mouseEventConsumer.accept(event);
        });

        list.getChildren().add(rippler);
    }

    public void addTogglableListElement(final String s, final ObservableBooleanValue isToggled, final Consumer<MouseEvent> mouseEventConsumer) {
        final Label label = new Label(s);
        label.getStyleClass().add("body2");

        final HBox container = new HBox();
        container.setStyle("-fx-padding: 8 16 8 16;");

        final FontIcon icon = new FontIcon();
        icon.setIconLiteral("gmi-done");
        icon.setFill(Color.GREY.getColor(Color.Intensity.I600));
        icon.setIconSize(20);
        icon.visibleProperty().bind(isToggled);

        final Region spacer = new Region();
        spacer.setMinWidth(8);

        container.getChildren().addAll(icon, spacer, label);

        final StackPane clickListenerFix = new StackPane(container);

        final JFXRippler rippler = new JFXRippler(clickListenerFix);
        rippler.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I300));

        rippler.setOnMouseEntered(event -> {
            // Set the background to a light grey
            container.setBackground(new Background(new BackgroundFill(
                    Color.GREY.getColor(Color.Intensity.I200),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        });

        rippler.setOnMouseExited(event -> {
            // Set the background to be transparent
            container.setBackground(new Background(new BackgroundFill(
                    TRANSPARENT,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        });

        // When the rippler is pressed, run the provided consumer.
        clickListenerFix.setOnMousePressed(event -> {
            mouseEventConsumer.accept(event);
            event.consume();
        });

        list.getChildren().add(rippler);
    }

    public void addSpacerElement() {
        final Region space1 = new Region();
        space1.setMinHeight(8);
        list.getChildren().add(space1);

        final Line sep = new Line(0, 0, width, 0);
        sep.setStroke(Color.GREY.getColor(Color.Intensity.I300));
        list.getChildren().add(sep);

        final Region space2 = new Region();
        space2.setMinHeight(8);
        list.getChildren().add(space2);
    }

    public void addCustomChild(final Node child) {
        list.getChildren().add(child);
    }
}
