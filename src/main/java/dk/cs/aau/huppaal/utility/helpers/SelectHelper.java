package dk.cs.aau.huppaal.utility.helpers;

import com.hp.hpl.jena.shared.NotFoundException;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.code_analysis.Nearable;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.utility.colors.Color;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

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

    public static Component selectComponent(String componentId) throws NotFoundException {
        var component = HUPPAAL.getProject().getComponents().stream().filter(c -> c.getName().equals(componentId)).findAny();
        if(component.isEmpty())
            throw new NotFoundException("No such component '%s'".formatted(componentId));
        if(!CanvasController.getActiveComponent().equals(component.get())) {
            SelectHelper.elementsToBeSelected = FXCollections.observableArrayList();
            CanvasController.setActiveComponent(component.get());
        }
        SelectHelper.clearSelectedElements();
        return component.get();
    }

    public static void selectLocation(Component parentComponent, String locationId) {
        var location = parentComponent.getLocationsWithInitialAndFinal().stream().filter(l -> l.getId().equals(locationId)).findAny();
        if(location.isEmpty())
            throw new NotFoundException("No such location '%s' in component '%s'".formatted(locationId, parentComponent.getName()));
        location.ifPresent(SelectHelper::select);
    }

    public static void selectSubComponent(Component parentComponent, String subcomponentId) {
        var subcomponent = parentComponent.getSubComponents().stream().filter(c -> c.getIdentifier().equals(subcomponentId)).findAny();
        if(subcomponent.isEmpty())
            throw new NotFoundException("No such subcomponent '%s' in component '%s'".formatted(subcomponent, parentComponent.getName()));
        subcomponent.ifPresent(SelectHelper::select);
    }

    public static void selectJork(Component parentComponent, String jorkId) {
        var jork = parentComponent.getJorks().stream().filter(c -> c.getId().equals(jorkId)).findAny();
        if(jork.isEmpty())
            throw new NotFoundException("No such jork '%s' in component '%s'".formatted(jorkId, parentComponent.getName()));
        jork.ifPresent(SelectHelper::select);
    }

    public static void selectEdge(Component parentComponent, String edgeId) {
        var edge = parentComponent.getEdges().stream().filter(c -> c.getUuid().equals(UUID.fromString(edgeId))).findAny();
        if(edge.isEmpty())
            throw new NotFoundException("No such edge '%s' in component '%s'".formatted(edgeId, parentComponent.getName()));
        edge.ifPresent(SelectHelper::select);
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
