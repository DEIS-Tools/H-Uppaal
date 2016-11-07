package SW9.controllers;

import SW9.utility.helpers.SelectHelperNew;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class CanvasController implements Initializable {

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

    }

    @FXML
    private void canvasPressed() {
        // Deselect all elements
        SelectHelperNew.clearSelectedElements();
    }

}
