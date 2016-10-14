package SW9.backend;

import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;

import java.util.List;

public class Trace {

    private final List<Location> locations;
    private final List<Edge> edges;
    private final char result;

    public Trace(final List<Location> locations, final List<Edge> edges, final char result) {
        this.locations = locations;
        this.edges = edges;
        this.result = result;
    }

    public List<Location> getLocation() {
        return locations;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public char getResult() {
        return result;
    }
}
