package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.presentations.DropDownMenu;
import SW9.presentations.FilePresentation;
import SW9.utility.UndoRedoStack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.beans.property.SimpleBooleanProperty;
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

    private void initializeColorSelector(final FilePresentation filePresentation) {
        final JFXRippler moreInformation = (JFXRippler) filePresentation.lookup("#moreInformation");
        final int listWidth = 230;
        final DropDownMenu moreInformationDropDown = new DropDownMenu(root, moreInformation, listWidth, true);

        moreInformationDropDown.addListElement("Configuration");

        /*
         * IS MAIN
         */
        moreInformationDropDown.addTogglableListElement("Main", filePresentation.getComponent().isMainProperty(), event -> {
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
        moreInformationDropDown.addTogglableListElement("Include in periodic check", includeInPeriodicCheck, event -> {
            final Component component = filePresentation.getComponent();
            final boolean didIncludeInPeriodicCheck = includeInPeriodicCheck.get();

            UndoRedoStack.push(() -> { // Perform
                includeInPeriodicCheck.set(!didIncludeInPeriodicCheck);
            }, () -> { // Undo
                includeInPeriodicCheck.set(didIncludeInPeriodicCheck);
            }, "Component " + component.getName() + " is included in periodic check: " + !didIncludeInPeriodicCheck, "search");
        });

        moreInformationDropDown.addSpacerElement();

        moreInformationDropDown.addListElement("Color");

        /*
         * COLOR SELECTOR
         */
        moreInformationDropDown.addColorPicker(filePresentation.getComponent());

        moreInformationDropDown.addSpacerElement();

        /*
         * THE DELETE BUTTON
         */
        moreInformationDropDown.addClickableListElement("Delete", event -> {
            final Component component = filePresentation.getComponent();

            UndoRedoStack.push(() -> { // Perform
                HUPPAAL.getProject().getComponents().remove(component);
            }, () -> { // Undo
                HUPPAAL.getProject().getComponents().add(component);
            }, "Deleted component " + component.getName(), "delete");

            moreInformationDropDown.close();
        });

        moreInformation.setOnMousePressed((e) -> {
            e.consume();
            moreInformationDropDown.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT, 10, 10);
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
