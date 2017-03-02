package SW9.utility.helpers;

import SW9.code_analysis.Nearable;
import SW9.controllers.CanvasController;
import SW9.utility.colors.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SelectHelper {

    public static final Color SELECT_COLOR = Color.ORANGE;
    public static final Color.Intensity SELECT_COLOR_INTENSITY_NORMAL = Color.Intensity.I700;
    public static final Color.Intensity SELECT_COLOR_INTENSITY_BORDER = Color.Intensity.I900;

    private static final ObservableList<ItemSelectable> selectedElements = FXCollections.observableArrayList();

    public static ObservableList<Nearable> elementsToBeSelected = FXCollections.observableArrayList();

    public static void select(final ItemSelectable selectable) {

        CanvasController.leaveTextAreas();

        // Check if the element is already selected
        if (selectedElements.contains(selectable)) return;

        // Clear the list
        clearSelectedElements();

        addToSelection(selectable);
    }

    public static void addToSelection(final ItemSelectable selectable) {
        // Check if the element is already selected
        if (selectedElements.contains(selectable)) return;

        selectable.select();
        selectedElements.add(selectable);
    }

    public static void select(final Nearable nearable) {
        elementsToBeSelected.add(nearable);
    }

    public static void deselect(final ItemSelectable selectable) {
        selectable.deselect();

        // deselect the element
        selectedElements.remove(selectable);
    }

    public static void clearSelectedElements() {
        elementsToBeSelected.clear();

        while (selectedElements.size() > 0) {
            deselect(selectedElements.get(0));
        }
    }

    public static ObservableList<ItemSelectable> getSelectedElements() {
        return selectedElements;
    }

    private static javafx.scene.paint.Color getColor(final Color.Intensity intensity) {
        return SELECT_COLOR.getColor(intensity);
    }

    public static javafx.scene.paint.Color getNormalColor() {
        return getColor(SELECT_COLOR_INTENSITY_NORMAL);
    }

    public static javafx.scene.paint.Color getBorderColor() {
        return getColor(SELECT_COLOR_INTENSITY_BORDER);
    }

    public interface Selectable {
        void select();

        void deselect();
    }

    public interface ItemSelectable extends Selectable, LocationAware {
        void color(Color color, Color.Intensity intensity);

        Color getColor();

        Color.Intensity getColorIntensity();

        ItemDragHelper.DragBounds getDragBounds();
    }

}
