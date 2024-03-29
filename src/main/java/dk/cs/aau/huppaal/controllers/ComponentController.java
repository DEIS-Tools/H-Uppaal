package dk.cs.aau.huppaal.controllers;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.*;
import dk.cs.aau.huppaal.backend.UPPAALDriverManager;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;
import dk.cs.aau.huppaal.code_analysis.Nearable;
import dk.cs.aau.huppaal.presentations.*;
import dk.cs.aau.huppaal.utility.UndoRedoStack;
import dk.cs.aau.huppaal.utility.helpers.*;
import dk.cs.aau.huppaal.utility.mouse.MouseTracker;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dk.cs.aau.huppaal.presentations.CanvasPresentation.GRID_SIZE;

public class ComponentController implements Initializable {

    private static final Map<Component, Timer> COMPONENT_SUBCOMPONENT_NAME_CHECK_TIMER_MAP = new HashMap<>();
    private static final Map<Component, ListChangeListener<Location>> locationListChangeListenerMap = new HashMap<>();
    private static final Map<Component, Boolean> errorsAndWarningsInitialized = new HashMap<>();
    private static Location placingLocation = null;
    private static double lastChanged = 0;
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);
    private final Map<Edge, EdgePresentation> edgePresentationMap = new HashMap<>();
    private final Map<Location, LocationPresentation> locationPresentationMap = new HashMap<>();
    private final Map<SubComponent, SubComponentPresentation> subComponentPresentationMap = new HashMap<>();
    private final Map<Jork, JorkPresentation> jorkPresentationMap = new HashMap<>();
    private Boolean alreadyRunningNoIncomingEdgesCheck = false;
    public BorderPane toolbar;
    public Rectangle background;
    public StyleClassedTextArea declaration;
    public JFXRippler toggleDeclarationButton;
    public BorderPane frame;
    public JFXTextField name;
    public StackPane root;
    public Line line1;
    public Line line2;
    public Label x;
    public Label y;
    public Pane defaultLocationsContainer;
    public Rectangle rightAnchor;
    public Rectangle bottomAnchor;
    public Pane modelContainerSubComponent;
    public Pane modelContainerLocation;
    public Pane modelContainerEdge;
    public Pane modelContainerJork;

    private MouseTracker mouseTracker;
    private DropDownMenu contextMenu;
    private DropDownMenu finishEdgeContextMenu;

    // Guiding indicator for initial and final location
    public Group initialLocationGuideContainer;
    public Path initialLocationGuideArrow;
    public Label initialLocationGuideLabel;

    public Group finalLocationGuideContainer;
    public Path finalLocationGuideArrow;
    public Label finalLocationGuideLabel;


    public static boolean isPlacingLocation() {
        return placingLocation != null;
    }

    public static void setPlacingLocation(final Location placingLocation) {
        ComponentController.placingLocation = placingLocation;
    }

    public static void setLastChanged() {
        lastChanged = System.currentTimeMillis() + 500;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        declaration.setParagraphGraphicFactory(LineNumberFactory.get(declaration));

        component.addListener((obs, oldComponent, newComponent) -> {
            // Bind the width and the height of the abstraction to the values in the view todo: reflect the height and width from the presentation into the abstraction
            // Bind the position of the abstraction to the values in the view

            // Ensure that the component snaps to the grid
            if(newComponent.getX() == 0 && newComponent.getY() == 0) {
                newComponent.setX(GRID_SIZE * 0.5);
                newComponent.setY(GRID_SIZE * 0.5);
            }

            root.layoutXProperty().set(newComponent.getX());
            root.layoutYProperty().set(newComponent.getY());
            newComponent.xProperty().bindBidirectional(root.layoutXProperty());
            newComponent.yProperty().bindBidirectional(root.layoutYProperty());

            // Bind the declarations of the abstraction the the view
            declaration.replaceText(0, declaration.getLength(), newComponent.getDeclarations());
            declaration.textProperty().addListener((observable, oldDeclaration, newDeclaration) -> newComponent.setDeclarations(newDeclaration));


            // Find the clocks in the decls
            newComponent.declarationsProperty().addListener((observable, oldValue, newValue) -> {

                final List<String> clocks = new ArrayList<String>();

                final String strippedDecls = newValue.replaceAll("[\\r\\n]+", "");

                Pattern pattern = Pattern.compile("clock (?<CLOCKS>[^;]*);");
                Matcher matcher = pattern.matcher(strippedDecls);

                while (matcher.find()) {
                    final String clockStrings[] = matcher.group("CLOCKS").split(",");
                    for (String clockString : clockStrings) {
                        clocks.add(clockString.replaceAll("\\s",""));
                    }
                }

                //TODO this logs the clocks System.out.println(clocks);
            });

            initializeEdgeHandling(newComponent);
            initializeLocationHandling(newComponent);
            initializeSubComponentHandling(newComponent);
            initializeJorkHandling(newComponent);
            initializeDeclarations();

            // When we update the color of the component, also update the color of the initial and final locations if the colors are the same
            newComponent.colorProperty().addListener((obs1, oldColor, newColor) -> {
                final Location initialLocation = newComponent.getInitialLocation();
                if (initialLocation.getColor().equals(oldColor)) {
                    initialLocation.setColorIntensity(newComponent.getColorIntensity());
                    initialLocation.setColor(newColor);
                }

                final Location finalLocation = newComponent.getFinalLocation();
                if (finalLocation.getColor().equals(oldColor)) {
                    finalLocation.setColorIntensity(newComponent.getColorIntensity());
                    finalLocation.setColor(newColor);
                }
            });
        });

        // The root view have been inflated, initialize the mouse tracker on it
        mouseTracker = new MouseTracker(root);

        initializeComponentContextMenu();

        component.addListener((obs, old, component) -> {
            if (component == null) return;

            if (!errorsAndWarningsInitialized.containsKey(component) || !errorsAndWarningsInitialized.get(component)) {
                initializeSubComponentUniqueNameError();
                initializeNoIncomingEdgesWarning();
                errorsAndWarningsInitialized.put(component, true);
            }
        });
    }

    private void initializeNoIncomingEdgesWarning() {
        final Map<Location, CodeAnalysis.Message> messages = new HashMap<>();

        final Function<Location, Boolean> hasIncomingEdges = location -> {
            if (!getComponent().getLocations().contains(location))
                return true; // Do now show messages for locations not in the set of locations

            for (final Edge edge : getComponent().getEdges()) {
                final Location targetLocation = edge.getTargetLocation();
                if (targetLocation != null && targetLocation.equals(location)) return true;
            }

            return false;
        };

        final Consumer<Component> checkLocations = (component) -> {
            if(alreadyRunningNoIncomingEdgesCheck) {
                return;
            }

            new Thread(() -> {
                alreadyRunningNoIncomingEdgesCheck = true;

                while(lastChanged > System.currentTimeMillis() - 5000){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        final List<Location> ignored = new ArrayList<>();

                        // Run through all of the locations we are currently displaying a warning for, checking if we should remove them
                        final Set<Location> removeMessages = new HashSet<>();
                        messages.keySet().forEach(location -> {
                            // Check if the location has some incoming edges
                            final boolean result = hasIncomingEdges.apply(location);

                            // The location has at least one incoming edge
                            if (result) {
                                CodeAnalysis.removeMessage(component, messages.get(location));
                                removeMessages.add(location);
                            }

                            // Ignore this location from now on (we already checked it)
                            ignored.add(location);
                        });
                        removeMessages.forEach(messages::remove);

                        // Run through all non-ignored locations
                        for (final Location location : component.getLocations()) {
                            if (ignored.contains(location)) continue; // Skip ignored
                            if (messages.containsKey(location)) continue; // Skip locations that already have warnings associated

                            // Check if the location has some incoming edges
                            final boolean result = hasIncomingEdges.apply(location);

                            // The location has no incoming edge
                            if (!result) {
                                final CodeAnalysis.Message message = new CodeAnalysis.Message("Location has no incoming edges", CodeAnalysis.MessageType.WARNING, location);
                                messages.put(location, message);
                                CodeAnalysis.addMessage(component, message);
                            }
                        }
                    }
                });

                alreadyRunningNoIncomingEdgesCheck = false;
            }).start();
        };

        final Component component = getComponent();
        checkLocations.accept(component);

        // Check location whenever we get new edges
        component.getEdges().addListener(new ListChangeListener<Edge>() {
            @Override
            public void onChanged(final Change<? extends Edge> c) {
                while (c.next()) {
                    checkLocations.accept(component);
                }
            }
        });

        // Check location whenever we get new locations
        component.getLocations().addListener(new ListChangeListener<Location>() {
            @Override
            public void onChanged(final Change<? extends Location> c) {
                while (c.next()) {
                    if(!c.getRemoved().isEmpty()){
                        alreadyRunningNoIncomingEdgesCheck = false;
                        lastChanged = 0;
                    }
                    checkLocations.accept(component);
                }
            }
        });
    }

    private void initializeJorkHandling(final Component newComponent) {
        final Consumer<Jork> handleAddedJork = newJork -> {
            final JorkPresentation jorkPresentation = new JorkPresentation(newJork, newComponent);
            jorkPresentationMap.put(newJork, jorkPresentation);
            modelContainerJork.getChildren().add(jorkPresentation);
        };


        // React on addition of jorks to the component
        newComponent.getJorks().addListener(new ListChangeListener<Jork>() {
            @Override
            public void onChanged(final Change<? extends Jork> c) {
                if (c.next()) {
                    // Edges are added to the component
                    c.getAddedSubList().forEach(handleAddedJork::accept);

                    // Edges are removed from the component
                    c.getRemoved().forEach(edge -> {
                        final JorkPresentation jorkPresentation = jorkPresentationMap.get(edge);
                        modelContainerJork.getChildren().remove(jorkPresentation);
                        jorkPresentationMap.remove(edge);
                    });
                }
            }
        });

        newComponent.getJorks().forEach(handleAddedJork);
    }

    private void initializeSubComponentUniqueNameError() {
        final HashMap<String, ArrayList<CodeAnalysis.Message>> errorsMap = new HashMap<>();

        final Runnable checkNames = () -> {
            final HashMap<String, Integer> occurrences = new HashMap<>();

            subComponentPresentationMap.keySet().forEach(subComponent -> {

                // Check if we have seen the identifier of the sub component before
                final String identifier = subComponent.getIdentifier();
                if (occurrences.containsKey(identifier)) {
                    occurrences.put(identifier, occurrences.get(identifier) + 1);
                } else {
                    occurrences.put(identifier, 0);
                }
            });

            // Check if we have previously added an error for each of the found duplicates
            occurrences.keySet().forEach(id -> {
                if (!errorsMap.containsKey(id)) {
                    errorsMap.put(id, new ArrayList<>());
                }

                final ArrayList<CodeAnalysis.Message> messages = errorsMap.get(id);
                final int addedErrors = messages.size();
                final int foundErrors = occurrences.get(id);

                if (addedErrors > foundErrors) { // There are too many errors in the view
                    final CodeAnalysis.Message messageToRemove = messages.get(0);
                    messages.remove(messageToRemove);
                    Platform.runLater(() -> CodeAnalysis.removeMessage(getComponent(), messageToRemove));
                } else if (addedErrors < foundErrors) { // There are too few errors in the view
                    // Find all subcomponents with that name
                    final List<Nearable> clashingSubcomponents = new ArrayList<>();

                    getComponent().getSubComponents().forEach(subComponent -> {
                        if (subComponent.getIdentifier().equals(id)) {
                            clashingSubcomponents.add(subComponent);
                        }
                    });

                    final CodeAnalysis.Message identifierIsNotUnique = new CodeAnalysis.Message("Identifier '" + id + "' is multiply defined", CodeAnalysis.MessageType.ERROR, clashingSubcomponents);
                    messages.add(identifierIsNotUnique);
                    Platform.runLater(() -> CodeAnalysis.addMessage(getComponent(), identifierIsNotUnique));
                }
            });

            // Remove any messages that are no longer found
            errorsMap.keySet().forEach(id -> {
                if (!occurrences.containsKey(id)) {
                    errorsMap.get(id).forEach(message -> Platform.runLater(() -> CodeAnalysis.removeMessage(getComponent(), message)));
                    errorsMap.put(id, new ArrayList<>());
                }
            });

        };

        // Wait until component is not null
        if (!COMPONENT_SUBCOMPONENT_NAME_CHECK_TIMER_MAP.containsKey(getComponent())) {
            final TimerTask reachabilityCheckTask = new TimerTask() {
                @Override
                public void run() {
                    if (getComponent() == null) return;
                    checkNames.run();
                }
            };

            final int interval = 2000; // ms
            final Timer timer = new Timer();
            timer.schedule(reachabilityCheckTask, 0, interval);

            COMPONENT_SUBCOMPONENT_NAME_CHECK_TIMER_MAP.put(getComponent(), timer);
        }

        // Cancel timers when the component is removed
        HUPPAAL.getProject().getComponents().addListener(new ListChangeListener<Component>() {
            @Override
            public void onChanged(Change<? extends Component> c) {
                while (c.next()) {
                    c.getRemoved().forEach(removedComponent -> {
                        if (COMPONENT_SUBCOMPONENT_NAME_CHECK_TIMER_MAP.containsKey(removedComponent)) {
                            COMPONENT_SUBCOMPONENT_NAME_CHECK_TIMER_MAP.get(removedComponent).cancel();
                        }
                    });
                }
            }
        });
    }

    private void initializeComponentContextMenu() {
        final Consumer<Component> initializeDropDownMenu = (component) -> {
            if (component == null)
                return;

            contextMenu = new DropDownMenu(root, 230, true);

            contextMenu.addClickableListElement("Add Location", event -> {
                contextMenu.close();

                final Location newLocation = new Location(component);

                double x = DropDownMenu.x - LocationPresentation.RADIUS / 2;
                x = Math.round(x / GRID_SIZE) * GRID_SIZE;
                newLocation.setX(x);

                double y = DropDownMenu.y - LocationPresentation.RADIUS / 2;
                y = Math.round(y / GRID_SIZE) * GRID_SIZE;
                newLocation.setY(y);

                newLocation.setColorIntensity(component.getColorIntensity());
                newLocation.setColor(component.getColor());

                // Add a new location
                UndoRedoStack.push(() -> { // Perform
                    component.addLocation(newLocation);
                }, () -> { // Undo
                    component.removeLocation(newLocation);
                }, "Added location '" + newLocation.toString() + "' to component '" + component.getName() + "'", "add-circle");
            });

            contextMenu.addClickableListElement("Add Fork", event -> {
                contextMenu.close();

                final Jork newJork = new Jork(Jork.Type.FORK, component);

                double x = DropDownMenu.x - LocationPresentation.RADIUS / 2;
                x = Math.round(x / GRID_SIZE) * GRID_SIZE;
                newJork.setX(x);

                double y = DropDownMenu.y - LocationPresentation.RADIUS / 2;
                y = Math.round(y / GRID_SIZE) * GRID_SIZE;
                newJork.setY(y);

                // Add a new location
                UndoRedoStack.push(() -> { // Perform
                    component.addJork(newJork);
                }, () -> { // Undo
                    component.removeJork(newJork);
                }, "Added fork '" + newJork + "' to component '" + component.getName() + "'", "add-circle");
            });

            contextMenu.addClickableListElement("Add Join", event -> {
                contextMenu.close();

                final Jork newJork = new Jork(Jork.Type.JOIN, component);

                double x = DropDownMenu.x - LocationPresentation.RADIUS / 2;
                x = Math.round(x / GRID_SIZE) * GRID_SIZE;
                newJork.setX(x);

                double y = DropDownMenu.y - LocationPresentation.RADIUS / 2;
                y = Math.round(y / GRID_SIZE) * GRID_SIZE;
                newJork.setY(y);

                // Add a new location
                UndoRedoStack.push(() -> { // Perform
                    component.addJork(newJork);
                }, () -> { // Undo
                    component.removeJork(newJork);
                }, "Added join '" + newJork.toString() + "' to component '" + component.getName() + "'", "add-circle");
            });

            //If-statement added to avoid empty submenu being added and appearing as a white box
            final DropDownMenu subcomponentSelectionMenu;
            if(HUPPAAL.getProject().getComponents().size() > 1) {
                subcomponentSelectionMenu = new DropDownMenu(root, 150, false);
                HUPPAAL.getProject().getComponents().forEach(c -> {
                    if (!c.equals(component)) {
                        subcomponentSelectionMenu.addClickableListElement(c.getName(), event -> {
                            contextMenu.close();
                            var newSubComponent = new SubComponent(c, component);
                            var x = DropDownMenu.x - GRID_SIZE * 2;
                            var y = DropDownMenu.y - GRID_SIZE * 2;
                            x -= x % GRID_SIZE;
                            y -= y % GRID_SIZE;
                            newSubComponent.setX(x);
                            newSubComponent.setY(y);

                            UndoRedoStack.push(() -> {
                                component.addSubComponent(newSubComponent);
                            }, () -> {
                                component.removeSubComponent(newSubComponent);
                            }, "Added sub-component '" + newSubComponent + "' to component '" + component.getName() + "'", "add-circle");
                        });
                    }
                });
            } else {
                subcomponentSelectionMenu = new DropDownMenu(root, 150, false);
                subcomponentSelectionMenu.addClickableAndDisableableListElement("No Subcomponents", new SimpleBooleanProperty(true), event -> {});
            }
            contextMenu.addSubMenu("Add Subcomponent", subcomponentSelectionMenu, 3 * 35);
            contextMenu.addSpacerElement();
            contextMenu.addClickableListElement("Contains deadlock?", event -> {
                var deadlockQuery = UPPAALDriverManager.getInstance().getExistDeadlockQuery(getComponent());
                var deadlockComment = "Does " + component.getName() + " contain a deadlock?";
                var query = new Query(deadlockQuery, deadlockComment, QueryState.UNKNOWN);
                HUPPAAL.getProject().getQueries().add(query);
                query.run();
                contextMenu.close();
            });

            contextMenu.addSpacerElement();
            contextMenu.addListElement("Color");
            contextMenu.addColorPicker(component, component::color);
        };

        component.addListener((obs, oldComponent, newComponent) -> {
            initializeDropDownMenu.accept(newComponent);
        });

        HUPPAAL.getProject().getComponents().addListener((ListChangeListener<Component>) c -> initializeDropDownMenu.accept(getComponent()));

        initializeDropDownMenu.accept(getComponent());
    }

    private void initializeFinishEdgeContextMenu(final Edge unfinishedEdge) {
        final Consumer<Component> initializeDropDownMenu = (component) -> {
            if (component == null)
                return;

            final Consumer<LocationAware> setCoordinates = (locationAware) -> {
                double x = DropDownMenu.x;
                x = Math.round(x / GRID_SIZE) * GRID_SIZE;

                double y = DropDownMenu.y;
                y = Math.round(y / GRID_SIZE) * GRID_SIZE;

                locationAware.xProperty().set(x);
                locationAware.yProperty().set(y);
            };

            finishEdgeContextMenu = new DropDownMenu(root, 230, true);

            finishEdgeContextMenu.addListElement("Finish edge in a:");

            finishEdgeContextMenu.addClickableListElement("Location", event -> {
                finishEdgeContextMenu.close();

                final Location location = new Location(component);

                location.setColorIntensity(getComponent().getColorIntensity());
                location.setColor(getComponent().getColor());

                unfinishedEdge.setTargetLocation(location);

                setCoordinates.accept(location);

                // Add a new location
                UndoRedoStack.push(() -> { // Perform
                    getComponent().addLocation(location);
                    UndoRedoStack.redo();
                }, () -> { // Undo
                    getComponent().removeLocation(location);
                    UndoRedoStack.undo();
                }, "Finished edge '" + unfinishedEdge + "' by adding '" + location + "' to component '" + component.getName() + "'", "add-circle");
            });

            finishEdgeContextMenu.addClickableAndDisableableListElement("Fork", new SimpleBooleanProperty(unfinishedEdge.getSourceLocation() == null), event -> {
                finishEdgeContextMenu.close();

                final Jork jork = new Jork(Jork.Type.FORK, component);

                unfinishedEdge.setTargetJork(jork);

                setCoordinates.accept(jork);

                // Add a new jork
                UndoRedoStack.push(() -> { // Perform
                    getComponent().addJork(jork);
                    UndoRedoStack.redo();
                }, () -> { // Undo
                    getComponent().removeJork(jork);
                    UndoRedoStack.undo();
                }, "Finished edge '" + unfinishedEdge + "' by adding '" + jork + "' to component '" + component.getName() + "'", "add-circle");
            });

            finishEdgeContextMenu.addClickableAndDisableableListElement("Join", new SimpleBooleanProperty(unfinishedEdge.getSourceSubComponent() == null), event -> {
                finishEdgeContextMenu.close();

                final Jork jork = new Jork(Jork.Type.JOIN, component);

                unfinishedEdge.setTargetJork(jork);

                setCoordinates.accept(jork);

                // Add a new jork
                UndoRedoStack.push(() -> { // Perform
                    getComponent().addJork(jork);
                    UndoRedoStack.redo();
                }, () -> { // Undo
                    getComponent().removeJork(jork);
                    UndoRedoStack.undo();
                }, "Finished edge '" + unfinishedEdge + "' by adding '" + jork + "' to component '" + component.getName() + "'", "add-circle");
            });

            final DropDownMenu subMenu = new DropDownMenu(root, 150, false);
            HUPPAAL.getProject().getComponents().forEach(c -> {
                if (!c.equals(component)) {
                    subMenu.addClickableListElement(c.getName(), event -> {
                        contextMenu.close();

                        final SubComponent newSubComponent = new SubComponent(c, component);

                        unfinishedEdge.setTargetSubComponent(newSubComponent);

                        setCoordinates.accept(newSubComponent);
                        newSubComponent.setX(newSubComponent.getX() - GRID_SIZE * 2);
                        newSubComponent.setY(newSubComponent.getY() - GRID_SIZE * 2);

                        // Add a new sub-component
                        UndoRedoStack.push(() -> { // Perform
                            component.addSubComponent(newSubComponent);
                            UndoRedoStack.redo();
                        }, () -> { // Undo
                            component.removeSubComponent(newSubComponent);
                            UndoRedoStack.undo();
                        }, "Finished edge '" + unfinishedEdge + "' by adding '" + newSubComponent + "' to component '" + component.getName() + "'", "add-circle");
                    });
                }
            });

            finishEdgeContextMenu.addSubMenu("Subcomponent", subMenu, 4 * 35);

        };

        component.addListener((obs, oldComponent, newComponent) -> {
            initializeDropDownMenu.accept(newComponent);
        });

        initializeDropDownMenu.accept(getComponent());
    }

    private void initializeLocationHandling(final Component newComponent) {
        final Consumer<Location> handleAddedLocation = (loc) -> {
            // Create a new presentation, and register it on the map
            final LocationPresentation newLocationPresentation = new LocationPresentation(loc, newComponent);

            final ChangeListener<Number> locationPlacementChangedListener = (observable, oldValue, newValue) -> {
                final double offset = newLocationPresentation.getController().circle.getRadius() * 2 + GRID_SIZE;
                boolean hit = false;
                ItemDragHelper.DragBounds componentBounds = newLocationPresentation.getController().getDragBounds();

                //Define the x and y coordinates for the initial and final locations
                final double initialLocationX = component.get().getX() + newLocationPresentation.getController().circle.getRadius() * 2,
                        initialLocationY = component.get().getY() + newLocationPresentation.getController().circle.getRadius() * 2,
                        finalLocationX = component.get().getX() + component.get().getWidth() - newLocationPresentation.getController().circle.getRadius() * 2,
                        finalLocationY = component.get().getY() + component.get().getHeight() - newLocationPresentation.getController().circle.getRadius() * 2;

                double latestHitRight = 0,
                        latestHitDown = 0,
                        latestHitLeft = 0,
                        latestHitUp = 0;

                //Check to see if the location is placed on top of the initial location
                if(Math.abs(initialLocationX - (newLocationPresentation.getLayoutX())) < offset &&
                        Math.abs(initialLocationY - (newLocationPresentation.getLayoutY())) < offset){
                    hit = true;
                    latestHitRight = initialLocationX;
                    latestHitDown = initialLocationY;
                    latestHitLeft = initialLocationX;
                    latestHitUp = initialLocationY;
                }

                //Check to see if the location is placed on top of the final location
                else if (Math.abs(finalLocationX - (newLocationPresentation.getLayoutX())) < offset &&
                        Math.abs(finalLocationY - (newLocationPresentation.getLayoutY())) < offset) {
                    hit = true;
                    latestHitRight = finalLocationX;
                    latestHitDown = finalLocationY;
                    latestHitLeft = finalLocationX;
                    latestHitUp = finalLocationY;
                }

                //Check to see if the location is placed on top of another location
                else {
                    for (Map.Entry<Location, LocationPresentation> entry : locationPresentationMap.entrySet()) {
                        if (entry.getValue() != newLocationPresentation &&
                                Math.abs(entry.getValue().getLayoutX() - (newLocationPresentation.getLayoutX())) < offset &&
                                Math.abs(entry.getValue().getLayoutY() - (newLocationPresentation.getLayoutY())) < offset) {
                            hit = true;
                            latestHitRight = entry.getValue().getLayoutX();
                            latestHitDown = entry.getValue().getLayoutY();
                            latestHitLeft = entry.getValue().getLayoutX();
                            latestHitUp = entry.getValue().getLayoutY();
                            break;
                        }
                    }
                }

                //If the location is not placed on top of any other locations, do not do anything
                if(!hit) {
                    return;
                }
                hit = false;

                //Find an unoccupied space for the location
                for(int i = 1; i < component.get().getWidth() / offset; i++){

                    //Check to see, if the location can be placed to the right of the existing locations
                    if(componentBounds.trimX(latestHitRight + offset) == latestHitRight + offset) {

                        //Check if the location would be placed on the final location
                        if(Math.abs(finalLocationX - (latestHitRight + offset)) < offset &&
                                Math.abs(finalLocationY - (newLocationPresentation.getLayoutY())) < offset){
                            hit = true;
                            latestHitRight = finalLocationX;
                        } else {
                            for (Map.Entry<Location, LocationPresentation> entry : locationPresentationMap.entrySet()) {
                                if (entry.getValue() != newLocationPresentation &&
                                        Math.abs(entry.getValue().getLayoutX() - (latestHitRight + offset)) < offset &&
                                        Math.abs(entry.getValue().getLayoutY() - (newLocationPresentation.getLayoutY())) < offset) {
                                    hit = true;
                                    latestHitRight = entry.getValue().getLayoutX();
                                    break;
                                }
                            }
                        }

                        if(!hit) {
                            newLocationPresentation.setLayoutX(latestHitRight + offset);
                            return;
                        }
                    }
                    hit = false;

                    //Check to see, if the location can be placed below the existing locations
                    if(componentBounds.trimY(latestHitDown + offset) == latestHitDown + offset) {

                        //Check if the location would be placed on the final location
                        if(Math.abs(finalLocationX - (newLocationPresentation.getLayoutX())) < offset &&
                                Math.abs(finalLocationY - (latestHitDown + offset)) < offset){
                            hit = true;
                            latestHitDown = finalLocationY;
                        } else {
                            for (Map.Entry<Location, LocationPresentation> entry : locationPresentationMap.entrySet()) {
                                if (entry.getValue() != newLocationPresentation &&
                                        Math.abs(entry.getValue().getLayoutX() - (newLocationPresentation.getLayoutX())) < offset &&
                                        Math.abs(entry.getValue().getLayoutY() - (latestHitDown + offset)) < offset) {
                                    hit = true;
                                    latestHitDown = entry.getValue().getLayoutY();
                                    break;
                                }
                            }
                        }
                        if(!hit) {
                            newLocationPresentation.setLayoutY(latestHitDown + offset);
                            return;
                        }
                    }
                    hit = false;

                    //Check to see, if the location can be placed to the left of the existing locations
                    if(componentBounds.trimX(latestHitLeft - offset) == latestHitLeft - offset) {

                        //Check if the location would be placed on the initial location
                        if(Math.abs(initialLocationX - (latestHitLeft - offset)) < offset &&
                                Math.abs(initialLocationY - (newLocationPresentation.getLayoutY())) < offset){
                            hit = true;
                            latestHitLeft = initialLocationX;
                        } else {
                            for (Map.Entry<Location, LocationPresentation> entry : locationPresentationMap.entrySet()) {
                                if (entry.getValue() != newLocationPresentation &&
                                        Math.abs(entry.getValue().getLayoutX() - (latestHitLeft - offset)) < offset &&
                                        Math.abs(entry.getValue().getLayoutY() - (newLocationPresentation.getLayoutY())) < offset) {
                                    hit = true;
                                    latestHitLeft = entry.getValue().getLayoutX();
                                    break;
                                }
                            }
                        }
                        if(!hit) {
                            newLocationPresentation.setLayoutX(latestHitLeft - offset);
                            return;
                        }
                    }
                    hit = false;

                    //Check to see, if the location can be placed above the existing locations
                    if(componentBounds.trimY(latestHitUp - offset) == latestHitUp - offset) {

                        //Check if the location would be placed on the initial location
                        if(Math.abs(initialLocationX - (newLocationPresentation.getLayoutX())) < offset &&
                                Math.abs(initialLocationY - (latestHitUp - offset)) < offset){
                            hit = true;
                            latestHitUp = initialLocationY;
                        } else {
                            for (Map.Entry<Location, LocationPresentation> entry : locationPresentationMap.entrySet()) {
                                if (entry.getValue() != newLocationPresentation &&
                                        Math.abs(entry.getValue().getLayoutX() - (newLocationPresentation.getLayoutX())) < offset &&
                                        Math.abs(entry.getValue().getLayoutY() - (latestHitUp - offset)) < offset) {
                                    hit = true;
                                    latestHitUp = entry.getValue().getLayoutY();
                                    break;
                                }
                            }
                        }
                        if(!hit) {
                            newLocationPresentation.setLayoutY(latestHitUp - offset);
                            return;
                        }
                    }
                    hit = false;
                    
                }
            };

            newLocationPresentation.layoutXProperty().addListener(locationPlacementChangedListener);

            newLocationPresentation.layoutYProperty().addListener(locationPlacementChangedListener);

            locationPresentationMap.put(loc, newLocationPresentation);

            // Add it to the view
            modelContainerLocation.getChildren().add(newLocationPresentation);

            // Bind the newly created location to the mouse and tell the ui that it is not placed yet
            if (loc.getX() == 0) {
                newLocationPresentation.setPlaced(false);
                BindingHelper.bind(loc, newComponent.xProperty(), newComponent.yProperty());
            }
        };

        if(locationListChangeListenerMap.containsKey(newComponent)) {
            newComponent.getLocations().removeListener(locationListChangeListenerMap.get(newComponent));
        }
        final ListChangeListener<Location> locationListChangeListener = c -> {
            if (c.next()) {
                c.getAddedSubList().forEach(handleAddedLocation);
                c.getRemoved().forEach(location -> {
                    modelContainerLocation.getChildren().remove(locationPresentationMap.get(location));
                    locationPresentationMap.remove(location);
                });
            }
        };
        newComponent.getLocations().addListener(locationListChangeListener);
        locationListChangeListenerMap.put(newComponent, locationListChangeListener);

        newComponent.getLocations().forEach(handleAddedLocation);
    }

    private void initializeEdgeHandling(final Component newComponent) {
        final Consumer<Edge> handleAddedEdge = edge -> {
            final EdgePresentation edgePresentation = new EdgePresentation(edge, newComponent);
            edgePresentationMap.put(edge, edgePresentation);
            modelContainerEdge.getChildren().add(edgePresentation);

            final Consumer<Circular> updateMouseTransparency = (newCircular) -> {
                if (newCircular == null) {
                    edgePresentation.setMouseTransparent(true);
                } else {
                    edgePresentation.setMouseTransparent(false);
                }
            };

            edge.targetCircularProperty().addListener((obs1, oldTarget, newTarget) -> updateMouseTransparency.accept(newTarget));
            updateMouseTransparency.accept(edge.getTargetCircular());
        };


        // React on addition of edges to the component
        newComponent.getEdges().addListener(new ListChangeListener<Edge>() {
            @Override
            public void onChanged(final Change<? extends Edge> c) {
                if (c.next()) {
                    // Edges are added to the component
                    c.getAddedSubList().forEach(handleAddedEdge::accept);

                    // Edges are removed from the component
                    c.getRemoved().forEach(edge -> {
                        final EdgePresentation edgePresentation = edgePresentationMap.get(edge);
                        modelContainerEdge.getChildren().remove(edgePresentation);
                        edgePresentationMap.remove(edge);
                    });
                }
            }
        });

        newComponent.getEdges().forEach(handleAddedEdge);
    }

    private void initializeSubComponentHandling(final Component newSubComponent) {
        final Consumer<SubComponent> handleAddedSubComponent = subComponent -> {
            if(subComponent.getComponent() == null)
                return;
            var subComponentPresentation = new SubComponentPresentation(subComponent, getComponent());
            subComponentPresentationMap.put(subComponent, subComponentPresentation);
            modelContainerSubComponent.getChildren().add(subComponentPresentation);
        };

        // React on addition of subcomponents to the component
        newSubComponent.getSubComponents().addListener((ListChangeListener<SubComponent>) c -> {
            if (c.next()) {
                // SubComponents are added to the component
                c.getAddedSubList().forEach(handleAddedSubComponent);
                // SubComponents are removed from the component
                c.getRemoved().forEach(subComponent -> {
                    final SubComponentPresentation subComponentPresentation = subComponentPresentationMap.get(subComponent);
                    modelContainerSubComponent.getChildren().remove(subComponentPresentation);
                });
            }
        });

        newSubComponent.getSubComponents().forEach(handleAddedSubComponent);
    }

    private void initializeDeclarations() {
        // Initially style the declarations
        declaration.setStyleSpans(0, ComponentPresentation.computeHighlighting(getComponent().getDeclarations()));

        final Circle circle = new Circle(0);
        if(getComponent().isDeclarationOpen()) {
            circle.setRadius(1000);
        }
        final ObjectProperty<Node> clip = new SimpleObjectProperty<>(circle);
        declaration.clipProperty().bind(clip);
        clip.set(circle);
    }

    public void toggleDeclaration(final MouseEvent mouseEvent) {
        final Circle circle = new Circle(0);
        circle.setCenterX(component.get().getWidth() - (toggleDeclarationButton.getWidth() - mouseEvent.getX()));
        circle.setCenterY(-1 * mouseEvent.getY());

        final ObjectProperty<Node> clip = new SimpleObjectProperty<>(circle);
        declaration.clipProperty().bind(clip);

        final Transition rippleEffect = new Transition() {
            private final double maxRadius = Math.sqrt(Math.pow(getComponent().getWidth(), 2) + Math.pow(getComponent().getHeight(), 2));

            {
                setCycleDuration(Duration.millis(500));
            }

            protected void interpolate(final double fraction) {
                if (getComponent().isDeclarationOpen()) {
                    circle.setRadius(fraction * maxRadius);
                } else {
                    circle.setRadius(maxRadius - fraction * maxRadius);
                }
                clip.set(circle);
            }
        };

        final Interpolator interpolator = Interpolator.SPLINE(0.785, 0.135, 0.15, 0.86);
        rippleEffect.setInterpolator(interpolator);

        rippleEffect.play();
        getComponent().declarationOpenProperty().set(!getComponent().isDeclarationOpen());
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
    private void modelContainerPressed(final MouseEvent event) {
        event.consume();

        CanvasController.leaveTextAreas();

        final Edge unfinishedEdge = getComponent().getUnfinishedEdge();


        if ((event.isShiftDown() && event.isPrimaryButtonDown()) || event.isMiddleButtonDown()) {

            final Location location = new Location(component.get());

            double x = event.getX();
            x = Math.round(x / GRID_SIZE) * GRID_SIZE;
            location.setX(x);

            double y = event.getY();
            y = Math.round(y / GRID_SIZE) * GRID_SIZE;
            location.setY(y);

            location.setColorIntensity(getComponent().getColorIntensity());
            location.setColor(getComponent().getColor());

            if (unfinishedEdge != null) {
                unfinishedEdge.setTargetLocation(location);
            }

            // Add a new location
            UndoRedoStack.push(() -> { // Perform
                getComponent().addLocation(location);
                if (unfinishedEdge != null) {
                    UndoRedoStack.redo();
                }
            }, () -> { // Undo
                getComponent().removeLocation(location);
                if (unfinishedEdge != null) {
                    UndoRedoStack.undo();
                }
            }, "Finished edge '" + unfinishedEdge + "' by adding '" + location + "' to component '" + component.getName() + "'", "add-circle");


        } else if (event.isSecondaryButtonDown()) {
            if (unfinishedEdge == null) {
                contextMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, event.getX(), event.getY());
            } else {
                initializeFinishEdgeContextMenu(unfinishedEdge);
                finishEdgeContextMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, event.getX(), event.getY());
            }
        } else if(event.isPrimaryButtonDown()) {
            // We are drawing an edge
            if (unfinishedEdge != null) {
                // Calculate the position for the new nail (based on the component position and the canvas mouse tracker)
                final DoubleBinding x = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty());
                final DoubleBinding y = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty());

                // Create the abstraction for the new nail and add it to the unfinished edge
                final Nail newNail = new Nail(x, y);
                unfinishedEdge.addNail(newNail);
            } else {
                SelectHelper.clearSelectedElements();
            }
        }

    }

    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

}
