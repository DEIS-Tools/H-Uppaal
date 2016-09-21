package SW9;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

public class Main extends Application {

    private Parent root;
    private double xOffset;
    private double yOffset;

    private final static DoubleProperty border = new SimpleDoubleProperty(3d);
    public static MouseTracker mouseTracker;

    public static void main(String[] args) {
        launch(Main.class, args);
    }

    public void start(final Stage stage) throws Exception {

        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        // Remove the classic decoration
        stage.initStyle(StageStyle.UNDECORATED);

        root = FXMLLoader.load(getClass().getResource("main.fxml"));

        stage.setTitle("Kick-ass Modelchecker");

        final Scene scene = new Scene(root, 500, 500);
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);
        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas/location.css");
        stage.setScene(scene);

        final Node modelCanvas = scene.lookup("#model_canvas");
        mouseTracker = new MouseTracker(modelCanvas);

        initializeStatusBar(stage);

        // Allows us to resize the window
        stage.resizableProperty().setValue(true);
        ResizeHelper.initialize(stage, border);

        stage.show();
    }

    private void initializeStatusBar(final Stage stage) {
        final Scene scene = stage.getScene();

        // Find the status bar and make it draggable
        final BorderPane statusBar = (BorderPane) scene.lookup("#status_bar");
        statusBar.setOnMouseEntered(event -> scene.setCursor(Cursor.CLOSED_HAND)); // Update the cursor to look draggable
        statusBar.setOnMouseExited(event -> scene.setCursor(Cursor.DEFAULT)); // Update the cursor to look normal
        statusBar.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        statusBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });

        // Align the status bar to the top of the window
        final StackPane stackpane = (StackPane) scene.lookup("#stackpane");
        stackpane.setAlignment(Pos.TOP_LEFT);

        final HBox leftStatusBar = (HBox) scene.lookup("#status_bar_left");
        // TODO: Add stuff to the left section

        final HBox middleStatusBar = (HBox) scene.lookup("#status_bar_middle");
        // TODO: Add stuff to the middle section

        final HBox rightStatusBar = (HBox) scene.lookup("#status_bar_right");

        // Add the minimize window button to the status bar
        final IconNode minimizeIcon = new IconNode(GoogleMaterialDesignIcons.REMOVE);
        minimizeIcon.setFill(Color.WHITE);
        final JFXButton minimizeBtn = new JFXButton("", minimizeIcon);
        minimizeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        minimizeBtn.setRipplerFill(Color.WHITE);
        minimizeBtn.setOnMouseClicked(event -> stage.setIconified(true));
        rightStatusBar.getChildren().add(minimizeBtn);

        // Add the resize window button to the status bar
        final IconNode resizeIcon = new IconNode(GoogleMaterialDesignIcons.FULLSCREEN);
        resizeIcon.setFill(Color.WHITE);
        final JFXButton resizeBtn = new JFXButton("", resizeIcon);
        resizeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        resizeBtn.setRipplerFill(Color.WHITE);
        resizeBtn.setOnMouseClicked(event -> {
            System.out.println("data");
            stage.resizableProperty().setValue(true);
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

class ResizeHelper {
    private static final double minHeight = 200, minWidth = 200;
    private static double xOffset, yOffset, width, height;

    private static Stage stage;

    private static EventHandler<MouseEvent> updateOffsets = event -> {
        xOffset = event.getScreenX();
        yOffset = event.getScreenY();
        width = stage.getWidth();
        height = stage.getHeight();
    };

    private static EventHandler<MouseEvent> resizeLeft = event -> {
        final double newWidth = width + (xOffset - event.getScreenX());
        if(newWidth < minWidth) return;

        stage.setWidth(newWidth);
        stage.setX(event.getScreenX());
    };

    private static EventHandler<MouseEvent> resizeRight = event -> {
        final double newWidth = width + (event.getScreenX() - xOffset);
        if(newWidth < minWidth) return;

        stage.setWidth(newWidth);
    };

    private static EventHandler<MouseEvent> resizeUp = event -> {
        final double newHeight = height + (yOffset - event.getScreenY());
        if(newHeight < minHeight) return;

        stage.setHeight(newHeight);
        stage.setY(event.getScreenY());
    };

    private static EventHandler<MouseEvent> resizeDown = event -> {
        final double newHeight = height - (yOffset - event.getScreenY());
        if(newHeight < minHeight) return;

        stage.setHeight(newHeight);
    };

    static void initialize(final Stage stage, final DoubleProperty border) {
        ResizeHelper.stage = stage;

        // Find the scene set on stage
        final Scene scene = stage.getScene();

        // Find the stack panel (which we will be adding draggable regions to)
        final StackPane stackpane = (StackPane) scene.lookup("#stackpane");

        // Add the north west corner

        final Rectangle NWDragCorner = rectangleHelper(border, border, stackpane, Pos.TOP_LEFT, Cursor.NW_RESIZE, event -> {
            resizeLeft.handle(event);
            resizeUp.handle(event);
        });

        final Rectangle SWDragCorner = rectangleHelper(border, border, stackpane, Pos.BOTTOM_LEFT, Cursor.SW_RESIZE, event -> {
            resizeLeft.handle(event);
            resizeDown.handle(event);
        });

        final Rectangle SEDragCorner = rectangleHelper(border, border, stackpane, Pos.BOTTOM_RIGHT, Cursor.SE_RESIZE, event -> {
            resizeRight.handle(event);
            resizeDown.handle(event);
        });

        final Rectangle NEDragCorner = rectangleHelper(border, border, stackpane, Pos.TOP_RIGHT, Cursor.NE_RESIZE, event -> {
            resizeRight.handle(event);
            resizeUp.handle(event);
        });

        final DoubleBinding heightBinding = new DoubleBinding() {
            {
                super.bind(stage.heightProperty(), border);
            }

            @Override
            protected double computeValue() {
                return stage.heightProperty().get() - 2 * border.get();
            }
        };

        final DoubleBinding widthBinding = new DoubleBinding() {
            {
                super.bind(stage.widthProperty(), border);
            }

            @Override
            protected double computeValue() {
                return stage.widthProperty().get() - 2 * border.get();
            }
        };

        final Rectangle EDragRegion = rectangleHelper(border, heightBinding, stackpane, Pos.CENTER_RIGHT, Cursor.E_RESIZE, event -> resizeRight.handle(event));
        final Rectangle WDragRegion = rectangleHelper(border, heightBinding, stackpane, Pos.CENTER_LEFT, Cursor.W_RESIZE, event -> resizeLeft.handle(event));
        final Rectangle NDragRegion = rectangleHelper(widthBinding, border, stackpane, Pos.TOP_CENTER, Cursor.N_RESIZE, event -> resizeUp.handle(event));
        final Rectangle SDragRegion = rectangleHelper(widthBinding, border, stackpane, Pos.BOTTOM_CENTER, Cursor.S_RESIZE, event -> resizeDown.handle(event));
    }

    private static Rectangle rectangleHelper(final ObservableDoubleValue width, final ObservableDoubleValue height, final StackPane parent, final Pos alignment, final Cursor cursor, final EventHandler<MouseEvent> onMouseDragged) {
        final Rectangle rectangle = new Rectangle(width.get(), height.get());
        rectangle.widthProperty().bind(width);
        rectangle.heightProperty().bind(height);

        rectangle.setFill(Color.TRANSPARENT);

        parent.getChildren().add(rectangle);

        StackPane.setAlignment(rectangle, alignment);

        rectangle.setOnMouseEntered(event -> {
            ResizeHelper.stage.getScene().setCursor(cursor);
            updateOffsets.handle(event);
        });

        rectangle.setOnMouseExited(event -> {
            ResizeHelper.stage.getScene().setCursor(Cursor.DEFAULT);
        });

        rectangle.setOnMouseDragged(onMouseDragged);

        return rectangle;
    }
}


