package SW9.utility.helpers;

import SW9.utility.colors.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectHelperNew {

    private static final ObservableList<Selectable> selectedElements = FXCollections.observableArrayList();

    public static void select(final Selectable selectable) {
        // Check if the element is already selected
        if (selectedElements.contains(selectable)) return;

        // Clear the list
        selectedElements.removeIf(s -> true);

        // todo: style the element

        // Select the element
        selectedElements.add(selectable);
    }

    public static void deselect(final Selectable selectable) {
        // todo: style the element

        // deselect the element
        selectedElements.remove(selectable);
    }

    public static void clearSelectedElements() {
        while (selectedElements.size() > 0) {
            deselect(selectedElements.get(0));
        }
    }

    public static ObservableList<Selectable> getSelectedElements() {
        return selectedElements;
    }

    public interface Selectable {
        void color(Color color, Color.Intensity intensity);
    }

}
