package SW9.utility.helpers;

import SW9.Main;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ResizeHelper {
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
        if (newWidth < minWidth) return;

        stage.setWidth(newWidth);
        stage.setX(event.getScreenX());
    };

    private static EventHandler<MouseEvent> resizeRight = event -> {
        final double newWidth = width + (event.getScreenX() - xOffset);
        if (newWidth < minWidth) return;

        stage.setWidth(newWidth);
    };

    private static EventHandler<MouseEvent> resizeUp = event -> {
        final double newHeight = height + (yOffset - event.getScreenY());
        if (newHeight < minHeight) return;

        stage.setHeight(newHeight);
        stage.setY(event.getScreenY());
    };

    private static EventHandler<MouseEvent> resizeDown = event -> {
        final double newHeight = height - (yOffset - event.getScreenY());
        if (newHeight < minHeight) return;

        stage.setHeight(newHeight);
    };

    public static void initialize(final Stage stage, final DoubleProperty border) {
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
            // The window is maximized do not allow resizing
            if (Main.isMaximized.get()) return;

            ResizeHelper.stage.getScene().setCursor(cursor);
            updateOffsets.handle(event);
        });

        rectangle.setOnMouseExited(event -> {
            // The window is maximized do not allow resizing
            if (Main.isMaximized.get()) return;

            ResizeHelper.stage.getScene().setCursor(Cursor.DEFAULT);
        });

        rectangle.setOnMouseDragged(event -> {
            // The window is maximized do not allow resizing
            if (Main.isMaximized.get()) return;

            onMouseDragged.handle(event);
        });

        return rectangle;
    }
}
