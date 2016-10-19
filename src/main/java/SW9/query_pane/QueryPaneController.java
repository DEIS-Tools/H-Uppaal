package SW9.query_pane;

import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import SW9.utility.helpers.DropShadowHelper;
import com.jfoenix.controls.JFXButton;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class QueryPaneController implements Initializable {

    private QueryPaneAbstraction abstraction;

    @FXML
    Label toolbarTitle;

    @FXML
    JFXButton addQueryButton;

    @FXML
    AnchorPane toolbar;

    @FXML
    JFXButton runAllQueriesButton;

    @FXML
    JFXButton clearAllQueriesButton;

    @FXML
    VBox queriesList;

    public QueryPaneAbstraction getAbstraction() {
        return abstraction;
    }

    public void setAbstraction(final QueryPaneAbstraction model) {
        this.abstraction = model;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Create new abstraction
        this.abstraction = new QueryPaneAbstraction();

        // We need to register these event manually this way because JFXButton overrides onPressed and onRelease to handle rippler effect
        addQueryButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> addQueryButtonPressed());
        addQueryButton.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> addQueryButtonReleased());

        abstraction.getQueries().addListener(new ListChangeListener<Query>() {
            @Override
            public void onChanged(final Change<? extends Query> c) {
                while (c.next()) {
                    for (final Query removeQuery : c.getRemoved()) {
                        queriesList.getChildren().remove(removeQuery);
                    }

                    for (final Query newQuery : c.getAddedSubList()) {
                        queriesList.getChildren().add(new QueryPresentation(newQuery));
                    }
                }

                //System.out.println(c);
            }
        });
    }

    @FXML
    private void addQueryButtonClicked() {
        abstraction.getQueries().add(new Query("A[] not deadlock", "The model does not contain a deadlock", QueryState.RUNNING));
    }

    @FXML
    private void addQueryButtonPressed() {
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(12));
    }

    @FXML
    private void addQueryButtonReleased() {
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(6));
    }
}
