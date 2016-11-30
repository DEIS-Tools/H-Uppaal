package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.abstractions.Nail;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.EdgePresentation;
import SW9.presentations.LocationPresentation;
import SW9.presentations.SubComponentPresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.BindingHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.mouse.MouseTracker;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ComponentController implements Initializable, SelectHelper.ColorSelectable {

    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);
    private final Map<Edge, EdgePresentation> edgePresentationMap = new HashMap<>();
    private final Map<Location, LocationPresentation> locationPresentationMap = new HashMap<>();
    private final Map<Component, SubComponentPresentation> subComponentPresentationMap = new HashMap<>();

    public BorderPane toolbar;
    public Rectangle background;
    public TextArea declaration;
    public JFXRippler toggleDeclarationButton;
    public BorderPane frame;
    public JFXTextField name;
    public StackPane root;
    public Pane modelContainer;
    public Line line1;
    public Line line2;
    public Label x;
    public Label y;
    public Pane defaultLocationsContainer;
    public Rectangle topAnchor;
    public Rectangle rightAnchor;
    public Rectangle bottomAnchor;
    public Rectangle leftAnchor;
    private MouseTracker mouseTracker;
    private double previousX;
    private double previousY;
    private boolean wasDragged;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // Register a keybind for adding new locations
        KeyboardTracker.registerKeybind(KeyboardTracker.ADD_NEW_LOCATION, new Keybind(new KeyCodeCombination(KeyCode.L), () -> {
            final Location newLocation = new Location();
            newLocation.setColorIntensity(getComponent().getColorIntensity());
            newLocation.setColor(getComponent().getColor());

            UndoRedoStack.push(() -> { // Perform
                component.get().addLocation(newLocation);
            }, () -> { // Undo
                component.get().removeLocation(newLocation);
            }, "Added new location: " + newLocation.getNickname(), "add-circle");
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.ADD_NEW_COMPONENT, new Keybind(new KeyCodeCombination(KeyCode.K), () -> {
            final Component thirdComp = HUPPAAL.getProject().getComponents().get(2);
            UndoRedoStack.push(() -> { // Perform
                        component.get().addSubComponent(thirdComp);
                    }, () -> { // Undo
                        component.get().removeSubComponent(thirdComp);
                    },
                    "Added subcomponent to " + component.getName() + "-component",
                    "add-circle");
        }));


        component.addListener((obs, oldComponent, newComponent) -> {
            // Bind the width and the height of the abstraction to the values in the view todo: reflect the height and width from the presentation into the abstraction
            // Bind the position of the abstraction to the values in the view
            root.layoutXProperty().set(newComponent.getX());
            root.layoutYProperty().set(newComponent.getY());
            newComponent.xProperty().bind(root.layoutXProperty());
            newComponent.yProperty().bind(root.layoutYProperty());

            // Bind the declarations of the abstraction the the view
            declaration.setText(newComponent.getDeclarations());
            newComponent.declarationsProperty().bind(declaration.textProperty());

            initializeEdgeHandling(newComponent);
            initializeLocationHandling(newComponent);
            initializeSubComponentHandling(newComponent);
        });

        // The root view have been inflated, initialize the mouse tracker on it
        mouseTracker = new MouseTracker(root);
    }

    private void initializeLocationHandling(final Component newComponent) {
        final Consumer<Location> handleAddedLocation = (loc) -> {
            // Create a new presentation, and register it on the map
            final LocationPresentation newLocationPresentation = new LocationPresentation(loc, getComponent());
            locationPresentationMap.put(loc, newLocationPresentation);

            // Add it to the view
            modelContainer.getChildren().add(newLocationPresentation);

            // Bind the newly created location to the mouse
            if (loc.getX() == 0) {
                BindingHelper.bind(loc, getComponent().xProperty(), getComponent().yProperty());
            }
        };

        newComponent.getLocations().addListener(new ListChangeListener<Location>() {
            @Override
            public void onChanged(final Change<? extends Location> c) {
                if (c.next()) {
                    // Locations are added to the component
                    c.getAddedSubList().forEach(handleAddedLocation);

                    // Locations are removed from the component
                    c.getRemoved().forEach(location -> {
                        final LocationPresentation locationPresentation = locationPresentationMap.get(location);
                        modelContainer.getChildren().remove(locationPresentation);
                        locationPresentationMap.remove(location);
                    });
                }
            }
        });

        newComponent.getLocations().forEach(handleAddedLocation);
    }

    private void initializeEdgeHandling(final Component newComponent) {
        final Consumer<Edge> handleAddedEdge = edge -> {
            final EdgePresentation edgePresentation = new EdgePresentation(edge, newComponent);
            edgePresentationMap.put(edge, edgePresentation);
            modelContainer.getChildren().add(edgePresentation);

            final Consumer<Location> updateMouseTransparency = (newTargetLocation) -> {
                if (newTargetLocation == null) {
                    edgePresentation.setMouseTransparent(true);
                } else {
                    edgePresentation.setMouseTransparent(false);
                }
            };

            updateMouseTransparency.accept(edge.getTargetLocation());
            edge.targetLocationProperty().addListener((obs1, oldTargetLocation, newTargetLocation) -> {
                updateMouseTransparency.accept(newTargetLocation);
            });
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
                        modelContainer.getChildren().remove(edgePresentation);
                        edgePresentationMap.remove(edge);
                    });
                }
            }
        });

        newComponent.getEdges().forEach(handleAddedEdge);
    }

    private void initializeSubComponentHandling(final Component newComponent) {
        final Consumer<Component> handleAddedSubComponent = component -> {
            final SubComponentPresentation subComponentPresentation = new SubComponentPresentation(component);
            subComponentPresentationMap.put(component, subComponentPresentation);
            modelContainer.getChildren().add(subComponentPresentation);
        };

        // React on addition of edges to the component
        newComponent.getSubComponents().addListener(new ListChangeListener<Component>() {
            @Override
            public void onChanged(final Change<? extends Component> c) {
                if (c.next()) {
                    // SubComponents are added to the component
                    c.getAddedSubList().forEach(handleAddedSubComponent::accept);

                    // SubComponents are removed from the component
                    c.getRemoved().forEach(subComponent -> {
                        final SubComponentPresentation subComponentPresentation = subComponentPresentationMap.get(subComponent);
                        modelContainer.getChildren().remove(subComponentPresentation);
                    });
                }
            }
        });

        newComponent.getSubComponents().forEach(handleAddedSubComponent);
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

    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    @FXML
    public void toolbarPressed(final MouseEvent event) {
        event.consume();

        // Make the component selected when pressing the toolbar
        SelectHelper.select(this);

        previousX = root.getLayoutX();
        previousY = root.getLayoutY();
    }

    @FXML
    public void toolbarDragged() {
        // Calculate the potential new x alongside min and max values
        final double newX = CanvasPresentation.mouseTracker.getGridX();
        root.setLayoutX(newX);


        // Calculate the potential new y alongside min and max values
        final double newY = CanvasPresentation.mouseTracker.getGridY();
        root.setLayoutY(newY);

        // Tell the mouse release action that we can store an update
        wasDragged = true;

    }

    @FXML
    public void toolbarReleased() {
        if (wasDragged) {
            // Add to undo redo stack
            final double currentX = root.getLayoutX();
            final double currentY = root.getLayoutY();
            final double storePreviousX = previousX;
            final double storePreviousY = previousY;
            UndoRedoStack.push(
                    () -> {
                        root.setLayoutX(currentX);
                        root.setLayoutY(currentY);
                    },
                    () -> {
                        root.setLayoutX(storePreviousX);
                        root.setLayoutY(storePreviousY);
                    },
                    String.format("Moved nail from (%f,%f) to (%f,%f)", currentX, currentY, storePreviousX, storePreviousY),
                    "pin-drop"
            );

            // Reset the was dragged boolean
            wasDragged = false;

        }
    }

    @FXML
    public void leftAnchorPressed(final MouseEvent event) {
        System.out.println("JADA");
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

        final Consumer<Node> sugLocations = node -> {
            if (node instanceof LocationPresentation) {
                SelectHelper.addToSelection(((LocationPresentation) node).getController());
            }
        };

        modelContainer.getChildren().forEach(sugLocations);
        defaultLocationsContainer.getChildren().forEach(sugLocations);
    }

    @Override
    public void deselect() {
        ((SelectHelper.Selectable) root).deselect();
    }
}
