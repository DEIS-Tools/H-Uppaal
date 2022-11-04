package dk.cs.aau.huppaal;

import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Project;
import dk.cs.aau.huppaal.abstractions.Query;
import dk.cs.aau.huppaal.backend.UPPAALDriverManager;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.controllers.HUPPAALController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.presentations.BackgroundThreadPresentation;
import dk.cs.aau.huppaal.presentations.HUPPAALPresentation;
import dk.cs.aau.huppaal.presentations.UndoRedoHistoryPresentation;
import dk.cs.aau.huppaal.presentations.PresentationFxmlLoader;
import dk.cs.aau.huppaal.utility.keyboard.Keybind;
import dk.cs.aau.huppaal.utility.keyboard.KeyboardTracker;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.Preferences;

import static dk.cs.aau.huppaal.code_analysis.CodeAnalysis.addBackendError;

public class HUPPAAL extends Application {
    public static Preferences preferences;
    public static String serverDirectory;
    public static String debugDirectory;
    public static String temporaryProjectDirectory;
    public static boolean serializationDone = false;
    private static Project project;
    private static HUPPAALPresentation presentation;
    public static SimpleStringProperty projectDirectory = new SimpleStringProperty();
    private Stage debugStage;
    public Stage searchStage;
    public static Runnable toggleSearchModal;
    private HBox searchBox;

    {
        try {
            preferences = Preferences.userRoot().node("HUPPAAL");
            var dir = new File(System.getProperty("user.home") + File.separator + "H-UPPAAL");
            var rootDirectory = dir.getPath() + File.separator;
            if (!dir.exists() && !dir.mkdir())
                throw new IOException("Could not create project directory "+dir);
            projectDirectory.set(preferences.get("latestProject", rootDirectory + "projects" + File.separator + "project"));
            projectDirectory.addListener((observable, oldValue, newValue) -> preferences.put("latestProject", newValue));
            temporaryProjectDirectory = rootDirectory + "projects" + File.separator + "temp";
            serverDirectory = rootDirectory + "servers";
            debugDirectory = rootDirectory + "uppaal-debug";
            forceCreateFolder(projectDirectory.getValue());
            forceCreateFolder(serverDirectory);
            forceCreateFolder(debugDirectory);
        } catch (IOException e) {
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
        try {
            // Check that the folder exists, if it doesn't, create it.
            if(!java.nio.file.Files.exists(Path.of(projectDirectory.getValue() + File.separator)))
                java.nio.file.Files.createDirectory(Path.of(projectDirectory.getValue() + File.separator));

            // Clear the project folder .json files
            var paths = java.nio.file.Files.walk(Paths.get(projectDirectory.getValue() + File.separator))
                    .filter(p -> java.nio.file.Files.isRegularFile(p) && p.getFileName().toString().endsWith(".json"))
                    .toList();
            for (var p : paths)
                java.nio.file.Files.delete(p);

            // Save components
            var gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            for(var c : HUPPAAL.getProject().getComponents()) {
                var writer = new FileWriter(String.format(projectDirectory.getValue() + File.separator + "%s.json", c.getName()), StandardCharsets.UTF_8);
                gson.toJson(c.serialize(), writer);
                writer.close();
            }

            // Save queries
            var queries = new JsonArray();
            HUPPAAL.getProject().getQueries().forEach(query -> queries.add(query.serialize()));
            var writer = new FileWriter(projectDirectory.getValue() + File.separator + "Queries.json", StandardCharsets.UTF_8);
            gson.toJson(queries, writer);
            writer.close();

            showToast("Project saved!");
        } catch (Exception e) {
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
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Log.addError(t.getName(), e.getMessage());
            } catch(Throwable throwable) {
                throwable.printStackTrace();
            }
            e.printStackTrace();
        });
    }

    @Override
    public void start(final Stage stage) throws Exception {
        // Load or create new project
        project = new Project();

        // Set the title and icon for the application
        stage.setTitle("H-UPPAAL");
        stage.getIcons().add(new Image("uppaal.ico"));

        // Load the fonts required for the project
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        loadFonts();

        // Remove the classic decoration
        // kyrke - 2020-04-17: Disabled due to bug https://bugs.openjdk.java.net/browse/JDK-8154847
        //stage.initStyle(StageStyle.UNIFIED);

        // Make the view used for the application
        final HUPPAALPresentation huppaal = new HUPPAALPresentation();
        presentation = huppaal;

        // Make the scene that we will use, and set its size to 80% of the primary screen
        final Screen screen = Screen.getPrimary();
        final Scene scene = new Scene(huppaal, screen.getVisualBounds().getWidth() * 0.8, screen.getVisualBounds().getHeight() * 0.8);
        stage.setScene(scene);

        // Load all .css files used todo: these should be loaded in the view classes (?)
        scene.getStylesheets().add("main.css");
        scene.getStylesheets().add("colors.css");
        scene.getStylesheets().add("model_canvas.css");

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

                debugStage.getScene().getStylesheets().add("main.css");
                debugStage.getScene().getStylesheets().add("colors.css");

                debugStage.setWidth(screen.getVisualBounds().getWidth() * 0.2);
                debugStage.setHeight(screen.getVisualBounds().getWidth() * 0.3);

                debugStage.show();
                //stage.requestFocus();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }));

        toggleSearchModal = () -> {
            try {
                if(searchStage == null) {
                    searchStage = new Stage();
                    searchBox = new HBox();
                    searchStage.initStyle(StageStyle.UNDECORATED);
                    searchStage.setScene(new Scene(PresentationFxmlLoader.loadSetRootGetElement("ProjectSearchPresentation.fxml", searchBox),
                            screen.getBounds().getWidth() * 0.4,
                            screen.getBounds().getHeight() * 0.6));
                    searchStage.initModality(Modality.WINDOW_MODAL);
                    searchStage.initOwner(scene.getWindow());
                    searchStage.addEventHandler(KeyEvent.KEY_PRESSED, (t) -> {
                        if(t.getCode()==KeyCode.ESCAPE && searchStage.isShowing())
                            searchStage.close();
                    });
                }
                if(searchStage.isShowing())
                    searchStage.close();
                else {
                    searchStage.show();
                    searchBox.requestFocus();
                }
            } catch (Exception e) {
                e.printStackTrace();
                searchStage = null;
                Log.addError(e.getMessage());
            }
        };

        stage.setOnCloseRequest(event -> {
            UPPAALDriverManager.getInstance().stopEngines();

            Platform.exit();
            System.exit(0);
        });
    }

    public static void initializeProjectFolder() throws IOException {
        try {
            // Make sure that the project directory exists
            final File directory = new File(projectDirectory.get());
            FileUtils.forceMkdir(directory);

            CodeAnalysis.getErrors()
                    .addListener((ListChangeListener<CodeAnalysis.Message>) c ->
                            CodeAnalysis.getErrors().forEach(message ->
                                    System.out.println(message.getMessage())));

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
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uppaalDriverUpdated(){
        //The UPPAALDriver has been updated, notify the presentation
        presentation.uppaalDriverUpdated();
    }

    private static void deserializeProject(final File projectFolder) throws IOException {
        // If there are no files do not try to deserialize
        var projectFiles = projectFolder.listFiles((file, s) -> s.endsWith(".json"));
        if (projectFiles == null || projectFiles.length == 0) {
            var msg = "Failed to open project in folder "+projectFolder.getPath();
            showToast(msg);
            throw new IOException(msg);
        }

        // Create maps for deserialization
        var componentJsonMap = new HashMap<String, JsonObject>();
        var componentMaxDepthMap = new HashMap<JsonObject, Integer>();
        JsonObject mainJsonComponent = null;

        for (var file : projectFiles) {
            var fileContent = Files.asCharSource(file, Charset.defaultCharset()).read();
            var parsedContent = JsonParser.parseString(fileContent);

            // If the file represents the queries
            if (file.getName().equals("Queries.json")) {
                parsedContent.getAsJsonArray().forEach(jsonElement -> getProject().getQueries().add(new Query((JsonObject) jsonElement)));
                continue; // Do not parse Queries.json as a component
            }

            // Parse the file to a json object
            var jsonObject = parsedContent.getAsJsonObject();

            // Fetch the name of the component
            var componentName = jsonObject.get("name").getAsString();

            // Add the name and the json object to the map
            componentJsonMap.put(componentName, jsonObject);

            // Initialize the max depth map
            componentMaxDepthMap.put(jsonObject, 0);

            // Find the main name of the main component
            if (jsonObject.get("main").getAsBoolean())
                mainJsonComponent = jsonObject;
        }

        if (mainJsonComponent != null)
            updateDepthMap(mainJsonComponent, 0, componentJsonMap, componentMaxDepthMap);

        var list = new LinkedList<>(componentMaxDepthMap.entrySet());
        // Defined Custom Comparator here
        list.sort(Map.Entry.comparingByValue());

        var orderedJsonComponents = new ArrayList<JsonObject>();
        for (final Map.Entry<JsonObject, Integer> mapEntry : list)
            orderedJsonComponents.add(mapEntry.getKey());

        // Reverse the list such that the greatest depth is first in the list
        Collections.reverse(orderedJsonComponents);

        // Add the components to the list
        orderedJsonComponents.forEach(jsonObject -> {
            // It is important that the components are added the list prior to deserialization
            var newComponent = new Component();
            getProject().getComponents().add(newComponent);
            newComponent.deserialize(jsonObject);
        });
    }

    private static void updateDepthMap(final JsonObject jsonObject, final int depth, final Map<String, JsonObject> nameToJson, final Map<JsonObject, Integer> jsonToDpeth) {
        if (jsonToDpeth.get(jsonObject) < depth)
            jsonToDpeth.put(jsonObject, depth);

        var subComponentNames = new ArrayList<String>();
        jsonObject.get("sub_components").getAsJsonArray().forEach(jsonElement -> subComponentNames.add(jsonElement.getAsJsonObject().get("component").getAsString()));
        for (final String subComponentName : subComponentNames)
            updateDepthMap(nameToJson.get(subComponentName), depth + 1, nameToJson, jsonToDpeth);
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
