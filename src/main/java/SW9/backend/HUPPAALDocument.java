package SW9.backend;

import SW9.model_canvas.Component;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import com.uppaal.model.core2.Document;
import com.uppaal.model.core2.Property;
import com.uppaal.model.core2.PrototypeDocument;
import com.uppaal.model.core2.Template;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HUPPAALDocument {

    private final Document uppaalDocument = new Document(new PrototypeDocument());

    // Maps to convert H-UPPAAL locations to UPPAAL locations
    private final Map<Location, com.uppaal.model.core2.Location> hToULocations = new HashMap<>();

    // Maps to convert back from UPPAAL to H-UPPAAL items
    private final Map<com.uppaal.model.core2.Location, Location> uToHLocations = new HashMap<>();
    private final Map<com.uppaal.model.core2.Edge, Edge> uToHEdges = new HashMap<>();

    private final List<Component> components;

    public HUPPAALDocument(final List<Component> components) {
        this.components = components;
        generateUPPAALDocument();
    }

    private Document generateUPPAALDocument() {
        for (final Component component : components) {
            // Set create a template for each model container
            final Template template = generateTemplate(component);
            template.setProperty("name", component.getName() + "Template");
        }

        String systemDclString = "\n";
        for (final Component component : components) {
            systemDclString += component.getName() + " = " + component.getName() + "Template();\n";
        }

        systemDclString += "system ";
        for (int i = 0; i < components.size(); i++) {
            if (i != 0) {
                systemDclString += ", ";
            }
            systemDclString += components.get(i).getName();
        }

        systemDclString += ";";// Set the system declaration

        uppaalDocument.setProperty("system", systemDclString);

        return uppaalDocument;
    }

    private Template generateTemplate(final Component component) {

        // Create empty template and insert it into the uppaal document
        final Template template = uppaalDocument.createTemplate();
        uppaalDocument.insert(template, null);

        template.setProperty("declaration", generateTemplateDeclaration(component));

        // Add all locations from the model container to our conversion map and to the template
        for (final Location hLocation : component.getLocations()) {

            // Add the location to the template
            final com.uppaal.model.core2.Location uLocation = addLocation(template, hLocation, "L" + hToULocations.size());

            // Populate the map
            hToULocations.put(hLocation, uLocation);
            uToHLocations.put(uLocation, hLocation);
        }

        for (final Edge hEdge : component.getEdges()) {
            uToHEdges.put(addEdge(template, hEdge, hToULocations), hEdge);
        }

        return template;
    }

    private String generateTemplateDeclaration(final SW9.model_canvas.Component component) {

        // TODO update the types of variables (int, byte etc) and channels (urgent) when added to the model
        String declStr = "";

        // Add the clocks
        for (final String clock : component.getClocks()) {
            declStr += "clock " + clock + ";\n";
        }

        // Add variables
        for (final String var : component.getVariables()) {
            declStr += "int " + var + ";\n";
        }

        // Add channels
        for (final String chan : component.getChannels()) {
            declStr += "chan " + chan + ";\n";
        }

        return declStr;
    }

    private com.uppaal.model.core2.Location addLocation(final Template template, final Location hLocation, final String fallbackName) {
        final int x = (int) hLocation.xProperty().get();
        final int y = (int) hLocation.yProperty().get();
        final Color color = hLocation.getColor().toAwtColor(hLocation.getColorIntensity());

        // Create new UPPAAL location and insert it into the template
        final com.uppaal.model.core2.Location uLocation = template.createLocation();
        template.insert(uLocation, null);

        // Set name of the location
        if (hLocation.getName() != null) {
            uLocation.setProperty("name", hLocation.getName());
        } else {
            uLocation.setProperty("name", fallbackName);
        }

        // Set the invariant if any
        if (hLocation.getInvariant() != null) {
            uLocation.setProperty("invariant", hLocation.getInvariant());
        }

        // Add committed property if location is committed
        if (hLocation.isCommitted()) {
            uLocation.setProperty("committed", true);
        }

        // Add urgent property if location is urgent
        if (hLocation.isUrgent()) {
            uLocation.setProperty("urgent", true);
        }

        // Add initial property if location is initial
        if (hLocation.isInitial()) {
            uLocation.setProperty("init", true);
        }

        // Update the placement of the name label
        Property p = uLocation.getProperty("name");
        p.setProperty("x", x);
        p.setProperty("y", y - 30);

        // Set the color of the location
        uLocation.setProperty("color", color);

        // Set the x and y properties
        uLocation.setProperty("x", x);
        uLocation.setProperty("y", y);

        return uLocation;
    }

    private com.uppaal.model.core2.Edge addEdge(final Template template, final Edge hEdge, final Map<Location, com.uppaal.model.core2.Location> hToULocations) {
        // Create new UPPAAL edge and insert it into the template
        final com.uppaal.model.core2.Edge uEdge = template.createEdge();
        template.insert(uEdge, null);

        // Find the source and target locations from the map
        final com.uppaal.model.core2.Location sourceULocation = hToULocations.get(hEdge.getSourceLocation());
        final com.uppaal.model.core2.Location targetULocation = hToULocations.get(hEdge.getTargetLocation());

        // Add the to the edge
        uEdge.setSource(sourceULocation);
        uEdge.setTarget(targetULocation);

        final int x = (sourceULocation.getX() + targetULocation.getX()) / 2;
        final int y = (sourceULocation.getY() + targetULocation.getY()) / 2;

        if (hEdge.getSelect() != null) {
            uEdge.setProperty("select", hEdge.getSelect());
            final Property p = uEdge.getProperty("select");
            p.setProperty("x", x - 15);
            p.setProperty("y", y - 42);
        }

        if (hEdge.getGuard() != null) {
            uEdge.setProperty("guard", hEdge.getGuard());
            final Property p = uEdge.getProperty("guard");
            p.setProperty("x", x - 15);
            p.setProperty("y", y - 28);
        }

        if (hEdge.getSync() != null) {
            uEdge.setProperty("synchronisation", hEdge.getSync());
            final Property p = uEdge.getProperty("synchronisation");
            p.setProperty("x", x - 15);
            p.setProperty("y", y - 14);
        }

        if (hEdge.getUpdate() != null) {
            uEdge.setProperty("assignment", hEdge.getUpdate());
            final Property p = uEdge.getProperty("assignment");
            p.setProperty("x", x - 15);
            p.setProperty("y", y);
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
}
