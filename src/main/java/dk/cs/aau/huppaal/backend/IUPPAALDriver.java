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
    int MAX_ENGINES = 10;
    Object engineLock = false; // Used to lock concurrent engine reference access

    void generateDebugUPPAALModel() throws Exception, BackendException;

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

    File getServerFile();

    enum TraceType {
        NONE, SOME, SHORTEST, FASTEST;

        @Override
        public String toString() {
            return "trace " + this.ordinal();
        }
    }
}
