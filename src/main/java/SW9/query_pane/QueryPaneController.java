package SW9.query_pane;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class QueryPaneController implements Initializable {

    private QueryPaneAbstraction model;

    public QueryPaneAbstraction getModel() {
        return model;
    }

    public void setModel(final QueryPaneAbstraction model) {
        this.model = model;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

    }
}
