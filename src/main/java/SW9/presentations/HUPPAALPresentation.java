package SW9.presentations;

import SW9.controllers.HUPPAALController;
import SW9.utility.colors.Color;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

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

            controller.bottomStatusBar.heightProperty().addListener((observable, oldValue, newValue) -> AnchorPane.setBottomAnchor(controller.queryPane, (Double) newValue));
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
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
