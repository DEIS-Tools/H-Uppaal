package SW9.backend;

import SW9.model_canvas.ModelContainer;
import SW9.utility.colors.Color;
import com.uppaal.engine.*;
import com.uppaal.model.core2.*;
import com.uppaal.model.system.SystemLocation;
import com.uppaal.model.system.UppaalSystem;
import com.uppaal.model.system.symbolic.SymbolicState;
import com.uppaal.model.system.symbolic.SymbolicTransition;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UPPAALDriver {


    public static char hasDeadLock(ModelContainer modelContainer) {

        final String deadLockQueryString = "E<> deadlock";

        Map<String, Location> locationMap = new HashMap<>();

        Document doc = new Document(new PrototypeDocument());

        // add a TA template:
        Template t = doc.createTemplate();
        doc.insert(t, null);
        t.setProperty("name", "Test");

        for (SW9.model_canvas.locations.Location location : modelContainer.getLocations()) {

            String name = "L" + location.hashCode();
            Location l = addLocation(t, name, (int) location.xProperty().get(), (int) location.yProperty().get());
            l.setProperty("color", location.getColor().toAwtColor(Color.Intensity.I500));
            locationMap.put(name, l);

            if (location.type == SW9.model_canvas.locations.Location.Type.INITIAL) {
                l.setProperty("init", true);
            }

        }

        for (SW9.model_canvas.edges.Edge egde : modelContainer.getEdges()) {
            Location sourceLocation = locationMap.get("L" + egde.getSourceLocation().hashCode());
            Location targetLocation = locationMap.get("L" + egde.getTargetLocation().hashCode());
            addEdge(t, sourceLocation, targetLocation, null, null, null);
        }

        // add system declaration:
        doc.setProperty("system",
                "T=Test();\n" +
                        "system T;");


        try {

            File resultFile = new File("result.xml");
            // save the model into a file:
            doc.save(resultFile);

            // create a link to a local Uppaal process:
            Engine engine = new Engine();
            engine.setServerPath(getEnginePath());
            engine.connect();

            // compile the model into system:
            ArrayList<Problem> problems = new ArrayList<Problem>();
            UppaalSystem system = engine.getSystem(doc, problems);
            if (!problems.isEmpty()) {
                boolean fatal = false;
                System.out.println("There are problems with the document:");
                for (Problem p : problems) {
                    System.out.println(p.toString());
                    if (!"warning".equals(p.getType())) { // ignore warnings
                        fatal = true;
                    }
                }
                if (fatal) {
                    System.exit(1);
                }
            }

            engine.getInitialState(system);
            QueryVerificationResult result = engine.query(system, "", deadLockQueryString, emptyQueryFeedback());

            engine.disconnect(); // terminate the engine process
            return result.result;

        } catch (EngineException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }

        return 'E';
    }

    private static QueryFeedback emptyQueryFeedback() {
        return new QueryFeedback() {
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
        };
    }


    // From the Demo.Java file

    /**
     * Valid kinds of labels on locations.
     */
    public enum LKind {
        name, init, urgent, committed, invariant, exponentialrate, comments
    }

    /**
     * Valid kinds of labels on edges.
     */
    public enum EKind {
        select, guard, synchronisation, assignment, comments
    }

    /**
     * Sets a label on a location.
     *
     * @param l     the location on which the label is going to be attached
     * @param kind  a kind of the label
     * @param value the label value (either boolean or String)
     * @param x     the x coordinate of the label
     * @param y     the y coordinate of the label
     */
    public static void setLabel(Location l, LKind kind, Object value, int x, int y) {
        l.setProperty(kind.name(), value);
        Property p = l.getProperty(kind.name());
        p.setProperty("x", x);
        p.setProperty("y", y);
    }

    /**
     * Adds a location to a template.
     *
     * @param t    the template
     * @param name a name for the new location
     * @param x    the x coordinate of the location
     * @param y    the y coordinate of the location
     * @return the new location instance
     */
    public static Location addLocation(Template t, String name, int x, int y) {
        Location l = t.createLocation();
        t.insert(l, null);
        l.setProperty("x", x);
        l.setProperty("y", y);
        setLabel(l, LKind.name, name, x, y - 28);
        return l;
    }

    /**
     * Sets a label on an edge.
     *
     * @param e     the edge
     * @param kind  the kind of the label
     * @param value the content of the label
     * @param x     the x coordinate of the label
     * @param y     the y coordinate of the label
     */
    public static void setLabel(Edge e, EKind kind, String value, int x, int y) {
        e.setProperty(kind.name(), value);
        Property p = e.getProperty(kind.name());
        p.setProperty("x", x);
        p.setProperty("y", y);
    }

    /**
     * Adds an edge to the template
     *
     * @param t      the template where the edge belongs
     * @param source the source location
     * @param target the target location
     * @param guard  guard expression
     * @param sync   synchronization expression
     * @param update update expression
     * @return
     */
    public static Edge addEdge(Template t, Location source, Location target,
                               String guard, String sync, String update) {
        Edge e = t.createEdge();
        t.insert(e, null);
        e.setSource(source);
        e.setTarget(target);
        int x = (source.getX() + target.getX()) / 2;
        int y = (source.getY() + target.getY()) / 2;
        if (guard != null) {
            setLabel(e, EKind.guard, guard, x - 15, y - 28);
        }
        if (sync != null) {
            setLabel(e, EKind.synchronisation, sync, x - 15, y - 14);
        }
        if (update != null) {
            setLabel(e, EKind.assignment, update, x - 15, y);
        }
        return e;
    }

    public static void print(UppaalSystem sys, SymbolicState s) {
        System.out.print("(");
        for (SystemLocation l : s.getLocations()) {
            System.out.print(l.getName() + ", ");
        }
        int val[] = s.getVariables();
        for (int i = 0; i < sys.getNoOfVariables(); i++) {
            System.out.print(sys.getVariableName(i) + "=" + val[i] + ", ");
        }
        List<String> constraints = new ArrayList<String>();
        s.getPolyhedron().getAllConstraints(constraints);
        for (String constraint : constraints) {
            System.out.print(constraint + ", ");
        }
        System.out.println(")");
    }

    /**
     * Locates the path to engine for different platforms.
     */
    public static String getEnginePath() {
        String os = System.getProperty("os.name");
        URL url = ClassLoader.getSystemResource("com/uppaal/engine/Engine.class");
        try {
            url = new URL(url.getPath()); // strip jar scheme
        } catch (MalformedURLException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
        File file = new File(url.getPath());
        while (file != null && !("model.jar!".equals(file.getName())))
            file = file.getParentFile();
        if (file == null) {
            System.err.println("Could not locate the Uppaal installation path.");
            System.exit(1);
        }
        file = file.getParentFile(); // lib
        file = file.getParentFile(); // installation
        if (os.contains("Mac")) {
            file = new File(new File(file, "/libs/servers/bin-MacOS"), "server");
        } else if (os.contains("Linux")) {
            file = new File(new File(file, "/libs/servers/bin-Linux"), "server");
        } else {
            file = new File(new File(file, "/libs/servers/bin-Win32"), "server.exe");
        }
        return file.getPath();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Document doc = null;
        if (true) {
            // Generate hardcoded model:
            // create a new Uppaal model with default properties:
            doc = new Document(new PrototypeDocument());
            // add global variables:

            // add a TA template:
            Template t = doc.createTemplate();
            doc.insert(t, null);
            t.setProperty("name", "Experiment");
            // the template has initial location:
            Location l0 = addLocation(t, "L0", 0, 0);
            l0.setProperty("init", true);
            // add another location to the right:
            Location l1 = addLocation(t, "L1", 150, 0);

            Edge e = addEdge(t, l0, l1, null, null, null);

            // add system declaration:
            doc.setProperty("system",
                    "Exp1=Experiment();\n" +
                            "Exp2=Experiment();\n\n" +
                            "system Exp1, Exp2;");
        }
        // Some operations with the created model:
        try {
            File resultFile = new File("result.xml");
            // save the model into a file:
            doc.save(resultFile);
            // create a link to a local Uppaal process:
            Engine engine = new Engine();
            engine.setServerPath(getEnginePath());
            engine.connect();

            // compile the model into system:
            ArrayList<Problem> problems = new ArrayList<Problem>();
            UppaalSystem system = engine.getSystem(doc, problems);
            if (!problems.isEmpty()) {
                boolean fatal = false;
                System.out.println("There are problems with the document:");
                for (Problem p : problems) {
                    System.out.println(p.toString());
                    if (!"warning".equals(p.getType())) { // ignore warnings
                        fatal = true;
                    }
                }
                if (fatal) {
                    System.exit(1);
                }
            }

            // Det hårde adder fix
            engine.getInitialState(system);
            QueryVerificationResult result = engine.query(system, "", "A[]Exp1.L0", emptyQueryFeedback());
            System.out.println((result.result == 'T') ? "ja" : "nej");

            engine.disconnect(); // terminate the engine process
        } catch (EngineException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
