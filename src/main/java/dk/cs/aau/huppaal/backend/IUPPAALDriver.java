package dk.cs.aau.huppaal.backend;

import com.uppaal.engine.Engine;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Location;

import java.util.function.Consumer;

public interface IUPPAALDriver {
    int MAX_ENGINES = 10;
    Object engineLock = false; // Used to lock concurrent engine reference access

    void generateDebugUPPAALModel() throws Exception, BackendException;

    void saveUPPAALModel(String fileName) throws Exception;

    void buildHUPPAALDocument() throws Exception, BackendException;

    Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure);

    Thread runQuery(final String query,
                    final Consumer<Boolean> success,
                    final Consumer<BackendException> failure,
                    final long timeout);

    Thread runQuery(final String query,
                    final Consumer<Boolean> success,
                    final Consumer<BackendException> failure,
                    final Consumer<Engine> engineConsumer);

    Thread runQuery(final String query,
                    final Consumer<Boolean> success,
                    final Consumer<BackendException> failure,
                    final Consumer<Engine> engineConsumer,
                    final QueryListener queryListener);

    void stopEngines();

    String getLocationReachableQuery(final Location location, final Component component);

    String getExistDeadlockQuery(final Component component);

    enum TraceType {
        NONE, SOME, SHORTEST, FASTEST;

        @Override
        public String toString() {
            return "trace " + this.ordinal();
        }
    }
}
