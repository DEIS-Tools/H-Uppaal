package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.presentations.FilePresentation;
import SW9.utility.UndoRedoStack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.jfoenix.controls.JFXRippler;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

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

    private void handleAddedComponent(final Component component) {
        final FilePresentation filePresentation = new FilePresentation(component);
        filesList.getChildren().add(filePresentation);
        componentPresentationMap.put(component, filePresentation);

        // Open the component if the presentation is pressed
        filePresentation.setOnMousePressed(event -> {
            event.consume();
            CanvasController.setActiveComponent(component);
        });

        component.nameProperty().addListener(obs -> sortPresentations());

        component.isMainProperty().addListener((obs, oldIsMain, newIsMain) -> {
            // Clear out the mainComponentContainer
            mainComponentContainer.getChildren().removeAll();

            // Remove the new active component from the list view
            final FilePresentation newActiveFilePresentation = componentPresentationMap.get(component);
            filesList.getChildren().remove(newActiveFilePresentation);

            // Add it to the mainComponentContainer
            mainComponentContainer.getChildren().add(newActiveFilePresentation);

            if (mainComponent != null) {
                // Re-add the old component to the list view
                final FilePresentation oldActiveFilePresentation = componentPresentationMap.get(mainComponent);
                filesList.getChildren().add(oldActiveFilePresentation);

                // The old main component can not be main component any longer
                mainComponent.setIsMain(false);
            }

            // Update the new main component
            mainComponent = component;

            // Sort the list
            sortPresentations();
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
