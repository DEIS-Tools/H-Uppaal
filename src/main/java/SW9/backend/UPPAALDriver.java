package SW9.backend;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.abstractions.SubComponent;
import SW9.code_analysis.CodeAnalysis;
import com.google.common.base.Strings;
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

    public static Thread verify(final String query, final Consumer<Boolean> success, final Consumer<BackendException> failure, final Component component) {
        return verify(query, success, failure, TraceType.NONE, e -> {
        }, component);
    }

    public static Thread verify(final String query,
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
        final Thread verifyThread = new Thread(task);
        verifyThread.start();
        return verifyThread;
    }

    private static synchronized boolean verify(final String query, final TraceType traceType, final Consumer<Trace> traceCallback, final Component component) throws BackendException {
        final HUPPAALDocument huppaalDocument = new HUPPAALDocument(component);

        // Store the debug document
        storeUppaalFile(huppaalDocument.toUPPAALDocument(), HUPPAAL.debugDirectory + File.separator + "debug.xml");

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
        final String os = System.getProperty("os.name");

        final File file;

        if (os.contains("Mac")) {
            file = new File(HUPPAAL.serverDirectory + File.separator + "bin-MacOS" + File.separator + "server");
        } else if (os.contains("Linux")) {
            file = new File(HUPPAAL.serverDirectory + File.separator + "bin-Linux" + File.separator + "server");
        } else {
            file = new File(HUPPAAL.serverDirectory + File.separator + "bin-Win32" + File.separator + "server.exe");
        }

        if (!file.exists()) {
            System.out.println("Could not find backend-file: " + file.getAbsolutePath() + ". Please make sure to copy UPPAAL binaries to this location.");
        }

        return file.getPath();
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

    public static String getLocationReachableQuery(final Location location, final Component component) {

        // Get the various flattened names of a location to produce a reachability query
        final List<String> templateNames = getTemplateNames(component);
        final List<String> locationNames = new ArrayList<>();

        for (final String templateName : templateNames) {
            locationNames.add(templateName + "." + location.getId());
        }

        return "E<> " + String.join(" || ", locationNames);
    }

    public static String getExistDeadlockQuery(final Component component) {
        // Get the various flattened names of a location to produce a reachability query
        final List<String> template = getTemplateNames(component);
        final List<String> locationNames = new ArrayList<>();


        for (final String templateName : template) {
            for (final Location location : component.getLocations()) {
                locationNames.add(templateName + "." + location.getId());
            }

            locationNames.add(templateName + "." + component.getInitialLocation().getId());
            locationNames.add(templateName + "." + component.getFinalLocation().getId());
        }

        return "E<> (" + String.join(" || ", locationNames) + ") && deadlock";
    }

    private static List<String> getTemplateNames(final Component component) {
        final List<String> subComponentInstanceNames = new ArrayList<>();

        if(component.isIsMain()) {
            subComponentInstanceNames.add(component.getName());
        }

        // Run through all sub components in main
        for (final SubComponent subComp : HUPPAAL.getProject().getMainComponent().getSubComponents()) {
            subComponentInstanceNames.addAll(getTemplateNames("", subComp, component));
        }
        return subComponentInstanceNames;
    }

    private static List<String> getTemplateNames(String str, final SubComponent subject, final Component needle) {
        final List<String> subComponentInstanceNames = new ArrayList<>();

        // Run all their sub components
        for (final SubComponent sc : subject.getComponent().getSubComponents()) {
            subComponentInstanceNames.addAll(getTemplateNames(subject.getIdentifier(), sc, needle));
        }

        if (subject.getComponent().equals(needle)) {
            if (!Strings.isNullOrEmpty(str)) {
                str += "_";
            }
            subComponentInstanceNames.add(str + subject.getIdentifier());
        }

        return subComponentInstanceNames;
    }

    public enum TraceType {
        NONE, SOME, SHORTEST, FASTEST;

        @Override
        public String toString() {
            return "trace " + this.ordinal();
        }
    }
}
