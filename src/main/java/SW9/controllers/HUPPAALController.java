package SW9.controllers;

import SW9.NewMain;
import SW9.backend.UPPAALDriver;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.HUPPAALPresentation;
import SW9.presentations.QueryPanePresentation;
import SW9.utility.colors.Color;
import SW9.utility.helpers.SelectHelperNew;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
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
    public TextField textFieldFix;
    public ContextMenu contextMenu;

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
    private void colorSelectedClicked(final MouseEvent event) {
        System.out.println(27);

        if (SelectHelperNew.getSelectedElements().size() > 0) {
            event.consume();

            SelectHelperNew.getSelectedElements().forEach(selectable -> {
                selectable.color(Color.AMBER, Color.Intensity.I700);
            });
        }
    }

}
