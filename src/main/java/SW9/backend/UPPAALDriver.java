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
import com.uppaal.engine.QueryVerificationResult;
import com.uppaal.model.core2.Document;
import com.uppaal.model.system.UppaalSystem;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UPPAALDriver {

    public static final int MAX_ENGINES = 10;
    public static final Object engineLock = false; // Used to lock concurrent engine reference access

    private static HUPPAALDocument huppaalDocument;

    public static void generateDebugUPPAALModel() throws Exception, BackendException {
        // Generate and store the debug document
        buildHUPPAALDocument();
        storeUppaalFile(huppaalDocument.toUPPAALDocument(), HUPPAAL.debugDirectory + File.separator + "debug.xml");
    }

    public static void buildHUPPAALDocument() throws BackendException, Exception {
        final Component mainComponent = HUPPAAL.getProject().getMainComponent();
        if (mainComponent == null) {
            throw new Exception("Main component is null");
        }

        // Generate HUPPAAL document based on the main component
        huppaalDocument = new HUPPAALDocument(mainComponent);
    }

    public static Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure) {
        return runQuery(query, success, failure, -1);
    }
    public static Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure,
                                  final long timeout) {

        final Consumer<Engine> engineConsumer = engine -> {
            if(timeout >= 0) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (engineLock) {
                            if(engine == null) return;
                            engine.cancel();
                        }
                    }
                }, timeout);
            }
        };

        return runQuery(query, success, failure, engineConsumer);
    }

    public static Thread runQuery(final String query,
                                  final Consumer<Boolean> success,
                                  final Consumer<BackendException> failure,
                                  final Consumer<Engine> engineConsumer) {
        return runQuery(query, success, failure, engineConsumer, new QueryListener());
    }

    public static Thread runQuery(final String query,
                                   final Consumer<Boolean> success,
                                   final Consumer<BackendException> failure,
                                   final Consumer<Engine> engineConsumer,
                                   final QueryListener queryListener) {
        return new Thread() {
            Engine engine;

            @Override
            public void run() {
                try {
                    // "Create" the engine and set the correct server path
                    while (engine == null) {
                        if (isInterrupted()) return;
                        engine = getOSDependentEngine();
                        if (engine == null) {
                            // Waiting for engine
                            Thread.yield();
                        } else {
                            break;
                        }
                    }

                    engine.connect();
                    engineConsumer.accept(engine);

                    // Create a list to store the problems of the query
                    final ArrayList<Problem> problems = new ArrayList<>();

                    // Get the system, and fill the problems list if any
                    final UppaalSystem system = engine.getSystem(huppaalDocument.toUPPAALDocument(), problems);

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

                    final QueryVerificationResult qvr = engine.query(system, "", query, queryListener);
                    final char result = qvr.result;

                    // Process the query result
                    if (result == 'T') {
                        success.accept(true);
                    } else if (result == 'F') {
                        success.accept(false);
                    } else if (result == 'M') {
                        failure.accept(new BackendException.QueryErrorException("UPPAAL Engine was uncertain on the result"));
                    } else {
                        failure.accept(new BackendException.BadUPPAALQueryException("Unable to run query", qvr.exception));
                    }

                } catch (EngineException | IOException | NullPointerException e) {
                    // Something went wrong
                    failure.accept(new BackendException.BadUPPAALQueryException("Unable to run query", e));
                } finally {
                    synchronized (engineLock) {
                        releaseOSDependentEngine(engine);
                        engine = null;
                    }

                }
            }
        };
    }

    private static final ArrayList<Engine> createdEngines = new ArrayList<>();
    private static final ArrayList<Engine> availableEngines = new ArrayList<>();

    private static File findServerFile(final String serverName) {
        final String os = System.getProperty("os.name");
        final File file;

        if (os.contains("Mac")) {
            file = new File(HUPPAAL.serverDirectory + File.separator + "bin-MacOS" + File.separator + serverName);
        } else if (os.contains("Linux")) {
            file = new File(HUPPAAL.serverDirectory + File.separator + "bin-Linux" + File.separator + serverName);
        } else {
            file = new File(HUPPAAL.serverDirectory + File.separator + "bin-Win32" + File.separator + serverName + ".exe");
        }

        return file;
    }

    private static Engine getAvailableEngineOrCreateNew() {
        if (availableEngines.size() == 0) {
            final String serverName = "server";
            final File serverFile = findServerFile(serverName);
            serverFile.setExecutable(true); // Allows us to use the server file

            // Check if the user copied the file correctly
            if (!serverFile.exists()) {
                System.out.println("Could not find backend-file: " + serverFile.getAbsolutePath() + ". Please make sure to copy UPPAAL binaries to this location.");
            }

            // Create a new engine, set the server path, and return it
            final Engine engine = new Engine();
            engine.setServerPath(serverFile.getPath());
            createdEngines.add(engine);
            return engine;
        } else {
            final Engine engine = availableEngines.get(0);
            availableEngines.remove(0);
            return engine;
        }
    }

    private static Engine getOSDependentEngine() {
        synchronized (createdEngines) {
            if (!(createdEngines.size() >= MAX_ENGINES && availableEngines.size() == 0)) {
                final Engine engine = getAvailableEngineOrCreateNew();
                if (engine != null) {
                    return engine;
                }
            }
        }

        // No engines are available currently, check back again later
        return null;
    }

    private static void releaseOSDependentEngine(final Engine engine) {
        synchronized (createdEngines) {
            availableEngines.add(engine);
        }
    }

    public static void stopEngines() {
        synchronized (createdEngines) {
            while (createdEngines.size() != 0) {
                final Engine engine = createdEngines.get(0);
                engine.cancel(); // Cancel any running tasks on this engine
                createdEngines.remove(0);
            }
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

        if (component.isIsMain()) {
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
