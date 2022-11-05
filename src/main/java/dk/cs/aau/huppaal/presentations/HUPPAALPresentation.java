package dk.cs.aau.huppaal.presentations;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.*;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Query;
import dk.cs.aau.huppaal.backend.DummyUPPAALDriver;
import dk.cs.aau.huppaal.backend.UPPAALDriverManager;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;
import dk.cs.aau.huppaal.controllers.HUPPAALController;
import dk.cs.aau.huppaal.runconfig.RunConfigurationPreferencesKeys;
import dk.cs.aau.huppaal.runconfig.RunConfiguration;
import dk.cs.aau.huppaal.runconfig.RunConfigurationButton;
import dk.cs.aau.huppaal.utility.UndoRedoStack;
import dk.cs.aau.huppaal.utility.colors.Color;
import dk.cs.aau.huppaal.utility.colors.EnabledColor;
import dk.cs.aau.huppaal.utility.helpers.ArrayUtils;
import dk.cs.aau.huppaal.utility.helpers.SelectHelper;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.reactfx.Observable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static dk.cs.aau.huppaal.utility.colors.EnabledColor.enabledColors;

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
            initializeRunConfigPicker();
            initializeColorSelector();

            initializeToggleQueryPaneFunctionality();
            initializeToggleFilePaneFunctionality();

            initializeSelectDependentToolbarButton(controller.colorSelected);
            initializeSelectDependentToolbarButton(controller.deleteSelected);

            initializeToolbarButton(controller.undo);
            initializeToolbarButton(controller.redo);
            initializeUndoRedoButtons();

            initializeToolbarButton(controller.zoomIn);
            initializeToolbarButton(controller.zoomOut);

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
        controller.snackbar = new JFXSnackbar(controller.root);
        controller.snackbar.setPrefWidth(568);
        controller.snackbar.autosize();
    }

    private void initializeMessageContainer() {
        // The element of which you drag to resize should be equal to the width of the window (main stage)
        controller.tabPaneResizeElement.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time. Now its the time to listen stage changes.
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null)
                        newWindow.widthProperty().addListener((observableWidth, oldWidth, newWidth) -> controller.tabPaneResizeElement.setWidth(newWidth.doubleValue() - 30));
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
        CodeAnalysis.getBackendErrors().addListener((InvalidationListener) observable -> {
            var errors = CodeAnalysis.getBackendErrors().size();
            controller.backendErrorsTab.setText("Backend Errors" + (errors != 0 ? " ("+errors+") " : ""));
            collapseIfNoErrorsOrWarnings.run();
        });

        // Update the tab-text and expand/collapse the view
        CodeAnalysis.getErrors().addListener((InvalidationListener) observable -> {
            var errors = CodeAnalysis.getErrors().size();
            controller.errorsTab.setText("Errors" + (errors != 0 ? " ("+errors+") " : ""));
            collapseIfNoErrorsOrWarnings.run();
        });


        // Update the tab-text and expand/collapse the view
        CodeAnalysis.getWarnings().addListener((InvalidationListener) observable -> {
            var warnings = CodeAnalysis.getWarnings().size();
            controller.warningsTab.setText("Warnings" + (warnings != 0 ? " ("+warnings+") " : ""));
            collapseIfNoErrorsOrWarnings.run();
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

                popup.hide();

                SelectHelper.clearSelectedElements();
            });

            list.getChildren().add(child);
        }
        list.setMinWidth(listWidth);
        list.setMaxWidth(listWidth);
        list.setStyle("-fx-background-color: white; -fx-padding: 8;");

        popup.setPopupContent(list);

        controller.colorSelected.setOnMouseClicked((e) -> {
            if (SelectHelper.getSelectedElements().size() == 0) return;

            final Bounds boundsInScreenButton = controller.colorSelected.localToScreen(controller.colorSelected.getBoundsInLocal());
            final Bounds boundsInScreenRoot = controller.root.localToScreen(controller.root.getBoundsInLocal());

            double fromLeft = 0;
            fromLeft = boundsInScreenButton.getMinX() - boundsInScreenRoot.getMinX();
            fromLeft -= listWidth/2;
            fromLeft += boundsInScreenButton.getWidth();
            if (!filePaneOpen.get()) {
                fromLeft -= controller.filePane.getWidth();
                System.out.println(controller.filePane.getWidth());
            }
            popup.show(this, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, fromLeft, boundsInScreenButton.getMinY() - boundsInScreenRoot.getMinY() + 30);
        });
    }

    private void initializeGenerateUppaalModelButton() {
        var color = Color.GREY_BLUE;
        var colorIntensity = Color.Intensity.I800;
        controller.generateUppaalModel.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        controller.generateUppaalModel.setRipplerFill(color.getTextColor(colorIntensity));
        if(UPPAALDriverManager.getInstance() instanceof DummyUPPAALDriver) {
            controller.generateUppaalModel.setEnabled(false);
            controller.generateUppaalModel.setOpacity(0.3);
        } else {
            controller.generateUppaalModel.setEnabled(true);
            controller.generateUppaalModel.setOpacity(1.0);
        }
    }

    private void initializeRunConfigExecuteButton() {
        var colorIntensity = Color.Intensity.I800;
        var color = Color.GREY_BLUE;
        controller.runConfigurationExecuteButton.setRipplerFill(color.getTextColor(colorIntensity));

        JFXTooltip tooltip;
        var c = controller.runConfigurationPicker.getSelectionModel().getSelectedItem();
        if(c == null || c.runConfiguration().isEmpty()) {
            tooltip = new JFXTooltip("no run configuration is selected");
            controller.runConfigurationExecuteButton.setEnabled(false);
            controller.runConfigurationExecuteButton.setOpacity(0.3);
            controller.runConfigurationExecuteButton.setOnMouseClicked(e -> HUPPAAL.showToast("no run configuration is selected"));
        } else {
            tooltip = new JFXTooltip("Run " + c.runConfiguration().get().name);
            controller.runConfigurationExecuteButton.setEnabled(true);
            controller.runConfigurationExecuteButton.setOpacity(1.0);
            controller.runConfigurationExecuteButton.setOnMouseClicked(e -> executeRunConfiguration(c.runConfiguration().get()));
        }
        JFXTooltip.install(controller.runConfigurationExecuteButton, tooltip);
    }

    private final static String[] sysEnv = System.getenv().entrySet().stream().map((e) -> e.getKey() + "=" + e.getValue()).toArray(String[]::new);
    private Process proc;
    private void executeRunConfiguration(RunConfiguration config) {
        // Stop the currently running process if it is running
        if(proc != null && proc.isAlive()) {
            proc.destroy();
            return;
        }

        // Else start the run configuration
        new Thread(() -> {
            try {
                var rt = Runtime.getRuntime();
                if(config.program.isEmpty())
                    throw new Exception("No program to run in selected run configuration");
                var dir = new File(config.executionDir);
                if(!(dir.exists() && dir.isDirectory()))
                    throw new Exception(String.format("'%s' does not exist or is not a directory", config.executionDir));
                proc = rt.exec(config.program + " " + config.arguments,
                                ArrayUtils.merge(sysEnv, config.environmentVariables.split(";")),
                                dir);
                var stdi = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                var stde = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                controller.runConfigurationExecuteButtonIcon.setIconLiteral("gmi-stop");
                controller.runConfigurationExecuteButtonIcon.setIconColor(javafx.scene.paint.Color.web("#ff7e79"));
                String s;
                while((s = stdi.readLine()) != null) {
                    var finalS = s;
                    Platform.runLater(() -> CodeAnalysis.addMessage(finalS));
                }
                while((s = stde.readLine()) != null) {
                    var finalS = s;
                    Platform.runLater(() -> CodeAnalysis.addMessage(new CodeAnalysis.Message(finalS, CodeAnalysis.MessageType.ERROR)));
                }
                HUPPAAL.showToast(config.name + " finished("+proc.exitValue()+")");
            } catch (Exception e) {
                HUPPAAL.showToast(e.getMessage());
                e.printStackTrace();
            } finally {
                controller.runConfigurationExecuteButtonIcon.setIconLiteral("gmi-play-arrow");
                controller.runConfigurationExecuteButtonIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            }
        }).start();
    }

    private void initializeRunConfigPicker() {
        var runConfigsJson = HUPPAAL.preferences.get(RunConfigurationPreferencesKeys.ConfigurationsList, "[]");
        var lastRunConfig = HUPPAAL.preferences.get(RunConfigurationPreferencesKeys.CurrentlySelected, "");
        var runConfigurations = parseRunConfigurationsClearPreferencesIfFails(runConfigsJson);

        // Add all the default things
        controller.runConfigurationPicker.getItems().clear(); // Clear, because this function might be called again during runtime
        controller.runConfigurationPicker.getItems().add(generateRunConfigurationEditButton());
        if (runConfigurations.isEmpty())
            controller.runConfigurationPicker.getItems().add(new RunConfigurationButton(Optional.empty(), new JFXButton("<no run configurations>")));
        for (var c : runConfigurations)
            controller.runConfigurationPicker.getItems().add(new RunConfigurationButton(Optional.of(c), new JFXButton(c.name)));

        // set the last picked run config to be the currently selected one
        if (!Strings.isNullOrEmpty(lastRunConfig)) {
            var e = controller.runConfigurationPicker.getItems().stream().filter(b -> {
                if (b.runConfiguration().isPresent())
                    return b.runConfiguration().get().name.equals(lastRunConfig);
                return false;
            }).findAny();
            e.ifPresent(b -> controller.runConfigurationPicker.setValue(b));
        }

        // When a new selection happens
        controller.runConfigurationPicker.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (oldValue == newValue || newValue == null)
                return;
            newValue.button().fire();
            if (newValue.runConfiguration().isEmpty())
                Platform.runLater(() -> controller.runConfigurationPicker.setValue(oldValue));
            else
                HUPPAAL.preferences.put(RunConfigurationPreferencesKeys.CurrentlySelected, newValue.runConfiguration().get().name);
            initializeRunConfigExecuteButton();
        });

        initializeRunConfigExecuteButton();
    }

    private List<RunConfiguration> parseRunConfigurationsClearPreferencesIfFails(String json) {
        try {
            return new Gson().fromJson(json, RunConfiguration.listTypeToken);
        } catch (Exception e) { // TODO: RunConfigurations should be saved in the project files, not in preferences!
            System.err.println("Could not parse RunConfigurations. Will clear the list, reason: " + e.getMessage());
            HUPPAAL.preferences.put(RunConfigurationPreferencesKeys.ConfigurationsList, "[]");
            return new ArrayList<>();
        }
    }

    Stage runconfigWindow;
    RunConfigurationEditorPresentation runConfigurationEditorPresentation;
    private RunConfigurationButton generateRunConfigurationEditButton() {
        var btn = new JFXButton("Edit Configs...");
        btn.setOnAction(event -> {
            try {
                if(runconfigWindow == null) {
                    runconfigWindow = new Stage();
                    runconfigWindow.setTitle("Run Configuration Editor");
                    runConfigurationEditorPresentation = new RunConfigurationEditorPresentation(runconfigWindow);
                    runConfigurationEditorPresentation.setOnRunConfigsSaved(this::initializeRunConfigPicker);
                    runconfigWindow.setScene(new Scene(runConfigurationEditorPresentation));
                }
                runconfigWindow.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return new RunConfigurationButton(Optional.empty(), btn);
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
        JFXSnackbarLayout content = new JFXSnackbarLayout(message);
        controller.snackbar.enqueue(new JFXSnackbar.SnackbarEvent(content, new Duration(3000)));
    }

    public void showHelp() {
        controller.dialogContainer.setVisible(true);
        controller.dialog.show(controller.dialogContainer);
    }

    public void uppaalDriverUpdated(){
        //Reflect update in GUI, by resetting the GenerateUPPAALModelButton
        initializeGenerateUppaalModelButton();
    }
}
