package SW9.query_pane;

import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class QueryPaneAbstraction {

    private final ObservableList<Query> queries = FXCollections.observableArrayList();

    public QueryPaneAbstraction() {

    }

    public ObservableList<Query> getQueries() {
        return queries;
    }

}
