package SW9.controllers;

import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Jork;
import SW9.code_analysis.CodeAnalysis;
import SW9.presentations.CanvasPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.NailHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class JorkController implements Initializable, SelectHelper.ColorSelectable {

    private static final HashMap<Jork, Boolean> REGISTERED_JORK_ERROR_CHECKER_MAP = new HashMap<>();

    private final ObjectProperty<Jork> jork = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    public Group root;
    public Rectangle rectangle;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        makeDraggable();
    }

    public void initializeJorkErrorHandling() {
        final CodeAnalysis.Message wrongIncoming = new CodeAnalysis.Message("Incoming edges to Jork '" + getJork().getId() + "' are of inconsistent types", CodeAnalysis.MessageType.ERROR);
        final CodeAnalysis.Message wrongOutgoing = new CodeAnalysis.Message("Outgoing edges from Jork '" + getJork().getId() + "' are of inconsistent types", CodeAnalysis.MessageType.ERROR);
        final CodeAnalysis.Message wrongJorkType = new CodeAnalysis.Message("Incoming and outgoing edges of Jork '" + getJork().getId() + "' are the same type (must be different)", CodeAnalysis.MessageType.ERROR);
        final CodeAnalysis.Message missingIncomingEdges = new CodeAnalysis.Message("Jork '" + getJork().getId() + "' have no incoming edges", CodeAnalysis.MessageType.ERROR);
        final CodeAnalysis.Message missingOutgoingEdges = new CodeAnalysis.Message("Jork '" + getJork().getId() + "' have no outgoing edges", CodeAnalysis.MessageType.ERROR);

        final boolean[] addedWrongIncomingMessage = {false};
        final boolean[] addedWrongOutgoingMessage = {false};
        final boolean[] addedWrongJorkTypeMessage = {false};
        final boolean[] addedMissingIncomingEdgesMessage = {false};
        final boolean[] addedMissingOutgoingEdgesMessage = {false};

        final Runnable checkJork = () -> {
            // Find the incoming and outgoing edges (from/to the jork)
            final List<Edge> incomingEdges = getComponent().getIncomingEdges(getJork());
            final List<Edge> outgoingEdges = getComponent().getOutGoingEdges(getJork());


            // Check if we have some incoming edges

            // We found an inconsistency that is not already in the map
            if (incomingEdges.size() == 0 && !addedMissingIncomingEdgesMessage[0]) {
                CodeAnalysis.addMessage(getComponent(), missingIncomingEdges);
                addedMissingIncomingEdgesMessage[0] = true;
            }

            // We did not find an inconsistency, but we have an error for it
            if (incomingEdges.size() != 0 && addedMissingIncomingEdgesMessage[0]) {
                CodeAnalysis.removeMessage(getComponent(), missingIncomingEdges);
                addedMissingIncomingEdgesMessage[0] = false;
            }

            // Check if we have some outgoing edges

            // We found an inconsistency that is not already in the map
            if (outgoingEdges.size() == 0 && !addedMissingOutgoingEdgesMessage[0]) {
                CodeAnalysis.addMessage(getComponent(), missingOutgoingEdges);
                addedMissingOutgoingEdgesMessage[0] = true;
            }

            // We did not find an inconsistency, but we have an error for it
            if (outgoingEdges.size() != 0 && addedMissingOutgoingEdgesMessage[0]) {
                CodeAnalysis.removeMessage(getComponent(), missingOutgoingEdges);
                addedMissingOutgoingEdgesMessage[0] = false;
            }


            // Check if all of the incoming edges are of the same type
            boolean foundIncomingInconsistency = false;
            JorkType previousIncomingType = null;
            for (final Edge incomingEdge : incomingEdges) {

                JorkType thisType = JorkType.UNKNOWN;
                if (incomingEdge.getSourceLocation() != null) thisType = JorkType.LOCATION;
                if (incomingEdge.getSourceSubComponent() != null) thisType = JorkType.SUB_COMPONENT;

                if (previousIncomingType != null) {
                    if (!previousIncomingType.equals(thisType)) {
                        foundIncomingInconsistency = true;
                        break;
                    }
                }

                previousIncomingType = thisType;
            }

            // We found an inconsistency that is not already in the map
            if (foundIncomingInconsistency && !addedWrongIncomingMessage[0]) {
                CodeAnalysis.addMessage(getComponent(), wrongIncoming);
                addedWrongIncomingMessage[0] = true;
            }

            // We did not find an inconsistency, but we have an error for it
            if (!foundIncomingInconsistency && addedWrongIncomingMessage[0]) {
                CodeAnalysis.removeMessage(getComponent(), wrongIncoming);
                addedWrongIncomingMessage[0] = false;
            }

            // Check if all of the outgoing edges are of the same type
            boolean foundOutgoingInconsistency = false;
            JorkType previousOutgoingType = null;
            boolean foundNonFinishedEdge = false;
            for (final Edge outgoingEdge : outgoingEdges) {

                JorkType thisType = JorkType.UNKNOWN;
                if (outgoingEdge.getTargetLocation() != null) thisType = JorkType.LOCATION;
                if (outgoingEdge.getTargetSubComponent() != null) thisType = JorkType.SUB_COMPONENT;
                if (thisType == JorkType.UNKNOWN) foundNonFinishedEdge = true;

                if (previousOutgoingType != null) {
                    if (!previousOutgoingType.equals(thisType)) {
                        foundOutgoingInconsistency = true;
                        break;
                    }
                }

                previousOutgoingType = thisType;
            }

            if (!foundNonFinishedEdge) {
                // We found an inconsistency that is not already in the map
                if (foundOutgoingInconsistency && !addedWrongOutgoingMessage[0]) {
                    CodeAnalysis.addMessage(getComponent(), wrongOutgoing);
                    addedWrongOutgoingMessage[0] = true;
                }

                // We did not find an inconsistency, but we have an error for it
                if (!foundOutgoingInconsistency && addedWrongOutgoingMessage[0]) {
                    CodeAnalysis.removeMessage(getComponent(), wrongOutgoing);
                    addedWrongOutgoingMessage[0] = false;
                }
            }


            // Check if the incoming type matches the outgoing type
            if (!foundIncomingInconsistency && !foundOutgoingInconsistency && !foundNonFinishedEdge) {

                // We found an inconsistency that is not already in the map
                if (previousIncomingType == previousOutgoingType && !addedWrongJorkTypeMessage[0]) {
                    CodeAnalysis.addMessage(getComponent(), wrongJorkType);
                    addedWrongJorkTypeMessage[0] = true;
                }

                // We did not find an inconsistency, but we have an error for it
                if (previousIncomingType != previousOutgoingType && addedWrongJorkTypeMessage[0]) {
                    CodeAnalysis.removeMessage(getComponent(), wrongJorkType);
                    addedWrongJorkTypeMessage[0] = false;
                }

            }

        };

        if (!REGISTERED_JORK_ERROR_CHECKER_MAP.containsKey(getJork())) {
            final InvalidationListener listener = observable -> checkJork.run();
            getComponent().getEdges().addListener(listener);
            REGISTERED_JORK_ERROR_CHECKER_MAP.put(getJork(), true);
        }
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {

    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return null;
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }

    private void makeDraggable() {

        ItemDragHelper.makeDraggable(
                root,
                root,
                () -> CanvasPresentation.mouseTracker.getGridX(),
                () -> CanvasPresentation.mouseTracker.getGridY(),
                (event) -> {
                    event.consume();
                    SelectHelper.select(this);
                    final Component component = getComponent();
                    final Edge unfinishedEdge = component.getUnfinishedEdge();
                    if (unfinishedEdge != null) {
                        unfinishedEdge.setTargetJork(getJork());
                        NailHelper.addMissingNails(unfinishedEdge);
                    } else if (event.isShiftDown()) {
                        final Edge newEdge = new Edge(getJork());

                        KeyboardTracker.registerKeybind(KeyboardTracker.ABANDON_EDGE, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                            component.removeEdge(newEdge);
                            UndoRedoStack.forget();
                        }));

                        UndoRedoStack.push(() -> { // Perform
                            component.addEdge(newEdge);
                        }, () -> { // Undo
                            component.removeEdge(newEdge);
                        }, "Created edge starting from jork " + getJork().getId(), "add-circle");
                    }
                },
                () -> {
                },
                () -> {
                }
        );
    }

    public Jork getJork() {
        return jork.get();
    }

    public void setJork(Jork jork) {
        this.jork.set(jork);
    }

    public ObjectProperty<Jork> jorkProperty() {
        return jork;
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    private enum JorkType {
        LOCATION,
        SUB_COMPONENT,
        UNKNOWN
    }
}
