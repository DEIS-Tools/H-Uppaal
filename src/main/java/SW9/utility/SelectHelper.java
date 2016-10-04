package SW9.utility;

import SW9.model_canvas.Removable;

import java.util.ArrayList;

public class SelectHelper {

    private static ArrayList<Removable> selectedElements = new ArrayList<>();

    public static void makeSelectable(final Removable removable) {
        removable.getMouseTracker().registerOnMousePressedEventHandler(event -> {
            while (!selectedElements.isEmpty()) {
                final Removable element = selectedElements.get(0);
                element.deselect();
                selectedElements.remove(element);
            }

            // Check if the select went well, if so add it to the selected list
            if (removable.select()) {
                selectedElements.add(removable);
            }
        });
    }

    public static ArrayList<Removable> getSelectedElements() {
        return selectedElements;
    }

    public static void clearSelectedElements() {
        while (!selectedElements.isEmpty()) {
            selectedElements.remove(0);
        }
    }

    public static boolean isSelected(final Removable needle) {
        return selectedElements.contains(needle);
    }

}
