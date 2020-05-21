package dk.cs.aau.huppaal.backend;

import com.uppaal.engine.Engine;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Location;

import java.io.File;
import java.util.function.Consumer;

public class DummyUPPAALDriver implements IUPPAALDriver {
    @Override
    public void generateDebugUPPAALModel() throws Exception, BackendException {
        throw new BackendException("UPPAAL not found at expected location");
    }

    @Override
    public void buildHUPPAALDocument() throws BackendException, Exception {
        throw new BackendException("UPPAAL not found at expected location");
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure) {
        HUPPAAL.showToast("UPPAAL not found");
        return null;
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure, long timeout) {
        HUPPAAL.showToast("UPPAAL not found");
        return null;
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure, Consumer<Engine> engineConsumer) {
        HUPPAAL.showToast("UPPAAL not found");
        return null;
    }

    @Override
    public Thread runQuery(String query, Consumer<Boolean> success, Consumer<BackendException> failure, Consumer<Engine> engineConsumer, QueryListener queryListener) {
        HUPPAAL.showToast("UPPAAL not found");
        return null;
    }

    @Override
    public void stopEngines() {
        HUPPAAL.showToast("UPPAAL not found");
    }

    @Override
    public String getLocationReachableQuery(Location location, Component component) {
        return null;
    }

    @Override
    public String getExistDeadlockQuery(Component component) {
        return null;
    }

    @Override
    public File getServerFile() {
        return new File("hey.ino");
    }
}
