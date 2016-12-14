package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.*;
import SW9.code_analysis.CodeAnalysis;
import SW9.presentations.*;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.Circular;
import SW9.utility.helpers.ItemDragHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.mouse.MouseTracker;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;

public class ComponentController implements Initializable, SelectHelper.ColorSelectable {

    private static final Map<Component, Timer> COMPONENT_SUBCOMPONENT_NAME_CHECK_TIMER_MAP = new HashMap<>();
    private static final Map<Component, ListChangeListener<Location>> locationListChangeListenerMap = new HashMap<>();
    private static Location placingLocation = null;
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);
    private final Map<Edge, EdgePresentation> edgePresentationMap = new HashMap<>();
    private final Map<Location, LocationPresentation> locationPresentationMap = new HashMap<>();
    private final Map<SubComponent, SubComponentPresentation> subComponentPresentationMap = new HashMap<>();
    public BorderPane toolbar;
    public Rectangle background;
    public TextArea declaration;
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
    private MouseTracker mouseTracker;
    private DropDownMenu dropDownMenu;
    private Circle dropDownMenuHelperCircle;

    public static boolean isPlacingLocation() {
        return placingLocation != null;
    }

    public static void setPlacingLocation(final Location placingLocation) {
        ComponentController.placingLocation = placingLocation;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        // Register a keybind for adding new locations
        KeyboardTracker.registerKeybind(KeyboardTracker.ADD_NEW_LOCATION, new Keybind(new KeyCodeCombination(KeyCode.L), () -> {
            if (isPlacingLocation()) return;

            final Location newLocation = new Location();
            setPlacingLocation(newLocation);

            UndoRedoStack.push(() -> { // Perform
                component.get().addLocation(newLocation);
            }, () -> { // Undo
                component.get().removeLocation(newLocation);
            }, "Added new location: " + newLocation.getNickname(), "add-circle");

            CanvasController.activeComponentProperty().addListener((observable, oldValue, newValue) -> {
                if(!newValue.equals(getComponent())) {
                    if (isPlacingLocation()) {
                        component.get().removeLocation(placingLocation);
                        ComponentController.setPlacingLocation(null);
                        UndoRedoStack.forget();
                    }
                }
            });

            newLocation.setColorIntensity(getComponent().getColorIntensity());
            newLocation.setColor(getComponent().getColor());

            KeyboardTracker.registerKeybind(KeyboardTracker.ABANDON_LOCATION, new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
                if (isPlacingLocation()) {
                    component.get().removeLocation(placingLocation);
                    ComponentController.setPlacingLocation(null);
                    UndoRedoStack.forget();
                }

            }));
        }));

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
            declaration.setText(newComponent.getDeclarations());
            newComponent.declarationsProperty().bindBidirectional(declaration.textProperty());

            initializeEdgeHandling(newComponent);
            initializeLocationHandling(newComponent);
            initializeSubComponentHandling(newComponent);

            // When we update the color of the component, also update the color of the initial and final locations if the colors are the same
            newComponent.colorProperty().addListener((obs1, oldColor, newColor) -> {
                final Location initialLocation = newComponent.getInitialLocation();
                if (initialLocation.getColor().equals(oldColor)) {
                    initialLocation.setColor(newColor);
                    initialLocation.setColorIntensity(newComponent.getColorIntensity());
                }

                final Location finalLocation = newComponent.getFinalLocation();
                if (finalLocation.getColor().equals(oldColor)) {
                    finalLocation.setColor(newColor);
                    finalLocation.setColorIntensity(newComponent.getColorIntensity());
                }
            });
        });

        // The root view have been inflated, initialize the mouse tracker on it
        mouseTracker = new MouseTracker(root);

        initializeDropDownMenu();

        initializeSubComponentUniqueNameError();
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
                    final CodeAnalysis.Message identifierIsNotUnique = new CodeAnalysis.Message("Identifier '" + id + "' is multiply defined", CodeAnalysis.MessageType.ERROR);
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
        component.addListener((obs, oldComponent, newComponent) -> {
            if (!COMPONENT_SUBCOMPONENT_NAME_CHECK_TIMER_MAP.containsKey(newComponent)) {
                TimerTask reachabilityCheckTask = new TimerTask() {
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
        });

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

    private void initializeDropDownMenu() {
        dropDownMenuHelperCircle = new Circle(5);
        dropDownMenuHelperCircle.setOpacity(0);
        dropDownMenuHelperCircle.setMouseTransparent(true);

        root.getChildren().add(dropDownMenuHelperCircle);

        final Consumer<Component> initializeDropDownMenu = (component) -> {
            if (component == null) {
                return;
            }

            dropDownMenu = new DropDownMenu(root, dropDownMenuHelperCircle, 230, true);

            final DropDownMenu subMenu = new DropDownMenu(root, dropDownMenuHelperCircle, 150, false);
            HUPPAAL.getProject().getComponents().forEach(c -> {
                if (!c.equals(component)) {
                    subMenu.addClickableListElement(c.getName(), event -> {
                        dropDownMenu.close();

                        final SubComponent newSubComponent = new SubComponent(c);

                        // Add a new sub-component
                        UndoRedoStack.push(() -> { // Perform
                            component.addSubComponent(newSubComponent);
                        }, () -> { // Undo
                            component.removeSubComponent(newSubComponent);
                        }, "Added sub-component '" + newSubComponent.toString() + "' to component '" + component.getName() + "'", "add-circle");
                    });
                }
            });

            dropDownMenu.addSubMenu("Add subcomponent", subMenu);
            dropDownMenu.addSpacerElement();
            dropDownMenu.addListElement("Color");
            dropDownMenu.addColorPicker(component);
        };


        component.addListener((obs, oldComponent, newComponent) -> {
            initializeDropDownMenu.accept(newComponent);
        });

        HUPPAAL.getProject().getComponents().addListener(new ListChangeListener<Component>() {
            @Override
            public void onChanged(final Change<? extends Component> c) {
                initializeDropDownMenu.accept(getComponent());
            }
        });

        initializeDropDownMenu.accept(getComponent());
    }

    private void initializeLocationHandling(final Component newComponent) {
        final Consumer<Location> handleAddedLocation = (loc) -> {
            // Create a new presentation, and register it on the map
            final LocationPresentation newLocationPresentation = new LocationPresentation(loc, newComponent);
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
                // Locations are added to the component
                c.getAddedSubList().forEach(handleAddedLocation::accept);

                // Locations are removed from the component
                c.getRemoved().forEach(location -> {
                    final LocationPresentation locationPresentation = locationPresentationMap.get(location);
                    modelContainerLocation.getChildren().remove(locationPresentation);
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

            final BiConsumer<Circular, Circular> updateMouseTransparency = (newTargetLocation, newTargetSubComponent) -> {
                if (newTargetLocation == null && newTargetSubComponent == null) {
                    edgePresentation.setMouseTransparent(true);
                } else {
                    edgePresentation.setMouseTransparent(false);
                }
            };

            edge.targetLocationProperty().addListener((obs1, oldTarget, newTarget) -> updateMouseTransparency.accept(newTarget, edge.getTargetSubComponent()));
            edge.targetSubComponentProperty().addListener((obs1, oldTarget, newTarget) -> updateMouseTransparency.accept(edge.getTargetLocation(), newTarget));
            updateMouseTransparency.accept(edge.getTargetLocation(), edge.getTargetSubComponent());
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
            final SubComponentPresentation subComponentPresentation = new SubComponentPresentation(subComponent, getComponent());
            subComponentPresentationMap.put(subComponent, subComponentPresentation);
            modelContainerSubComponent.getChildren().add(subComponentPresentation);
        };

        // React on addition of sub components to the component
        newSubComponent.getSubComponents().addListener(new ListChangeListener<SubComponent>() {
            @Override
            public void onChanged(final Change<? extends SubComponent> c) {
                if (c.next()) {
                    // SubComponents are added to the component
                    c.getAddedSubList().forEach(handleAddedSubComponent::accept);

                    // SubComponents are removed from the component
                    c.getRemoved().forEach(subComponent -> {
                        final SubComponentPresentation subComponentPresentation = subComponentPresentationMap.get(subComponent);
                        modelContainerSubComponent.getChildren().remove(subComponentPresentation);
                    });
                }
            }
        });

        newSubComponent.getSubComponents().forEach(handleAddedSubComponent);

        makeDraggable();
    }

    public void toggleDeclaration(final MouseEvent mouseEvent) {
        declaration.setVisible(true);

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

        final Edge unfinishedEdge = getComponent().getUnfinishedEdge();

        if (event.isSecondaryButtonDown() && unfinishedEdge == null) {
            dropDownMenuHelperCircle.setLayoutX(event.getX());
            dropDownMenuHelperCircle.setLayoutY(event.getY());
            dropDownMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 0, 0);
        } else {
            // We are drawing an edge
            if (unfinishedEdge != null) {
                // Calculate the position for the new nail (based on the component position and the canvas mouse tracker)
                final DoubleBinding x = CanvasPresentation.mouseTracker.gridXProperty().subtract(getComponent().xProperty());
                final DoubleBinding y = CanvasPresentation.mouseTracker.gridYProperty().subtract(getComponent().yProperty());

                // Create the abstraction for the new nail and add it to the unfinished edge
                final Nail newNail = new Nail(x, y);
                unfinishedEdge.addNail(newNail);
            }
        }


    }

    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    @Override
    public void color(final Color color, final Color.Intensity intensity) {
        final Component component = getComponent();

        final Color componentColor = component.getColor();

        final Consumer<Location> locationPainter = location -> {
            if (!location.getColor().equals(componentColor)) return; // Do not color something of a different color

            location.setColorIntensity(intensity);
            location.setColor(color);
        };

        // Set the color for all locations that are of the same color
        component.getLocations().forEach(locationPainter);
        locationPainter.accept(component.getInitialLocation());
        locationPainter.accept(component.getFinalLocation());

        // Set the color of the component
        component.setColorIntensity(intensity);
        component.setColor(color);
    }

    @Override
    public Color getColor() {
        return getComponent().getColor();
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return getComponent().getColorIntensity();
    }

    @Override
    public void select() {
        ((SelectHelper.Selectable) root).select();

        final Consumer<Node> selectLocations = node -> {
            if (node instanceof LocationPresentation) {
                SelectHelper.addToSelection(((LocationPresentation) node).getController());
            }
        };

        modelContainerLocation.getChildren().forEach(selectLocations);
        defaultLocationsContainer.getChildren().forEach(selectLocations);
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }

    private void makeDraggable() {

        ItemDragHelper.makeDraggable(
                root,
                toolbar,
                () -> CanvasPresentation.mouseTracker.getGridX(),
                () -> CanvasPresentation.mouseTracker.getGridY(),
                (event) -> {
                    event.consume();
                    SelectHelper.select(this);
                },
                () -> {},
                () -> {}
        );
    }
}
