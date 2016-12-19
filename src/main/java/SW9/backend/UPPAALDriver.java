package SW9.backend;

import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.code_analysis.CodeAnalysis;
import com.uppaal.engine.Engine;
import com.uppaal.engine.EngineException;
import com.uppaal.engine.Problem;
import com.uppaal.model.core2.Document;
import com.uppaal.model.system.UppaalSystem;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UPPAALDriver {

    public static void verify(final String query, final Consumer<Boolean> success, final Consumer<BackendException> failure, final Component component) {
        verify(query, success, failure, TraceType.NONE, e -> {
        }, component);
    }

    public static void verify(final String query,
                              final Consumer<Boolean> success,
                              final Consumer<BackendException> failure,
                              final TraceType traceType,
                              final Consumer<Trace> traceCallBack,
                              final Component component) {
        // The task that should be executed on the background thread
        // calls success if no exception happens with the result
        // otherwise calls failure with the exception
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                {
                    try {
                        success.accept(UPPAALDriver.verify(query, traceType, traceCallBack, component));
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

    private static synchronized boolean verify(final String query, final TraceType traceType, final Consumer<Trace> traceCallback, final Component component) throws BackendException {
        final HUPPAALDocument huppaalDocument = new HUPPAALDocument(component);

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

            // Run on UI thread
            Platform.runLater(() -> {
                // Clear the UI for backend-errors
                CodeAnalysis.clearBackendErrors();

                // Check if there is any problems
                if (!problems.isEmpty()) {
                    problems.forEach(problem -> {
                        System.out.println("problem: " + problem);

                        // Generate the message
                        CodeAnalysis.Message message = null;
                        if (problem.getPath().contains("declaration")) {
                            final String[] lines = problem.getLocation().split("\\n");
                            final String errorLine = lines[problem.getFirstLine() - 1];

                            message = new CodeAnalysis.Message(
                                    problem.getMessage() + " on line " + problem.getFirstLine() + " (" + errorLine + ")",
                                    CodeAnalysis.MessageType.ERROR
                            );
                        } else {
                            message = new CodeAnalysis.Message(
                                    problem.getMessage() + " (" + problem.getLocation() + ")",
                                    CodeAnalysis.MessageType.ERROR
                            );
                        }


                        CodeAnalysis.addBackendError(message);
                    });
                }
            });

            // Update some internal state for the engine by getting the initial state
            engine.getInitialState(system);

            // Return the query
            final char result = engine.query(system, traceType.toString(), query, queryListener).result;
            engine.disconnect();

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
        } finally {
            engine.disconnect();
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
        } catch (final IOException e) {
            // TODO Handle exception
            e.printStackTrace();
        }
    }

    public enum TraceType {
        NONE, SOME, SHORTEST, FASTEST;

        @Override
        public String toString() {
            return "trace " + this.ordinal();
        }
    }

    public static String getLocationReachableQuery(final Location location, final Component mainComponent) throws BackendException {
        // Generate a uppaal document
        final HUPPAALDocument huppaalDocument = new HUPPAALDocument(mainComponent);

        // Get the various flattened names of a location to produce a reachability query
        final List<String> locationsNames = huppaalDocument.getFlattenedNames(location);
        String result = "E<> ";

        for (int i = 0; i < locationsNames.size(); i++) {
            if(i != 0) {
                result += " || ";
            }
            result += mainComponent.getName() + "." + locationsNames.get(i);
        }

        return result;
    }
}
