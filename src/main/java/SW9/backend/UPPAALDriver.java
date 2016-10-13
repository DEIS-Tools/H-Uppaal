package SW9.backend;

import SW9.model_canvas.ModelContainer;
import com.uppaal.engine.Engine;
import com.uppaal.engine.EngineException;
import com.uppaal.engine.Problem;
import com.uppaal.model.core2.Document;
import com.uppaal.model.system.UppaalSystem;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UPPAALDriver {

    enum TraceType {
        NONE, SOME, SHORTEST, FASTEST;

        @Override
        public String toString() {
            return "trace " + this.ordinal();
        }
    }

    public static void verify(final String query, final Consumer<Boolean> success, final Consumer<BackendException> failure, final List<ModelContainer> modelContainers) {
        verify(query, success, failure, TraceType.NONE,e -> {}, modelContainers);
    }

    public static void verify(final String query, final Consumer<Boolean> success, final Consumer<BackendException> failure, final ModelContainer... modelContainers) {
        verify(query, success, failure, TraceType.NONE, e -> {}, modelContainers);
    }

    public static void verify(final String query,
                              final Consumer<Boolean> success,
                              final Consumer<BackendException> failure,
                              final TraceType traceType,
                              final Consumer<Trace> traceCallBack,
                              final ModelContainer... modelContainers) {
        final List<ModelContainer> modelContainerList = new ArrayList<>();
        for (final ModelContainer modelContainer : modelContainers) {
            modelContainerList.add(modelContainer);
        }
        verify(query, success, failure, traceType, traceCallBack, modelContainerList);
    }

    public static void verify(final String query,
                              final Consumer<Boolean> success,
                              final Consumer<BackendException> failure,
                              final TraceType traceType,
                              final Consumer<Trace> traceCallBack,
                              final List<ModelContainer> modelContainers) {
        // The task that should be executed on the background thread
        // calls success if no exception happens with the result
        // otherwise calls failure with the exception
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                {
                    try {
                        success.accept(UPPAALDriver.verify(query, traceType, traceCallBack, modelContainers));
                    } catch (final BackendException backendException) {
                        failure.accept(backendException);
                    }
                }
                return null;
            }
        };

        // Start a new thread
        new Thread(task).start();
    }

    private static synchronized boolean verify(final String query, final TraceType traceType, final Consumer<Trace> traceCallback, final List<ModelContainer> modelContainers) throws BackendException {
        final HUPPAALDocument huppaalDocument = new HUPPAALDocument(modelContainers);

        // Store the debug document
        storeUppaalFile(huppaalDocument.toUPPAALDocument(), "uppaal-debug/debug.xml");

        final QueryListener queryListener = new QueryListener(huppaalDocument, traceCallback);

        // Run the query
        return runQuery(queryListener, query, traceType);
    }

    private static boolean runQuery(final QueryListener queryListener, final String query, final TraceType traceType) throws BackendException {

        // Create the engine and set the correct server path
        final Engine engine = new Engine();
        engine.setServerPath(getOSDependentServerPath());

        try {
            engine.connect();

            // Create a list to store the problems of the query
            final ArrayList<Problem> problems = new ArrayList<>();

            // Get the system, and fill the problems list if any
            final UppaalSystem system = engine.getSystem(queryListener.getHUPPAALDocument().toUPPAALDocument(), problems);

            // Check if there is any problems
            if (!problems.isEmpty()) {
                problems.forEach(problem -> System.out.println("problem: " + problem));
            }

            // Update some internal state for the engine by getting the initial state
            engine.getInitialState(system);

            // Return the query
            final char result = engine.query(system, traceType.toString(), query, queryListener).result;

            if (result == 'T') {
                return true;
            } else if (result == 'F') {
                return false;

            } else if (result == 'M') {
                throw new BackendException.QueryErrorException("UPPAAL Engine was uncertain on the result");

            } else {
                throw new BackendException.QueryErrorException("UPPAAL Engine returned with an error");
            }

        } catch (EngineException | IOException e) {
            // Something went wrong
            throw new BackendException.BadUPPAALQueryException("Unable to run query", e);
        }
    }

    private static String getOSDependentServerPath() {
        final String basePath = "src/main/resources/SW9";
        final String os = System.getProperty("os.name");
        if (os.contains("Mac")) {
            return new File(basePath + "/servers/bin-MacOS/server").getPath();
        } else if (os.contains("Linux")) {
            return new File(basePath + "/servers/bin-Linux/server").getPath();
        } else {
            return new File(basePath + "/servers/bin-Win32/server.exe").getPath();
        }
    }

    private static void storeUppaalFile(final Document uppaalDocument, final String fileName) {
        final File file = new File(fileName);
        try {
            uppaalDocument.save(file);
        } catch (IOException e) {
            // TODO Handle exception
            e.printStackTrace();
        }
    }


}
