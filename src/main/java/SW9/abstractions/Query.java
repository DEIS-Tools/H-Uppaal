package SW9.abstractions;

import SW9.utility.serialize.Serializable;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Query implements Serializable {
    private static final String QUERY = "query";
    private static final String COMMENT = "comment";

    private final ObjectProperty<QueryState> queryState = new SimpleObjectProperty<>(QueryState.UNKNOWN);
    private final StringProperty query = new SimpleStringProperty("");
    private final StringProperty comment = new SimpleStringProperty("");

    public Query(final String query, final String comment, final QueryState queryState) {
        this.query.set(query);
        this.comment.set(comment);
        this.queryState.set(queryState);
    }

    public Query(final JsonObject jsonElement) {
        deserialize(jsonElement);
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
        final JsonObject result = new JsonObject();

        result.addProperty(QUERY, getQuery());
        result.addProperty(COMMENT, getComment());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setQuery(json.getAsJsonPrimitive(QUERY).getAsString());
        setComment(json.getAsJsonPrimitive(COMMENT).getAsString());
    }
}
