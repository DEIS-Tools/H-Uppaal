package SW9.presentations;

import SW9.HUPPAAL;
import SW9.abstractions.Query;
import SW9.code_analysis.CodeAnalysis;
import SW9.controllers.HUPPAALController;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.colors.EnabledColor;
import SW9.utility.helpers.SelectHelper;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXSnackbar;
import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static SW9.utility.colors.EnabledColor.enabledColors;

public class HUPPAALPresentation extends StackPane {

    private final HUPPAALController controller;

    private final BooleanProperty queryPaneOpen = new SimpleBooleanProperty(false);
    private final SimpleDoubleProperty queryPaneAnimationProperty = new SimpleDoubleProperty(0);
    private final BooleanProperty filePaneOpen = new SimpleBooleanProperty(false);
    private final SimpleDoubleProperty filePaneAnimationProperty = new SimpleDoubleProperty(0);
    private Timeline closeQueryPaneAnimation;
    private Timeline openQueryPaneAnimation;
    private Timeline closeFilePaneAnimation;
    private Timeline openFilePaneAnimation;

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
            initializeToolbar();
            initializeQueryDetailsDialog();
            initializeGenerateUppaalModelButton();
            initializeColorSelector();

            initializeToggleQueryPaneFunctionality();
            initializeToggleFilePaneFunctionality();

            initializeSelectDependentToolbarButton(controller.colorSelected);
            initializeSelectDependentToolbarButton(controller.deleteSelected);

            initializeToolbarButton(controller.undo);
            initializeToolbarButton(controller.redo);
            initializeUndoRedoButtons();

            initializeLogo();

            initializeMessageContainer();

            initializeSnackbar();

            // Open the file and query panel initially
            final BooleanProperty ranInitialToggle = new SimpleBooleanProperty(false);
            controller.filePane.widthProperty().addListener((observable) -> {
                if (ranInitialToggle.get()) return;
                toggleFilePane();
                ranInitialToggle.set(true);
            });

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeSnackbar() {
        controller.snackbar.registerSnackbarContainer(controller.root);
        controller.snackbar.setPrefWidth(568);
        controller.snackbar.autosize();

        final StackPane parentFix = (StackPane) controller.root.lookup(".jfx-snackbar-toast").getParent();
        parentFix.setPadding(new Insets(14, 24, 14, 24));
    }

    private void initializeMessageContainer() {
        // The element of which you drag to resize should be equal to the width of the window (main stage)
        controller.tabPaneResizeElement.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time. Now its the time to listen stage changes.
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        newWindow.widthProperty().addListener((observableWidth, oldWidth, newWidth) -> {
                            controller.tabPaneResizeElement.setWidth(newWidth.doubleValue() - 30);
                        });
                    }
                });
            }
        });

        // Resize cursor
        controller.tabPaneResizeElement.setCursor(Cursor.N_RESIZE);

        controller.tabPaneContainer.maxHeightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (newHeight.doubleValue() > 35) {
                controller.collapseMessagesIcon.setIconLiteral("gmi-close");
                controller.collapseMessagesIcon.setIconSize(24);
            } else {
                controller.tabPane.getSelectionModel().clearSelection(); // Clear the currently selected tab (so that the view will open again when selecting a tab)
                controller.collapseMessagesIcon.setIconLiteral("gmi-expand-less");
                controller.collapseMessagesIcon.setIconSize(24);
            }
        });

        // Remove the background of the scroll panes
        controller.errorsScrollPane.setStyle("-fx-background-color: transparent;");
        controller.warningsScrollPane.setStyle("-fx-background-color: transparent;");

        final Runnable collapseIfNoErrorsOrWarnings = () -> {
            new Thread(() -> {
                // Wait for a second to check if new warnings or errors occur
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Check if any warnings or errors occurred
                if (CodeAnalysis.getBackendErrors().size() + CodeAnalysis.getErrors().size() + CodeAnalysis.getWarnings().size() == 0) {
                    controller.collapseMessagesIfNotCollapsed();
                }
            }).start();
        };

        // Update the tab-text and expand/collapse the view
        CodeAnalysis.getBackendErrors().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                final int errors = CodeAnalysis.getBackendErrors().size();
                if (errors == 0) {
                    controller.backendErrorsTab.setText("Backend Errors");
                } else {
                    controller.backendErrorsTab.setText("Backend Errors (" + errors + ")");
                    controller.expandMessagesIfNotExpanded();
                    controller.tabPane.getSelectionModel().select(controller.backendErrorsTab);
                }

                collapseIfNoErrorsOrWarnings.run();
            }
        });

        // Update the tab-text and expand/collapse the view
        CodeAnalysis.getErrors().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                final int errors = CodeAnalysis.getErrors().size();
                if (errors == 0) {
                    controller.errorsTab.setText("Errors");
                } else {
                    controller.errorsTab.setText("Errors (" + errors + ")");
                    controller.expandMessagesIfNotExpanded();
                    controller.tabPane.getSelectionModel().select(controller.errorsTab);
                }

                collapseIfNoErrorsOrWarnings.run();
            }
        });


        // Update the tab-text and expand/collapse the view
        CodeAnalysis.getWarnings().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                final int warnings = CodeAnalysis.getWarnings().size();
                if (warnings == 0) {
                    controller.warningsTab.setText("Warnings");
                } else {
                    controller.warningsTab.setText("Warnings (" + warnings + ")");
                    controller.expandMessagesIfNotExpanded();
                    controller.tabPane.getSelectionModel().select(controller.warningsTab);
                }

                collapseIfNoErrorsOrWarnings.run();
            }
        });
    }

    private void initializeLogo() {
        controller.logo.setImage(new Image(HUPPAAL.class.getResource("ic_launcher/mipmap-mdpi/ic_launcher.png").toExternalForm()));
    }

    private void initializeUndoRedoButtons() {
        UndoRedoStack.canUndoProperty().addListener((obs, oldState, newState) -> {
            if (newState) {
                // Enable the undo button
                controller.undo.setEnabled(true);
                controller.undo.setOpacity(1);
            } else {
                // Disable the undo button
                controller.undo.setEnabled(false);
                controller.undo.setOpacity(0.3);
            }
        });

        UndoRedoStack.canRedoProperty().addListener((obs, oldState, newState) -> {
            if (newState) {
                // Enable the redo button
                controller.redo.setEnabled(true);
                controller.redo.setOpacity(1);
            } else {
                // Disable the redo button
                controller.redo.setEnabled(false);
                controller.redo.setOpacity(0.3);
            }
        });

        // Disable the undo button
        controller.undo.setEnabled(false);
        controller.undo.setOpacity(0.3);

        // Disable the redo button
        controller.redo.setEnabled(false);
        controller.redo.setOpacity(0.3);
    }

    private void initializeColorSelector() {

        final JFXPopup popup = new JFXPopup();

        final double listWidth = 136;
        final FlowPane list = new FlowPane();
        for (final EnabledColor color : enabledColors) {
            final Circle circle = new Circle(16, color.color.getColor(color.intensity));
            circle.setStroke(color.color.getColor(color.intensity.next(2)));
            circle.setStrokeWidth(1);

            final Label label = new Label(color.keyCode.getName());
            label.getStyleClass().add("subhead");
            label.setTextFill(color.color.getTextColor(color.intensity));

            final StackPane child = new StackPane(circle, label);
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
                final List<Pair<SelectHelper.ItemSelectable, EnabledColor>> previousColor = new ArrayList<>();

                SelectHelper.getSelectedElements().forEach(selectable -> {
                    previousColor.add(new Pair<>(selectable, new EnabledColor(selectable.getColor(), selectable.getColorIntensity())));
                });

                UndoRedoStack.push(() -> { // Perform
                    SelectHelper.getSelectedElements().forEach(selectable -> {
                        selectable.color(color.color, color.intensity);
                    });
                }, () -> { // Undo
                    previousColor.forEach(selectableEnabledColorPair -> {
                        selectableEnabledColorPair.getKey().color(selectableEnabledColorPair.getValue().color, selectableEnabledColorPair.getValue().intensity);
                    });
                }, String.format("Changed the color of %d elements to %s", previousColor.size(), color.color.name()), "color-lens");

                popup.close();
                SelectHelper.clearSelectedElements();
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
            if (SelectHelper.getSelectedElements().size() == 0) return;

            final Bounds boundsInScreenButton = controller.colorSelected.localToScreen(controller.colorSelected.getBoundsInLocal());
            final Bounds boundsInScreenRoot = controller.root.localToScreen(controller.root.getBoundsInLocal());

            double fromLeft = 0;
            fromLeft = boundsInScreenButton.getMinX() - boundsInScreenRoot.getMinX();
            fromLeft -= listWidth;
            fromLeft += boundsInScreenButton.getWidth();
            if (!filePaneOpen.get()) {
                fromLeft -= controller.filePane.getWidth();
                System.out.println(controller.filePane.getWidth());
            }

            popup.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, fromLeft, boundsInScreenButton.getMinY() - boundsInScreenRoot.getMinY());
        });
    }

    private void initializeGenerateUppaalModelButton() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        controller.generateUppaalModel.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        controller.generateUppaalModel.setRipplerFill(color.getTextColor(colorIntensity));
    }

    private void initializeSelectDependentToolbarButton(final JFXRippler button) {
        initializeToolbarButton(button);

        // The color button should only be enabled when an element is selected
        SelectHelper.getSelectedElements().addListener(new ListChangeListener<SelectHelper.ItemSelectable>() {
            @Override
            public void onChanged(final Change<? extends SelectHelper.ItemSelectable> c) {
                if (SelectHelper.getSelectedElements().size() > 0) {
                    button.setEnabled(true);

                    final FadeTransition fadeAnimation = new FadeTransition(Duration.millis(100), button);
                    fadeAnimation.setFromValue(button.getOpacity());
                    fadeAnimation.setToValue(1);
                    fadeAnimation.play();
                } else {
                    button.setEnabled(false);

                    final FadeTransition fadeAnimation = new FadeTransition(Duration.millis(100), button);
                    fadeAnimation.setFromValue(1);
                    fadeAnimation.setToValue(0.3);
                    fadeAnimation.play();
                }
            }
        });

        // Disable the button
        button.setEnabled(false);
        button.setOpacity(0.3);
    }

    private void initializeToolbarButton(final JFXRippler button) {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        button.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        button.setRipplerFill(color.getTextColor(colorIntensity));
        button.setPosition(JFXRippler.RipplerPos.BACK);
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
            controller.queryPaneFillerElement.setMinWidth(newValue.doubleValue());
            controller.queryPaneFillerElement.setMaxWidth(newValue.doubleValue());
        });

        // When new queries are added, make sure that the query pane is open
        HUPPAAL.getProject().getQueries().addListener(new ListChangeListener<Query>() {
            @Override
            public void onChanged(final Change<? extends Query> c) {
                if (queryPaneOpen == null || openQueryPaneAnimation == null)
                    return; // The query pane is not yet initialized

                while (c.next()) {
                    c.getAddedSubList().forEach(o -> {
                        if (!queryPaneOpen.get()) {
                            // Open the pane
                            closeQueryPaneAnimation.play();

                            // Toggle the open state
                            queryPaneOpen.set(queryPaneOpen.not().get());
                        }
                    });
                }
            }
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

    private void initializeToggleFilePaneFunctionality() {
        // Set the translation of the file pane to be equal to its width
        // Will hide the element, and force it in then the left side of the border pane is enlarged
        controller.filePane.translateXProperty().bind(controller.filePane.widthProperty().multiply(-1));

        // Whenever the width of the file pane is updated, update the animations
        controller.filePane.widthProperty().addListener((observable) -> {
            initializeOpenFilePaneAnimation();
            initializeCloseFilePaneAnimation();
        });

        // Whenever the animation property changed, change the size of the filler element to push the canvas
        filePaneAnimationProperty.addListener((observable, oldValue, newValue) -> {
            controller.filePaneFillerElement.setMinWidth(newValue.doubleValue());
            controller.filePaneFillerElement.setMaxWidth(newValue.doubleValue());
        });
    }

    private void initializeCloseFilePaneAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        openFilePaneAnimation = new Timeline();

        final KeyValue open = new KeyValue(filePaneAnimationProperty, controller.filePane.getWidth(), interpolator);
        final KeyValue closed = new KeyValue(filePaneAnimationProperty, 0, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), open);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), closed);

        openFilePaneAnimation.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeOpenFilePaneAnimation() {
        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        closeFilePaneAnimation = new Timeline();

        final KeyValue closed = new KeyValue(filePaneAnimationProperty, 0, interpolator);
        final KeyValue open = new KeyValue(filePaneAnimationProperty, controller.filePane.getWidth(), interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), closed);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(200), open);

        closeFilePaneAnimation.getKeyFrames().addAll(kf1, kf2);
    }

    private void initializeTopBar() {
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

    private void initializeToolbar() {
        final Color color = Color.GREY_BLUE;
        final Color.Intensity intensity = Color.Intensity.I700;

        // Set the background for the top toolbar
        controller.toolbar.setBackground(
                new Background(new BackgroundFill(color.getColor(intensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));
    }

    public BooleanProperty toggleQueryPane() {
        if (queryPaneOpen.get()) {
            openQueryPaneAnimation.play();
        } else {
            closeQueryPaneAnimation.play();
        }

        // Toggle the open state
        queryPaneOpen.set(queryPaneOpen.not().get());

        return queryPaneOpen;
    }

    public BooleanProperty toggleFilePane() {
        if (filePaneOpen.get()) {
            openFilePaneAnimation.play();
        } else {
            closeFilePaneAnimation.play();
        }

        // Toggle the open state
        filePaneOpen.set(filePaneOpen.not().get());

        return filePaneOpen;
    }

    public void showSnackbarMessage(final String message) {
        controller.snackbar.enqueue(new JFXSnackbar.SnackbarEvent(message, "", 3000, event -> {

        }));
    }

    public void showHelp() {
        controller.dialogContainer.setVisible(true);
        controller.dialog.show(controller.dialogContainer);
    }
}
