package SW9;

import SW9.abstractions.Component;
import SW9.abstractions.Project;
import SW9.abstractions.Query;
import SW9.presentations.HUPPAALPresentation;
import SW9.presentations.UndoRedoHistoryPresentation;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Scanner;

public class HUPPAAL extends Application {

    private static Project project;
    private Stage debugStage;

    public static void main(final String[] args) {
        launch(HUPPAAL.class, args);
    }

    public static Project getProject() {
        return project;
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

        // Make sure that the project directory exists
        final File projectFolder = new File("project");
        projectFolder.mkdir();

        // Load the project from disk
        for (final File file : projectFolder.listFiles()) {
            if (!file.getName().equals("Queries.json")) {
                final Component componentFromFile = new Component(file.getName().replace(".json", ""));
                getProject().getComponents().add(componentFromFile);
            }
        }

        // Load the project from disk
        for (final File file : projectFolder.listFiles()) {
            final String fileContent = Files.toString(file, Charset.defaultCharset());

            final JsonParser parser = new JsonParser();
            final JsonElement element = parser.parse(fileContent);

            if (file.getName().equals("Queries.json")) {
                element.getAsJsonArray().forEach(jsonElement -> {
                    final Query newQuery = new Query((JsonObject) jsonElement);
                    getProject().getQueries().add(newQuery);
                });
            } else {
                final Component componentFromFile = HUPPAAL.getProject().getComponents().filtered(component -> component.getName().equals(file.getName().replace(".json", ""))).get(0);
                componentFromFile.deserialize(element.getAsJsonObject());
            }
        }

        // We're now ready! Let the curtains fall!
        stage.show();

        // Register a key-bind for showing debug-information
        KeyboardTracker.registerKeybind("DEBUG", new Keybind(new KeyCodeCombination(KeyCode.D), () -> {
            // Toggle the debug mode for the debug class (will update misc. debug variables which presentations bind to)
            Debug.debugModeEnabled.set(!Debug.debugModeEnabled.get());

            if (debugStage != null) {
                debugStage.close();
                debugStage = null;
                return;
            }

            try {
                final UndoRedoHistoryPresentation undoRedoHistoryPresentation = new UndoRedoHistoryPresentation();
                debugStage = new Stage();
                debugStage.setScene(new Scene(undoRedoHistoryPresentation));

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
