package SW9.controllers;

import SW9.NewMain;
import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.backend.UPPAALDriver;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.FilePanePresentation;
import SW9.presentations.HUPPAALPresentation;
import SW9.presentations.QueryPanePresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.EnabledColor;
import SW9.utility.helpers.SelectHelperNew;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
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
    public FilePanePresentation filePane;
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
                final List<Pair<SelectHelperNew.ColorSelectable, EnabledColor>> previousColor = new ArrayList<>();

                SelectHelperNew.getSelectedElements().forEach(selectable -> {
                    previousColor.add(new Pair<>(selectable, new EnabledColor(selectable.getColor(), selectable.getColorIntensity())));
                });

                UndoRedoStack.push(() -> { // Perform
                    SelectHelperNew.getSelectedElements().forEach(selectable -> {
                        selectable.color(enabledColor.color, enabledColor.intensity);
                    });
                }, () -> { // Undo
                    previousColor.forEach(selectableEnabledColorPair -> {
                        selectableEnabledColorPair.getKey().color(selectableEnabledColorPair.getValue().color, selectableEnabledColorPair.getValue().intensity);
                    });
                });

                SelectHelperNew.clearSelectedElements();
            }));
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
                NewMain.getProject().getComponents()
        );
    }

    @FXML
    private void deleteSelectedClicked() {
        if (SelectHelperNew.getSelectedElements().size() == 0) return;

        // Run through the selected elements and look for something that we can delete
        SelectHelperNew.getSelectedElements().forEach(selectable -> {
            if (selectable instanceof LocationController) {
                final Component component = ((LocationController) selectable).getComponent();
                final Location location = ((LocationController) selectable).getLocation();
                final double previousX = location.getX();
                final double previousY = location.getY();

                final Location initialLocation = component.getInitialLocation();
                final Location finalLocation = component.getFinalLocation();

                if (location.equals(initialLocation) || location.equals(finalLocation))
                    return; // Do not delete initial or final locations

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
                });
            }
        });

        SelectHelperNew.clearSelectedElements();
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
