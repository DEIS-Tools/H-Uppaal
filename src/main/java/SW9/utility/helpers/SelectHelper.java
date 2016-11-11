package SW9.utility.helpers;

import SW9.utility.colors.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectHelper {

    private static final ObservableList<ColorSelectable> selectedElements = FXCollections.observableArrayList();

    public static void select(final ColorSelectable selectable) {
        // Check if the element is already selected
        if (selectedElements.contains(selectable)) return;

        // Clear the list
        clearSelectedElements();

        addToSelection(selectable);
    }

    public static void addToSelection(final ColorSelectable selectable) {
        // Check if the element is already selected
        if (selectedElements.contains(selectable)) return;

        selectable.select();
        selectedElements.add(selectable);
    }

    public static void deselect(final ColorSelectable selectable) {
        selectable.deselect();

        // deselect the element
        selectedElements.remove(selectable);
    }

    public static void clearSelectedElements() {
        while (selectedElements.size() > 0) {
            deselect(selectedElements.get(0));
        }
    }

    public static ObservableList<ColorSelectable> getSelectedElements() {
        return selectedElements;
    }

    public interface Selectable {
        void select();

        void deselect();
    }

    public interface ColorSelectable extends Selectable {
        void color(Color color, Color.Intensity intensity);

        Color getColor();

        Color.Intensity getColorIntensity();
    }

}
