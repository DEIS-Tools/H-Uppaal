package dk.cs.aau.huppaal.backend;

import com.uppaal.engine.Engine;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Location;

import java.io.File;
import java.util.function.Consumer;

/**
 * Handles calls made to the UPPAALDriver, when no UPPAAL server file is found.
 * generateDebugUPPAALModel or buildHUPPAALDocument is always called before other methods,
 * hence the remaining methods are left empty
 */
public class DummyUPPAALDriver implements IUPPAALDriver {

    @Override
    public void generateDebugUPPAALModel() throws Exception, BackendException {
        throw new BackendException("The specified UPPAAL server file does not exist. Check the 'warnings' tab for more information");
    }

    @Override
    public void buildHUPPAALDocument() throws BackendException, Exception {
        throw new BackendException("The specified UPPAAL server file does not exist. Check the 'warnings' tab for more information");
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure) {
        return null;
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure, long timeout) {
        return null;
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure, Consumer<Engine> engineConsumer) {
        return null;
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure, Consumer<Engine> engineConsumer, QueryListener queryListener) {
        return null;
    }

    @Override
    public void stopEngines() {
    }

    @Override
    public String getLocationReachableQuery(Location location, Component component) {
        return null;
    }

    @Override
    public String getExistDeadlockQuery(Component component) {
        return null;
    }
}
