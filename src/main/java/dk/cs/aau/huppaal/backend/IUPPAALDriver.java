package dk.cs.aau.huppaal.backend;

import com.google.common.base.Strings;
import com.uppaal.engine.Engine;
import com.uppaal.engine.EngineException;
import com.uppaal.engine.Problem;
import com.uppaal.engine.QueryVerificationResult;
import com.uppaal.model.core2.Document;
import com.uppaal.model.system.UppaalSystem;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Location;
import dk.cs.aau.huppaal.abstractions.SubComponent;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public interface IUPPAALDriver {
    public final int MAX_ENGINES = 10;
    public final Object engineLock = false; // Used to lock concurrent engine reference access

    public void generateDebugUPPAALModel() throws Exception, BackendException;

    public void buildHUPPAALDocument() throws BackendException, Exception;

    public Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure);

    public Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure,
                                  final long timeout);

    public Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure,
                                  final Consumer<Engine> engineConsumer);

    public Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure,
                                  final Consumer<Engine> engineConsumer,
                                  final QueryListener queryListener);

    public void stopEngines();

    public String getLocationReachableQuery(final Location location, final Component component);

    public String getExistDeadlockQuery(final Component component);

    public File getServerFile();

    public enum TraceType {
        NONE, SOME, SHORTEST, FASTEST;

        @Override
        public String toString() {
            return "trace " + this.ordinal();
        }
    }
}
