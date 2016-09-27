package SW9;

import SW9.model_canvas.ModelCanvas;
import SW9.utility.DragHelper;
import SW9.utility.ResizeHelper;
import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

public class Main extends Application {

    private Parent root;
    private double xOffset;
    private double yOffset;
    private double previousX, previousY, previousWidth, previousHeight;
    public static BooleanProperty isMaximized = new SimpleBooleanProperty(false);

    private final static DoubleProperty border = new SimpleDoubleProperty(3d);
    public static MouseTracker mouseTracker;

    public static void main(String[] args) {
        launch(Main.class, args);
    }

    public void start(final Stage stage) throws Exception {
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        loadFonts();

        // Remove the classic decoration
        stage.initStyle(StageStyle.UNDECORATED);

        root = FXMLLoader.load(getClass().getResource("main.fxml"));

        stage.setTitle("Kick-ass Modelchecker");

        final Scene scene = new Scene(root, 500, 500);
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);
        scene.getStylesheets().add("SW9/main.css");
        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas.css");
        stage.setScene(scene);

        final ModelCanvas modelCanvas = (ModelCanvas) scene.lookup("#model-canvas");
        mouseTracker = new MouseTracker(modelCanvas);
        DragHelper.makeDraggable(modelCanvas, mouseEvent -> mouseEvent.getButton().equals(MouseButton.SECONDARY));

        initializeStatusBar(stage);

        // Allows us to resize the window
        stage.resizableProperty().setValue(true);
        ResizeHelper.initialize(stage, border);

        stage.show();
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
    }

    private void initializeStatusBar(final Stage stage) {
        final Scene scene = stage.getScene();

        // Find the status bar and make it draggable
        final BorderPane statusBar = (BorderPane) scene.lookup("#status-bar");
        statusBar.setOnMouseEntered(event -> scene.setCursor(Cursor.CLOSED_HAND)); // Update the cursor to look draggable
        statusBar.setOnMouseExited(event -> scene.setCursor(Cursor.DEFAULT)); // Update the cursor to look normal
        statusBar.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        statusBar.setOnMouseDragged(event -> {
            // Undo maximized if pulled while maximized
            if (isMaximized.get()) {
                xOffset = -1 * previousWidth * (event.getX() / (stage.getWidth() * 10 / 8));
                yOffset = -1 * event.getY();

                stage.setWidth(previousWidth);
                stage.setHeight(previousHeight);

                isMaximized.set(false);
            }
            // Move the stage
            else {
                stage.setX(event.getScreenX() + xOffset);
                stage.setY(event.getScreenY() + yOffset);
            }
        });

        // Align the status bar to the top of the window
        final StackPane stackpane = (StackPane) scene.lookup("#stackpane");
        stackpane.setAlignment(Pos.TOP_LEFT);

        final Label titleLabel = (Label) scene.lookup("#status-bar-title");
        titleLabel.textProperty().bind(stage.titleProperty());

        final HBox rightStatusBar = (HBox) scene.lookup("#status-bar-right");

        // Add the minimize window button to the status bar
        final IconNode minimizeIcon = new IconNode(GoogleMaterialDesignIcons.REMOVE);
        minimizeIcon.setFill(Color.WHITE);
        final JFXButton minimizeBtn = new JFXButton("", minimizeIcon);
        minimizeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        minimizeBtn.setRipplerFill(Color.WHITE);
        minimizeBtn.setOnMouseClicked(event -> stage.setIconified(true));
        rightStatusBar.getChildren().add(minimizeBtn);

        // Add the resize window button to the status bar
        final IconNode resizeIcon = new IconNode();
        resizeIcon.setFill(Color.WHITE);
        resizeIcon.iconCodeProperty().bind(Bindings.when(isMaximized)
                .then(GoogleMaterialDesignIcons.FULLSCREEN_EXIT)
                .otherwise(GoogleMaterialDesignIcons.FULLSCREEN));

        final JFXButton resizeBtn = new JFXButton("", resizeIcon);
        resizeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        resizeBtn.setRipplerFill(Color.WHITE);
        resizeBtn.setOnMouseClicked(event -> {
            if (isMaximized.get()) {

                // Undo maximized again
                stage.setX(previousX);
                stage.setY(previousY);
                stage.setWidth(previousWidth);
                stage.setHeight(previousHeight);
                isMaximized.set(false);
            } else {
                previousX = stage.getX();
                previousY = stage.getY();
                previousWidth = stage.getWidth();
                previousHeight = stage.getHeight();
                stage.setX(0d);
                stage.setY(0d);

                // Maximize the window
                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getVisualBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
                isMaximized.set(true);
            }
        });

        rightStatusBar.getChildren().add(resizeBtn);

        // Add the close button to the status bar
        final IconNode closeIcon = new IconNode(GoogleMaterialDesignIcons.CLOSE);
        closeIcon.setFill(Color.WHITE);
        final JFXButton closeBtn = new JFXButton("", closeIcon);
        closeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        closeBtn.setRipplerFill(Color.WHITE);
        closeBtn.setOnMouseClicked(event -> System.exit(0));
        rightStatusBar.getChildren().add(closeBtn);
    }

}


