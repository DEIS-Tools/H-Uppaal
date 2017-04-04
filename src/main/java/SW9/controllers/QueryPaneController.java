package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import SW9.backend.UPPAALDriver;
import SW9.presentations.QueryPresentation;
import SW9.utility.helpers.DropShadowHelper;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRippler;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class QueryPaneController implements Initializable {

    public Label toolbarTitle;
    public JFXButton addQueryButton;
    public AnchorPane toolbar;
    public JFXRippler runAllQueriesButton;
    public JFXRippler clearAllQueriesButton;
    public VBox queriesList;
    public StackPane root;
    public ScrollPane scrollPane;

    private Map<Query, QueryPresentation> queryPresentationMap = new HashMap<>();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        // We need to register these event manually this way because JFXButton overrides onPressed and onRelease to handle rippler effect
        addQueryButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> addQueryButtonPressed());
        addQueryButton.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> addQueryButtonReleased());

        HUPPAAL.getProject().getQueries().addListener(new ListChangeListener<Query>() {
            @Override
            public void onChanged(final Change<? extends Query> c) {
                while (c.next()) {
                    for (final Query removeQuery : c.getRemoved()) {
                        queriesList.getChildren().remove(queryPresentationMap.get(removeQuery));
                        queryPresentationMap.remove(removeQuery);
                    }

                    for (final Query newQuery : c.getAddedSubList()) {
                        final QueryPresentation newQueryPresentation = new QueryPresentation(newQuery);
                        queryPresentationMap.put(newQuery, newQueryPresentation);
                        queriesList.getChildren().add(newQueryPresentation);
                    }
                }
            }
        });

        for (final Query newQuery : HUPPAAL.getProject().getQueries()) {
            queriesList.getChildren().add(new QueryPresentation(newQuery));
        }
    }

    @FXML
    private void addQueryButtonClicked() {
        HUPPAAL.getProject().getQueries().add(new Query("", "", QueryState.UNKNOWN));
    }

    @FXML
    private void addQueryButtonPressed() {
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(12));
    }

    @FXML
    private void addQueryButtonReleased() {
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(6));
    }

    @FXML
    private void runAllQueriesButtonClicked() {
        try {
            UPPAALDriver.buildHUPPAALDocument();
            HUPPAAL.getProject().getQueries().forEach(query -> {
                query.cancel();
                query.run(false);
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearAllQueriesButtonClicked() {
        HUPPAAL.getProject().getQueries().forEach(query -> query.setQueryState(QueryState.UNKNOWN));
    }
}
