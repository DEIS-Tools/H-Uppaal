package SW9.backend;

import SW9.model_canvas.ModelContainer;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import com.uppaal.engine.*;
import com.uppaal.model.core2.Document;
import com.uppaal.model.core2.PrototypeDocument;
import com.uppaal.model.core2.Template;
import com.uppaal.model.system.UppaalSystem;
import com.uppaal.model.system.symbolic.SymbolicTransition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UPPAALDriver {

    public static boolean verify(final String query, final ModelContainer modelContainer) throws EngineException {
        final Document uppaalDocument = new Document(new PrototypeDocument());

        // Give the model container a name based on its hascode
        // TODO use the name of the component here instead (special case if it root element is being implemented)
        final String modelContainerName = "ModelContainer" + modelContainer.hashCode();

        final Template template = generateTemplate(uppaalDocument, modelContainer);
        template.setProperty("name", modelContainerName);

        final String systemDclString = modelContainerName + "instance = " + modelContainerName + "();\n" +
                "system " + modelContainerName + "instance;";

        // Set the system declaration
        uppaalDocument.setProperty("system",systemDclString);

        storeUppaalFile(uppaalDocument, "debug.xml");

        final char result = runQuery(uppaalDocument, query).result;

        if (result == 'T') return true;
        else if (result == 'F') return false;
        else throw new EngineException("Query returned from engine was: " + result + " (E is Error, M is uncertain)");
    }

    private static Template generateTemplate(final Document uppaalDocument, final ModelContainer modelContainer) {

        // Map to convert H-UPPAAL locations to UPPAAL locations
        final Map<Location, com.uppaal.model.core2.Location> hToULocations = new HashMap<>();

        // Create empty template and insert it into the uppaal document
        final Template template = uppaalDocument.createTemplate();
        uppaalDocument.insert(template, null);

        // Add all locations from the model container to our conversion map and to the template
        for (final Location hLocation : modelContainer.getLocations()) {

            // Create new UPPAAL location and insert it into the template
            final com.uppaal.model.core2.Location uLocation = template.createLocation();
            template.insert(uLocation, null);

            // If it was the initial location
            if (hLocation.type == Location.Type.INITIAL) {
                uLocation.setProperty("init", true);
            }

            // Set the x and y properties
            uLocation.setProperty("x", (int) hLocation.xProperty().get());
            uLocation.setProperty("y", (int) hLocation.yProperty().get());

            // Set the color property
            uLocation.setProperty("color", hLocation.getColor().toAwtColor(hLocation.getIntensity()));

            // Map our location to a UPPAAL location
            hToULocations.put(hLocation, uLocation);
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

    private static QueryVerificationResult runQuery(final Document uppaalDocument, final String query) throws EngineException {

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
                problems.forEach(System.out::println);
                // TODO handle them
            }

            // Update some internal state for the engine by getting the initial state
            engine.getInitialState(system);

            // Return the query
            // TODO use the trace and progress from this method call
            return engine.query(system, "", query, new QueryFeedback() {
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

        } catch (EngineException | IOException e) {
            // TODO Handle exception
            e.printStackTrace();
        }

        throw new EngineException("Could not connect to server");
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
