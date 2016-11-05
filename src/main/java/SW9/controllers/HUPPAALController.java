package SW9.controllers;

import SW9.presentations.CanvasPresentation;
import SW9.presentations.HUPPAALPresentation;
import SW9.presentations.QueryPanePresentation;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
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

}
