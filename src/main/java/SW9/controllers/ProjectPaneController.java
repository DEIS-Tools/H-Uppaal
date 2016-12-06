package SW9.controllers;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.presentations.DropDownMenu;
import SW9.presentations.FilePresentation;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.colors.EnabledColor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
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

import static SW9.utility.colors.EnabledColor.enabledColors;

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
        final DropDownMenu moreInformationDropDown = new DropDownMenu(root, moreInformation, listWidth);

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
        final FlowPane flowPane = new FlowPane();
        flowPane.setStyle("-fx-padding: 0 8 0 8");

        for (final EnabledColor color : enabledColors) {
            final Circle circle = new Circle(16, color.color.getColor(color.intensity));
            circle.setStroke(color.color.getColor(color.intensity.next(2)));
            circle.setStrokeWidth(1);

            final FontIcon icon = new FontIcon();
            icon.setIconLiteral("gmi-done");
            icon.setFill(color.color.getTextColor(color.intensity));
            icon.setIconSize(20);
            icon.visibleProperty().bind(new When(filePresentation.getComponent().colorProperty().isEqualTo(color.color)).then(true).otherwise(false));

            final StackPane child = new StackPane(circle, icon);
            child.setMinSize(40, 40);
            child.setMaxSize(40, 40);

            child.setOnMouseEntered(event -> {
                final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), circle);
                scaleTransition.setFromX(circle.getScaleX());
                scaleTransition.setFromY(circle.getScaleY());
                scaleTransition.setToX(1.1);
                scaleTransition.setToY(1.1);
                scaleTransition.play();
            });

            child.setOnMouseExited(event -> {
                final ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), circle);
                scaleTransition.setFromX(circle.getScaleX());
                scaleTransition.setFromY(circle.getScaleY());
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });

            child.setOnMouseClicked(event -> {
                event.consume();

                final Component component = filePresentation.getComponent();

                // Only color the component if the user chooses a new color
                if (component.getColor().equals(color.color)) return;

                final Color previousColor = component.getColor();
                final Color.Intensity previousColorIntensity = component.getColorIntensity();

                UndoRedoStack.push(() -> { // Perform
                    component.setColorIntensity(color.intensity);
                    component.setColor(color.color);
                }, () -> { // Undo
                    component.setColorIntensity(previousColorIntensity);
                    component.setColor(previousColor);
                }, String.format("Changed the color of component %s to %s", component.getName(), color.color.name()), "color-lens");

            });

            flowPane.getChildren().add(child);
        }

        moreInformationDropDown.addCustomChild(flowPane);

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
