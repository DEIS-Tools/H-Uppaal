package SW9.backend;

import SW9.abstractions.*;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.abstractions.Nail;
import com.google.common.base.Strings;
import com.uppaal.model.core2.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class HUPPAALDocument {

    private final Document uppaalDocument = new Document(new PrototypeDocument());
    // Map to convert H-UPPAAL locations to UPPAAL locations
    private final Map<String, com.uppaal.model.core2.Location> hToULocations = new HashMap<>();
    // Map to convert back from UPPAAL to H-UPPAAL items
    private final Map<com.uppaal.model.core2.Location, Location> uToHLocations = new HashMap<>();
    // Map to convert back from UPPAAL edges to H-UPPAAL edges
    private final Map<com.uppaal.model.core2.Edge, Edge> uToHEdges = new HashMap<>();
    private final Component mainComponent;
    /**
     * Used to figure out the layering of sub components
     */
    private Stack<SubComponent> subComponentList = new Stack<>();
    private int offset = 10;

    public HUPPAALDocument(final Component mainComponent) throws BackendException {
        this.mainComponent = mainComponent;
        generateUPPAALDocument();
    }

    private Document generateUPPAALDocument() throws BackendException {
        // Set create a template for each model container
        final Template template = generateTemplate(mainComponent);
        template.setProperty("name", mainComponent.getName() + "Template");

        String systemDclString = mainComponent.getName() + " = " + mainComponent.getName() + "Template();\n";

        // Generate the system declaration
        systemDclString += "system ";
        systemDclString += mainComponent.getName();
        systemDclString += ";";

        // Set the system declaration
        uppaalDocument.setProperty("system", systemDclString);

        return uppaalDocument;
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


        final String declarations = mainComponent.getDeclarations();
        template.setProperty("declaration", declarations);

        // Add all locations from the model to our conversion map and to the template
        for (final Location hLocation : mainComponent.getLocations()) {

            // Add the location to the template
            final com.uppaal.model.core2.Location uLocation = addLocation(template, hLocation, 0);

            // Populate the map
            hToULocations.put(generateName(hLocation), uLocation);
            uToHLocations.put(uLocation, hLocation);
        }

        // Add the initial location to the template
        final Location initialLocation = mainComponent.getInitialLocation();
        final com.uppaal.model.core2.Location uLocation1 = addLocation(template, initialLocation, 0);
        hToULocations.put(generateName(initialLocation), uLocation1);
        uToHLocations.put(uLocation1, initialLocation);

        // Add the final location to the template
        final Location finalLocation = mainComponent.getFinalLocation();
        final com.uppaal.model.core2.Location uLocation2 = addLocation(template, finalLocation, 0);
        hToULocations.put(generateName(finalLocation), uLocation2);
        uToHLocations.put(uLocation2, finalLocation);

        // todo: Add sub-components here
        for (final SubComponent subComponent : mainComponent.getSubComponents()) {
            addSubComponentToTemplate(template, subComponent);
        }

        for (final Edge hEdge : mainComponent.getEdges()) {
            uToHEdges.put(addEdge(template, hEdge, 0), hEdge);
        }

        return template;
    }

    private void addSubComponentToTemplate(final Template template, final SubComponent subComponent) throws BackendException {
        // todo få declarations med

        // Add the sub component to the stack
        subComponentList.push(subComponent);

        final Component component = subComponent.getComponent();
        offset += component.getHeight() + 10;

        // Add all locations from the model to our conversion map and to the template
        for (final Location hLocation : component.getLocations()) {

            // Add the location to the template
            final com.uppaal.model.core2.Location uLocation = addLocation(template, hLocation, offset);

            // Populate the map
            hToULocations.put(generateName(hLocation), uLocation);
            uToHLocations.put(uLocation, hLocation);
        }

        // Add the initial location to the template
        final Location initialLocation = component.getInitialLocation();
        final com.uppaal.model.core2.Location uLocation1 = addLocation(template, initialLocation, offset);
        hToULocations.put(generateName(initialLocation), uLocation1);
        uToHLocations.put(uLocation1, initialLocation);

        // Add the final location to the template
        final Location finalLocation = component.getFinalLocation();
        final com.uppaal.model.core2.Location uLocation2 = addLocation(template, finalLocation, offset);
        hToULocations.put(generateName(finalLocation), uLocation2);
        uToHLocations.put(uLocation2, finalLocation);

        // todo: Add sub-components here
        for (final SubComponent nestedSubComponents : subComponent.getComponent().getSubComponents()) {
            addSubComponentToTemplate(template, nestedSubComponents);
        }

        for (final Edge hEdge : component.getEdges()) {
            uToHEdges.put(addEdge(template, hEdge, offset), hEdge);
        }

        // Remove the sub component from the list
        subComponentList.pop();
    }

    private com.uppaal.model.core2.Location addLocation(final Template template, final Location hLocation, final int offset) {
        final int x = (int) hLocation.xProperty().get();
        final int y = (int) hLocation.yProperty().get() + offset;
        final Color color = hLocation.getColor().toAwtColor(hLocation.getColorIntensity());

        // Create new UPPAAL location and insert it into the template
        final com.uppaal.model.core2.Location uLocation = template.createLocation();
        template.insert(uLocation, null);

        // Set name of the location
        uLocation.setProperty("name", generateName(hLocation));

        // Set the invariant if any
        if (hLocation.getInvariant() != null) {
            uLocation.setProperty("invariant", hLocation.getInvariant());
        }

        // Add committed property if location is committed
        if (hLocation.getUrgency().equals(Location.Urgency.COMMITTED)) {
            uLocation.setProperty("committed", true);
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
        final Property p = uLocation.getProperty("name");
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
        // Create new UPPAAL edge and insert it into the template
        final com.uppaal.model.core2.Edge uEdge = template.createEdge();
        template.insert(uEdge, null);

        final com.uppaal.model.core2.Location sourceULocation;
        final com.uppaal.model.core2.Location targetULocation;

        // Find the source locations
        if (hEdge.getSourceLocation() != null) {
            sourceULocation = hToULocations.get(generateName(hEdge.getSourceLocation()));
        } else if (hEdge.getSourceSubComponent() != null) {
            subComponentList.push(hEdge.getSourceSubComponent());
            sourceULocation = hToULocations.get(generateName(hEdge.getSourceSubComponent().getComponent().getFinalLocation()));
            subComponentList.pop();
        } else {
            throw new BackendException("No source found");
        }

        // Find the target locations
        if (hEdge.getTargetLocation() != null) {
            targetULocation = hToULocations.get(generateName(hEdge.getTargetLocation()));
        } else if (hEdge.getTargetSubComponent() != null) {
            subComponentList.push(hEdge.getTargetSubComponent());
            targetULocation = hToULocations.get(generateName(hEdge.getTargetSubComponent().getComponent().getInitialLocation()));
            subComponentList.pop();
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
                uEdge.setProperty("guard", hEdge.getGuard());
                final Property p = uEdge.getProperty("guard");
                p.setProperty("x", x + ((int) hNail.getPropertyX()));
                p.setProperty("y", y + ((int) hNail.getPropertyY()) + offset);
            }

            if (!Strings.isNullOrEmpty(hEdge.getSync()) && hNail.getPropertyType().equals(Edge.PropertyType.SYNCHRONIZATION)) {
                uEdge.setProperty("synchronisation", hEdge.getSync());
                final Property p = uEdge.getProperty("synchronisation");
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

    private class SubComponentLocationPair {
        private SubComponent subComponent;
        private Location location;

        public SubComponentLocationPair(final SubComponent subComponent, final Location location) {
            this.subComponent = subComponent;
            this.location = location;
        }

        public SubComponent getSubComponent() {
            return subComponent;
        }

        public Location getLocation() {
            return location;
        }

        @Override
        public boolean equals(final Object obj) {
            if (super.equals(obj)) {
                return true;
            }

            if (obj instanceof SubComponentLocationPair) {
                final SubComponentLocationPair subComponentLocationPair = (SubComponentLocationPair) obj;
                return (subComponent.equals(subComponentLocationPair.subComponent) && location.equals(subComponentLocationPair.location));
            }
            return false;
        }
    }
}
