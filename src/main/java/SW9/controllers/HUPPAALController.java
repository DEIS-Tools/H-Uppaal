package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.abstractions.Nail;
import SW9.backend.UPPAALDriver;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.HUPPAALPresentation;
import SW9.presentations.ProjectPanePresentation;
import SW9.presentations.QueryPanePresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.EnabledColor;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HUPPAALController implements Initializable {

    public StackPane root;
    public BorderPane bottomStatusBar;
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
    public JFXRippler delete;
    public ImageView logo;

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

        dialog.setDialogContainer(dialogContainer);
        dialogContainer.opacityProperty().bind(dialog.getChildren().get(0).scaleXProperty());
        dialog.setOnDialogClosed(event -> dialogContainer.setVisible(false));

        // Keybind for showing dialog // todo: remove this when done with testing
        KeyboardTracker.registerKeybind("DIALOG", new Keybind(new KeyCodeCombination(KeyCode.I), () -> {
            dialogContainer.setVisible(true);
            dialog.show();
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
                final double previousX = location.getX();
                final double previousY = location.getY();

                final Location initialLocation = component.getInitialLocation();
                final Location finalLocation = component.getFinalLocation();

                if (location.equals(initialLocation) || location.equals(finalLocation)) {
                    return; // Do not delete initial or final locations
                }

                UndoRedoStack.push(() -> { // Perform
                    // Remove the location
                    component.getLocations().remove(location);
                }, () -> { // Undo
                    // Re-all the location
                    component.getLocations().add(location);

                    location.xProperty().unbind();
                    location.xProperty().set(previousX);

                    location.yProperty().unbind();
                    location.yProperty().set(previousY);
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

    @FXML
    private void deleteClicked() {
        // todo: add a confirmation dialog

        final Component activeComponent = CanvasController.getActiveComponent();

        if (activeComponent == null) return;

        UndoRedoStack.push(() -> { // Perform
            HUPPAAL.getProject().getComponents().remove(activeComponent);
        }, () -> { // Undo
            HUPPAAL.getProject().getComponents().add(activeComponent);
            CanvasController.setActiveComponent(activeComponent);
        }, String.format("Deleted component %s", activeComponent.getName()), "delete");

    }

}
