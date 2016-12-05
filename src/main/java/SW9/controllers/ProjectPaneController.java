package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.presentations.FilePresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static javafx.scene.paint.Color.TRANSPARENT;

public class ProjectPaneController implements Initializable {

    private final HashMap<Component, FilePresentation> componentPresentationMap = new HashMap<>();
    public StackPane root;
    public AnchorPane toolbar;
    public Label toolbarTitle;
    public ScrollPane scrollPane;
    public VBox filesList;
    public JFXRippler createComponent;
    public JFXRippler saveProject;
    public VBox mainComponentContainer;

    private Component mainComponent = null;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        HUPPAAL.getProject().getComponents().addListener(new ListChangeListener<Component>() {
            @Override
            public void onChanged(final Change<? extends Component> c) {
                while (c.next()) {
                    c.getAddedSubList().forEach(o -> handleAddedComponent(o));
                    c.getRemoved().forEach(o -> handleRemovedComponent(o));

                    // We should make a new component active
                    if (c.getRemoved().size() > 0) {
                        if (HUPPAAL.getProject().getComponents().size() > 0) {
                            // Find the first available component and show it instead of the removed one
                            final Component component = HUPPAAL.getProject().getComponents().get(0);
                            CanvasController.setActiveComponent(component);
                        } else {
                            // Show no components (since there are none in the project)
                            CanvasController.setActiveComponent(null);
                        }
                    }

                    // Sort the children alphabetically
                    sortPresentations();
                }
            }
        });

        HUPPAAL.getProject().getComponents().forEach(this::handleAddedComponent);
    }

    private void sortPresentations() {
        final ArrayList<Component> sortedComponentList = new ArrayList<>();
        componentPresentationMap.keySet().forEach(sortedComponentList::add);
        sortedComponentList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        sortedComponentList.forEach(component -> componentPresentationMap.get(component).toFront());
    }

    private void initializeColorSelector(final FilePresentation filePresentation) {

        final JFXPopup popup = new JFXPopup();

        final double listWidth = 230;
        final VBox list = new VBox();
        list.setOnMouseExited(event -> popup.close());

        // Adds an element into the list
        final Consumer<String> addListElement = (s) -> {
            final Label label = new Label(s);

            label.setStyle("-fx-padding: 8 16 8 16;");
            label.getStyleClass().add("body2");
            label.setMinWidth(listWidth);

            list.getChildren().add(label);
        };

        // Adds a clickable element into the list
        final BiConsumer<String, Consumer<MouseEvent>> addClickListElement = (s, mouseEventConsumer) -> {
            final Label label = new Label(s);

            label.setStyle("-fx-padding: 8 16 8 16;");
            label.getStyleClass().add("body2");
            label.setMinWidth(listWidth);

            final JFXRippler rippler = new JFXRippler(label);
            rippler.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I300));

            rippler.setOnMouseEntered(event -> {
                // Set the background to a light grey
                label.setBackground(new Background(new BackgroundFill(
                        Color.GREY.getColor(Color.Intensity.I200),
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )));
            });

            rippler.setOnMouseExited(event -> {
                // Set the background to be transparent
                label.setBackground(new Background(new BackgroundFill(
                        TRANSPARENT,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )));
            });

            // When the rippler is pressed, run the provided consumer.
            rippler.setOnMousePressed(event -> {
                // If we do not do this, the method below will be called twice
                if (!(event.getTarget() instanceof StackPane)) return;

                mouseEventConsumer.accept(event);
            });

            list.getChildren().add(rippler);
        };

        // Adds a toggleable element into the list
        final BiConsumer<Pair<String, ObservableBooleanValue>, Consumer<MouseEvent>> addToggleListElement = (pair, mouseEventConsumer) -> {
            final Label label = new Label(pair.getKey());
            label.getStyleClass().add("body2");

            final HBox container = new HBox();
            container.setStyle("-fx-padding: 8 16 8 16;");

            final FontIcon icon = new FontIcon();
            icon.setIconLiteral("gmi-done");
            icon.setFill(Color.GREY.getColor(Color.Intensity.I600));
            icon.setIconSize(20);
            icon.visibleProperty().bind(pair.getValue());

            final Region spacer = new Region();
            spacer.setMinWidth(8);

            container.getChildren().addAll(icon, spacer, label);

            final StackPane clickListenerFix = new StackPane(container);

            final JFXRippler rippler = new JFXRippler(clickListenerFix);
            rippler.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I300));

            rippler.setOnMouseEntered(event -> {
                // Set the background to a light grey
                container.setBackground(new Background(new BackgroundFill(
                        Color.GREY.getColor(Color.Intensity.I200),
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )));
            });

            rippler.setOnMouseExited(event -> {
                // Set the background to be transparent
                container.setBackground(new Background(new BackgroundFill(
                        TRANSPARENT,
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )));
            });

            // When the rippler is pressed, run the provided consumer.
            clickListenerFix.setOnMousePressed(event -> {
                System.out.println(event);
                mouseEventConsumer.accept(event);
                event.consume();
            });

            list.getChildren().add(rippler);
        };

        // Adds a spacer element
        final Runnable addSpacer = () -> {
            final Region space1 = new Region();
            space1.setMinHeight(8);
            list.getChildren().add(space1);

            final Line sep = new Line(0, 0, listWidth, 0);
            sep.setStroke(Color.GREY.getColor(Color.Intensity.I300));
            list.getChildren().add(sep);

            final Region space2 = new Region();
            space2.setMinHeight(8);
            list.getChildren().add(space2);
        };

        addListElement.accept("Configuration");

        /*
         * IS MAIN
         */
        addToggleListElement.accept(new Pair<>("Main", filePresentation.getComponent().isMainProperty()), event -> {
            final Component component = filePresentation.getComponent();
            final boolean wasMain = component.isIsMain();

            UndoRedoStack.push(() -> { // Perform
                component.setIsMain(!wasMain);
            }, () -> { // Undo
                component.setIsMain(wasMain);
            }, "Component " + component.getName() + " isMain: " + !wasMain, "star");
        });

        /*
         * INCLUDE IN PERIODIC CHECK
         */
        final SimpleBooleanProperty includeInPeriodicCheck = new SimpleBooleanProperty(true); // todo: This should be placed in the model
        addToggleListElement.accept(new Pair<>("Include in periodic check", includeInPeriodicCheck), event -> {
            final Component component = filePresentation.getComponent();
            final boolean didIncludeInPeriodicCheck = includeInPeriodicCheck.get();

            UndoRedoStack.push(() -> { // Perform
                includeInPeriodicCheck.set(!didIncludeInPeriodicCheck);
            }, () -> { // Undo
                includeInPeriodicCheck.set(didIncludeInPeriodicCheck);
            }, "Component " + component.getName() + " is included in periodic check: " + !didIncludeInPeriodicCheck, "search");
        });

        addSpacer.run();

        /*
         * THE DELETE BUTTON
         */
        addClickListElement.accept("Delete", event -> {
            final Component component = filePresentation.getComponent();

            UndoRedoStack.push(() -> { // Perform
                HUPPAAL.getProject().getComponents().remove(component);
            }, () -> { // Undo
                HUPPAAL.getProject().getComponents().add(component);
            }, "Deleted component " + component.getName(), "delete");

            popup.close();
        });

        list.setStyle("-fx-background-color: white; -fx-padding: 8 0 8 0;");
        list.setMinWidth(listWidth);
        list.setMaxWidth(listWidth);
        final JFXRippler moreInformation = (JFXRippler) filePresentation.lookup("#moreInformation");

        popup.setContent(list);
        popup.setPopupContainer(root);
        popup.setSource(moreInformation);

        moreInformation.setOnMousePressed((e) -> {
            e.consume();
            popup.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 10, 10);
        });
    }

    private void handleAddedComponent(final Component component) {
        final FilePresentation filePresentation = new FilePresentation(component);
        initializeColorSelector(filePresentation);
        filesList.getChildren().add(filePresentation);
        componentPresentationMap.put(component, filePresentation);

        // Open the component if the presentation is pressed
        filePresentation.setOnMousePressed(event -> {
            event.consume();
            CanvasController.setActiveComponent(component);
        });

        component.nameProperty().addListener(obs -> sortPresentations());

        component.isMainProperty().addListener((obs, oldIsMain, newIsMain) -> {
            if (component.equals(mainComponent)) return;

            if (mainComponent != null && newIsMain) {
                mainComponent.setIsMain(false);
            }

            mainComponent = component;
        });
    }

    private void handleRemovedComponent(final Component component) {
        filesList.getChildren().remove(componentPresentationMap.get(component));
        componentPresentationMap.remove(component);
    }

    @FXML
    private void saveProjectClicked() {
        // Clear the project folder
        try {
            FileUtils.cleanDirectory(new File("project"));
        } catch (final IOException e) {
            e.printStackTrace();
        }

        HUPPAAL.getProject().getComponents().forEach(component -> {
            try {
                final Writer writer = new FileWriter(String.format("project/%s.json", component.getName()));
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();

                gson.toJson(component.serialize(), writer);

                writer.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });

        final JsonArray queries = new JsonArray();
        HUPPAAL.getProject().getQueries().forEach(query -> {
            queries.add(query.serialize());
        });

        final Writer writer;
        try {
            writer = new FileWriter("project/Queries.json");
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(queries, writer);
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void createComponentClicked() {
        final Component newComponent = new Component();

        UndoRedoStack.push(() -> { // Perform
            HUPPAAL.getProject().getComponents().add(newComponent);
        }, () -> { // Undo
            HUPPAAL.getProject().getComponents().remove(newComponent);
        }, "Created new component: " + newComponent.getName(), "add-circle");

        CanvasController.setActiveComponent(newComponent);
    }

}
