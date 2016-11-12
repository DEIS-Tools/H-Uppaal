package SW9.abstractions;

import SW9.utility.serialize.Serializable;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Query implements Serializable {
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

    public void setQueryState(final QueryState queryState) {
        this.queryState.set(queryState);
    }

    public ObjectProperty<QueryState> queryStateProperty() {
        return queryState;
    }

    public String getQuery() {
        return query.get();
    }

    public void setQuery(final String query) {
        this.query.set(query);
    }

    public StringProperty queryProperty() {
        return query;
    }

    public String getComment() {
        return comment.get();
    }

    public void setComment(final String comment) {
        this.comment.set(comment);
    }

    public StringProperty commentProperty() {
        return comment;
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public void deserialize(JsonObject json) {

    }
}
