package SW9.utility.helpers;

import SW9.model_canvas.Removable;

import java.util.ArrayList;
import java.util.List;

public class SelectHelper {

    private static ArrayList<Removable> selectedElements = new ArrayList<>();

    public static void makeSelectable(final Removable removable) {
        removable.getMouseTracker().registerOnMousePressedEventHandler(event -> {
            select(removable);
        });
    }

    public static void select(final Removable removable) {
        clearSelectedElements();

        // Check if the select went well, if so add it to the selected list
        if (removable.select()) {
            selectedElements.add(removable);
        }
    }

    public static ArrayList<Removable> getSelectedElements() {
        return selectedElements;
    }

    public static void clearSelectedElements() {
        while (!selectedElements.isEmpty()) {
            selectedElements.get(0).deselect();
            selectedElements.remove(0);
        }
    }

    public static boolean isSelected(final Removable... needles) {
        for (Removable needle : needles) {
            if (selectedElements.contains(needle)) return true;
        }
        return false;
    }

    public static boolean isSelected(final List<? extends Removable> needles) {
        for (Removable needle : needles) {
            if (selectedElements.contains(needle)) return true;
        }
        return false;
    }

}
