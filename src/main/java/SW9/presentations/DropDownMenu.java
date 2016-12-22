package SW9.presentations;

import SW9.utility.colors.Color;
import SW9.utility.colors.EnabledColor;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.swing.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static SW9.utility.colors.EnabledColor.enabledColors;
import static javafx.scene.paint.Color.TRANSPARENT;
import static javafx.scene.paint.Color.WHITE;

public class DropDownMenu {

    public static double x = 0;
    public static double y = 0;
    private final int width;
    private final StackPane content;
    private final VBox list;
    private final JFXPopup popup;
    private final SimpleBooleanProperty isHoveringSubMenu = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isHoveringMenu = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty showSubMenu = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty canIShowSubMenu = new SimpleBooleanProperty(false);
    private StackPane subMenuContent;

    public DropDownMenu(final Pane container, final Node source, final int width, final boolean closeOnMouseExit) {
        this.width = width;

        popup = new JFXPopup();

        list = new VBox();
        list.setStyle("-fx-background-color: white; -fx-padding: 8 0 8 0;");
        list.setMaxHeight(1);
        StackPane.setAlignment(list, Pos.TOP_CENTER);

        content = new StackPane(list);
        content.setMinWidth(width);
        content.setMaxWidth(width);

        if (closeOnMouseExit) {
            final Runnable checkIfWeShouldClose = () -> {
                if (!isHoveringMenu.get() && !isHoveringSubMenu.get()) {
                    final Timer timer = new Timer(20, arg0 -> {
                        if (!isHoveringMenu.get() && !isHoveringSubMenu.get()) {
                            close();
                        }
                    });
                    timer.setRepeats(false); // Only execute once
                    timer.start(); // Go go go!
                }
            };
            isHoveringMenu.addListener(observable -> checkIfWeShouldClose.run());
            isHoveringSubMenu.addListener(observable -> checkIfWeShouldClose.run());
        }


        list.setOnMouseExited(event -> isHoveringMenu.set(false));
        list.setOnMouseEntered(event -> isHoveringMenu.set(true));

        popup.setContent(content);
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

        label.setOnMouseEntered(event -> canIShowSubMenu.set(false));
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

            canIShowSubMenu.set(false);
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

    public void addSubMenu(final String s, final DropDownMenu subMenu, final int offset) {
        final Label label = new Label(s);

        label.setStyle("-fx-padding: 8 16 8 16;");
        label.getStyleClass().add("body2");
        label.setMinWidth(width);

        subMenuContent = subMenu.content;
        if (!this.content.getChildren().contains(subMenuContent)) {
            subMenuContent.setStyle("-fx-padding: 0 0 0 5;");
            subMenuContent.setMinWidth(subMenuContent.getMinWidth() + 1);
            subMenuContent.setMaxWidth(subMenuContent.getMinWidth() + 1);
            subMenuContent.setTranslateX(width - 40);
            this.content.getChildren().add(subMenuContent);
        }

        subMenuContent.setTranslateY(offset);

        subMenuContent.setOpacity(0);
        final Runnable showHideSubMenu = () -> {
            if (showSubMenu.get() || isHoveringSubMenu.get()) {
                subMenuContent.setOpacity(1);
            } else {
                subMenuContent.setOpacity(0);
            }
        };

        showSubMenu.addListener((obs, oldShow, newShow) -> showHideSubMenu.run());
        isHoveringSubMenu.addListener((obs, oldHovering, newHovering) -> showHideSubMenu.run());

        subMenuContent.setOnMouseEntered(event -> {
            if (canIShowSubMenu.get()) {
                isHoveringSubMenu.set(true);
            }
        });
        subMenuContent.setOnMouseExited(event -> {
            isHoveringSubMenu.set(false);
        });

        final JFXRippler rippler = new JFXRippler(label);
        rippler.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I300));

        rippler.setOnMouseEntered(event -> {
            // Set the background to a light grey
            label.setBackground(new Background(new BackgroundFill(
                    Color.GREY.getColor(Color.Intensity.I200),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            canIShowSubMenu.set(true);
            showSubMenu.set(true);
        });

        rippler.setOnMouseExited(event -> {
            // Set the background to be transparent
            label.setBackground(new Background(new BackgroundFill(
                    TRANSPARENT,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            if (!isHoveringSubMenu.get()) {
                showSubMenu.set(false);
            }
        });

        final FontIcon icon = new FontIcon();
        icon.setIconLiteral("gmi-chevron-right");
        icon.setFill(Color.GREY.getColor(Color.Intensity.I600));
        icon.setIconSize(20);

        final StackPane iconContainer = new StackPane(icon);
        iconContainer.setMaxWidth(20);
        iconContainer.setMaxHeight(20);
        iconContainer.setStyle("-fx-padding: 8;");
        iconContainer.setMouseTransparent(true);

        rippler.getChildren().add(iconContainer);
        StackPane.setAlignment(iconContainer, Pos.CENTER_RIGHT);

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

            canIShowSubMenu.set(false);
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

        final Line sep = new Line(0, 0, width - 1, 0);
        sep.setStroke(Color.GREY.getColor(Color.Intensity.I300));
        list.getChildren().add(sep);

        final Region space2 = new Region();
        space2.setMinHeight(8);
        list.getChildren().add(space2);

        space1.setOnMouseEntered(event -> canIShowSubMenu.set(false));
        space2.setOnMouseEntered(event -> canIShowSubMenu.set(false));
    }

    public void addColorPicker(final HasColor hasColor, final BiConsumer<Color, Color.Intensity> consumer) {
        final FlowPane flowPane = new FlowPane();
        flowPane.setStyle("-fx-padding: 0 8 0 8");

        for (final EnabledColor color : enabledColors) {
            final Circle circle = new Circle(16, color.color.getColor(color.intensity));
            circle.setStroke(color.color.getColor(color.intensity.next(2)));
            circle.setStrokeWidth(1);

            final FontIcon icon = new FontIcon();
            icon.setIconLiteral("gmi-done");
            icon.setFill(color.color.getTextColor(color.intensity));
            icon.setIconSize(20);
            icon.visibleProperty().bind(new When(hasColor.colorProperty().isEqualTo(color.color)).then(true).otherwise(false));

            final StackPane child = new StackPane(circle, icon);
            child.setMinSize(40, 40);
            child.setMaxSize(40, 40);

            child.setOnMouseEntered(event -> {
                final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), circle);
                scaleTransition.setFromX(circle.getScaleX());
                scaleTransition.setFromY(circle.getScaleY());
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.play();
            });

            child.setOnMouseExited(event -> {
                final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), circle);
                scaleTransition.setFromX(circle.getScaleX());
                scaleTransition.setFromY(circle.getScaleY());
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });

            child.setOnMouseClicked(event -> {
                event.consume();

                // Only color the subject if the user chooses a new color
                if (hasColor.colorProperty().get().equals(color.color)) return;

                consumer.accept(color.color, color.intensity);
            });

            flowPane.getChildren().add(child);
        }

        flowPane.setOnMouseEntered(event -> canIShowSubMenu.set(false));

        addCustomChild(flowPane);
    }

    public void addCustomChild(final Node child) {
        list.getChildren().add(child);
    }

    public void addClickableAndDisableableListElement(final String s, final ObservableBooleanValue isDisabled, final Consumer<MouseEvent> mouseEventConsumer) {
        final Label label = new Label(s);

        label.setStyle("-fx-padding: 8 16 8 16;");
        label.getStyleClass().add("body2");
        label.setMinWidth(width);

        final JFXRippler rippler = new JFXRippler(label);

        rippler.setOnMouseEntered(event -> {
            if (isDisabled.get()) return;

            // Set the background to a light grey
            label.setBackground(new Background(new BackgroundFill(
                    Color.GREY.getColor(Color.Intensity.I200),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            canIShowSubMenu.set(false);
        });

        rippler.setOnMouseExited(event -> {
            if (isDisabled.get()) return;

            // Set the background to be transparent
            label.setBackground(new Background(new BackgroundFill(
                    TRANSPARENT,
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
        });

        // When the rippler is pressed, run the provided consumer.
        rippler.setOnMousePressed(event -> {
            if (isDisabled.get()) {
                event.consume();
                return;
            }

            // If we do not do this, the method below will be called twice
            if (!(event.getTarget() instanceof StackPane)) return;

            mouseEventConsumer.accept(event);
        });

        final Consumer<Boolean> updateTransparency = (disabled) -> {
            if (disabled) {
                rippler.setRipplerFill(WHITE);
                label.setOpacity(0.5);
            } else {
                rippler.setOpacity(1);
                rippler.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I300));
                label.setOpacity(1);
            }
        };

        isDisabled.addListener((obs, oldDisabled, newDisabled) -> updateTransparency.accept(newDisabled));
        updateTransparency.accept(isDisabled.get());

        list.getChildren().add(rippler);
    }

    public interface HasColor {
        ObjectProperty<Color> colorProperty();

        ObjectProperty<Color.Intensity> colorIntensityProperty();
    }
}
