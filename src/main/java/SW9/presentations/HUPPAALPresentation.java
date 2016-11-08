package SW9.presentations;

import SW9.controllers.HUPPAALController;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.SelectHelperNew;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.animation.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.paint.Color.TRANSPARENT;

public class HUPPAALPresentation extends StackPane {

    private final HUPPAALController controller;

    private final BooleanProperty queryPaneOpen = new SimpleBooleanProperty(false);
    private final SimpleDoubleProperty queryPaneAnimationProperty = new SimpleDoubleProperty(0);

    private Timeline closeQueryPaneAnimation;
    private Timeline openQueryPaneAnimation;

    public HUPPAALPresentation() {
        final URL location = this.getClass().getResource("HUPPAALPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            controller = fxmlLoader.getController();

            initializeTopBar();
            initializeBottomStatusBar();
            initializeToolbar();
            initializeToggleQueryPaneFunctionality();
            initializeQueryDetailsDialog();
            initializeGenerateUppaalModelButton();
            initializeColorSelectedButton();
            initializeColorSelector();

            controller.bottomStatusBar.heightProperty().addListener((observable, oldValue, newValue) -> AnchorPane.setBottomAnchor(controller.queryPane, (Double) newValue));

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeColorSelector() {
        class EnabledColor {
            private final Color color;
            private final Color.Intensity intensity;

            private EnabledColor(final Color color, final Color.Intensity intensity) {
                this.color = color;
                this.intensity = intensity;
            }

            @Override
            public boolean equals(final Object obj) {
                return obj instanceof EnabledColor && ((EnabledColor) obj).color.equals(this.color);
            }
        }

        final JFXPopup popup = new JFXPopup();

        final ArrayList<EnabledColor> enabledColors = new ArrayList<>();
        enabledColors.add(new EnabledColor(Color.RED, Color.Intensity.I500));
        enabledColors.add(new EnabledColor(Color.PINK, Color.Intensity.I300));
        enabledColors.add(new EnabledColor(Color.PURPLE, Color.Intensity.I300));
        enabledColors.add(new EnabledColor(Color.INDIGO, Color.Intensity.I300));
        enabledColors.add(new EnabledColor(Color.BLUE, Color.Intensity.I500));
        enabledColors.add(new EnabledColor(Color.CYAN, Color.Intensity.I700));
        enabledColors.add(new EnabledColor(Color.GREEN, Color.Intensity.I600));
        enabledColors.add(new EnabledColor(Color.BROWN, Color.Intensity.I300));
        enabledColors.add(new EnabledColor(Color.GREY_BLUE, Color.Intensity.I700));

        final double listWidth = 136;
        final FlowPane list = new FlowPane();
        for (final EnabledColor color : enabledColors) {
            final Circle circle = new Circle(16, color.color.getColor(color.intensity));
            circle.setStroke(color.color.getColor(color.intensity.next(2)));
            circle.setStrokeWidth(1);

            final StackPane child = new StackPane(circle);
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
                final List<Pair<SelectHelperNew.Selectable, EnabledColor>> previousColor = new ArrayList<>();

                SelectHelperNew.getSelectedElements().forEach(selectable -> {
                    previousColor.add(new Pair<>(selectable, new EnabledColor(selectable.getColor(), selectable.getColorIntensity())));
                });

                UndoRedoStack.push(() -> { // Perform
                    SelectHelperNew.getSelectedElements().forEach(selectable -> {
                        System.out.println(color.color);
                        System.out.println(color.intensity);
                        selectable.color(color.color, color.intensity);
                    });
                }, () -> { // Undo
                    previousColor.forEach(selectableEnabledColorPair -> {
                        selectableEnabledColorPair.getKey().color(selectableEnabledColorPair.getValue().color, selectableEnabledColorPair.getValue().intensity);
                    });
                });

                popup.close();
            });

            list.getChildren().add(child);
        }
        list.setMinWidth(listWidth);
        list.setMaxWidth(listWidth);
        list.setStyle("-fx-background-color: white; -fx-padding: 8;");

        popup.setContent(list);
        popup.setPopupContainer(controller.root);
        popup.setSource(controller.toolbar);

        controller.colorSelected.setOnMouseClicked((e) -> {
            if (SelectHelperNew.getSelectedElements().size() == 0) return;

            final Bounds boundsInScreenButton = controller.colorSelected.localToScreen(controller.colorSelected.getBoundsInLocal());
            final Bounds boundsInScreenRoot = controller.root.localToScreen(controller.root.getBoundsInLocal());

            double fromLeft = 0;
            fromLeft = boundsInScreenButton.getMinX() - boundsInScreenRoot.getMinX();
            fromLeft -= listWidth;
            fromLeft += boundsInScreenButton.getWidth();

            popup.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, fromLeft, boundsInScreenButton.getMinY() - boundsInScreenRoot.getMinY());
        });
    }

    private void initializeGenerateUppaalModelButton() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        controller.generateUppaalModel.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        controller.generateUppaalModel.setRipplerFill(color.getTextColor(colorIntensity));
    }

    private void initializeColorSelectedButton() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        controller.colorSelected.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        controller.colorSelected.setRipplerFill(color.getTextColor(colorIntensity));

        // The color button should only be enabled when an element is selected
        SelectHelperNew.getSelectedElements().addListener(new ListChangeListener<SelectHelperNew.Selectable>() {
            @Override
            public void onChanged(final Change<? extends SelectHelperNew.Selectable> c) {
                if (SelectHelperNew.getSelectedElements().size() > 0) {
                    controller.colorSelected.setEnabled(true);

                    final FadeTransition fadeAnimation = new FadeTransition(Duration.millis(100), controller.colorSelected);
                    fadeAnimation.setFromValue(controller.colorSelected.getOpacity());
                    fadeAnimation.setToValue(1);
                    fadeAnimation.play();
                } else {
                    controller.colorSelected.setEnabled(false);

                    final FadeTransition fadeAnimation = new FadeTransition(Duration.millis(100), controller.colorSelected);
                    fadeAnimation.setFromValue(1);
                    fadeAnimation.setToValue(0.3);
                    fadeAnimation.play();
                }
            }
        });

        // Disable the color button
        controller.colorSelected.setEnabled(false);
        controller.colorSelected.setOpacity(0.3);
    }

    private void initializeQueryDetailsDialog() {
        final Color modalBarColor = Color.GREY_BLUE;
        final Color.Intensity modalBarColorIntensity = Color.Intensity.I500;

        // Set the background of the modal bar
        controller.modalBar.setBackground(new Background(new BackgroundFill(
                modalBarColor.getColor(modalBarColorIntensity),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        // Set the color of the query text field
        controller.queryTextField.setUnFocusColor(TRANSPARENT);
        controller.queryTextField.setFocusColor(modalBarColor.getColor(modalBarColorIntensity));

        // Set the color of the comment text field
        controller.commentTextField.setUnFocusColor(TRANSPARENT);
        controller.commentTextField.setFocusColor(modalBarColor.getColor(modalBarColorIntensity));
    }

    private void initializeToggleQueryPaneFunctionality() {
        // Set the translation of the query pane to be equal to its width
        // Will hide the element, and force it in then the right side of the border pane is enlarged
        controller.queryPane.translateXProperty().bind(controller.queryPane.widthProperty());

        // Whenever the width of the query pane is updated, update the animations
        controller.queryPane.widthProperty().addListener((observable) -> {
            initializeOpenQueryPaneAnimation();
            initializeCloseQueryPaneAnimation();
        });

        // Whenever the animation property changed, change the size of the filler element to push the canvas
        queryPaneAnimationProperty.addListener((observable, oldValue, newValue) -> {
            controller.fillerElement.setMinWidth(newValue.doubleValue());
            controller.fillerElement.setMaxWidth(newValue.doubleValue());
        });
    }

    private void initializeCloseQueryPaneAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        openQueryPaneAnimation = new Timeline();

        final KeyValue open = new KeyValue(queryPaneAnimationProperty, controller.queryPane.getWidth(), interpolator);
        final KeyValue closed = new KeyValue(queryPaneAnimationProperty, 0, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), open);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), closed);

        openQueryPaneAnimation.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeOpenQueryPaneAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        closeQueryPaneAnimation = new Timeline();

        final KeyValue closed = new KeyValue(queryPaneAnimationProperty, 0, interpolator);
        final KeyValue open = new KeyValue(queryPaneAnimationProperty, controller.queryPane.getWidth(), interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), closed);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), open);

        closeQueryPaneAnimation.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeTopBar() {
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            controller.menuBar.useSystemMenuBarProperty().set(true);
            controller.menuBar.setVisible(false);
        } else {
            final Color color = Color.GREY_BLUE;
            final Color.Intensity intensity = Color.Intensity.I800;

            // Set the background for the top toolbar
            controller.menuBar.setBackground(
                    new Background(new BackgroundFill(color.getColor(intensity),
                            CornerRadii.EMPTY,
                            Insets.EMPTY)
                    ));

            // Set the bottom border
            controller.menuBar.setBorder(new Border(new BorderStroke(
                    color.getColor(intensity.next()),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(0, 0, 1, 0)
            )));
        }
    }

    private void initializeToolbar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I700;

        // Set the background for the top toolbar
        controller.toolbar.setBackground(
                new Background(new BackgroundFill(color.getColor(intensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));

        // Set the font color for the title
        controller.title.setTextFill(color.getTextColor(intensity));
    }

    private void initializeBottomStatusBar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I200;

        // Set the background for the bottom status bar
        controller.bottomStatusBar.setBackground(
                new Background(new BackgroundFill(color.getColor(intensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));
    }

    public void toggleQueryPane() {
        if (queryPaneOpen.get()) {
            openQueryPaneAnimation.play();
        } else {
            closeQueryPaneAnimation.play();
        }

        // Toggle the open state
        queryPaneOpen.set(queryPaneOpen.not().get());
    }
}
