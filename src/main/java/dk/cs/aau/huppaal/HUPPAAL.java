package dk.cs.aau.huppaal;

import com.sun.javafx.application.LauncherImpl;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Project;
import dk.cs.aau.huppaal.abstractions.Query;
import dk.cs.aau.huppaal.backend.UPPAALDriverManager;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;
import dk.cs.aau.huppaal.controllers.CanvasController;
import dk.cs.aau.huppaal.controllers.HUPPAALController;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.presentations.HUPPAALPresentation;
import dk.cs.aau.huppaal.presentations.PresentationFxmlLoader;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
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

public class HUPPAAL extends Application {

    public static Preferences preferences;
    public static String serverDirectory;
    public static String debugDirectory;
    public static String temporaryProjectDirectory;
    public static boolean serializationDone = false;
    private static Project project;
    private static HUPPAALPresentation presentation;
    public static SimpleStringProperty projectDirectory = new SimpleStringProperty();
    public static Stage debugStage;
    public Stage searchStage;
    public static Runnable toggleSearchModal;
    private HBox searchBox;
    private Scene scene;

    private static void initializeFileSystem() {
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
        } catch (Exception e) {
            System.out.println("Unable to initialize project files: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void main(final String[] args) {
        initializeFileSystem();
        LauncherImpl.launchApplication(HUPPAAL.class, HUPPAALPreloader.class, args);
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

    private static void forceCreateFolder(final String directoryPath) throws IOException {
        FileUtils.forceMkdir(new File(directoryPath));
    }

    @Override
    public void init() throws Exception {
        notifyPreloader(new HUPPAALPreloader.Notification(HUPPAALPreloader.LoadStage.LOADING_PROJECT));
        initDefaultExceptionHandler();
        project = new Project();
        loadPresentations();
        initializeProjectFolder();
        notifyPreloader(new HUPPAALPreloader.Notification(HUPPAALPreloader.LoadStage.INITALIZE_JFX));
        initializeProjectSearchModal();
        initScene();
        initFonts();
        initReachabilityService();
        notifyPreloader(new HUPPAALPreloader.Notification(HUPPAALPreloader.LoadStage.AFTER_INIT));
    }

    private void initFonts() {
        // Load the fonts required for the project
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        loadFonts();
    }

    private void initReachabilityService() {
        HUPPAALController.reachabilityServiceEnabled = true;
    }

    private void initDefaultExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Log.addError(t.getName(), e.getMessage());
            } catch(Throwable throwable) {
                throwable.printStackTrace();
            }
            e.printStackTrace();
        });
    }

    private void loadPresentations() {
        presentation = new HUPPAALPresentation();
    }

    private void initScene() {
        // Make the scene that we will use, and set its size to 80% of the primary screen
        var screenBounds = Screen.getPrimary().getVisualBounds();
        scene = new Scene(presentation, screenBounds.getWidth() * 0.8, screenBounds.getHeight() * 0.8);

        // Load all .css files used
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
    }

    private void initializeProjectSearchModal() {
        toggleSearchModal = () -> {
            try {
                if(searchStage == null) {
                    var screenBounds = Screen.getPrimary().getBounds();

                    searchStage = new Stage();
                    searchBox = new HBox();
                    searchStage.initStyle(StageStyle.UNDECORATED);
                    searchStage.setScene(new Scene(PresentationFxmlLoader.loadSetRootGetElement("ProjectSearchPresentation.fxml", searchBox),
                            screenBounds.getWidth() * 0.4,
                            screenBounds.getHeight() * 0.6));
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

            serializationDone = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(final Stage stage) throws Exception {
        notifyPreloader(new HUPPAALPreloader.Notification(HUPPAALPreloader.LoadStage.START_JFX));
        initializeStage(stage);
        stage.show();
        notifyPreloader(new HUPPAALPreloader.Notification(HUPPAALPreloader.LoadStage.AFTER_SHOW));
        setMainAsActiveComponent();
        notifyPreloader(new HUPPAALPreloader.Notification(HUPPAALPreloader.LoadStage.FINISHED));
    }

    private void initializeStage(Stage stage) {
        // kyrke - 2020-04-17: Disabled due to bug https://bugs.openjdk.java.net/browse/JDK-8154847
        //stage.initStyle(StageStyle.UNIFIED);
        stage.setTitle("H-UPPAAL");
        stage.getIcons().add(new Image("uppaal.ico"));
        stage.getIcons().addAll(
                new Image(getClass().getResource("ic_launcher/mipmap-hdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-mdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-xhdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-xxhdpi/ic_launcher.png").toExternalForm()),
                new Image(getClass().getResource("ic_launcher/mipmap-xxxhdpi/ic_launcher.png").toExternalForm())
        );
        stage.setOnCloseRequest(event -> {
            UPPAALDriverManager.getInstance().stopEngines();
            Platform.exit();
            System.exit(0);
        });
        stage.setScene(scene);
    }

    public static void setMainAsActiveComponent() {
        for (var component : HUPPAAL.getProject().getComponents())
            if (component.isIsMain())
                CanvasController.setActiveComponent(component);
    }

    public static void uppaalDriverUpdated(){
        //The UPPAALDriver has been updated, notify the presentation
        presentation.uppaalDriverUpdated();
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
            if (!file.getName().endsWith(".json"))
                continue;

            final String fileContent = Files.asCharSource(file, Charset.defaultCharset()).read();

            final var parsedContent = JsonParser.parseString(fileContent);

            // If the file represents the queries
            if (file.getName().equals("Queries.json")) {
                parsedContent.getAsJsonArray().forEach(jsonElement -> {
                    final Query newQuery = new Query((JsonObject) jsonElement);
                    getProject().getQueries().add(newQuery);
                });
                // Do not parse Queries.json as a component
                continue;
            }

            // Parse the file to an json object
            final JsonObject jsonObject = parsedContent.getAsJsonObject();

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
        list.sort(Map.Entry.comparingByValue());

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

        jsonObject.get("sub_components").getAsJsonArray().forEach(jsonElement -> subComponentNames.add(jsonElement.getAsJsonObject().get("component").getAsString()));

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
