package SW9.backend;

import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;

import java.util.List;

public class Trace {

    private final Location location;
    private final List<Edge> edges;
    private final char result;

    public Trace(final List<Edge> edges, final Location location, final char result) {
        this.edges = edges;
        this.location = location;
        this.result = result;
    }

    public Location getLocation() {
        return location;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public char getResult() {
        return result;
    }
}
