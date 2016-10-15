package SW9;

import SW9.issues.Warning;
import SW9.model_canvas.ModelCanvas;
import SW9.model_canvas.ModelContainer;
import SW9.ui_elements.QueryPane;
import SW9.utility.colors.Color;
import SW9.utility.helpers.ResizeHelper;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.keyboard.KeyboardTracker;
import com.jfoenix.controls.JFXButton;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

public class Main extends Application {

    private static Parent root;
    private double xOffset;
    private double yOffset;
    private double previousX, previousY, previousWidth, previousHeight;
    public static final BooleanProperty isMaximized = new SimpleBooleanProperty(false);

    private final static DoubleProperty border = new SimpleDoubleProperty(3d);

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

        // Find the primary screen (will be used to set initial width and height of the program)
        final Screen screen = Screen.getPrimary();

        final Scene scene = new Scene(root, screen.getVisualBounds().getWidth() * 0.8, screen.getVisualBounds().getHeight() * 0.8);
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);
        scene.getStylesheets().add("SW9/main.css");
        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas.css");
        stage.setScene(scene);

        // Clear any selected elements on any mouse event
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> SelectHelper.clearSelectedElements());

        // Allows us to resize the window
        stage.resizableProperty().setValue(true);
        ResizeHelper.initialize(stage, border);

        stage.show();

        initializeStatusBar(stage);

        initializeBottomBar(stage);
    }

    public static ModelCanvas getModelCanvas() {
        return (ModelCanvas) root.lookup("#model-canvas");
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

    private void initializeBottomBar(final Stage stage) {
        final Scene scene = stage.getScene();

        // Find the bottom bar
        final BorderPane bottomBar = (BorderPane) scene.lookup("#bottom-bar");

        final Color bottomBarColor = Color.GREY_BLUE;
        final Color.Intensity bottomBarColorIntensity = Color.Intensity.I200;

        // Align the bottom bar to the bottom
        StackPane.setAlignment(bottomBar, Pos.BOTTOM_LEFT);

        // Set the background of the bottom bar
        bottomBar.backgroundProperty().set(new Background(new BackgroundFill(
                bottomBarColor.getColor(bottomBarColorIntensity),
                CornerRadii.EMPTY,
                Insets.EMPTY))
        );

        // Find the first model container
        Node container = scene.lookup("#root");
        container = container.lookup("#main-content");
        ((ModelCanvas) container.lookup("#model-canvas")).getChildren().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(final Change<? extends Node> change) {
                if (change.next()) {
                    change.getAddedSubList().forEach(o -> {
                        if (o instanceof ModelContainer) {
                            // A new model container was added!
                            final ModelContainer modelContainer = (ModelContainer) o;

                            // Label for the warning
                            final Label label = new Label(modelContainer.getName());
                            label.setTextFill(bottomBarColor.getTextColor(bottomBarColorIntensity));
                            label.getStyleClass().add("caption");

                            // Generate warning and warning icon
                            final Warning<ModelContainer> modelContainerHasDeadlockWarning = new Warning<>(
                                    modelContainer1 -> modelContainer1.hasDeadlockProperty().get(),
                                    modelContainer,
                                    modelContainer.hasDeadlockProperty()
                            );
                            modelContainerHasDeadlockWarning.setMessage(modelContainer.getName() + " contains deadlock!");

                            final IconNode warningIcon = modelContainerHasDeadlockWarning.generateIconNode();
                            warningIcon.setFill(Color.GREY_BLUE.getColor(Color.Intensity.I700));
                            warningIcon.setIconSize(20);
                            warningIcon.xProperty().setValue(200);
                            warningIcon.yProperty().setValue(200);

                            modelContainer.hasDeadlockProperty().setValue(false);
                            modelContainer.hasDeadlockProperty().setValue(true);

                            // Add the warning icon to the label
                            label.setGraphic(warningIcon);

                            // The label will be invisible whenever the warning icon is
                            label.visibleProperty().bind(warningIcon.visibleProperty());

                            label.paddingProperty().set(new Insets(2));

                            // Find the right element in the bottom bar
                            final HBox rightHBox = (HBox) scene.lookup("#bottom-bar-right");
                            rightHBox.getChildren().add(label);
                        }
                    });
                }
            }
        });

        /*
        final Warning<ModelContainer> modelContainerHasDeadlockWarning = new Warning<>(
                modelContainer1 -> modelContainer1.hasDeadlockProperty().get(),
                modelContainer,
                modelContainer.hasDeadlockProperty()
        );

        final IconNode warningIcon = modelContainerHasDeadlockWarning.generateIconNode();

        // Find the right element in the bottom bar
        final HBox rightHBox = (HBox) scene.lookup("#bottom-bar-right");
        rightHBox.getChildren().add(label);
        rightHBox.getChildren().add(warningIcon);
        */
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

        final javafx.scene.paint.Color fontAndRippleColor = Color.GREY_BLUE.getTextColor(Color.Intensity.I500);

        // Align the status bar to the top of the window
        final StackPane stackpane = (StackPane) scene.lookup("#stackpane");
        stackpane.setAlignment(Pos.TOP_LEFT);

        final Label titleLabel = (Label) scene.lookup("#status-bar-title");
        titleLabel.textProperty().bind(stage.titleProperty());

        final HBox rightStatusBar = (HBox) scene.lookup("#status-bar-right");

        // Add the "show query pane" button to the status bar
        final IconNode queryIcon = new IconNode(GoogleMaterialDesignIcons.LIST);
        queryIcon.setFill(fontAndRippleColor);
        final JFXButton showQueryBtn = new JFXButton("", queryIcon);
        showQueryBtn.setButtonType(JFXButton.ButtonType.FLAT);
        showQueryBtn.setRipplerFill(fontAndRippleColor);
        showQueryBtn.setOnMouseClicked(event -> toggleQueryPane(scene));
        rightStatusBar.getChildren().add(showQueryBtn);

        // Add the minimize window button to the status bar
        final IconNode minimizeIcon = new IconNode(GoogleMaterialDesignIcons.REMOVE);
        minimizeIcon.setFill(fontAndRippleColor);
        final JFXButton minimizeBtn = new JFXButton("", minimizeIcon);
        minimizeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        minimizeBtn.setRipplerFill(fontAndRippleColor);
        minimizeBtn.setOnMouseClicked(event -> stage.setIconified(true));
        rightStatusBar.getChildren().add(minimizeBtn);

        // Add the resize window button to the status bar
        final IconNode resizeIcon = new IconNode();
        resizeIcon.setFill(fontAndRippleColor);
        resizeIcon.iconCodeProperty().bind(Bindings.when(isMaximized)
                .then(GoogleMaterialDesignIcons.FULLSCREEN_EXIT)
                .otherwise(GoogleMaterialDesignIcons.FULLSCREEN));

        final JFXButton resizeBtn = new JFXButton("", resizeIcon);
        resizeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        resizeBtn.setRipplerFill(fontAndRippleColor);
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
        closeIcon.setFill(fontAndRippleColor);
        final JFXButton closeBtn = new JFXButton("", closeIcon);
        closeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        closeBtn.setRipplerFill(fontAndRippleColor);
        closeBtn.setOnMouseClicked(event -> System.exit(0));
        rightStatusBar.getChildren().add(closeBtn);
    }

    private final BooleanProperty isQueryPaneShown = new SimpleBooleanProperty(false);

    private void toggleQueryPane(final Scene scene) {
        // Find the query pane
        Node container = scene.lookup("#root");
        container = container.lookup("#main-content");
        final QueryPane queryPane = (QueryPane) container.lookup("#query-pane");

        final Timeline animation = new Timeline();
        final KeyValue hiddenTranslateX = new KeyValue(queryPane.translateXProperty(), queryPane.getWidth() * 1.1, Interpolator.EASE_IN);
        final KeyValue visibleTranslateX = new KeyValue(queryPane.translateXProperty(), 0, Interpolator.EASE_OUT);

        // Initialize the animation accordingly to the property
        if(isQueryPaneShown.get()) {
            final KeyFrame visibleKeyFrame = new KeyFrame(Duration.millis(0), visibleTranslateX);
            final KeyFrame hiddenKeyFrame = new KeyFrame(Duration.millis(150), hiddenTranslateX);

            animation.getKeyFrames().add(visibleKeyFrame);
            animation.getKeyFrames().add(hiddenKeyFrame);
        } else {
            final KeyFrame hiddenKeyFrame = new KeyFrame(Duration.millis(0), hiddenTranslateX);
            final KeyFrame visibleKeyFrame = new KeyFrame(Duration.millis(150), visibleTranslateX);

            animation.getKeyFrames().add(hiddenKeyFrame);
            animation.getKeyFrames().add(visibleKeyFrame);
        }

        // Toggle the shown status
        isQueryPaneShown.set(!isQueryPaneShown.get());

        // Play the animation
        animation.play();
    }

}


