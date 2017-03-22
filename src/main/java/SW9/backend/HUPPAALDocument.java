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

public class HUPPAALDocument {

    private static final String SUBS_DONE_BROADCAST = "subDone";
    private static final String DECLARATION_PROPERTY_TAG = "declaration";
    private static final String COMMITTED_PROPERTY_TAG = "committed";
    private static final String NAME_PROPERTY_TAG = "name";
    private static final String INVARIANT_PROPERTY_TAG = "invariant";
    private static final String GUARD_PROPERTY_TAG = "guard";
    private static final String SYNC_PROPERTY_TAG = "synchronisation";
    private static final String UPDATE_PROPERTY_TAG = "assignment";

    private final Document uppaalDocument = new Document(new PrototypeDocument());

    // Map to convert H-UPPAAL locations to UPPAAL locations
    private final Map<Location, com.uppaal.model.core2.Location> hToULocations = new HashMap<>();

    // Map to convert back from UPPAAL to H-UPPAAL items
    private final Map<com.uppaal.model.core2.Location, Location> uToHLocations = new HashMap<>();

    // Map to convert back from UPPAAL edges to H-UPPAAL edges
    private final Map<com.uppaal.model.core2.Edge, Edge> uToHEdges = new HashMap<>();

    // Map from location to all of its uppaal names
    private final Map<Location, List<String>> hLocationToFlattenedNames = new HashMap<>();

    // Map from subComponent to the Enter and Exit pseudo locations
    private final Map<String, Pair<com.uppaal.model.core2.Location, com.uppaal.model.core2.Location>> subComponentPseudoLocationMap = new HashMap<>();

    private final Component mainComponent;

    /**
     * Map used to store startX! and endX! channels
     */
    private final Map<String, Integer> subComponentIdentifiers = new HashMap<>();

    /**
     * Used to generate unique channel identifiers for starting and ending sub-procedures (synchronizations: startX! and endX!)
     */
    private AtomicInteger uniqueChannelIdentifier = new AtomicInteger(0);

    /**
     * Used to figure out the layering of sub components
     */
    private Stack<SubComponent> subComponentList = new Stack<>();

    private ArrayList<String> subComponentTemplates = new ArrayList<>();

    public HUPPAALDocument(final Component mainComponent) throws BackendException {
        this.mainComponent = mainComponent;
        generateUPPAALDocument();
    }

    private Document generateUPPAALDocument() throws BackendException {
        // Set create a template for each model container
        generateTemplate(mainComponent);

        // Generate the system declaration
        String systemDclString = "system ";

        // Add the main component process to the system declaration
        systemDclString += mainComponent.getName();

        // Append all of the sub component template strings (found in generateTemplate())
        for (final String subComponentTemplate : subComponentTemplates) {
            systemDclString += ", ";
            systemDclString += subComponentTemplate;
        }

        // Finish the system declaration
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

    private String generateName(final SubComponent component) {
        return generateName(component, false);
    }

    private String generateName(final SubComponent component, final boolean ignoreMe) {
        String result = "";

        // Add the identifier for each sub component (separated with underscore)
        for (final SubComponent subComponent : subComponentList) {
            if (!result.isEmpty()) {
                result += "_";
            }
            result += subComponent.getIdentifier();
        }

        if (!ignoreMe) {
            // Add the identifier for the component
            if (!result.isEmpty()) {
                result += "_";
            }
            result += component.getIdentifier();
        }

        // Return the result
        return result;
    }

    private void generateTemplate(final Component mainComponent) throws BackendException {
        final Template template = generateTemplate(mainComponent, null, false);
        template.setProperty(NAME_PROPERTY_TAG, mainComponent.getName());
    }

    private void generateTemplate(final SubComponent subComponent, final boolean isStarted) throws BackendException {
        subComponentList.add(subComponent);
        final Template template = generateTemplate(subComponent.getComponent(), subComponent, isStarted);
        subComponentList.remove(subComponent);

        final String subComponentTemplateName = generateName(subComponent);
        template.setProperty(NAME_PROPERTY_TAG, subComponentTemplateName);
        subComponentTemplates.add(subComponentTemplateName);
    }

    private Template generateTemplate(final Component component, final SubComponent subComponent, final boolean isStarted) throws BackendException {

        // Create empty template and insert it into the uppaal document
        final Template template = uppaalDocument.createTemplate();
        uppaalDocument.insert(template, null);

        if(subComponent != null) {
            template.setProperty(DECLARATION_PROPERTY_TAG, component.getDeclarations());
        } else {
            addToGlobalDeclarations(component.getDeclarations());
        }

        // Add all locations from the model to our conversion map and to the template
        for (final Location hLocation : component.getLocations()) {

            // Add the location to the template
            final com.uppaal.model.core2.Location uLocation = addLocation(template, hLocation, 0);

            // Populate the map
            addLocationsToMaps(hLocation, uLocation);
        }

        // Add the initial location to the template
        final Location hInitialLocation = component.getInitialLocation();
        final com.uppaal.model.core2.Location uInitialLocation = addLocation(template, hInitialLocation, 0);
        addLocationsToMaps(hInitialLocation, uInitialLocation);

        // Add the final location to the template
        final Location hFinalLocation = component.getFinalLocation();
        final com.uppaal.model.core2.Location uFinalLocation = addLocation(template, hFinalLocation, 0);
        addLocationsToMaps(hFinalLocation, uFinalLocation);

        // Find all edges going into the final location and make them go into SubUpdateFinished instead
        final List<Edge> ignoredEdges = component.getRelatedEdges(component.getFinalLocation());

        for (final Edge hEdge : component.getEdges()) {
            // Ignore edges being added in the sub component
            if (subComponent != null && ignoredEdges.contains(hEdge)) continue;

            // Draw edges that are purely location to location edges
            if (hEdge.getSourceLocation() != null && hEdge.getTargetLocation() != null) {
                uToHEdges.put(addEdge(template, hEdge, 0), hEdge);
            }

            // If the edge starts in a locations and ends in a sub component
            if (hEdge.getSourceLocation() != null && hEdge.getTargetSubComponent() != null) {

                // If we have not already created pseudo locations for this sub component
                if (!subComponentPseudoLocationMap.containsKey(generateName(hEdge.getTargetSubComponent()))) {
                    addPseudoLocationsForSubComponent(template, hEdge.getTargetSubComponent());
                }

                // Add an edge from the location to the pseudo enter location
                final com.uppaal.model.core2.Location pseudoEnter = subComponentPseudoLocationMap.get(generateName(hEdge.getTargetSubComponent())).getKey();
                addEdge(template, hEdge, 0, pseudoEnter);
            }

            // If the edge starts somewhere and ends in a fork
            if (hEdge.getTargetJork() != null && hEdge.getTargetJork().getType().equals(Jork.Type.FORK)) {
                // Find all outgoing edges from this fork and make sure that they are sub-components
                final List<SubComponent> subComponentsToRunInParallel = new ArrayList<>();
                for (final Edge edge : component.getOutGoingEdges(hEdge.getTargetJork())) {
                    if (edge.getTargetSubComponent() != null) {
                        subComponentsToRunInParallel.add(edge.getTargetSubComponent());
                    } else {
                        throw new BackendException("Fork has an edge to something that is not a subcomponent");
                    }
                }

                // If we have not already created pseudo locations for this sub component
                if (!subComponentPseudoLocationMap.containsKey(generateName(subComponentsToRunInParallel.get(0)))) {
                    addPseudoLocationsForSubComponent(template, subComponentsToRunInParallel);
                }

                // Add an edge from the source to the pseudo enter location
                final com.uppaal.model.core2.Location pseudoEnter = subComponentPseudoLocationMap.get(generateName(subComponentsToRunInParallel.get(0))).getKey();
                addEdge(template, hEdge, 0, pseudoEnter);
            }
        }

        // Draw edges from a subComponent to locations
        for (final Edge hEdge : component.getEdges()) {
            // Ignore edges being added in the sub component
            if (subComponent != null && ignoredEdges.contains(hEdge)) continue;

            // If the edge starts in a sub component and ends in a location
            if (hEdge.getSourceSubComponent() != null && hEdge.getTargetLocation() != null) {
                // Add an edge from the pseudo exit location to the location
                final com.uppaal.model.core2.Location pseudoExit = subComponentPseudoLocationMap.get(generateName(hEdge.getSourceSubComponent())).getValue();
                addEdge(template, hEdge, 0, pseudoExit);
            }

            // If the edge starts in a join and ends somewhere
            if (hEdge.getSourceJork() != null && hEdge.getSourceJork().getType().equals(Jork.Type.JOIN)) {
                // Find all outgoing edges from this fork and make sure that they are sub-components
                final List<SubComponent> subComponentsToRunInParallel = new ArrayList<>();
                for (final Edge edge : component.getIncomingEdges(hEdge.getSourceJork())) {
                    if (edge.getSourceSubComponent() != null) {
                        subComponentsToRunInParallel.add(edge.getSourceSubComponent());
                    } else {
                        throw new BackendException("Join has an edge from something that is not a subcomponent");
                    }
                }

                // Add an edge from the pseudo exit location to the target
                final com.uppaal.model.core2.Location pseudoExit = subComponentPseudoLocationMap.get(generateName(subComponentsToRunInParallel.get(0))).getValue();
                addEdge(template, hEdge, 0, pseudoExit);
            }
        }

        // Generate templates for all sub components
        for (final SubComponent subComponent1 : component.getSubComponents()) {
            boolean isSubcomponent1Started = false;
            for (final Edge edge : component.getRelatedEdges(subComponent1)) {
                if (edge.getSourceJork() != null || edge.getSourceLocation() != null) {
                    isSubcomponent1Started = true;
                    break;
                }
            }
            generateTemplate(subComponent1, isSubcomponent1Started);
        }

        // Add pseudo locations for being a sub component
        if (subComponent != null && isStarted) {
            final int offset = 300;

            final Location initialLocation = component.getInitialLocation();
            final Location finalLocation = component.getFinalLocation();

            // Add the three pseudo locations
            final com.uppaal.model.core2.Location subStart = generatePseudoLocationInTemplate(template, "SubStart", false, initialLocation.getX() - offset, initialLocation.getY());
            subStart.setProperty("init", true);
            final com.uppaal.model.core2.Location subUpdateFinished = generatePseudoLocationInTemplate(template, "SubUpdateFinished", true, finalLocation.getX() + offset * 2, finalLocation.getY());
            final com.uppaal.model.core2.Location subIndicateDone = generatePseudoLocationInTemplate(template, "SubIndicateDone", true, finalLocation.getX() + offset, finalLocation.getY());

            // Add edges between the pseudo locations
            final com.uppaal.model.core2.Edge subStartToInitial = generateEdgeInTemplate(template, subStart, hToULocations.get(initialLocation));
            addPropertyToEdge(subStartToInitial, SYNC_PROPERTY_TAG, "start" + subComponentIdentifiers.get(generateName(subComponent, true)) + "?");

            final com.uppaal.model.core2.Edge subUpdateFinishedToSubIndicateDone = generateEdgeInTemplate(template, subUpdateFinished, subIndicateDone);
            addPropertyToEdge(subUpdateFinishedToSubIndicateDone, UPDATE_PROPERTY_TAG, "isDone" + generateName(subComponent, true) + " = true");

            final com.uppaal.model.core2.Edge subIndicateDoneToFinal = generateEdgeInTemplate(template, subIndicateDone, hToULocations.get(finalLocation));
            addPropertyToEdge(subIndicateDoneToFinal, SYNC_PROPERTY_TAG, SUBS_DONE_BROADCAST + "!");

            // Add the pseudo edge from the final location to the subStart pseudo location
            final com.uppaal.model.core2.Edge finalToSubStart = generateEdgeInTemplate(template, hToULocations.get(finalLocation), subStart);
            addPropertyToEdge(finalToSubStart, UPDATE_PROPERTY_TAG, "isDone" + generateName(subComponent, true) + " = false");
            addPropertyToEdge(finalToSubStart, SYNC_PROPERTY_TAG, "end" + subComponentIdentifiers.get(generateName(subComponent, true)) + "?");

            final Property syncProperty = finalToSubStart.getProperty(SYNC_PROPERTY_TAG);
            syncProperty.setProperty("x", subStart.getX() + 15);
            syncProperty.setProperty("y", subIndicateDone.getY() - 20);

            final Property updateProperty = finalToSubStart.getProperty(UPDATE_PROPERTY_TAG);
            updateProperty.setProperty("x", subStart.getX() + 15);
            updateProperty.setProperty("y", subIndicateDone.getY());

            final com.uppaal.model.core2.Nail nail = finalToSubStart.createNail();
            finalToSubStart.insert(nail, null);
            nail.setProperty("x", subStart.getX());
            nail.setProperty("y", subIndicateDone.getY());

            for (final Edge hEdge : ignoredEdges) {
                // From location
                if (hEdge.getSourceLocation() != null) {
                    final com.uppaal.model.core2.Edge edge = generateEdgeInTemplate(template, hToULocations.get(hEdge.getSourceLocation()), subUpdateFinished);
                    annotateEdge(edge, hEdge, 0);
                }
                // From sub component
                else if (hEdge.getSourceSubComponent() != null) {
                    final com.uppaal.model.core2.Location pseudoExit = subComponentPseudoLocationMap.get(generateName(hEdge.getSourceSubComponent())).getValue();
                    final com.uppaal.model.core2.Edge edge = generateEdgeInTemplate(template, pseudoExit, subUpdateFinished);
                    annotateEdge(edge, hEdge, 0);
                }

            }

        }

        return template;
    }

    private com.uppaal.model.core2.Location generatePseudoLocationInTemplate(final Template template, final String name, final boolean isCommitted, final double x, final double y) {
        final com.uppaal.model.core2.Location uLocation = template.createLocation();
        template.insert(uLocation, null);

        if (isCommitted) {
            uLocation.setProperty(COMMITTED_PROPERTY_TAG, true);
        }

        final int xAsInt = (int) x;
        final int yAsInt = (int) y;

        /* todo: navne her
        // Add the name label
        final Property p = uLocation.setProperty(NAME_PROPERTY_TAG, name);
        p.setProperty("x", xAsInt);
        p.setProperty("y", yAsInt);*/

        // Update the placement of location
        uLocation.setProperty("x", xAsInt);
        uLocation.setProperty("y", yAsInt);

        return uLocation;
    }

    private com.uppaal.model.core2.Edge generateEdgeInTemplate(final Template template, final com.uppaal.model.core2.Location source, final com.uppaal.model.core2.Location target) {
        final com.uppaal.model.core2.Edge edge = template.createEdge();

        template.insert(edge, null);
        edge.setSource(source);
        edge.setTarget(target);

        return edge;
    }

    private void addPropertyToEdge(final com.uppaal.model.core2.Edge edge, final String propertyTag, final String property) {
        final int pX = (edge.getSource().getX() + edge.getTarget().getX()) / 2;
        int pY = (edge.getSource().getY() + edge.getTarget().getY()) / 2;

        if (edge.getSource().equals(edge.getTarget())) {
            pY -= 40;
        }

        if (propertyTag.equals(GUARD_PROPERTY_TAG)) {
            pY -= 15;
        }

        final Property p = edge.setProperty(propertyTag, property);
        p.setProperty("x", pX - 10);
        p.setProperty("y", pY - 10);
    }

    private void addPseudoLocationsForSubComponent(final Template template, final SubComponent targetSubComponent) {
        addPseudoLocationsForSubComponent(template, new ArrayList<SubComponent>() {{
            add(targetSubComponent);
        }});
    }

    private void addPseudoLocationsForSubComponent(final Template template, final List<SubComponent> targetSubComponents) {
        // Styling properties (used to place them in the uppaal document)
        final int x = (int) targetSubComponents.get(0).getX();
        final int y = (int) targetSubComponents.get(0).getY();

        // Produce the four pseudo locations
        final com.uppaal.model.core2.Location enter = generatePseudoLocationInTemplate(template, targetSubComponents.get(0).getIdentifier() + "_Enter", true, x, y);
        final com.uppaal.model.core2.Location running = generatePseudoLocationInTemplate(template, targetSubComponents.get(0).getIdentifier() + "_Running", false, x + 40, y + 40);
        final com.uppaal.model.core2.Location exiting = generatePseudoLocationInTemplate(template, targetSubComponents.get(0).getIdentifier() + "_Exiting", false, x + 100, y + 100);
        final com.uppaal.model.core2.Location exit = generatePseudoLocationInTemplate(template, targetSubComponents.get(0).getIdentifier() + "_Exit", true, x + 140, y + 140);

        // Add invariant to the exit pseudo location
        String finalLocationInvariants = "";
        for (final SubComponent subComponent : targetSubComponents) {
            if (!finalLocationInvariants.isEmpty()) {
                finalLocationInvariants += " && ";
            }
            finalLocationInvariants += subComponent.getComponent().getFinalLocation().getInvariant();
        }

        exit.setProperty(INVARIANT_PROPERTY_TAG, finalLocationInvariants);

        // Generate a new identifier for the collection of sub-components
        final int id = uniqueChannelIdentifier.getAndIncrement();

        // Store identifier for all sub components so that they know which channels to sync on
        targetSubComponents.forEach(subComponent -> subComponentIdentifiers.put(generateName(subComponent), id));

        // Draw edge from enter to running
        final com.uppaal.model.core2.Edge enterToRunning = generateEdgeInTemplate(template, enter, running);
        // Add a start broadcast channel for this procedure in the global declarations
        final String startSubProcedureChanName = "start" + id;
        addToGlobalDeclarations("broadcast chan " + startSubProcedureChanName + ";");
        addPropertyToEdge(enterToRunning, SYNC_PROPERTY_TAG, startSubProcedureChanName + "!");

        // List to store the variables indicating when sub components are done
        final List<String> isDoneBooleans = new ArrayList<>();

        // Loop through the provided sub components, generating guards and sync for them
        for (final SubComponent targetSubComponent : targetSubComponents) {
            // Add isDone boolean for this sub component.
            final String subComponentIsDoneBoolName = "isDone" + generateName(targetSubComponent);
            addToGlobalDeclarations("bool " + subComponentIsDoneBoolName + " = false;");

            // Add the variable to the list
            isDoneBooleans.add(subComponentIsDoneBoolName);
        }

        // Generate the junction of all the booleans (&& between all boolean variables)
        final String allSubComponentsDoneBoolean = String.join(" && ", isDoneBooleans);

        // Draw edge from running to exiting
        final com.uppaal.model.core2.Edge runningToExiting = generateEdgeInTemplate(template, running, exiting);
        addPropertyToEdge(runningToExiting, GUARD_PROPERTY_TAG, "(" + allSubComponentsDoneBoolean + ")"); // All sub components are done
        addPropertyToEdge(runningToExiting, SYNC_PROPERTY_TAG, SUBS_DONE_BROADCAST + "?");

        // Draw edge from running to it self
        final com.uppaal.model.core2.Edge runningToRunning = generateEdgeInTemplate(template, running, running);
        addPropertyToEdge(runningToRunning, GUARD_PROPERTY_TAG, "!(" + allSubComponentsDoneBoolean + ")"); // At least one sub component is not done
        addPropertyToEdge(runningToRunning, SYNC_PROPERTY_TAG, SUBS_DONE_BROADCAST + "?");

        // Draw edge from exiting to exit
        final com.uppaal.model.core2.Edge exitingToExit = generateEdgeInTemplate(template, exiting, exit);
        // Add an end broadcast channel for this procedure in the global declarations
        final String endSubProcedureChanName = "end" + id;
        addToGlobalDeclarations("broadcast chan " + endSubProcedureChanName + ";");
        addPropertyToEdge(exitingToExit, SYNC_PROPERTY_TAG, endSubProcedureChanName + "!");

        subComponentPseudoLocationMap.put(generateName(targetSubComponents.get(0)), new Pair<>(enter, exit));
    }

    private void addLocationsToMaps(final Location hLocation, final com.uppaal.model.core2.Location uLocation) {
        final String serializedHLocationName = generateName(hLocation);
        hToULocations.put(hLocation, uLocation);
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
        uLocation.setProperty(NAME_PROPERTY_TAG, hLocation.getId());

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
        if (hLocation.getType().equals(Location.Type.INITIAL)) {
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
            sourceULocation = hToULocations.get(hEdge.getSourceLocation());
        } else if (fallBackLocation != null) {
            sourceULocation = fallBackLocation;
        } else {
            throw new BackendException("No source found");
        }

        // Find the target locations
        if (hEdge.getTargetLocation() != null) {
            targetULocation = hToULocations.get(hEdge.getTargetLocation());
        } else if (fallBackLocation != null) {
            targetULocation = fallBackLocation;
        } else {
            throw new BackendException("No target found");
        }

        // Add the to the edge
        uEdge.setSource(sourceULocation);
        uEdge.setTarget(targetULocation);

        annotateEdge(uEdge, hEdge, offset);

        return uEdge;
    }

    private void annotateEdge(final com.uppaal.model.core2.Edge uEdge, final Edge hEdge, final int offset) {
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
                uEdge.setProperty(UPDATE_PROPERTY_TAG, hEdge.getUpdate());
                final Property p = uEdge.getProperty(UPDATE_PROPERTY_TAG);
                p.setProperty("x", x + ((int) hNail.getPropertyX()));
                p.setProperty("y", y + ((int) hNail.getPropertyY()) + offset);
            }

            // Add the position of the nail
            uNail.setProperty("x", x);
            uNail.setProperty("y", y);

        }
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

}
