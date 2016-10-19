package SW9.query_pane;

import SW9.utility.helpers.DropShadowHelper;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.beans.EventHandler;
import java.net.URL;
import java.util.ResourceBundle;

public class QueryPaneController implements Initializable {

    private QueryPaneAbstraction model;

    @FXML
    JFXButton addQueryButton;

    @FXML
    HBox toolbar;

    public QueryPaneAbstraction getModel() {
        return model;
    }

    public void setAbstraction(final QueryPaneAbstraction model) {
        this.model = model;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        //addQueryButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> addQueryButtonPressed());
        //addQueryButton.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> addQueryButtonReleased());
    }

    @FXML
    private void addQueryButtonPressed(final MouseEvent event) {
        System.out.println(2);
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(12));
    }

    @FXML
    private void addQueryButtonReleased(final MouseEvent event) {
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(6));
    }
}
