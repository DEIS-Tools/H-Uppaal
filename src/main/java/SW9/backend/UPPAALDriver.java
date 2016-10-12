package SW9.backend;

import SW9.model_canvas.ModelContainer;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import com.uppaal.engine.*;
import com.uppaal.model.core2.Document;
import com.uppaal.model.core2.Property;
import com.uppaal.model.core2.PrototypeDocument;
import com.uppaal.model.core2.Template;
import com.uppaal.model.system.SystemState;
import com.uppaal.model.system.UppaalSystem;
import com.uppaal.model.system.symbolic.SymbolicTransition;
import javafx.concurrent.Task;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UPPAALDriver {

    public static void verify(final String query, final ModelContainer modelContainer, final Consumer<Boolean> success, final Consumer<BackendException> failure) {
        // The task that should be executed on the background thread
        // calls success if no exception happens with the result
        // otherwise calls failure with the exception
        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                {
                    try {
                        success.accept(UPPAALDriver.verify(query, modelContainer));
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

    private static synchronized boolean verify(final String query, final ModelContainer modelContainer) throws BackendException.BadUPPAALQueryException {
        final Document uppaalDocument = new Document(new PrototypeDocument());

        // Give the model container a name based on its hascode
        // TODO use the name of the component here instead (special case if it root element is being implemented)
        final String modelContainerName = "ModelContainer" + modelContainer.hashCode();

        final Template template = generateTemplate(uppaalDocument, modelContainer);
        template.setProperty("name", modelContainerName);

        final String systemDclString = modelContainerName + "instance = " + modelContainerName + "();\n" +
                "system " + modelContainerName + "instance;";

        // Set the system declaration
        uppaalDocument.setProperty("system", systemDclString);

        // Run the query
        final char result = runQuery(uppaalDocument, query).result;

        // Store the document
        storeUppaalFile(uppaalDocument, "debug.xml");

        if (result == 'T') return true;
        else if (result == 'F') return false;
        else
            throw new BackendException.BadUPPAALQueryException("Query returned from engine was: " + result + " (E is Error, M is uncertain)");
    }

    private static Template generateTemplate(final Document uppaalDocument, final ModelContainer modelContainer) {

        // Maps to convert H-UPPAAL locations to UPPAAL locations
        final Map<Location, com.uppaal.model.core2.Location> hToULocations = new HashMap<>();

        // TODO remove this when names are updated
        int locationCounter = 0;

        // Create empty template and insert it into the uppaal document
        final Template template = uppaalDocument.createTemplate();
        uppaalDocument.insert(template, null);

        template.setProperty("declaration", generateTemplateDeclaration(modelContainer));

        // Add all locations from the model container to our conversion map and to the template
        for (final Location hLocation : modelContainer.getLocations()) {

            // Add the location to the template
            final com.uppaal.model.core2.Location uLocation = addLocation(template, hLocation, "L" + locationCounter);

            // Populate the map
            hToULocations.put(hLocation, uLocation);
            
            locationCounter++;
        }

        for (final Edge hEdge : modelContainer.getEdges()) {

            // Create new UPPAAL edge and insert it into the template
            final com.uppaal.model.core2.Edge uEdge = template.createEdge();
            template.insert(uEdge, null);

            // Find the source and target locations from the map
            final com.uppaal.model.core2.Location sourceULocation = hToULocations.get(hEdge.getSourceLocation());
            final com.uppaal.model.core2.Location targetULocation = hToULocations.get(hEdge.getTargetLocation());

            // Add the to the edge
            uEdge.setSource(sourceULocation);
            uEdge.setTarget(targetULocation);
        }

        return template;
    }

    private static QueryVerificationResult runQuery(final Document uppaalDocument, final String query) throws BackendException.BadUPPAALQueryException {

        // Create the engine and set the correct server path
        final Engine engine = new Engine();
        engine.setServerPath(getOSDependentServerPath());

        try {
            engine.connect();

            // Create a list to store the problems of the query
            final ArrayList<Problem> problems = new ArrayList<>();

            // Get the system, and fill the problems list if any
            final UppaalSystem system = engine.getSystem(uppaalDocument, problems);

            // Check if there is any problems
            if (!problems.isEmpty()) {
                problems.forEach(problem -> System.out.println("problem: " + problem));
            }

            // Update some internal state for the engine by getting the initial state
            SystemState state = engine.getInitialState(system);

            // Return the query
            // TODO use the trace and progress from this method call
            final QueryVerificationResult result = engine.query(system, "", query, new QueryFeedback() {
                @Override
                public void setProgressAvail(boolean b) {

                }

                @Override
                public void setProgress(int i, long l, long l1, long l2, long l3, long l4, long l5, long l6, long l7, long l8) {

                }

                @Override
                public void setSystemInfo(long l, long l1, long l2) {

                }

                @Override
                public void setLength(int i) {

                }

                @Override
                public void setCurrent(int i) {

                }

                @Override
                public void setTrace(char c, String s, ArrayList<SymbolicTransition> arrayList, int i, QueryVerificationResult queryVerificationResult) {
                }

                @Override
                public void setFeedback(String s) {
                }

                @Override
                public void appendText(String s) {

                }

                @Override
                public void setResultText(String s) {

                }
            });

            return result;


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

    private static com.uppaal.model.core2.Location addLocation(final Template template, final Location hLocation, final String name) {

        // TODO get name of location instead of having a separate parameter for the name

        final int x = (int) hLocation.xProperty().get();
        final int y = (int) hLocation.xProperty().get();
        final Color color = hLocation.getColor().toAwtColor(hLocation.getColorIntensity());


        // Create new UPPAAL location and insert it into the template
        final com.uppaal.model.core2.Location uLocation = template.createLocation();
        template.insert(uLocation, null);

        // Set name of the location
        uLocation.setProperty("name", name);

        // Update the placement of the name label
        Property p = uLocation.getProperty("name");
        p.setProperty("x", x);
        p.setProperty("y", y - 30);

        // If it was the initial location
        if (hLocation.type == Location.Type.INITIAL) {
            uLocation.setProperty("init", true);
        }

        // Set the color of the location
        uLocation.setProperty("color", color);

        // Set the x and y properties
        uLocation.setProperty("x", x);
        uLocation.setProperty("y", y);

        return uLocation;
    }

    private static String generateTemplateDeclaration(final ModelContainer modelContainer) {

        // TODO update the types of variables (int, byte etc) and channels (urgent) when added to the model
        String declStr = "";

        // Add the clocks
        for(final String clock : modelContainer.getClocks()) {
            declStr += "clock " + clock + ";\n";
        }

        // Add variables
        for(final String var : modelContainer.getVariables()) {
            declStr += "int " + var + ";\n";
        }

        // Add channels
        for(final String chan : modelContainer.getChannels()) {
            declStr += "chan " + chan + ";\n";
        }

        return  declStr;
    }

}
