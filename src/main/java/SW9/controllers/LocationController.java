package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.*;
import SW9.backend.UPPAALDriver;
import SW9.code_analysis.CodeAnalysis;
import SW9.code_analysis.Nearable;
import SW9.presentations.*;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.NailHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXPopup;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
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
import java.util.function.Supplier;

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
    public TagPresentation nicknameTag;
    public TagPresentation invariantTag;
    public Path locationShape;
    public Label idLabel;
    public Line nameTagLine;
    public Line invariantTagLine;
    private TimerTask reachabilityCheckTask;
    private DropDownMenu dropDownMenu;
    private boolean dropDownMenuInitialized = false;

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

        initializeSelectListener();
        initializeMouseControls();
    }

    private void initializeSelectListener() {
        SelectHelper.elementsToBeSelected.addListener(new ListChangeListener<Nearable>() {
            @Override
            public void onChanged(final Change<? extends Nearable> c) {
                while (c.next()) {
                    if (c.getAddedSize() == 0) return;

                    for (final Nearable nearable : SelectHelper.elementsToBeSelected) {
                        if (nearable instanceof Location) {
                            if (nearable.equals(getLocation())) {
                                SelectHelper.addToSelection(LocationController.this);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public void initializeDropDownMenu() {
        if (dropDownMenuInitialized) return;
        dropDownMenuInitialized = true;

        dropDownMenu = new DropDownMenu(((Pane) root.getParent().getParent().getParent()), root, 230, true);

        dropDownMenu.addClickableListElement("Add Invariant", event -> {
            invariantTag.setOpacity(1);
            invariantTag.requestTextFieldFocus();
            dropDownMenu.close();
        });

        dropDownMenu.addSpacerElement();

        dropDownMenu.addClickableListElement("Add Nickname", event -> {
            nicknameTag.setOpacity(1);
            nicknameTag.requestTextFieldFocus();
            dropDownMenu.close();
        });

        dropDownMenu.addSpacerElement();

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

        dropDownMenu.addClickableListElement("Is " + getLocation().getId() + " reachable?", event -> {
            // Generate the query from the backend
            final String reachabilityQuery = UPPAALDriver.getLocationReachableQuery(getLocation(), getComponent());

            // Add proper comment
            final String reachabilityComment = "Is " + getLocation().getMostDescriptiveIdentifier() + " reachable?";

            // Add new query for this location
            final Query query = new Query(reachabilityQuery, reachabilityComment, QueryState.UNKNOWN);
            HUPPAAL.getProject().getQueries().add(query);
            query.run();

            dropDownMenu.close();
        });

        dropDownMenu.addSpacerElement();

        dropDownMenu.addColorPicker(getLocation(), (color, intensity) -> {
            getLocation().setColorIntensity(intensity);
            getLocation().setColor(color);
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
    private void locationEntered() {
        ((LocationPresentation) root).animateLocationEntered();
    }

    @FXML
    private void locationExited() {
        ((LocationPresentation) root).animateLocationExited();
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
    }

    @FXML
    private void mouseExited() {
        final LocationPresentation locationPresentation = (LocationPresentation) this.root;
        if(!locationPresentation.isInteractable()) return;

        circle.setCursor(Cursor.DEFAULT);

        locationPresentation.animateHoverExited();

        KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_URGENT);
        KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED);
    }

    private void initializeMouseControls() {

        final DoubleProperty mouseXDiff = new SimpleDoubleProperty(0);
        final DoubleProperty mouseYDiff = new SimpleDoubleProperty(0);

        final Consumer<MouseEvent> mousePressed = (event) -> {
            mouseXDiff.set(event.getX());
            mouseYDiff.set(event.getY());

            event.consume();

            final Component component = getComponent();

            event.consume();
            if (((LocationPresentation) root).isPlaced()) {

                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    initializeDropDownMenu();
                    dropDownMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 50, 50);
                    return;
                }

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
                            if (event.isShortcutDown()) {
                                SelectHelper.addToSelection(this);
                            } else {
                                SelectHelper.select(this);
                            }
                        }
                    }
                }
            } else {

                // Allowed x and y coordinates
                final double minX = GRID_SIZE * 2;
                final double maxX = getComponent().getWidth() - GRID_SIZE * 2;
                final double minY = ComponentPresentation.TOOL_BAR_HEIGHT + GRID_SIZE * 2;
                final double maxY = getComponent().getHeight() - GRID_SIZE * 2;

                if(root.getLayoutX() >= minX && root.getLayoutX() <= maxX && root.getLayoutY() >= minY && root.getLayoutY() <= maxY) {
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
                } else {
                    ((LocationPresentation) root).shake();
                }

            }

        };

        final Supplier<Double> supplyX = () -> {
            // Calculate the potential new x alongside min and max values
            final double newX = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty()).doubleValue();
            final double minX = GRID_SIZE * 2;
            final double maxX = getComponent().getWidth() - GRID_SIZE * 2;

            // Drag according to min and max
            if (newX < minX) {
                return minX;
            } else if (newX > maxX) {
                return maxX;
            } else {
                return newX;
            }
        };

        final Supplier<Double> supplyY = () -> {
            // Calculate the potential new y alongside min and max values
            final double newY = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty()).doubleValue();
            final double minY = ComponentPresentation.TOOL_BAR_HEIGHT + GRID_SIZE * 2;
            final double maxY = getComponent().getHeight() - GRID_SIZE * 2;

            // Drag according to min and max
            if (newY < minY) {
                return minY;
            } else if (newY > maxY) {
                return maxY;
            } else {
                return newY;
            }
        };

        locationProperty().addListener((obs, oldLocation, newLocation) -> {

            if(newLocation == null) return;

            if(newLocation.getType() != Location.Type.NORMAL) {
                root.setOnMousePressed(mousePressed::accept);
            } else {
                ItemDragHelper.makeDraggable(
                        root,
                        root,
                        supplyX,
                        supplyY,
                        mousePressed,
                        () -> {},
                        () -> {}
                );
            }
        });


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
