package SW9.utility.helpers;

import SW9.abstractions.Edge;
import SW9.abstractions.Nail;
import SW9.utility.keyboard.KeyboardTracker;
import javafx.util.Pair;

import java.util.*;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class NailHelper {

    public static int REQUIRED_NAILS = 4;

    public static void addMissingNails(final Edge unfinishedEdge) {
        KeyboardTracker.unregisterKeybind(KeyboardTracker.ABANDON_EDGE);

        // Maps index of a nail to a list of potential new nails before that nail
        final Map<Integer, List<Pair<Double, Double>>> nailIndexToPotentialNewNailsMap = new HashMap<>();

        // Run through all the segments that we have (between all nails, and from the locations of the edge)
        int nailIndex = 0; // Counts the index of the nail in the edge we find segments for
        int totalPotentialNails = 0; // A counter to count total amount of new potential nails
        Circular from = unfinishedEdge.getSourceCircular(); // We start from source locations
        for (final Nail nail : unfinishedEdge.getNails()) { // Run through all nails

            // Find the index of the nail at hans
            final List<Pair<Double, Double>> potentialNails = getPotentialNailSegments(from, nail);
            totalPotentialNails += potentialNails.size(); // Increment the total potential nails
            nailIndexToPotentialNewNailsMap.put(nailIndex, potentialNails); // Add potential segment the list to the map
            from = nail; // In the next iteration this nail is the start of the segment
            nailIndex++;
        }

        // Find the last segment from the last nail (or source location given no nails) to the target locations
        final Circular end = unfinishedEdge.getTargetCircular();
        final List<Pair<Double, Double>> potentialNails = getPotentialNailSegments(from, end);
        nailIndexToPotentialNewNailsMap.put(nailIndex, potentialNails);
        totalPotentialNails += potentialNails.size();

        final double neededNails = REQUIRED_NAILS - unfinishedEdge.getNails().size();

        // If we do not have enough potential nails simply draw enough below the source location
        if (neededNails > totalPotentialNails) {
            for (int i = 0; i < neededNails; i++) {
                final double x = unfinishedEdge.getSourceCircular().getX();
                final double y = unfinishedEdge.getSourceCircular().getY() + GRID_SIZE * 2 * i + 2 * GRID_SIZE;
                unfinishedEdge.insertNailAt(new Nail(x, y), i);
            }
        } else {
            int newNailsAdded = 0; // How many nails have been added after completion

            // Run through the maps of index and segments of potential new nails
            for (final Map.Entry<Integer, List<Pair<Double, Double>>> newNailsSegment : nailIndexToPotentialNewNailsMap.entrySet()) {
                // Run through the segment
                for (final Pair<Double, Double> toBeNail : newNailsSegment.getValue()) {
                    if (newNailsAdded >= neededNails) break; // If we added enough nails break out

                    unfinishedEdge.insertNailAt(new Nail(toBeNail.getKey(), toBeNail.getValue()), newNailsSegment.getKey() + newNailsAdded);
                    newNailsAdded++;
                }
            }
        }
    }

    private static List<Pair<Double, Double>> getPotentialNailSegments(Circular start, Circular end) {

        final List<Pair<Double, Double>> result = new ArrayList<>();
        boolean wasSwapped = false;

        if (start.xProperty().get() == end.xProperty().get()) {
            if (end.yProperty().get() < start.yProperty().get()) {
                final Circular swap = start;
                start = end;
                end = swap;
                wasSwapped = true;
            }

            double count = 2;
            final double x = start.xProperty().get();
            while (start.yProperty().get() + count * GRID_SIZE <= end.yProperty().get() - GRID_SIZE * 2) {
                final double y = start.yProperty().get() + count * GRID_SIZE;
                result.add(new Pair<>(x, y));
                count += 2;
            }
        }

        if (end.xProperty().get() < start.xProperty().get()) {
            final Circular swap = start;
            start = end;
            end = swap;
            wasSwapped = true;
        }

        final double a = (start.yProperty().get() - end.yProperty().get()) / (start.xProperty().get() - end.xProperty().get());
        final double b = start.yProperty().get() - a * start.xProperty().get();

        double count = 2;
        while (start.xProperty().get() + count * GRID_SIZE <= end.xProperty().get() - GRID_SIZE * 2) {
            final double x = start.xProperty().get() + count * GRID_SIZE;
            final double y = x * a + b;
            if (y % GRID_SIZE < 1) {
                result.add(new Pair<>(x, y));
                count++;
            }
            count++;
        }

        if (wasSwapped) {
            Collections.reverse(result);
        }
        return result;
    }

}
