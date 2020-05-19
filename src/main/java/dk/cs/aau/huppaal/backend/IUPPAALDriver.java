package dk.cs.aau.huppaal.backend;

import com.uppaal.engine.Engine;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Location;

import java.io.File;
import java.util.function.Consumer;

public interface IUPPAALDriver {
    Object engineLock = false;

    String getLocationReachableQuery(Location location, Component component);

    void stopEngines();

    File getServerFile();

    void buildHUPPAALDocument() throws BackendException, Exception;

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


    void generateDebugUPPAALModel() throws Exception;

    String getExistDeadlockQuery(Component component);
}
