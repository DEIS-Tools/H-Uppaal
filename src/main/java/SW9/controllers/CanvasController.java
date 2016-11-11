package SW9.controllers;

import SW9.presentations.CanvasPresentation;
import SW9.presentations.ComponentPresentation;
import SW9.utility.helpers.SelectHelper;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class CanvasController implements Initializable {

    public ComponentPresentation component;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        CanvasPresentation.mouseTracker.registerOnMousePressedEventHandler(event -> {
            // Deselect all elements
            SelectHelper.clearSelectedElements();
        });
    }

}
