package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.abstractions.Nail;
import SW9.backend.UPPAALDriver;
import SW9.code_analysis.CodeAnalysis;
import SW9.presentations.*;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.EnabledColor;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class HUPPAALController implements Initializable {

    public StackPane root;
    public QueryPanePresentation queryPane;
    public ProjectPanePresentation filePane;
    public StackPane toolbar;
    public MenuBar menuBar;
    public Label queryPaneFillerElement;
    public Label filePaneFillerElement;
    public CanvasPresentation canvas;
    public StackPane dialogContainer;
    public JFXDialog dialog;
    public StackPane modalBar;
    public JFXTextField queryTextField;
    public JFXTextField commentTextField;
    public JFXRippler generateUppaalModel;
    public JFXRippler colorSelected;
    public JFXRippler deleteSelected;
    public JFXRippler undo;
    public JFXRippler redo;
    public ImageView logo;
    public JFXTabPane tabPane;
    public Tab errorsTab;
    public Tab warningsTab;
    public Rectangle tabPaneResizeElement;
    public StackPane tabPaneContainer;
    private final Transition expandMessagesContainer = new Transition() {
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
    private double tabPanePreviousY = 0;
    private boolean shouldISkipOpeningTheMessagesContainer = true;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        // Keybind for toggling the query pane
        KeyboardTracker.registerKeybind(KeyboardTracker.TOGGLE_QUERY_PANE, new Keybind(new KeyCodeCombination(KeyCode.Q), () -> {
            ((HUPPAALPresentation) root).toggleQueryPane();
        }));

        // Keybind for toggling the file pane
        KeyboardTracker.registerKeybind(KeyboardTracker.TOGGLE_FILE_PANE, new Keybind(new KeyCodeCombination(KeyCode.F), () -> {
            ((HUPPAALPresentation) root).toggleFilePane();
        }));


        // TODO - This is debugging for messages
        KeyboardTracker.registerKeybind("DEBUGGING_WARNINGS", new Keybind(new KeyCodeCombination(KeyCode.W), () -> {
            CodeAnalysis.addMessage(CanvasController.getActiveComponent(), new CodeAnalysis.Message("A warning", CodeAnalysis.MessageType.WARNING));
        }));

        KeyboardTracker.registerKeybind("DEBUGGING_ERROR", new Keybind(new KeyCodeCombination(KeyCode.E), () -> {
            CodeAnalysis.addMessage(CanvasController.getActiveComponent(), new CodeAnalysis.Message("An error", CodeAnalysis.MessageType.ERROR));
        }));

        dialog.setDialogContainer(dialogContainer);
        dialogContainer.opacityProperty().bind(dialog.getChildren().get(0).scaleXProperty());
        dialog.setOnDialogClosed(event -> dialogContainer.setVisible(false));

        // Keybind for showing dialog // todo: remove this when done with testing
        KeyboardTracker.registerKeybind("DIALOG", new Keybind(new KeyCodeCombination(KeyCode.I), () -> {
            System.out.println(CodeAnalysis.getErrors(CanvasController.getActiveComponent()));
        }));

        // Keybind for deleting the selected elements
        KeyboardTracker.registerKeybind(KeyboardTracker.DELETE_SELECTED, new Keybind(new KeyCodeCombination(KeyCode.DELETE), this::deleteSelectedClicked));

        // Keybinds for coloring the selected elements
        EnabledColor.enabledColors.forEach(enabledColor -> {
            KeyboardTracker.registerKeybind(KeyboardTracker.COLOR_SELECTED + "_" + enabledColor.keyCode.getName(), new Keybind(new KeyCodeCombination(enabledColor.keyCode), () -> {
                final List<Pair<SelectHelper.ColorSelectable, EnabledColor>> previousColor = new ArrayList<>();

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
            }
        });

        initializeTabPane();
        initializeMessages();
    }

    private void initializeMessages() {
        final Map<Component, MessageCollectionPresentation> componentMessageCollectionPresentationMapForErrors = new HashMap<>();
        final Map<Component, MessageCollectionPresentation> componentMessageCollectionPresentationMapForWarnings = new HashMap<>();

        final Consumer<Component> addComponent = (component) -> {
            final MessageCollectionPresentation messageCollectionPresentationErrors = new MessageCollectionPresentation(component, CodeAnalysis.getErrors(component));
            componentMessageCollectionPresentationMapForErrors.put(component, messageCollectionPresentationErrors);
            errorsList.getChildren().add(messageCollectionPresentationErrors);

            final MessageCollectionPresentation messageCollectionPresentationWarnings = new MessageCollectionPresentation(component, CodeAnalysis.getWarnings(component));
            componentMessageCollectionPresentationMapForWarnings.put(component, messageCollectionPresentationWarnings);
            warningsList.getChildren().add(messageCollectionPresentationWarnings);
        };

        HUPPAAL.getProject().getComponents().forEach(addComponent);
        HUPPAAL.getProject().getComponents().addListener(new ListChangeListener<Component>() {
            @Override
            public void onChanged(final Change<? extends Component> c) {
                while (c.next()) {
                    c.getAddedSubList().forEach(addComponent::accept);

                    c.getRemoved().forEach(component -> {
                        errorsList.getChildren().remove(componentMessageCollectionPresentationMapForErrors.get(component));
                        componentMessageCollectionPresentationMapForErrors.remove(component);

                        warningsList.getChildren().remove(componentMessageCollectionPresentationMapForWarnings.get(component));
                        componentMessageCollectionPresentationMapForWarnings.remove(component);
                    });
                }
            }
        });
    }

    private void initializeTabPane() {
        bottomFillerElement.heightProperty().bind(tabPaneContainer.maxHeightProperty());

        tabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelected, newSelected) -> {
            if (newSelected.intValue() < 0 || tabPaneContainer.getMaxHeight() > 35) return;

            if (shouldISkipOpeningTheMessagesContainer) {
                tabPane.getSelectionModel().clearSelection();
                shouldISkipOpeningTheMessagesContainer = false;
            } else {
                expandMessagesContainer.play();
            }
        });

        tabPane.getSelectionModel().clearSelection();
    }

    @FXML
    private void tabPaneResizeElementPressed(final MouseEvent event) {
        tabPanePreviousY = event.getScreenY();
    }

    @FXML
    private void tabPaneResizeElementDragged(final MouseEvent event) {
        final double mouseY = event.getScreenY();
        double newHeight = tabPaneContainer.getMaxHeight() - (mouseY - tabPanePreviousY);
        newHeight = Math.max(35, newHeight);

        tabPaneContainer.setMaxHeight(newHeight);
        tabPanePreviousY = mouseY;
    }

    @FXML
    private void collapseMessagesClicked() {
        final Transition collapse = new Transition() {
            double height = tabPaneContainer.getMaxHeight();

            {
                setInterpolator(Interpolator.SPLINE(0.645, 0.045, 0.355, 1));
                setCycleDuration(Duration.millis(200));
            }

            @Override
            protected void interpolate(final double frac) {
                tabPaneContainer.setMaxHeight(((height - 35) * (1 - frac)) + 35);
            }
        };

        if (tabPaneContainer.getMaxHeight() > 35) {
            collapse.play();
        } else {
            expandMessagesContainer.play();
        }
    }

    @FXML
    private void generateUppaalModelClicked() {
        UPPAALDriver.verify("E<> true", // todo: consider creating an interface for generating the model instead of this query
                aBoolean -> {
                    // success
                    System.out.println("Generated UPPAAL file!");
                },
                e -> {
                    System.out.println("ERROR");
                },
                HUPPAAL.getProject().getComponents()
        );
    }

    @FXML
    private void deleteSelectedClicked() {
        if (SelectHelper.getSelectedElements().size() == 0) return;

        // Run through the selected elements and look for something that we can delete
        SelectHelper.getSelectedElements().forEach(selectable -> {
            if (selectable instanceof LocationController) {
                final Component component = ((LocationController) selectable).getComponent();
                final Location location = ((LocationController) selectable).getLocation();

                final Location initialLocation = component.getInitialLocation();
                final Location finalLocation = component.getFinalLocation();

                if (location.equals(initialLocation) || location.equals(finalLocation)) {
                    return; // Do not delete initial or final locations
                }

                final List<Edge> relatedEdges = component.getRelatedEdges(location);

                UndoRedoStack.push(() -> { // Perform
                    // Remove the location
                    component.getLocations().remove(location);
                    relatedEdges.forEach(component::removeEdge);
                }, () -> { // Undo
                    // Re-all the location
                    component.getLocations().add(location);
                    relatedEdges.forEach(component::addEdge);

                }, String.format("Deleted %s", selectable.toString()), "delete");
            } else if (selectable instanceof EdgeController) {
                final Component component = ((EdgeController) selectable).getComponent();
                final Edge edge = ((EdgeController) selectable).getEdge();

                UndoRedoStack.push(() -> { // Perform
                    // Remove the edge
                    component.getEdges().remove(edge);
                }, () -> { // Undo
                    // Re-all the edge
                    component.getEdges().add(edge);
                }, String.format("Deleted %s", selectable.toString()), "delete");
            } else if(selectable instanceof NailController) {
                final NailController nailController = (NailController) selectable;
                final Edge edge = nailController.getEdge();
                final Nail nail = nailController.getNail();
                final int index = edge.getNails().indexOf(nail);
                UndoRedoStack.push(
                        ()-> edge.removeNail(nail),
                        ()-> edge.insertNailAt(nail,index),
                        "Nail removed",
                        "add-circle"
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

}
