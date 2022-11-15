package dk.cs.aau.huppaal.controllers;

import dk.cs.aau.huppaal.BuildConfig;
import dk.cs.aau.huppaal.Debug;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.*;
import dk.cs.aau.huppaal.backend.*;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.logging.LogLevel;
import dk.cs.aau.huppaal.presentations.*;
import dk.cs.aau.huppaal.runconfig.RunConfiguration;
import dk.cs.aau.huppaal.runconfig.RunConfigurationButton;
import dk.cs.aau.huppaal.utility.UndoRedoStack;
import dk.cs.aau.huppaal.utility.colors.Color;
import dk.cs.aau.huppaal.utility.colors.EnabledColor;
import dk.cs.aau.huppaal.utility.helpers.ArrayUtils;
import dk.cs.aau.huppaal.utility.helpers.SelectHelper;
import dk.cs.aau.huppaal.utility.helpers.ZoomHelper;
import dk.cs.aau.huppaal.utility.keyboard.Keybind;
import dk.cs.aau.huppaal.utility.keyboard.KeyboardTracker;
import dk.cs.aau.huppaal.utility.keyboard.NudgeDirection;
import dk.cs.aau.huppaal.utility.keyboard.Nudgeable;
import com.jfoenix.controls.*;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HUPPAALController implements Initializable {

    // Reachability analysis
    public static boolean reachabilityServiceEnabled = false;
    private static long reachabilityTime = Long.MAX_VALUE;
    private static ExecutorService reachabilityService;

    // View stuff
    public StackPane root;
    public QueryPanePresentation queryPane;
    public ProjectPanePresentation filePane;
    public StackPane toolbar;
    public Label queryPaneFillerElement;
    public Label filePaneFillerElement;
    public CanvasPresentation canvas;
    public StackPane dialogContainer;
    public JFXDialog dialog;
    public StackPane modalBar;
    public JFXTextField queryTextField;
    public JFXTextField commentTextField;
    public JFXRippler generateUppaalModel;
    public ComboBox<RunConfigurationButton> runConfigurationPicker;
    public JFXRippler runConfigurationExecuteButton;
    public JFXRippler colorSelected;
    public JFXRippler deleteSelected;
    public JFXRippler undo;
    public JFXRippler redo;
    public JFXRippler zoomIn;
    public JFXRippler zoomOut;
    public JFXRippler resetZoom;
    public JFXRippler zoomToFit;
    public ImageView logo;
    public JFXTabPane tabPane;
    public Tab errorsTab;
    public Tab warningsTab;
    public Rectangle tabPaneResizeElement;
    public StackPane tabPaneContainer;
    public final Transition expandMessagesContainer = new Transition() {
        {
            setInterpolator(Interpolator.SPLINE(0.645, 0.045, 0.355, 1));
            setCycleDuration(Duration.millis(200));
        }

        @Override
        protected void interpolate(final double frac) {
            tabPaneContainer.setMaxHeight(35 + frac * (300 - 35));
        }
    };
    public Rectangle bottomFillerElement;
    public JFXRippler collapseMessages;
    public FontIcon collapseMessagesIcon;
    public ScrollPane errorsScrollPane;
    public VBox errorsList;
    public ScrollPane warningsScrollPane;
    public VBox warningsList;
    public Tab backendErrorsTab;
    public ScrollPane backendErrorsScrollPane;
    public VBox backendErrorsList;

    // The program top menu
    public MenuBar menuBar;
    public MenuItem menuBarViewFilePanel;
    public MenuItem menuBarViewQueryPanel;
    public MenuItem menuBarPreferencesUppaalLocation;
    public MenuItem menuBarFileNew;
    public MenuItem menuBarFileOpenProject;
    public MenuItem menuBarFileSave;
    public MenuItem menuBarFileSaveAs;
    public MenuItem menuBarFileExport;
    public MenuItem menuBarFileExportAsXML;
    public MenuItem menuBarHelpHelp;
    public MenuItem menuBarEditBalance;
    public MenuItem menuBarProjectExecuteRunConfigMenuItem;
    public MenuItem menuBarProjectEditConfigs;

    public JFXSnackbar snackbar;
    public HBox statusBar;
    public Label statusLabel;
    public Label versionLabel;
    public Label queryLabel;
    public HBox queryStatusContainer;

    public StackPane queryDialogContainer;
    public JFXDialog queryDialog;
    public Text queryTextResult;
    public Text queryTextQuery;

    private static JFXDialog _queryDialog;
    private static Text _queryTextResult;
    private static Text _queryTextQuery;
    public FontIcon runConfigurationExecuteButtonIcon;
    public LogTabPresentation infoLog, warnLog, errLog;
    public Tab infoLogTab, warnLogTab, errLogTab;
    private double tabPanePreviousY = 0;
    private boolean shouldISkipOpeningTheMessagesContainer = true;

    public static void runReachabilityAnalysis() {
        if (!reachabilityServiceEnabled) return;

        reachabilityTime = System.currentTimeMillis() + 500;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        dialog.setDialogContainer(dialogContainer);
        dialogContainer.opacityProperty().bind(dialog.getChildren().get(0).scaleXProperty());
        dialog.setOnDialogClosed(event -> dialogContainer.setVisible(false));

        _queryDialog = queryDialog;
        _queryTextResult = queryTextResult;
        _queryTextQuery = queryTextQuery;
        queryDialog.setDialogContainer(queryDialogContainer);
        queryDialogContainer.opacityProperty().bind(queryDialog.getChildren().get(0).scaleXProperty());
        queryDialog.setOnDialogClosed(event -> {
            queryDialogContainer.setVisible(false);
            queryDialogContainer.setMouseTransparent(true);
        });
        queryDialog.setOnDialogOpened(event -> {
            queryDialogContainer.setVisible(true);
            queryDialogContainer.setMouseTransparent(false);
        });

        // Keybind for nudging the selected elements
        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_UP, new Keybind(new KeyCodeCombination(KeyCode.UP), (event) -> {
            event.consume();
            nudgeSelected(NudgeDirection.UP);
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_DOWN, new Keybind(new KeyCodeCombination(KeyCode.DOWN), (event) -> {
            event.consume();
            nudgeSelected(NudgeDirection.DOWN);
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_LEFT, new Keybind(new KeyCodeCombination(KeyCode.LEFT), (event) -> {
            event.consume();
            nudgeSelected(NudgeDirection.LEFT);
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_RIGHT, new Keybind(new KeyCodeCombination(KeyCode.RIGHT), (event) -> {
            event.consume();
            nudgeSelected(NudgeDirection.RIGHT);
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.DESELECT, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), (event) -> {
            SelectHelper.clearSelectedElements();
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_W, new Keybind(new KeyCodeCombination(KeyCode.W), () -> nudgeSelected(NudgeDirection.UP)));
        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_A, new Keybind(new KeyCodeCombination(KeyCode.A), () -> nudgeSelected(NudgeDirection.LEFT)));
        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_S, new Keybind(new KeyCodeCombination(KeyCode.S), () -> nudgeSelected(NudgeDirection.DOWN)));
        KeyboardTracker.registerKeybind(KeyboardTracker.NUDGE_D, new Keybind(new KeyCodeCombination(KeyCode.D), () -> nudgeSelected(NudgeDirection.RIGHT)));

        // Keybind for deleting the selected elements
        KeyboardTracker.registerKeybind(KeyboardTracker.DELETE_SELECTED, new Keybind(new KeyCodeCombination(KeyCode.DELETE), this::deleteSelectedClicked));

        // Keybinds for coloring the selected elements
        EnabledColor.enabledColors.forEach(enabledColor -> {
            KeyboardTracker.registerKeybind(KeyboardTracker.COLOR_SELECTED + "_" + enabledColor.keyCode.getName(), new Keybind(new KeyCodeCombination(enabledColor.keyCode), () -> {
                var previousColor = new ArrayList<Pair<SelectHelper.ItemSelectable, EnabledColor>>();

                SelectHelper.getSelectedElements().forEach(selectable -> {
                    previousColor.add(new Pair<>(selectable, new EnabledColor(selectable.getColor(), selectable.getColorIntensity())));
                });

                UndoRedoStack.push(() -> { // Perform
                    SelectHelper.getSelectedElements().forEach(selectable -> {
                        selectable.color(enabledColor.color, enabledColor.intensity);
                    });
                }, () -> { // Undo
                    previousColor.forEach(selectableEnabledColorPair -> {
                        selectableEnabledColorPair.getKey().color(selectableEnabledColorPair.getValue().color, selectableEnabledColorPair.getValue().intensity);
                    });
                }, String.format("Changed the color of %d elements to %s", previousColor.size(), enabledColor.color.name()), "color-lens");

                SelectHelper.clearSelectedElements();
            }));
        });

        final BooleanProperty hasChanged = new SimpleBooleanProperty(false);

        HUPPAAL.getProject().getComponents().addListener(new ListChangeListener<Component>() {
            @Override
            public void onChanged(final Change<? extends Component> c) {
                if (!hasChanged.get()) {
                    CanvasController.setActiveComponent(HUPPAAL.getProject().getComponents().get(0));
                    hasChanged.set(true);
                }

                if(HUPPAAL.serializationDone && HUPPAAL.getProject().getComponents().size() - 1 == 0 && HUPPAAL.getProject().getMainComponent() == null) {
                    c.next();
                    c.getAddedSubList().get(0).setIsMain(true);
                }

            }
        });

        initializeTabPane();
        initializeStatusBar();
        initializeMessages();
        initializeMenuBar();
        initializeNoMainComponentError();
        initializeUppaalFileNotFoundWarning();
        initializeGenerateUppaalButton();
        initializeLogTabs();

        ZoomHelper.setCanvas(canvas);

        initializeNotificationJumpTransition();
        initializeLogTabNotifications();
    }

    private void initializeGenerateUppaalButton() {
        var uppaalDriver = UPPAALDriverManager.getInstance();
        if(uppaalDriver instanceof DummyUPPAALDriver)
            JFXTooltip.install(generateUppaalModel, new JFXTooltip("The UPPAAL server file does not exist"));
        else
            initializeReachabilityAnalysisThread();
    }

    private ScaleTransition infoIconJumpTransition, warnIconJumpTransition, errorIconJumpTransition;

    private void initializeNotificationJumpTransition() {
        infoIconJumpTransition = createNotificationJumpTransition();
        infoIconJumpTransition.setNode(infoLogTab.getGraphic());
        warnIconJumpTransition = createNotificationJumpTransition();
        warnIconJumpTransition.setNode(warnLogTab.getGraphic());
        errorIconJumpTransition = createNotificationJumpTransition();
        errorIconJumpTransition.setNode(errLogTab.getGraphic());
    }

    private ScaleTransition createNotificationJumpTransition() {
        var notificationJumpTransition = new ScaleTransition();
        notificationJumpTransition.interpolatorProperty().set(Interpolator.SPLINE(.87, .13, .62, .32));
        notificationJumpTransition.setDuration(new Duration(200));
        notificationJumpTransition.setFromX(1);
        notificationJumpTransition.setFromY(1);
        notificationJumpTransition.setByX(1.15);
        notificationJumpTransition.setByY(1.15);
        notificationJumpTransition.setAutoReverse(true);
        notificationJumpTransition.setCycleCount(2);
        return notificationJumpTransition;
    }

    private void initializeLogTabNotifications() {
        Log.addOnLogAddedListener(log -> {
            var selectedTab = Optional.ofNullable(tabPane.getSelectionModel().selectedItemProperty().get());
            Optional<Tab> tabToChange = switch (log.level()) {
                case Information -> Optional.of(infoLogTab);
                case Warning -> Optional.of(warnLogTab);
                case Error -> Optional.of(errLogTab);
                default -> Optional.empty();
            };
            if(tabToChange.isEmpty())
                return;
            if(selectedTab.isEmpty() || selectedTab.get() != tabToChange.get()) {
                ((FontIcon) tabToChange.get().getGraphic()).setIconColor(Color.YELLOW.getColor(Color.Intensity.I800));
                switch (log.level()) {
                    case Information -> infoIconJumpTransition.play();
                    case Warning -> warnIconJumpTransition.play();
                    case Error -> errorIconJumpTransition.play();
                }
            }
        });
        tabPane.getSelectionModel().selectedItemProperty().addListener((e,o,n) -> {
            if(n == null)
                return;
            ((FontIcon) n.getGraphic()).setIconColor(javafx.scene.paint.Color.WHITE);
        });
    }

    private void initializeLogTabs() {
        infoLog.controller.level = LogLevel.Information;
        warnLog.controller.level = LogLevel.Warning;
        errLog.controller.level  = LogLevel.Error;
        infoLogTab.setGraphic(createLogTabIcon("gmi-info", javafx.scene.paint.Color.WHITE));
        warnLogTab.setGraphic(createLogTabIcon("gmi-warning", javafx.scene.paint.Color.WHITE));
        errLogTab.setGraphic(createLogTabIcon("gmi-error", javafx.scene.paint.Color.WHITE));
    }

    private FontIcon createLogTabIcon(String iconName, javafx.scene.paint.Color color) {
        var i = new FontIcon(iconName);
        i.setIconColor(color);
        return i;
    }

    private void initializeReachabilityAnalysisThread() {
        new Thread(() -> {
            while (true) {

                // Wait for the reachability (the last time we changed the model) becomes smaller than the current time with a 5 second delay
                while (reachabilityTime > System.currentTimeMillis() - 5000) {
                    try {
                        Thread.sleep(2000);
                        Debug.backgroundThreads.removeIf(thread -> !thread.isAlive());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // We are now performing the analysis. Do not do another analysis before another change is introduced
                reachabilityTime = Long.MAX_VALUE;

                // Cancel any ongoing analysis
                if (reachabilityService != null) {
                    reachabilityService.shutdownNow();
                }

                // Start new analysis
                reachabilityService = Executors.newFixedThreadPool(10);

                while (Debug.backgroundThreads.size() > 0) {
                    final Thread thread = Debug.backgroundThreads.get(0);
                    thread.interrupt();
                    Debug.removeThread(thread);
                }

                try {
                    // Make sure that the model is generated
                    UPPAALDriverManager.getInstance().buildHUPPAALDocument();

                    HUPPAAL.getProject().getQueries().forEach(query -> {
                        if (query.isPeriodic()) query.run();
                    });

                    // List of threads to start
                    List<Thread> threads = new ArrayList<>();

                    // Submit all background reachability queries
                    HUPPAAL.getProject().getComponents().forEach(component -> {
                        // Check if we should consider this component
                        if (!component.isIncludeInPeriodicCheck()) {
                            component.getLocationsWithInitialAndFinal().forEach(location -> location.setReachability(Location.Reachability.EXCLUDED));
                        } else {
                            component.getLocationsWithInitialAndFinal().forEach(location -> {
                                final String locationReachableQuery = UPPAALDriverManager.getInstance().getLocationReachableQuery(location, component);
                                final Thread verifyThread = UPPAALDriverManager.getInstance().runQuery(
                                        locationReachableQuery,
                                        (result -> {
                                            if (result) {
                                                location.setReachability(Location.Reachability.REACHABLE);
                                            } else {
                                                location.setReachability(Location.Reachability.UNREACHABLE);
                                            }
                                            Debug.removeThread(Thread.currentThread());
                                        }),
                                        (e) -> {
                                            location.setReachability(Location.Reachability.UNKNOWN);
                                            Debug.removeThread(Thread.currentThread());
                                        },
                                        2000
                                );

                                verifyThread.setName(locationReachableQuery + " (" + verifyThread.getName() + ")");
                                Debug.addThread(verifyThread);
                                threads.add(verifyThread);
                            });
                        }
                    });

                    threads.forEach((verifyThread) -> reachabilityService.submit(verifyThread::start));

                } catch (final BackendException e) {
                    // Something went wrong with creating the document
                    Log.addError(e.getMessage());
                    e.printStackTrace();
                } catch (final Exception e) {
                    Log.addError(e.getMessage());
                }
            }
        }).start();
    }

    private void initializeStatusBar() {
        statusBar.setBackground(new Background(new BackgroundFill(
                Color.GREY_BLUE.getColor(Color.Intensity.I800),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        versionLabel.setTextFill(Color.GREY_BLUE.getColor(Color.Intensity.I50));
        versionLabel.setText(BuildConfig.NAME+" v"+BuildConfig.VERSION+"+"+BuildConfig.COMMIT_SHA_SHORT);
        versionLabel.setOpacity(0.5);

        statusLabel.setTextFill(Color.GREY_BLUE.getColor(Color.Intensity.I50));
        statusLabel.textProperty().bind(HUPPAAL.projectDirectory);
        statusLabel.setOpacity(0.5);

        queryLabel.setTextFill(Color.GREY_BLUE.getColor(Color.Intensity.I50));
        queryLabel.setOpacity(0.5);

        Debug.backgroundThreads.addListener((ListChangeListener<Thread>) c -> {
            while (c.next()) {
                Platform.runLater(() -> {
                    if(Debug.backgroundThreads.size() == 0) {
                        queryStatusContainer.setOpacity(0);
                    } else {
                        queryStatusContainer.setOpacity(1);
                        queryLabel.setText(Debug.backgroundThreads.size() + " background queries running");
                    }
                });
            }
        });
    }

    private void initializeNoMainComponentError() {
        var noMainComponentErrorMessage = new CodeAnalysis.Message("No main component specified", CodeAnalysis.MessageType.ERROR);
        HUPPAAL.getProject().mainComponentProperty().addListener((obs, oldMain, newMain) -> {
            if(newMain == null) {
                CodeAnalysis.addMessage(null, noMainComponentErrorMessage);
            } else {
                HUPPAALController.runReachabilityAnalysis();
                ComponentController.setLastChanged();
                CodeAnalysis.removeMessage(null, noMainComponentErrorMessage);
            }
        });
    }

    private void initializeUppaalFileNotFoundWarning() {
        var uppaalNotFoundMessage = new CodeAnalysis.Message("Please set the UPPAAL server location through the 'Preferences' tab.\n" +
                "Make sure to have UPPAAL installed. This can be done at [uppaal.org](generic:https://www.uppaal.org)", CodeAnalysis.MessageType.WARNING);
        UPPAALDriverManager.getUppalFilePathProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals("dummy"))
                CodeAnalysis.addMessage(null, uppaalNotFoundMessage);
            else
                CodeAnalysis.removeMessage(null, uppaalNotFoundMessage);
        });
    }

    private void initializeMenuBar() {
        menuBarFileNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        menuBarFileNew.setOnAction(event -> {
            //Set the project directory to the temporary location to handle save correctly later (will not save anything to the directory)
            HUPPAAL.projectDirectory.set(HUPPAAL.temporaryProjectDirectory);

            //Clear the errors, warning, and loaded project
            CodeAnalysis.getBackendErrors().clear();
            CodeAnalysis.getErrors().clear();
            CodeAnalysis.getWarnings().clear();
            HUPPAAL.getProject().getQueries().clear();
            HUPPAAL.getProject().getComponents().clear();
            HUPPAAL.getProject().setMainComponent(null);
        });

        menuBarFileOpenProject.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        menuBarFileOpenProject.setOnAction(event -> {
            // Dialog title
            var projectPicker = new DirectoryChooser();
            projectPicker.setTitle("Open project");

            // The initial location for the file choosing dialog
            var jarDir = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParentFile();

            // If the file does not exist, we must be running it from a development environment, use a default location
            if(jarDir.exists())
                projectPicker.setInitialDirectory(jarDir);

            try {
                // Prompt the user to find a file (will halt the UI thread)
                var file = projectPicker.showDialog(root.getScene().getWindow());
                if(file == null)
                    return;
                HUPPAAL.projectDirectory.set(file.getAbsolutePath());
                HUPPAAL.initializeProjectFolder();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });

        menuBarFileSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        menuBarFileSave.setOnAction(event -> {
            //Check if the current project is an unsaved new project
            if(HUPPAAL.projectDirectory.getValue().equals(HUPPAAL.temporaryProjectDirectory))
                this.menuBarFileSaveAs.fire();
            else
                HUPPAAL.save();
        });

        menuBarFileSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        menuBarFileSaveAs.setOnAction(event -> {
            // Dialog title
            var filePicker = new FileChooser();
            filePicker.setTitle("Save project as");
            filePicker.setInitialFileName("New project");

            //Open dialog at project location if it exists and is not an unsaved new project (added to avoid exception if the current project directory has been deleted during execution)
            var currentProject = new File(HUPPAAL.projectDirectory.getValue());
            if(currentProject.exists() && !HUPPAAL.projectDirectory.getValue().equals(HUPPAAL.temporaryProjectDirectory)){
                filePicker.setInitialDirectory(currentProject);
            }

            // Prompt the user to find a location and give a project name (will halt the UI thread)
            var file = filePicker.showSaveDialog(root.getScene().getWindow());
            if(file != null) {
                HUPPAAL.projectDirectory.set(file.getAbsolutePath());
                HUPPAAL.save();
            }
        });

        menuBarFileExport.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
        menuBarFileExport.setOnAction(event -> {
            try {
                //Todo: When additional files can be handled, the project should be exported to the folder usign: HUPPAAL.projectDirectory.getValue() + File.separator + HUPPAAL.projectDirectory.getValue().substring(HUPPAAL.projectDirectory.getValue().lastIndexOf(File.separator) + 1) + ".xml"
                var exportLocation = HUPPAAL.projectDirectory.getValue() + ".xml";
                UPPAALDriverManager.getInstance().saveUPPAALModel(exportLocation);
                HUPPAAL.showToast("Project exported to: " + exportLocation);
            } catch (Exception e) {
                HUPPAAL.showToast("Unable to export the project: " + e.getMessage());
                e.printStackTrace();
            }

        });

        menuBarFileExportAsXML.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        menuBarFileExportAsXML.setOnAction(event -> {
            // Dialog title
            var locationPicker = new FileChooser();
            locationPicker.setTitle("Export as XML");
            locationPicker.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML file", "*.xml"));

            // If the file does not exist, we must be running it from a development environment, use a default location
            locationPicker.setInitialDirectory(new File(HUPPAAL.projectDirectory.getValue()));

            try {
                // Prompt the user to find a file (will halt the UI thread)
                var file = locationPicker.showSaveDialog(root.getScene().getWindow());
                if(file == null)
                    return;
                UPPAALDriverManager.getInstance().saveUPPAALModel(file.getAbsolutePath());
                HUPPAAL.showToast("Project exported!");
            } catch (Exception e) {
                HUPPAAL.showToast("Unable to export the project: " + e.getMessage());
                e.printStackTrace();
            }
        });

        menuBarPreferencesUppaalLocation.setOnAction(event -> {
            var filePicker = new FileChooser();
            filePicker.setTitle("Choose UPPAAL server file");
            var uppaalFile = new File(UPPAALDriverManager.getUppaalFilePath()).getAbsoluteFile().getParentFile();
            if(uppaalFile.exists())
                filePicker.setInitialDirectory(uppaalFile);
            var file = filePicker.showOpenDialog(root.getScene().getWindow());
            if(file != null)
                UPPAALDriverManager.setUppaalFilePath(file.getAbsolutePath());
        });

        menuBarViewFilePanel.getGraphic().setOpacity(1);
        menuBarViewFilePanel.setAccelerator(new KeyCodeCombination(KeyCode.F));
        menuBarViewFilePanel.setOnAction(event -> {
            var isOpen = HUPPAAL.toggleFilePane();
            menuBarViewFilePanel.getGraphic().opacityProperty().bind(new When(isOpen).then(1).otherwise(0));
        });

        menuBarViewQueryPanel.getGraphic().setOpacity(0);
        menuBarViewQueryPanel.setAccelerator(new KeyCodeCombination(KeyCode.Q));
        menuBarViewQueryPanel.setOnAction(event -> {
            var isOpen = HUPPAAL.toggleQueryPane();
            menuBarViewQueryPanel.getGraphic().opacityProperty().bind(new When(isOpen).then(1).otherwise(0));
        });

        menuBarHelpHelp.setOnAction(event -> HUPPAAL.showHelp());

        menuBarEditBalance.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN));
        menuBarEditBalance.setOnAction(event -> {
            // Map to store the previous identifiers (to undo/redo)
            var previousIdentifiers = new HashMap<Location, String>();
            UndoRedoStack.push(() -> { // Perform
                // Set the counter used to generate the identifiers
                Location.resetHiddenID();

                // A list of components we have not ordered yet
                var missingComponents = new ArrayList<>(HUPPAAL.getProject().getComponents());

                // List to iterate through the components
                var subComponentsToCheck = new ArrayList<SubComponent>();

                // Consumer to reset the location identifier
                var resetLocation = (Consumer<Location>)(location -> {
                    previousIdentifiers.put(location, location.getId());
                    location.resetId();
                });

                // Consumer to reset the location identifiers in a given component
                var resetLocationsInComponent = (Consumer<Component>)(component) -> {
                    // Check if we already balanced this component
                    if(!missingComponents.contains(component)) return;

                    // Set the identifier for the initial location
                    resetLocation.accept(component.getInitialLocation());

                    // Set the identifiers for the rest of the locations
                    component.getLocations().forEach(resetLocation);

                    // Set the identifier for the final location
                    resetLocation.accept(component.getFinalLocation());

                    // We are now finished with this component, remove it from the list and add subcomponents to the checking list
                    missingComponents.remove(component);
                    component.getSubComponents().forEach(subComponentsToCheck::add);
                };

                // Balance the identifiers in the main component
                resetLocationsInComponent.accept(HUPPAAL.getProject().getMainComponent());

                // While we are missing subcomponents, balance them!
                while(!subComponentsToCheck.isEmpty()) {
                    // Pick the 0th element which we will now check
                    final SubComponent subComponent = subComponentsToCheck.get(0);

                    // Reset the location identifiers in the given subcomponent's component
                    resetLocationsInComponent.accept(subComponent.getComponent());

                    // Remove the subcomponent from the list
                    subComponentsToCheck.remove(0);
                }

                // If we still need to balance some component (they might not be used) then do it now
                while(!missingComponents.isEmpty())
                    resetLocationsInComponent.accept(missingComponents.get(0));
            }, () -> { // Undo
                previousIdentifiers.forEach(Location::setId);
            }, "Balanced location identifiers", "shuffle");
        });

        // Project
        menuBarProjectExecuteRunConfigMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        menuBarProjectExecuteRunConfigMenuItem.setOnAction(event -> executeSelectedRunConfiguration());
        menuBarProjectEditConfigs.setAccelerator(new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN));
        menuBarProjectEditConfigs.setOnAction(event -> openRunConfigurationEditor());
    }

    private void initializeMessages() {
        var componentMessageCollectionPresentationMapForErrors = new HashMap<Component, MessageCollectionPresentation>();
        var componentMessageCollectionPresentationMapForWarnings = new HashMap<Component, MessageCollectionPresentation>();

        var addComponent = (Consumer<Component>)(component) -> {
            final MessageCollectionPresentation messageCollectionPresentationErrors = new MessageCollectionPresentation(component, CodeAnalysis.getErrors(component));
            componentMessageCollectionPresentationMapForErrors.put(component, messageCollectionPresentationErrors);
            errorsList.getChildren().add(messageCollectionPresentationErrors);

            final Runnable addIfErrors = () -> {
                if (CodeAnalysis.getErrors(component).size() == 0)
                    errorsList.getChildren().remove(messageCollectionPresentationErrors);
                else if (!errorsList.getChildren().contains(messageCollectionPresentationErrors))
                    errorsList.getChildren().add(messageCollectionPresentationErrors);
            };

            addIfErrors.run();
            CodeAnalysis.getErrors(component).addListener((ListChangeListener<CodeAnalysis.Message>) c -> {
                while (c.next())
                    addIfErrors.run();
            });

            var messageCollectionPresentationWarnings = new MessageCollectionPresentation(component, CodeAnalysis.getWarnings(component));
            componentMessageCollectionPresentationMapForWarnings.put(component, messageCollectionPresentationWarnings);
            warningsList.getChildren().add(messageCollectionPresentationWarnings);

            var addIfWarnings = (Runnable) () -> {
                if (CodeAnalysis.getWarnings(component).size() == 0)
                    warningsList.getChildren().remove(messageCollectionPresentationWarnings);
                else if (!warningsList.getChildren().contains(messageCollectionPresentationWarnings))
                    warningsList.getChildren().add(messageCollectionPresentationWarnings);
            };

            addIfWarnings.run();
            CodeAnalysis.getWarnings(component).addListener((ListChangeListener<CodeAnalysis.Message>) c -> {
                while (c.next())
                    addIfWarnings.run();
            });
        };

        // Add error that is project wide but not a backend error
        addComponent.accept(null);

        HUPPAAL.getProject().getComponents().forEach(addComponent);
        HUPPAAL.getProject().getComponents().addListener((ListChangeListener<Component>) c -> {
            while (c.next()) {
                c.getAddedSubList().forEach(addComponent);
                c.getRemoved().forEach(component -> {
                    errorsList.getChildren().remove(componentMessageCollectionPresentationMapForErrors.get(component));
                    componentMessageCollectionPresentationMapForErrors.remove(component);

                    warningsList.getChildren().remove(componentMessageCollectionPresentationMapForWarnings.get(component));
                    componentMessageCollectionPresentationMapForWarnings.remove(component);
                });
            }
        });

        var messageMessagePresentationHashMap = new HashMap<CodeAnalysis.Message, MessagePresentation>();
        CodeAnalysis.getBackendErrors().addListener((ListChangeListener<CodeAnalysis.Message>) c -> {
            while (c.next()) {
                c.getAddedSubList().forEach(addedMessage -> {
                    var messagePresentation = new MessagePresentation(addedMessage);
                    backendErrorsList.getChildren().add(messagePresentation);
                    messageMessagePresentationHashMap.put(addedMessage, messagePresentation);
                });

                c.getRemoved().forEach(removedMessage -> {
                    backendErrorsList.getChildren().remove(messageMessagePresentationHashMap.get(removedMessage));
                    messageMessagePresentationHashMap.remove(removedMessage);
                });
            }
        });
    }

    private void initializeTabPane() {
        bottomFillerElement.heightProperty().bind(tabPaneContainer.maxHeightProperty());
        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelected, newSelected) -> {
            if (newSelected.intValue() < 0 || tabPaneContainer.getMaxHeight() > 35)
                return;

            if (shouldISkipOpeningTheMessagesContainer) {
                tabPane.getSelectionModel().clearSelection();
                shouldISkipOpeningTheMessagesContainer = false;
            } else {
                expandMessagesContainer.play();
            }
        });

        tabPane.getSelectionModel().clearSelection();
        tabPane.setTabMinHeight(35);
        tabPane.setTabMaxHeight(35);
    }

    @FXML
    private void tabPaneResizeElementPressed(final MouseEvent event) {
        tabPanePreviousY = event.getScreenY();
    }

    @FXML
    private void tabPaneResizeElementDragged(final MouseEvent event) {
        var mouseY = event.getScreenY();
        var newHeight = tabPaneContainer.getMaxHeight() - (mouseY - tabPanePreviousY);
        newHeight = Math.max(35, newHeight);

        tabPaneContainer.setMaxHeight(newHeight);
        tabPanePreviousY = mouseY;
    }

    public void expandMessagesIfNotExpanded() {
        if (tabPaneContainer.getMaxHeight() <= 35)
            expandMessagesContainer.play();
    }

    private Transition collapseTransition() {
        return new Transition() {
            private final double height = tabPaneContainer.getMaxHeight();
            {
                setInterpolator(Interpolator.SPLINE(0.645, 0.045, 0.355, 1));
                setCycleDuration(Duration.millis(200));
            }

            @Override
            protected void interpolate(final double frac) {
                tabPaneContainer.setMaxHeight(((height - 35) * (1 - frac)) + 35);
            }
        };
    }

    public void collapseMessagesIfNotCollapsed() {
        var collapse = collapseTransition();
        if (tabPaneContainer.getMaxHeight() > 35)
            collapse.play();
    }

    @FXML
    public void collapseMessagesClicked() {
        var collapse = collapseTransition();
        if (tabPaneContainer.getMaxHeight() > 35)
            collapse.play();
        else
            expandMessagesContainer.play();
    }

    @FXML
    private void generateUppaalModelClicked() {
        var mainComponent = HUPPAAL.getProject().getMainComponent();
        if (mainComponent == null) {
            HUPPAAL.showToast("No main component");
            return;
        }

        try {
            UPPAALDriverManager.getInstance().generateDebugUPPAALModel();
            HUPPAAL.showToast("UPPAAL debug file stored");
        } catch (final Exception e) {
            HUPPAAL.showToast("UPPAAL debug file not stored: " + e.getMessage());
        }
    }

    private void nudgeSelected(final NudgeDirection direction) {
        var selectedElements = SelectHelper.getSelectedElements();
        var nudgedElements = new ArrayList<Nudgeable>();
        UndoRedoStack.push(() -> { // Perform
                    boolean[] foundUnNudgableElement = {false};
                    selectedElements.forEach(selectable -> {
                        if (selectable instanceof Nudgeable nudgeable) {
                            if (nudgeable.nudge(direction))
                                nudgedElements.add(nudgeable);
                            else
                                foundUnNudgableElement[0] = true;
                        }
                    });

                    // If some one was not able to nudge disallow the current nudge and remove from the undo stack
                    if(foundUnNudgableElement[0]){
                        nudgedElements.forEach(nudgedElement -> nudgedElement.nudge(direction.reverse()));
                        UndoRedoStack.forgetLast();
                    }

                }, () -> { // Undo
                    nudgedElements.forEach(nudgedElement -> nudgedElement.nudge(direction.reverse()));
                },
                "Nudge " + selectedElements + " in direction: " + direction,
                "open-with");
    }

    @FXML
    public void executeSelectedRunConfiguration() {
        var c = runConfigurationPicker.getSelectionModel().getSelectedItem();
        if(c == null || c.runConfiguration().isEmpty()) {
            HUPPAAL.showToast("No run configuration is selected");
            return;
        }
        executeRunConfiguration(c.runConfiguration().get());
    }

    public Stage runConfigEditorWindow;
    public RunConfigurationEditorPresentation runConfigurationEditorPresentation;
    public void openRunConfigurationEditor() {
        if(runConfigEditorWindow == null) {
            runConfigEditorWindow = new Stage();
            runConfigEditorWindow.setTitle("Run Configuration Editor");
            runConfigurationEditorPresentation = new RunConfigurationEditorPresentation(runConfigEditorWindow);
            runConfigEditorWindow.setScene(new Scene(runConfigurationEditorPresentation));
        }
        try {
            runConfigEditorWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                runConfigurationExecuteButtonIcon.setIconLiteral("gmi-stop");
                runConfigurationExecuteButtonIcon.setIconColor(Color.RED.getColor(Color.Intensity.I300));
                String s;
                while ((s = stdi.readLine()) != null)
                    Log.addInfo(config.name, s);
                while ((s = stde.readLine()) != null)
                    Log.addError(config.name, s);
                var exitCode = proc.waitFor();
                var msg = config.name + " exited with code " + exitCode;
                Log.addInfo(config.name, msg);

                HUPPAAL.showToast(msg);
            } catch (Exception e) {
                Log.addError(e.getMessage());
                HUPPAAL.showToast(e.getMessage());
                e.printStackTrace();
            } finally {
                runConfigurationExecuteButtonIcon.setIconLiteral("gmi-play-arrow");
                runConfigurationExecuteButtonIcon.setIconColor(javafx.scene.paint.Color.WHITE);
            }
        }).start();
    }

    @FXML
    private void deleteSelectedClicked() {
        if (SelectHelper.getSelectedElements().size() == 0)
            return;

        // Run through the selected elements and look for something that we can delete
        SelectHelper.getSelectedElements().forEach(selectable -> {
            if (selectable instanceof LocationController) {
                var component = ((LocationController) selectable).getComponent();
                var location = ((LocationController) selectable).getLocation();
                var initialLocation = component.getInitialLocation();
                var finalLocation = component.getFinalLocation();

                if (location.getId().equals(initialLocation.getId()) || location.getId().equals(finalLocation.getId())) {
                    ((LocationPresentation) ((LocationController) selectable).root).shake();
                    return; // Do not delete initial or final locations
                }

                var relatedEdges = component.getRelatedEdges(location);
                UndoRedoStack.push(() -> { // Perform
                    // Remove the location
                    component.getLocations().remove(location);
                    relatedEdges.forEach(component::removeEdge);
                }, () -> { // Undo
                    // Re-all the location
                    component.getLocations().add(location);
                    relatedEdges.forEach(component::addEdge);

                }, String.format("Deleted %s", selectable.toString()), "delete");
            } else if (selectable instanceof EdgeController edgeController) {
                var component = edgeController.getComponent();
                var edge = edgeController.getEdge();
                UndoRedoStack.push(() -> { // Perform
                    component.removeEdge(edge);
                }, () -> { // Undo
                    component.addEdge(edge);
                }, String.format("Deleted %s", selectable), "delete");
            } else if (selectable instanceof JorkController jorkController) {
                var component = CanvasController.getActiveComponent();
                var jork = jorkController.getJork();
                var relatedEdges = component.getRelatedEdges(jork);
                UndoRedoStack.push(() -> { // Perform
                    component.getJorks().remove(jork);
                    relatedEdges.forEach(component::removeEdge);
                }, () -> { // Undo
                    component.getJorks().add(jork);
                    relatedEdges.forEach(component::addEdge);
                }, String.format("Deleted %s", selectable), "delete");
            } else if (selectable instanceof SubComponentController subComponentController) {
                var component = CanvasController.getActiveComponent();
                var subComponent = subComponentController.getSubComponent();
                var relatedEdges = component.getRelatedEdges(subComponent);
                UndoRedoStack.push(() -> { // Perform
                    component.getSubComponents().remove(subComponent);
                    relatedEdges.forEach(component::removeEdge);
                }, () -> { // Undo
                    component.getSubComponents().add(subComponent);
                    relatedEdges.forEach(component::addEdge);
                }, String.format("Deleted %s", selectable), "delete");
            } else if (selectable instanceof final NailController nailController) {
                var edge = nailController.getEdge();
                var component = nailController.getComponent();
                var nail = nailController.getNail();
                var index = edge.getNails().indexOf(nail);
                var restoreProperty = edge.getProperty(nail.getPropertyType());

                // If the last nail on a self loop for a location or join/fork delete the edge also
                var shouldDeleteEdgeAlso = edge.isSelfLoop() && edge.getNails().size() == 1 && edge.getSourceSubComponent() == null;

                // Create an undo redo description based, add extra comment if edge is also deleted
                var message =  String.format("Deleted %s", selectable);
                if(shouldDeleteEdgeAlso)
                    message += String.format("(Was last Nail on self loop edge --> %s also deleted)", edge);

                UndoRedoStack.push(
                        () -> {
                            edge.removeNail(nail);
                            edge.setProperty(nail.getPropertyType(), "");
                            if(shouldDeleteEdgeAlso)
                                component.removeEdge(edge);
                        },
                        () -> {
                            if(shouldDeleteEdgeAlso)
                                component.addEdge(edge);
                            edge.setProperty(nail.getPropertyType(), restoreProperty);
                            edge.insertNailAt(nail, index);
                        },
                        message,
                        "delete"
                );
            }
        });

        SelectHelper.clearSelectedElements();
    }

    @FXML
    private void undoClicked() {
        UndoRedoStack.undo();
    }

    @FXML
    private void redoClicked() {
        UndoRedoStack.redo();
    }

    @FXML
    private void zoomInClicked() {
        ZoomHelper.zoomIn();
    }

    @FXML
    private void zoomOutClicked() {
        ZoomHelper.zoomOut();
    }

    @FXML
    private void zoomToFitClicked() {
        ZoomHelper.zoomToFit();
    }

    @FXML
    private void resetZoomClicked() {
        ZoomHelper.resetZoom();
    }

    @FXML
    private void closeDialog() {
        dialog.close();
        queryDialog.close();
    }

    public static void openQueryDialog(final Query query, final String text) {
        if (text != null)
            _queryTextResult.setText(text);
        if (query != null)
            _queryTextQuery.setText(query.getQuery());
        _queryDialog.show();
    }
}
