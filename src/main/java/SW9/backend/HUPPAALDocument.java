package SW9.backend;

import SW9.abstractions.Component;
import SW9.abstractions.*;
import com.google.common.base.Strings;
import com.uppaal.model.core2.Document;
import com.uppaal.model.core2.Property;
import com.uppaal.model.core2.PrototypeDocument;
import com.uppaal.model.core2.Template;
import javafx.util.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class HUPPAALDocument {

    private static final String SUBS_DONE_BROADCAST = "subDone";
    private static final String DECLARATION_PROPERTY_TAG = "declaration";
    private static final String COMMITTED_PROPERTY_TAG = "committed";
    private static final String NAME_PROPERTY_TAG = "name";
    private static final String INVARIANT_PROPERTY_TAG = "invariant";
    private static final String GUARD_PROPERTY_TAG = "guard";
    private static final String SYNC_PROPERTY_TAG = "synchronisation";

    private final Document uppaalDocument = new Document(new PrototypeDocument());

    // Map to convert H-UPPAAL locations to UPPAAL locations
    private final Map<String, com.uppaal.model.core2.Location> hToULocations = new HashMap<>();

    // Map to convert back from UPPAAL to H-UPPAAL items
    private final Map<com.uppaal.model.core2.Location, Location> uToHLocations = new HashMap<>();

    // Map to convert back from UPPAAL edges to H-UPPAAL edges
    private final Map<com.uppaal.model.core2.Edge, Edge> uToHEdges = new HashMap<>();

    // Map from location to all of its uppaal names
    private final Map<Location, List<String>> hLocationToFlattenedNames = new HashMap<>();

    // Map from subComponent to the Enter and Exit pseudo locations
    private final Map<SubComponent, Pair<com.uppaal.model.core2.Location, com.uppaal.model.core2.Location>> subComponentPseudoLocationMap = new HashMap<>();

    private final Component mainComponent;
    private final AtomicInteger subProcedureCount = new AtomicInteger(0);

    /**
     * Used to figure out the layering of sub components
     */
    private Stack<SubComponent> subComponentList = new Stack<>();

    public HUPPAALDocument(final Component mainComponent) throws BackendException {
        this.mainComponent = mainComponent;
        generateUPPAALDocument();
    }

    private Document generateUPPAALDocument() throws BackendException {
        // Set create a template for each model container
        final Template template = generateTemplate(mainComponent);
        template.setProperty(NAME_PROPERTY_TAG, mainComponent.getName() + "Template");

        String systemDclString = mainComponent.getName() + " = " + mainComponent.getName() + "Template();\n";

        // Generate the system declaration
        systemDclString += "system ";
        systemDclString += mainComponent.getName();
        systemDclString += ";";

        // Set the system declaration
        uppaalDocument.setProperty("system", systemDclString);

        // Add global broadcast channel used to join currently parallel running sub components
        addToGlobalDeclarations("broadcast chan " + SUBS_DONE_BROADCAST + ";");

        return uppaalDocument;
    }

    private void addToGlobalDeclarations(final String declaration) {
        String currentDeclarations = (String) uppaalDocument.getProperty(DECLARATION_PROPERTY_TAG).getValue();
        if (!Strings.isNullOrEmpty(currentDeclarations)) {
            currentDeclarations += "\n";
        } else {
            currentDeclarations = "";
        }
        uppaalDocument.setProperty(DECLARATION_PROPERTY_TAG, currentDeclarations + declaration);
    }

    private String generateName(final Location location) {
        String result = "L";

        // Add the identifier for each sub component (separated with underscore)
        for (final SubComponent subComponent : subComponentList) {
            result += subComponent.getIdentifier() + "_";
        }

        // Add the identifier for the location
        result += location.getId();

        // Return the result
        return result;
    }

    private Template generateTemplate(final Component mainComponent) throws BackendException {
        // Create empty template and insert it into the uppaal document
        final Template template = uppaalDocument.createTemplate();
        uppaalDocument.insert(template, null);

        template.setProperty("declaration", mainComponent.getDeclarations());

        // Add all locations from the model to our conversion map and to the template
        for (final Location hLocation : mainComponent.getLocations()) {

            // Add the location to the template
            final com.uppaal.model.core2.Location uLocation = addLocation(template, hLocation, 0);

            // Populate the map
            addLocationsToMaps(hLocation, uLocation);
        }

        // Add the initial location to the template
        final Location hInitialLocation = mainComponent.getInitialLocation();
        final com.uppaal.model.core2.Location uInitialLocation = addLocation(template, hInitialLocation, 0);
        addLocationsToMaps(hInitialLocation, uInitialLocation);

        // Add the final location to the template
        final Location hFinalLocation = mainComponent.getFinalLocation();
        final com.uppaal.model.core2.Location uFinalLocation = addLocation(template, hFinalLocation, 0);
        addLocationsToMaps(hFinalLocation, uFinalLocation);

        for (final SubComponent subComponent : mainComponent.getSubComponents()) {
            // TODO Generate template for subcomponent
        }

        for (final Edge hEdge : mainComponent.getEdges()) {

            // Draw edges that are purely location to location edges
            if (hEdge.getSourceLocation() != null && hEdge.getTargetLocation() != null) {
                uToHEdges.put(addEdge(template, hEdge, 0), hEdge);
            }

            // If we the edge starts in a locations and ends in a sub component
            if (hEdge.getSourceLocation() != null && hEdge.getTargetSubComponent() != null) {

                // If we have not already created pseudo locations for this sub component
                if (!subComponentPseudoLocationMap.containsKey(hEdge.getTargetSubComponent())) {
                    addPseudoLocationsForSubComponent(template, hEdge.getTargetSubComponent());
                }

                // Add an edge from the location to the pseudo enter location
                final com.uppaal.model.core2.Location pseudoEnter = subComponentPseudoLocationMap.get(hEdge.getTargetSubComponent()).getKey();
                addEdge(template, hEdge, 0, pseudoEnter);
            }
        }

        // Draw edges from a subComponent to locations
        for (final Edge hEdge : mainComponent.getEdges()) {

            // If we the edge starts in a sub component and ends in a location
            if (hEdge.getSourceSubComponent() != null && hEdge.getTargetLocation() != null) {

                // Add an edge from the pseudo exit location to the location
                final com.uppaal.model.core2.Location pseudoExit = subComponentPseudoLocationMap.get(hEdge.getSourceSubComponent()).getValue();
                addEdge(template, hEdge, 0, pseudoExit);
            }
        }

        return template;
    }

    private void addPseudoLocationsForSubComponent(final Template template, final SubComponent targetSubComponent) {
        // TODO refactor this so it can take n number of subcomponents (allow for fork join with more than one subcomponent)s

        // Styling properties (used to place them in the uppaal document)
        final int[] x = {(int) targetSubComponent.getX()};
        final int[] y = {(int) targetSubComponent.getY()};
        final int offset = 100;

        final BiFunction<String, Boolean, com.uppaal.model.core2.Location> generatePseudoLocation = (name, isCommitted) -> {
            final com.uppaal.model.core2.Location uLocation = template.createLocation();
            template.insert(uLocation, null);

            if (isCommitted) {
                uLocation.setProperty(COMMITTED_PROPERTY_TAG, true);
            }

            // Add the name label
            final Property p = uLocation.setProperty(NAME_PROPERTY_TAG, "S" + targetSubComponent.getIdentifier() + "_" + name);
            p.setProperty("x", x[0]);
            p.setProperty("y", y[0]);

            // Update the placement of location
            uLocation.setProperty("x", x[0]);
            uLocation.setProperty("y", y[0]);

            // Next location is placed with an componentOffset
            x[0] += offset;
            y[0] += offset;

            return uLocation;
        };

        final BiFunction<com.uppaal.model.core2.Location, com.uppaal.model.core2.Location, com.uppaal.model.core2.Edge> generateEdge = (source, target) -> {
            final com.uppaal.model.core2.Edge edge = template.createEdge();
            template.insert(edge, null);
            edge.setSource(source);
            edge.setTarget(target);
            return edge;
        };

        final BiConsumer<com.uppaal.model.core2.Edge, String> addGuard = (edge, guard) -> {
            final int pX = (edge.getSource().getX() + edge.getTarget().getX()) / 2;
            final int pY = (edge.getSource().getY() + edge.getTarget().getY()) / 2;
            final Property p = edge.setProperty(GUARD_PROPERTY_TAG, guard);
            p.setProperty("x", pX);
            p.setProperty("y", pY);
        };

        final BiConsumer<com.uppaal.model.core2.Edge, String> addSync = (edge, sync) -> {
            final int pX = (edge.getSource().getX() + edge.getTarget().getX()) / 2;
            final int pY = (edge.getSource().getY() + edge.getTarget().getY()) / 2;
            final Property p = edge.setProperty(SYNC_PROPERTY_TAG, sync);
            p.setProperty("x", pX);
            p.setProperty("y", pY);
        };

        // Produce the four pseudo locations
        final com.uppaal.model.core2.Location enter = generatePseudoLocation.apply("Enter", true);
        final com.uppaal.model.core2.Location running = generatePseudoLocation.apply("Running", false);
        final com.uppaal.model.core2.Location exiting = generatePseudoLocation.apply("Exiting", false);
        final com.uppaal.model.core2.Location exit = generatePseudoLocation.apply("Exit", true);

        // Add invariant to the exit pseudo location
        exit.setProperty(INVARIANT_PROPERTY_TAG, targetSubComponent.getComponent().getFinalLocation().getInvariant());

        // Draw edge from enter to running
        final com.uppaal.model.core2.Edge enterToRunning = generateEdge.apply(enter, running);
        // Add a start broadcast channel for this procedure in the global declarations
        final String startSubProcedureChanName = "start" + subProcedureCount.get();
        addToGlobalDeclarations("broadcast chan " + startSubProcedureChanName + ";");
        addSync.accept(enterToRunning, startSubProcedureChanName + "!");

        // Add isDone boolean for this sub component. TODO: Loop through all subc components of the subprocedure here
        final String subComponentIsDoneBoolName = "isDone" + targetSubComponent.getIdentifier();
        addToGlobalDeclarations("bool " + subComponentIsDoneBoolName + ";");

        // Draw edge from running to exiting
        final com.uppaal.model.core2.Edge runningToExiting = generateEdge.apply(running, exiting);
        addGuard.accept(runningToExiting, "(" + subComponentIsDoneBoolName + ")"); // TODO bool loop
        addSync.accept(runningToExiting, SUBS_DONE_BROADCAST + "?");

        // Draw edge from running to it self
        final com.uppaal.model.core2.Edge runningToRunning = generateEdge.apply(running, running);
        addGuard.accept(runningToRunning, "!(" + subComponentIsDoneBoolName + ")"); // TODO bool loop
        addSync.accept(runningToRunning, SUBS_DONE_BROADCAST + "?");

        // Draw edge from exiting to exit
        final com.uppaal.model.core2.Edge exitingToExit = generateEdge.apply(exiting, exit);
        // Add an end broadcast channel for this procedure in the global declarations
        final String endSubProcedureChanName = "end" + subProcedureCount.get();
        addToGlobalDeclarations("broadcast chan " + endSubProcedureChanName + ";");
        addSync.accept(exitingToExit, endSubProcedureChanName + "!");

        // Increment the subProcedure count
        subProcedureCount.incrementAndGet();

        subComponentPseudoLocationMap.put(targetSubComponent, new Pair<>(enter, exit));
    }


    private void addLocationsToMaps(final Location hLocation, final com.uppaal.model.core2.Location uLocation) {
        final String serializedHLocationName = generateName(hLocation);
        hToULocations.put(serializedHLocationName, uLocation);
        uToHLocations.put(uLocation, hLocation);

        List<String> nameList;
        nameList = hLocationToFlattenedNames.get(hLocation);
        if(nameList == null) {
            nameList = new ArrayList<>();
            hLocationToFlattenedNames.put(hLocation, nameList);
        }
        nameList.add(serializedHLocationName);
    }

    private com.uppaal.model.core2.Location addLocation(final Template template, final Location hLocation, final int offset) {
        final int x = (int) hLocation.xProperty().get();
        final int y = (int) hLocation.yProperty().get() + offset;
        final Color color = hLocation.getColor().toAwtColor(hLocation.getColorIntensity());

        // Create new UPPAAL location and insert it into the template
        final com.uppaal.model.core2.Location uLocation = template.createLocation();
        template.insert(uLocation, null);

        // Set name of the location
        uLocation.setProperty(NAME_PROPERTY_TAG, generateName(hLocation));

        // Set the invariant if any
        if (hLocation.getInvariant() != null) {
            uLocation.setProperty(INVARIANT_PROPERTY_TAG, hLocation.getInvariant());
        }

        // Add committed property if location is committed
        if (hLocation.getUrgency().equals(Location.Urgency.COMMITTED)) {
            uLocation.setProperty(COMMITTED_PROPERTY_TAG, true);
        }

        // Add urgent property if location is urgent
        if (hLocation.getUrgency().equals(Location.Urgency.URGENT)) {
            uLocation.setProperty("urgent", true);
        }

        // Add initial property if location is initial
        if (subComponentList.empty() && hLocation.getType().equals(Location.Type.INITIAL)) {
            uLocation.setProperty("init", true);
        }

        // Update the placement of the name label
        final Property p = uLocation.getProperty(NAME_PROPERTY_TAG);
        p.setProperty("x", x);
        p.setProperty("y", y - 30);

        // Set the color of the location
        uLocation.setProperty("color", color);

        // Set the x and y properties
        uLocation.setProperty("x", x);
        uLocation.setProperty("y", y);

        return uLocation;
    }

    private com.uppaal.model.core2.Edge addEdge(final Template template, final Edge hEdge, final int offset) throws BackendException {
        return addEdge(template, hEdge, offset, null);
    }

    private com.uppaal.model.core2.Edge addEdge(final Template template, final Edge hEdge, final int offset, final com.uppaal.model.core2.Location fallBackLocation) throws BackendException {
        // Create new UPPAAL edge and insert it into the template
        final com.uppaal.model.core2.Edge uEdge = template.createEdge();
        template.insert(uEdge, null);

        final com.uppaal.model.core2.Location sourceULocation;
        final com.uppaal.model.core2.Location targetULocation;

        // Find the source locations
        if (hEdge.getSourceLocation() != null) {
            sourceULocation = hToULocations.get(generateName(hEdge.getSourceLocation()));
        } else if (fallBackLocation != null) {
            sourceULocation = fallBackLocation;
        } else {
            throw new BackendException("No source found");
        }

        // Find the target locations
        if (hEdge.getTargetLocation() != null) {
            targetULocation = hToULocations.get(generateName(hEdge.getTargetLocation()));
        } else if (fallBackLocation != null) {
            targetULocation = fallBackLocation;
        } else {
            throw new BackendException("No target found");
        }

        // Add the to the edge
        uEdge.setSource(sourceULocation);
        uEdge.setTarget(targetULocation);

        final List<Nail> reversedNails = new ArrayList<>();

        hEdge.getNails().forEach(nail -> reversedNails.add(0, nail));

        for (final Nail hNail : reversedNails) {

            // Create a Uppaal nail
            final com.uppaal.model.core2.Nail uNail = uEdge.createNail();
            uEdge.insert(uNail, null);

            final int x = (int) hNail.getX();
            final int y = ((int) hNail.getY()) + offset;

            // If the nail is a property nail and the edge have this property set, add it to the view
            if (!Strings.isNullOrEmpty(hEdge.getSelect()) && hNail.getPropertyType().equals(Edge.PropertyType.SELECTION)) {
                uEdge.setProperty("select", hEdge.getSelect());
                final Property p = uEdge.getProperty("select");
                p.setProperty("x", x + ((int) hNail.getPropertyX()));
                p.setProperty("y", y + ((int) hNail.getPropertyY()) + offset);
            }

            if (!Strings.isNullOrEmpty(hEdge.getGuard()) && hNail.getPropertyType().equals(Edge.PropertyType.GUARD)) {
                uEdge.setProperty(GUARD_PROPERTY_TAG, hEdge.getGuard());
                final Property p = uEdge.getProperty(GUARD_PROPERTY_TAG);
                p.setProperty("x", x + ((int) hNail.getPropertyX()));
                p.setProperty("y", y + ((int) hNail.getPropertyY()) + offset);
            }

            if (!Strings.isNullOrEmpty(hEdge.getSync()) && hNail.getPropertyType().equals(Edge.PropertyType.SYNCHRONIZATION)) {
                uEdge.setProperty(SYNC_PROPERTY_TAG, hEdge.getSync());
                final Property p = uEdge.getProperty(SYNC_PROPERTY_TAG);
                p.setProperty("x", x + ((int) hNail.getPropertyX()));
                p.setProperty("y", y + ((int) hNail.getPropertyY()) + offset);
            }

            if (!Strings.isNullOrEmpty(hEdge.getUpdate()) && hNail.getPropertyType().equals(Edge.PropertyType.UPDATE)) {
                uEdge.setProperty("assignment", hEdge.getUpdate());
                final Property p = uEdge.getProperty("assignment");
                p.setProperty("x", x + ((int) hNail.getPropertyX()));
                p.setProperty("y", y + ((int) hNail.getPropertyY()) + offset);
            }

            // Add the position of the nail
            uNail.setProperty("x", x);
            uNail.setProperty("y", y);

        }

        return uEdge;
    }

    public Document toUPPAALDocument() {
        return uppaalDocument;
    }

    public Location getLocation(final com.uppaal.model.core2.Location uLocation) {
        return uToHLocations.get(uLocation);
    }

    public Edge getEdge(final com.uppaal.model.core2.Edge uEdge) {
        return uToHEdges.get(uEdge);
    }

    public List<String> getFlattenedNames(final Location location) {
        List<String> result = hLocationToFlattenedNames.get(location);

        // If list does not exist in the map
        if(result == null) {
            result = new ArrayList<>();
        }
        return result;
    }
}
