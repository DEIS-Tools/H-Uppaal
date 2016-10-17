package SW9.utility.helpers;

import SW9.Main;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ResizeHelper {

    public static void initialize(final Resizeable pane, final DoubleProperty border) {

        final double minHeight = 200, minWidth = 200;
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];
        final double[] width = new double[1];
        final double[] height = new double[1];

        final EventHandler<MouseEvent> updateOffsets = event -> {
            xOffset[0] = event.getScreenX();
            yOffset[0] = event.getScreenY();
            width[0] = pane.widthProperty().get();
            height[0] = pane.heightProperty().get();
        };

        final EventHandler<MouseEvent> resizeLeft = event -> {
            final double newWidth = width[0] + (xOffset[0] - event.getScreenX());
            if (newWidth < minWidth) return;

            pane.setWidth(newWidth);
            pane.setX(event.getScreenX());
        };

        final EventHandler<MouseEvent> resizeRight = event -> {
            final double newWidth = width[0] + (event.getScreenX() - xOffset[0]);
            if (newWidth < minWidth) return;

            pane.setWidth(newWidth);
        };

        final EventHandler<MouseEvent> resizeUp = event -> {
            final double newHeight = height[0] + (yOffset[0] - event.getScreenY());
            if (newHeight < minHeight) return;

            pane.setHeight(newHeight);
            pane.setY(event.getScreenY());

        };

        final EventHandler<MouseEvent> resizeDown = event -> {
            final double newHeight = height[0] - (yOffset[0] - event.getScreenY());
            if (newHeight < minHeight) return;

            pane.setWidth(newHeight);
        };

        // Add the north west corner
        generateResizeRegion(border.multiply(2), border.multiply(2), Pos.TOP_LEFT, Cursor.NW_RESIZE, event -> {
            resizeLeft.handle(event);
            resizeUp.handle(event);
        }, pane.getRegionContainer(), updateOffsets);

        generateResizeRegion(border.multiply(2), border.multiply(2), Pos.BOTTOM_LEFT, Cursor.SW_RESIZE, event -> {
            resizeLeft.handle(event);
            resizeDown.handle(event);
        }, pane.getRegionContainer(), updateOffsets);

        generateResizeRegion(border.multiply(2), border.multiply(2), Pos.BOTTOM_RIGHT, Cursor.SE_RESIZE, event -> {
            resizeRight.handle(event);
            resizeDown.handle(event);
        }, pane.getRegionContainer(), updateOffsets);

        generateResizeRegion(border.multiply(2), border.multiply(2), Pos.TOP_RIGHT, Cursor.NE_RESIZE, event -> {
            resizeRight.handle(event);
            resizeUp.handle(event);
        }, pane.getRegionContainer(), updateOffsets);

        System.out.println(height[0]);

        generateResizeRegion(border, pane.heightProperty(), Pos.CENTER_RIGHT, Cursor.E_RESIZE, resizeRight, pane.getRegionContainer(), updateOffsets);
        generateResizeRegion(border, pane.heightProperty(), Pos.CENTER_LEFT, Cursor.W_RESIZE, resizeLeft, pane.getRegionContainer(), updateOffsets);
        generateResizeRegion(pane.widthProperty(), border, Pos.TOP_CENTER, Cursor.N_RESIZE, resizeUp, pane.getRegionContainer(), updateOffsets);
        generateResizeRegion(pane.widthProperty(), border, Pos.BOTTOM_CENTER, Cursor.S_RESIZE, resizeDown, pane.getRegionContainer(), updateOffsets);
    }

    private static Rectangle generateResizeRegion(final ObservableDoubleValue width,
                                                  final ObservableDoubleValue height,
                                                  final Pos alignment,
                                                  final Cursor cursor,
                                                  final EventHandler<MouseEvent> onMouseDragged,
                                                  final StackPane regionContainer,
                                                  final EventHandler<MouseEvent> updateOffsets) {
        final Rectangle rectangle = new Rectangle(width.get(), height.get());

        rectangle.widthProperty().bind(width);
        rectangle.heightProperty().bind(height);

        rectangle.setFill(Color.TRANSPARENT);

        regionContainer.getChildren().add(rectangle);

        StackPane.setAlignment(rectangle, alignment);

        rectangle.setOnMouseEntered(event -> {
            // The window is maximized do not allow resizing
            if (Main.isMaximized.get()) return;

            regionContainer.getScene().setCursor(cursor);
            updateOffsets.handle(event);
        });

        rectangle.setOnMouseExited(event -> {
            // The window is maximized do not allow resizing
            if (Main.isMaximized.get()) return;

            regionContainer.getScene().setCursor(Cursor.DEFAULT);
        });

        rectangle.setOnMouseDragged(event -> {
            // The window is maximized do not allow resizing
            if (Main.isMaximized.get()) return;

            onMouseDragged.handle(event);
        });

        return rectangle;
    }
}
