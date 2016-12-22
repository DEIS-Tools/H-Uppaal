package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.*;
import SW9.backend.UPPAALDriver;
import SW9.code_analysis.CodeAnalysis;
import SW9.presentations.*;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.NailHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXPopup;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class LocationController implements Initializable, SelectHelper.ColorSelectable {

    private static final Map<Location, Boolean> invalidNameError = new HashMap<>();

    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>();
    public Group root;
    public Path initialIndicator;
    public StackPane finalIndicator;
    public Group shakeContent;
    public Circle circle;
    public Circle circleShakeIndicator;
    public Group scaleContent;
    public TagPresentation nameTag;
    public TagPresentation invariantTag;
    public Circle hiddenAreaCircle;
    public Path locationShape;
    public Label idLabel;
    public Line nameTagLine;
    public Line invariantTagLine;
    private TimerTask reachabilityCheckTask;
    private double previousX;
    private double previousY;
    private boolean wasDragged = false;
    private DropDownMenu dropDownMenu;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.location.addListener((obsLocation, oldLocation, newLocation) -> {
            // The radius property on the abstraction must reflect the radius in the view
            newLocation.radiusProperty().bind(circle.radiusProperty());

            // The scale property on the abstraction must reflect the radius in the view
            newLocation.scaleProperty().bind(scaleContent.scaleXProperty());
        });

        // Scale x and y 1:1 (based on the x-scale)
        scaleContent.scaleYProperty().bind(scaleContent.scaleXProperty());

        //initializeReachabilityCheck();
    }

    public void initializeDropDownMenu() {
        dropDownMenu = new DropDownMenu(((Pane) root.getParent()), root, 230, true);

        dropDownMenu.addListElement("Set Urgency");

        final BooleanProperty isUrgent = new SimpleBooleanProperty(false);
        isUrgent.bind(getLocation().urgencyProperty().isEqualTo(Location.Urgency.URGENT));
        dropDownMenu.addTogglableListElement("Urgent", isUrgent, event -> {
            if (isUrgent.get()) {
                getLocation().setUrgency(Location.Urgency.NORMAL);
            } else {
                getLocation().setUrgency(Location.Urgency.URGENT);
            }
        });

        final BooleanProperty isCommitted = new SimpleBooleanProperty(false);
        isCommitted.bind(getLocation().urgencyProperty().isEqualTo(Location.Urgency.COMMITTED));
        dropDownMenu.addTogglableListElement("Committed", isCommitted, event -> {
            if (isCommitted.get()) {
                getLocation().setUrgency(Location.Urgency.NORMAL);
            } else {
                getLocation().setUrgency(Location.Urgency.COMMITTED);
            }
        });

        dropDownMenu.addSpacerElement();

        dropDownMenu.addClickableListElement("Is reachable?", event -> {
            // Generate the query from the backend
            final String reachabilityQuery = UPPAALDriver.getLocationReachableQuery(getLocation(), getComponent());

            // Add proper comment
            final String reachabilityComment = "Is " + getLocation().getMostDescriptiveIdentifier() + " reachable?";

            // Add new query for this location
            HUPPAAL.getProject().getQueries().add(new Query(reachabilityQuery, reachabilityComment, QueryState.UNKNOWN));

            dropDownMenu.close();
        });
    }

    public void initializeInvalidNameError() {
        final Location location = getLocation();
        if (invalidNameError.containsKey(location)) return;
        invalidNameError.put(location, true);

        final CodeAnalysis.Message invalidNickName = new CodeAnalysis.Message("Nicknames for locations must be alpha-numeric", CodeAnalysis.MessageType.ERROR, location);

        final Consumer<String> updateNickNameCheck = (nickname) -> {
            if (!nickname.matches("[A-Za-z0-9_-]*$")) {
                // Invalidate the list (will update the UI with the new name)
                invalidNickName.getNearables().remove(location);
                invalidNickName.getNearables().add(location);
                CodeAnalysis.addMessage(getComponent(), invalidNickName);
            } else {
                CodeAnalysis.removeMessage(getComponent(), invalidNickName);
            }
        };

        location.nicknameProperty().addListener((obs, oldNickName, newNickName) -> {
            updateNickNameCheck.accept(newNickName);
        });
        updateNickNameCheck.accept(location.getNickname());
    }

    public void initializeReachabilityCheck() {
        final int interval = 5000; // ms

        // Could not run query
        reachabilityCheckTask = new TimerTask() {

            @Override
            public void run() {
                if (getComponent() == null || getLocation() == null) return;

                // The location might have been remove from the component (through ctrl + z)
                if (getLocation().getType() == Location.Type.NORMAL && !getComponent().getLocations().contains(getLocation())) return;

                final Component mainComponent = HUPPAAL.getProject().getMainComponent();

                if (mainComponent == null) {
                    return; // We cannot generate a UPPAAL file without a main component
                }

                UPPAALDriver.verify(
                        "E<> " + getComponent().getName() + "." + getLocation().getId(),
                        result -> {
                            final LocationPresentation locationPresentation = (LocationPresentation) LocationController.this.root;

                            locationPresentation.animateShakeWarning(!result);
                        },
                        e -> {
                            // Could not run query
                            System.out.println(e);
                        },
                        mainComponent
                );
            }

        };

        new Timer().schedule(reachabilityCheckTask, 0, interval);
    }

    public Location getLocation() {
        return location.get();
    }

    public void setLocation(final Location location) {
        this.location.set(location);

        if (ComponentController.isPlacingLocation()) {
            root.layoutXProperty().bind(location.xProperty());
            root.layoutYProperty().bind(location.yProperty());
        } else {
            root.setLayoutX(location.getX());
            root.setLayoutY(location.getY());
            location.xProperty().bind(root.layoutXProperty());
            location.yProperty().bind(root.layoutYProperty());
            ((LocationPresentation) root).setPlaced(true);
        }
    }

    public ObjectProperty<Location> locationProperty() {
        return location;
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(final Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    @FXML
    private void mouseEntered() {
        final LocationPresentation locationPresentation = (LocationPresentation) this.root;

        if(!locationPresentation.isInteractable()) return;

        circle.setCursor(Cursor.HAND);

        locationPresentation.animateHoverEntered();

        // Keybind for making location urgent
        KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_URGENT, new Keybind(new KeyCodeCombination(KeyCode.U), () -> {
            final Location.Urgency previousUrgency = location.get().getUrgency();

            if (previousUrgency.equals(Location.Urgency.URGENT)) {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.NORMAL);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getNickname() + " urgent", "hourglass-full");
            } else {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.URGENT);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getNickname() + " normal (back form urgent)", "hourglass-empty");
            }
        }));

        // Keybind for making location committed
        KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED, new Keybind(new KeyCodeCombination(KeyCode.C), () -> {
            final Location.Urgency previousUrgency = location.get().getUrgency();

            if (previousUrgency.equals(Location.Urgency.COMMITTED)) {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.NORMAL);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getNickname() + " committed", "hourglass-full");
            } else {
                UndoRedoStack.push(() -> { // Perform
                    getLocation().setUrgency(Location.Urgency.COMMITTED);
                }, () -> { // Undo
                    getLocation().setUrgency(previousUrgency);
                }, "Made location " + getLocation().getNickname() + " normal (back from committed)", "hourglass-empty");
            }

        }));

        hiddenAreaEntered();
    }

    @FXML
    private void mouseExited() {
        final LocationPresentation locationPresentation = (LocationPresentation) this.root;
        if(!locationPresentation.isInteractable()) return;

        circle.setCursor(Cursor.DEFAULT);

        locationPresentation.animateHoverExited();

        KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_URGENT);
        KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED);

        hiddenAreaExited();
    }

    @FXML
    private void mousePressed(final MouseEvent event) {
        final Component component = getComponent();

        if (event.getButton().equals(MouseButton.SECONDARY)) {
            dropDownMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 380, 160);
            return;
        }

        event.consume();
        if (((LocationPresentation) root).isPlaced()) {
            final Edge unfinishedEdge = component.getUnfinishedEdge();

            if (unfinishedEdge != null) {
                unfinishedEdge.setTargetLocation(getLocation());
                NailHelper.addMissingNails(unfinishedEdge);

            } else {
                // If shift is being held down, start drawing a new edge
                if (event.isShiftDown()) {
                    final Edge newEdge = new Edge(getLocation());

                    KeyboardTracker.registerKeybind(KeyboardTracker.ABANDON_EDGE, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                        component.removeEdge(newEdge);
                        UndoRedoStack.forget();
                    }));

                    UndoRedoStack.push(() -> { // Perform
                        component.addEdge(newEdge);
                    }, () -> { // Undo
                        component.removeEdge(newEdge);
                    }, "Created edge starting from location " + getLocation().getNickname(), "add-circle");
                }
                // Otherwise, select the location
                else {
                    if(((LocationPresentation) root).isInteractable()) {
                        SelectHelper.select(this);
                    }
                }

                // Update position for undo dragging
                previousX = root.getLayoutX();
                previousY = root.getLayoutY();
            }
        } else {

            // Unbind presentation root x and y coordinates (bind the view properly to enable dragging)
            root.layoutXProperty().unbind();
            root.layoutYProperty().unbind();

            // Bind the location to the presentation root x and y
            getLocation().xProperty().bind(root.layoutXProperty());
            getLocation().yProperty().bind(root.layoutYProperty());

            // Notify that the location was placed
            ((LocationPresentation) root).setPlaced(true);
            ComponentController.setPlacingLocation(null);
            KeyboardTracker.unregisterKeybind(KeyboardTracker.ABANDON_LOCATION);
        }
    }

    @FXML
    private void mouseDragged(final MouseEvent event) {
        // If the location is not a normal location (not initial/final) make it draggable
        if (getLocation().getType() == Location.Type.NORMAL) {

            // Calculate the potential new x alongside min and max values
            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue();
            final double minX = LocationPresentation.RADIUS + GRID_SIZE;
            final double maxX = getComponent().getWidth() - LocationPresentation.RADIUS - GRID_SIZE;

            // Drag according to min and max
            if (newX < minX) {
                root.setLayoutX(minX);
            } else if (newX > maxX) {
                root.setLayoutX(maxX);
            } else {
                root.setLayoutX(newX);
            }

            // Calculate the potential new y alongside min and max values
            final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue();
            final double minY = LocationPresentation.RADIUS + ComponentPresentation.TOOL_BAR_HEIGHT + GRID_SIZE;
            final double maxY = getComponent().getHeight() - LocationPresentation.RADIUS - GRID_SIZE;

            // Drag according to min and max
            if (newY < minY) {
                root.setLayoutY(minY);
            } else if (newY > maxY) {
                root.setLayoutY(maxY);
            } else {
                root.setLayoutY(newY);
            }

            // Tell the mouse release action that we can store an update
            wasDragged = true;
        }
    }

    @FXML
    private void mouseReleased(final MouseEvent event) {
        if (wasDragged) {
            // Add to undo redo stack
            final double currentX = root.getLayoutX();
            final double currentY = root.getLayoutY();
            final double storePreviousX = previousX;
            final double storePreviousY = previousY;
            UndoRedoStack.push(() -> {
                        root.setLayoutX(currentX);
                        root.setLayoutY(currentY);
            }, () -> {
                root.setLayoutX(storePreviousX);
                root.setLayoutY(storePreviousY);
            }, String.format("Moved location from (%.0f,%.0f) to (%.0f,%.0f)", currentX, currentY, storePreviousX, storePreviousY), "pin-drop");

            // Reset the was dragged boolean
            wasDragged = false;
        }
    }

    @FXML
    private void hiddenAreaEntered() {
        invariantTag.setOpacity(1);
        nameTag.setOpacity(1);
    }

    @FXML
    private void hiddenAreaExited() {
        if (getLocation().getInvariant().equals("")) {
            invariantTag.setOpacity(0);
        } else {
            invariantTag.setOpacity(1);
        }

        if(getLocation().getNickname().equals("")) {
            nameTag.setOpacity(0);
        } else {
            nameTag.setOpacity(1);
        }
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {
        final Location location = getLocation();

        // Set the color of the location
        location.setColorIntensity(intensity);
        location.setColor(color);
    }

    @Override
    public Color getColor() {
        return getLocation().getColor();
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return getLocation().getColorIntensity();
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }
}
