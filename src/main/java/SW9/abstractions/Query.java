package SW9.abstractions;

import SW9.HUPPAAL;
import SW9.backend.QueryListener;
import SW9.backend.UPPAALDriver;
import SW9.controllers.HUPPAALController;
import SW9.utility.serialize.Serializable;
import com.google.gson.JsonObject;
import com.uppaal.engine.Engine;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.util.function.Consumer;

public class Query implements Serializable {
    private static final String QUERY = "query";
    private static final String COMMENT = "comment";
    private static final String IS_PERIODIC = "is_periodic";

    private final ObjectProperty<QueryState> queryState = new SimpleObjectProperty<>(QueryState.UNKNOWN);
    private final StringProperty query = new SimpleStringProperty("");
    private final StringProperty comment = new SimpleStringProperty("");
    private final SimpleBooleanProperty isPeriodic = new SimpleBooleanProperty(false);

    private Consumer<Boolean> runQuery;

    public Query(final String query, final String comment, final QueryState queryState) {
        this.query.set(query);
        this.comment.set(comment);
        this.queryState.set(queryState);

        initializeRunQuery();
    }

    public Query(final JsonObject jsonElement) {
        deserialize(jsonElement);

        initializeRunQuery();
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

    public boolean isPeriodic() {
        return isPeriodic.get();
    }

    public SimpleBooleanProperty isPeriodicProperty() {
        return isPeriodic;
    }

    public void setIsPeriodic(final boolean isPeriodic) {
        this.isPeriodic.set(isPeriodic);
    }

    private Engine engine = null;
    private Boolean forcedCancel = false;

    private void initializeRunQuery() {
        runQuery = (buildHUPPAALDocument) -> {
            setQueryState(QueryState.RUNNING);

            final Component mainComponent = HUPPAAL.getProject().getMainComponent();

            if (mainComponent == null) {
                return; // We cannot generate a UPPAAL file without a main component
            }

            try {
                if (buildHUPPAALDocument) {
                    UPPAALDriver.buildHUPPAALDocument();
                }
                UPPAALDriver.runQuery(getQuery(),
                        aBoolean -> {
                            if (aBoolean) {
                                setQueryState(QueryState.SUCCESSFUL);
                            } else {
                                setQueryState(QueryState.ERROR);
                            }
                        },
                        e -> {
                            if (forcedCancel) {
                                setQueryState(QueryState.UNKNOWN);
                            } else {
                                setQueryState(QueryState.SYNTAX_ERROR);
                                final Throwable cause = e.getCause();
                                if (cause != null) {
                                    // We had trouble generating the model if we get a NullPointerException
                                    if(cause instanceof NullPointerException) {
                                        setQueryState(QueryState.UNKNOWN);
                                    } else {
                                        Platform.runLater(() -> HUPPAALController.openQueryDialog(this, cause.toString()));
                                    }
                                }
                            }
                        },
                        eng -> {
                            engine = eng;
                        },
                        new QueryListener(this)
                ).start();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        };
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(QUERY, getQuery());
        result.addProperty(COMMENT, getComment());
        result.addProperty(IS_PERIODIC, isPeriodic());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setQuery(json.getAsJsonPrimitive(QUERY).getAsString());
        setComment(json.getAsJsonPrimitive(COMMENT).getAsString());

        if (json.has(IS_PERIODIC)) {
            setIsPeriodic(json.getAsJsonPrimitive(IS_PERIODIC).getAsBoolean());
        }
    }

    public void run() {
        run(true);
    }

    public void run(final boolean buildHUPPAALDocument) {
        runQuery.accept(buildHUPPAALDocument);
    }

    public void cancel() {
        if (getQueryState().equals(QueryState.RUNNING)) {
            synchronized (UPPAALDriver.engineLock) {
                if (engine != null) {
                    forcedCancel = true;
                    engine.cancel();
                }
            }
            setQueryState(QueryState.UNKNOWN);
        }
    }
}
