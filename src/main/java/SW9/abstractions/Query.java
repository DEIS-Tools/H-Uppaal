package SW9.abstractions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Query {
    private final ObjectProperty<QueryState> queryState;
    private final StringProperty query;
    private final StringProperty comment;

    public Query(final String query, final String comment, final QueryState queryState) {
        this(new SimpleStringProperty(query), new SimpleStringProperty(comment), new SimpleObjectProperty<>(queryState));
    }

    Query(final StringProperty query, final StringProperty comment, final ObjectProperty<QueryState> queryState) {
        this.query = query;
        this.comment = comment;
        this.queryState = queryState;
    }

    public QueryState getQueryState() {
        return queryState.get();
    }

    public ObjectProperty<QueryState> queryStateProperty() {
        return queryState;
    }

    public void setQueryState(final QueryState queryState) {
        this.queryState.set(queryState);
    }

    public String getQuery() {
        return query.get();
    }

    public StringProperty queryProperty() {
        return query;
    }

    public void setQuery(final String query) {
        this.query.set(query);
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment.set(comment);
    }


}
