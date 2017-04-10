package SW9;

import SW9.abstractions.Component;
import SW9.abstractions.Project;
import SW9.abstractions.Query;
import SW9.backend.UPPAALDriver;
import SW9.code_analysis.CodeAnalysis;
import SW9.controllers.CanvasController;
import SW9.controllers.HUPPAALController;
import SW9.presentations.BackgroundThreadPresentation;
import SW9.presentations.HUPPAALPresentation;
import SW9.presentations.UndoRedoHistoryPresentation;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.google.common.io.Files;
import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.util.*;

public class HUPPAAL extends Application {

    public static String serverDirectory;
    public static String debugDirectory;
    public static boolean serializationDone = false;
    private static Project project;
    private static HUPPAALPresentation presentation;
    public static SimpleStringProperty projectDirectory = new SimpleStringProperty();
    private Stage debugStage;

    {
        try {
            final CodeSource codeSource = HUPPAAL.class.getProtectionDomain().getCodeSource();
            final File jarFile = new File(codeSource.getLocation().toURI().getPath());
            final String rootDirectory = jarFile.getParentFile().getPath() + File.separator;
            projectDirectory.set(rootDirectory + "projects" + File.separator + "project");
            serverDirectory = rootDirectory + "servers";
            debugDirectory = rootDirectory + "uppaal-debug";
            forceCreateFolder(projectDirectory.getValue());
            forceCreateFolder(serverDirectory);
            forceCreateFolder(debugDirectory);
        } catch (final URISyntaxException e) {
            System.out.println("Could not create project directory!");
            System.exit(1);
        } catch (final IOException e) {
            System.out.println("Could not create project directory!");
            System.exit(2);
        }
    }

    public static void main(final String[] args) {
        launch(HUPPAAL.class, args);
    }

    public static Project getProject() {
        return project;
    }

    public static void save() {
        // Clear the project folder
        try {
            final File directory = new File(projectDirectory.getValue());

            FileUtils.forceMkdir(directory);
            FileUtils.cleanDirectory(directory);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        HUPPAAL.getProject().getComponents().forEach(component -> {
            try {
                final Writer writer = new FileWriter(String.format(projectDirectory.getValue() + File.separator + "%s.json", component.getName()));
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();

                gson.toJson(component.serialize(), writer);

                writer.close();
            } catch (final IOException e) {
                showToast("Could not save project: " + e.getMessage());
                e.printStackTrace();
            }
        });

        final JsonArray queries = new JsonArray();
        HUPPAAL.getProject().getQueries().forEach(query -> {
            queries.add(query.serialize());
        });

        final Writer writer;
        try {
            writer = new FileWriter(projectDirectory.getValue() + File.separator + "Queries.json");
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(queries, writer);
            writer.close();

            showToast("Project saved!");
        } catch (final IOException e) {
            showToast("Could not save project: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showToast(final String message) {
        presentation.showSnackbarMessage(message);
    }

    public static void showHelp() {
        presentation.showHelp();
    }

    public static BooleanProperty toggleFilePane() {
        return presentation.toggleFilePane();
    }

    public static BooleanProperty toggleQueryPane() {
        return presentation.toggleQueryPane();
    }

    private void forceCreateFolder(final String directoryPath) throws IOException {
        final File directory = new File(directoryPath);
        FileUtils.forceMkdir(directory);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        // Load or create new project
        project = new Project();

        // Set the title and icon for the application
        stage.setTitle("H-UPPAAL");
        stage.getIcons().add(new Image("SW9/uppaal.ico"));

        // Load the fonts required for the project
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        loadFonts();

        // Remove the classic decoration
        stage.initStyle(StageStyle.UNIFIED);

        // Make the view used for the application
        final HUPPAALPresentation huppaal = new HUPPAALPresentation();
        presentation = huppaal;

        // Make the scene that we will use, and set its size to 80% of the primary screen
        final Screen screen = Screen.getPrimary();
        final Scene scene = new Scene(huppaal, screen.getVisualBounds().getWidth() * 0.8, screen.getVisualBounds().getHeight() * 0.8);
        stage.setScene(scene);

        // Load all .css files used todo: these should be loaded in the view classes (?)
        scene.getStylesheets().add("SW9/main.css");
        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas.css");

        // Handle a mouse click as a deselection of all elements
        scene.setOnMousePressed(event -> {
            if (scene.getFocusOwner() == null || scene.getFocusOwner().getParent() == null) return;
            scene.getFocusOwner().getParent().requestFocus();
        });

        // Let our keyboard tracker handle all key presses
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);

        // Set the icon for the application
        stage.getIcons().addAll(
                new Image(getClass().getResource("ic_launcher/mipmap-hdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-mdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-xhdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-xxhdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-xxxhdpi/ic_launcher.png").toExternalForm())
        );

        initializeProjectFolder();

        // We're now ready! Let the curtains fall!
        stage.show();

        HUPPAALController.reachabilityServiceEnabled = true;

        // Register a key-bind for showing debug-information
        KeyboardTracker.registerKeybind("DEBUG", new Keybind(new KeyCodeCombination(KeyCode.F12), () -> {
            // Toggle the debug mode for the debug class (will update misc. debug variables which presentations bind to)
            Debug.debugModeEnabled.set(!Debug.debugModeEnabled.get());

            if (debugStage != null) {
                debugStage.close();
                debugStage = null;
                return;
            }

            try {
                final UndoRedoHistoryPresentation undoRedoHistoryPresentation = new UndoRedoHistoryPresentation();
                undoRedoHistoryPresentation.setMinWidth(100);

                final BackgroundThreadPresentation backgroundThreadPresentation = new BackgroundThreadPresentation();
                backgroundThreadPresentation.setMinWidth(100);

                final HBox root = new HBox(undoRedoHistoryPresentation, backgroundThreadPresentation);
                root.setStyle("-fx-background-color: brown;");
                HBox.setHgrow(undoRedoHistoryPresentation, Priority.ALWAYS);
                HBox.setHgrow(backgroundThreadPresentation, Priority.ALWAYS);


                debugStage = new Stage();
                debugStage.setScene(new Scene(root));

                debugStage.getScene().getStylesheets().add("SW9/main.css");
                debugStage.getScene().getStylesheets().add("SW9/colors.css");

                debugStage.setWidth(screen.getVisualBounds().getWidth() * 0.2);
                debugStage.setHeight(screen.getVisualBounds().getWidth() * 0.3);

                debugStage.show();
                //stage.requestFocus();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }));

        stage.setOnCloseRequest(event -> {
            UPPAALDriver.stopEngines();

            Platform.exit();
            System.exit(0);
        });
    }

    public static void initializeProjectFolder() throws IOException {
        // Make sure that the project directory exists
        final File directory = new File(projectDirectory.get());
        FileUtils.forceMkdir(directory);

        CodeAnalysis.getErrors().addListener(new ListChangeListener<CodeAnalysis.Message>() {
            @Override
            public void onChanged(Change<? extends CodeAnalysis.Message> c) {
                CodeAnalysis.getErrors().forEach(message -> {
                    System.out.println(message.getMessage());
                });
            }
        });

        CodeAnalysis.getBackendErrors().removeIf(message -> true);
        CodeAnalysis.getErrors().removeIf(message -> true);
        CodeAnalysis.getWarnings().removeIf(message -> true);
        CodeAnalysis.disable();
        HUPPAAL.getProject().getQueries().removeIf(query -> true);
        HUPPAAL.getProject().getComponents().removeIf(component -> true);
        HUPPAAL.getProject().setMainComponent(null);

        // Deserialize the project
        deserializeProject(directory);
        CodeAnalysis.enable();

        // Generate all component presentations by making them the active component in the view one by one
        Component initialShownComponent = null;
        for (final Component component : HUPPAAL.getProject().getComponents()) {
            // The first component should be shown if there is no main
            if (initialShownComponent == null) {
                initialShownComponent = component;
            }

            // If the component is the main show that one
            if (component.isIsMain()) {
                initialShownComponent = component;
            }

            CanvasController.setActiveComponent(component);
        }

        // If we found a component (preferably main) set that as active
        if (initialShownComponent != null) {
            CanvasController.setActiveComponent(initialShownComponent);
        }

        serializationDone = true;
    }

    private static void deserializeProject(final File projectFolder) throws IOException {

        // If there are no files do not try to deserialize
        final File[] projectFiles = projectFolder.listFiles();
        if (projectFiles == null || projectFiles.length == 0) return;

        // Create maps for deserialization
        final Map<String, JsonObject> componentJsonMap = new HashMap<>();
        final Map<JsonObject, Integer> componentMaxDepthMap = new HashMap<>();
        JsonObject mainJsonComponent = null;

        for (final File file : projectFiles) {

            final String fileContent = Files.toString(file, Charset.defaultCharset());

            // If the file represents the queries
            if (file.getName().equals("Queries.json")) {
                new JsonParser().parse(fileContent).getAsJsonArray().forEach(jsonElement -> {
                    final Query newQuery = new Query((JsonObject) jsonElement);
                    getProject().getQueries().add(newQuery);
                });
                // Do not parse Queries.json as a component
                continue;
            }

            // Parse the file to an json object
            final JsonObject jsonObject = new JsonParser().parse(fileContent).getAsJsonObject();

            // Fetch the name of the component
            final String componentName = jsonObject.get("name").getAsString();

            // Add the name and the json object to the map
            componentJsonMap.put(componentName, jsonObject);

            // Initialize the max depth map
            componentMaxDepthMap.put(jsonObject, 0);

            // Find the main name of the main component
            if (jsonObject.get("main").getAsBoolean()) {
                mainJsonComponent = jsonObject;
            }

        }

        if (mainJsonComponent != null) {
            updateDepthMap(mainJsonComponent, 0, componentJsonMap, componentMaxDepthMap);
        }

        final List<Map.Entry<JsonObject, Integer>> list = new LinkedList<>(componentMaxDepthMap.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        final List<JsonObject> orderedJsonComponents = new ArrayList<>();


        for (final Map.Entry<JsonObject, Integer> mapEntry : list) {
            orderedJsonComponents.add(mapEntry.getKey());
        }

        // Reverse the list such that the greatest depth is first in the list
        Collections.reverse(orderedJsonComponents);

        // Add the components to the list
        orderedJsonComponents.forEach(jsonObject -> {

            // It is important that the components are added the list prior to deserialiation
            final Component newComponent = new Component();
            getProject().getComponents().add(newComponent);
            newComponent.deserialize(jsonObject);
        });
    }

    private static void updateDepthMap(final JsonObject jsonObject, final int depth, final Map<String, JsonObject> nameToJson, final Map<JsonObject, Integer> jsonToDpeth) {
        if (jsonToDpeth.get(jsonObject) < depth) {
            jsonToDpeth.put(jsonObject, depth);
        }

        final List<String> subComponentNames = new ArrayList<>();

        jsonObject.get("sub_components").getAsJsonArray().forEach(jsonElement -> {
            subComponentNames.add(jsonElement.getAsJsonObject().get("component").getAsString());
        });

        for (final String subComponentName : subComponentNames) {
            updateDepthMap(nameToJson.get(subComponentName), depth + 1, nameToJson, jsonToDpeth);
        }
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-Black.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-BlackItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-BoldItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/RobotoCondensed-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/RobotoCondensed-BoldItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/RobotoCondensed-Italic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/RobotoCondensed-Light.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/RobotoCondensed-LightItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/RobotoCondensed-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-Italic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-Light.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-LightItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-Medium.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-MediumItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-Thin.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto/Roboto-ThinItalic.ttf"), 14);

        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-Bold.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-BoldItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-Italic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-Light.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-LightItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-Medium.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-MediumItalic.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-Thin.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/roboto_mono/RobotoMono-ThinItalic.ttf"), 14);

    }
}
