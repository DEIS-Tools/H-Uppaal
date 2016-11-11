package SW9.backend;

import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import com.uppaal.model.system.SystemEdgeSelect;
import com.uppaal.model.system.SystemLocation;
import com.uppaal.model.system.symbolic.SymbolicState;
import com.uppaal.model.system.symbolic.SymbolicTransition;

import java.util.ArrayList;
import java.util.List;

public class Trace {

    public class Transition {
        private final List<Location> sourceLocations = new ArrayList<>();
        private final List<Location> targetLocations = new ArrayList<>();
        private final List<Edge> edges = new ArrayList<>();

        private Transition(final SymbolicTransition symbolicTransition, final HUPPAALDocument huppaalDocument) {

            final SymbolicState sourceState = symbolicTransition.getSource();
            final SymbolicState targetState = symbolicTransition.getTarget();

            final SystemEdgeSelect[] chosenEdges = symbolicTransition.getEdges();

            if (sourceState != null) {
                for (final SystemLocation sourceSystemLocation : sourceState.getLocations()) {
                    sourceLocations.add(huppaalDocument.getLocation(sourceSystemLocation.getLocation()));
                }
            }

            if (targetState != null) {
                for (final SystemLocation targetSystemLocation : targetState.getLocations()) {
                    targetLocations.add(huppaalDocument.getLocation(targetSystemLocation.getLocation()));
                }
            }

            if (chosenEdges != null) {
                for (final SystemEdgeSelect chosenEdge : chosenEdges) {
                    edges.add(huppaalDocument.getEdge(chosenEdge.getEdge()));
                }
            }
        }

        public List<Location> getTargetLocations() {
            return targetLocations;
        }

        public List<Location> getSourceLocation() {
            return sourceLocations;
        }

        public List<Edge> getEdges() {
            return edges;
        }
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    private List<Transition> transitions = new ArrayList<>();

    public Trace(final List<SymbolicTransition> symbolicTransitions, final HUPPAALDocument huppaalDocument) {
        symbolicTransitions.forEach(symbolicTransition -> transitions.add(new Transition(symbolicTransition, huppaalDocument)));
    }
}
