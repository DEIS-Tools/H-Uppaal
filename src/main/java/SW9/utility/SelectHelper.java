package SW9.utility;

import SW9.model_canvas.Selectable;

import java.util.ArrayList;

public class SelectHelper {

    private static ArrayList<Selectable> selectedElements = new ArrayList<>();

    public static void makeSelectable(final Selectable selectable) {
        selectable.getMouseTracker().registerOnMousePressedEventHandler(event -> {
            while (!selectedElements.isEmpty()) {
                final Selectable element = selectedElements.get(0);
                element.deselect();
                selectedElements.remove(element);
            }

            selectedElements.add(selectable);
            selectable.select();
        });
    }

    public static ArrayList<Selectable> getSelectedElements() {
        return selectedElements;
    }

    public static void clearSelectedElements() {
        while(!selectedElements.isEmpty()) {
            selectedElements.remove(0);
        }
    }

}
