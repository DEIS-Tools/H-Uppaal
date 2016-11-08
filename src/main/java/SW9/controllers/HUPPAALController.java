package SW9.controllers;

import SW9.NewMain;
import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.backend.UPPAALDriver;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.HUPPAALPresentation;
import SW9.presentations.QueryPanePresentation;
import SW9.utility.UndoRedoStack;
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

import java.net.URL;
import java.util.ResourceBundle;

public class HUPPAALController implements Initializable {

    public StackPane root;
    public BorderPane bottomStatusBar;
    public QueryPanePresentation queryPane;
    public StackPane toolbar;
    public Label title;
    public MenuBar menuBar;
    public Label fillerElement;
    public CanvasPresentation canvas;

    public StackPane dialogContainer;
    public JFXDialog dialog;
    public StackPane modalBar;
    public JFXTextField queryTextField;
    public JFXTextField commentTextField;
    public JFXRippler generateUppaalModel;
    public JFXRippler colorSelected;
    public JFXRippler deleteSelected;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        // Keybind for toggling the query pane
        KeyboardTracker.registerKeybind(KeyboardTracker.TOGGLE_QUERY_PANE, new Keybind(new KeyCodeCombination(KeyCode.Q), () -> {
            ((HUPPAALPresentation) root).toggleQueryPane();
        }));

        dialog.setDialogContainer(dialogContainer);
        dialogContainer.opacityProperty().bind(dialog.getChildren().get(0).scaleXProperty());
        dialog.setOnDialogClosed(event -> dialogContainer.setVisible(false));

        // Keybind for showing dialog // todo: remove this when done with testing
        KeyboardTracker.registerKeybind("DIALOG", new Keybind(new KeyCodeCombination(KeyCode.I), () -> {
            dialogContainer.setVisible(true);
            dialog.show();
        }));

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

}
