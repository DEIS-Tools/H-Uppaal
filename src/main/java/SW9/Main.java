package SW9;

import com.jfoenix.controls.JFXButton;
import com.sun.javafx.geom.Shape;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

import javax.swing.*;

public class Main extends Application {

    public final static MouseTracker mouseTracker = new MouseTracker();
    private Parent root;
    private double xOffset;
    private double yOffset;


    public static void main(String[] args) {
        launch(Main.class, args);
    }

    public void start(final Stage stage) throws Exception {

        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        // Remove the classic
        stage.initStyle(StageStyle.UNDECORATED);

        root = FXMLLoader.load(getClass().getResource("main.fxml"));

        stage.setTitle("Kick-ass Modelchecker");

        final Scene scene = new Scene(root, 500, 500);
        scene.setOnKeyPressed(KeyboardTracker.handleKeyPress);
        scene.getStylesheets().add("SW9/colors.css");
        scene.getStylesheets().add("SW9/model_canvas/location.css");
        stage.setScene(scene);

        final Node modelCanvas = scene.lookup("#model_canvas");
        modelCanvas.setOnMouseMoved(mouseTracker.onMouseMovedEventHandler);
        modelCanvas.setOnMouseClicked(mouseTracker.onMouseClickedEventHandler);
        modelCanvas.setOnMouseEntered(mouseTracker.onMouseEnteredEventHandler);
        modelCanvas.setOnMouseExited(mouseTracker.onMouseExitedEventHandler);

        initializeStatusBar(stage);

        stage.show();
    }

    private void initializeStatusBar(final Stage stage) {
        final Scene scene = stage.getScene();

        // Find the status bar and make it draggable
        final BorderPane statusBar = (BorderPane) scene.lookup("#status_bar");
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
        final IconNode closeIcon = new IconNode(GoogleMaterialDesignIcons.CLOSE);
        closeIcon.setFill(Color.WHITE);
        final JFXButton closeBtn = new JFXButton("", closeIcon);
        closeBtn.setButtonType(JFXButton.ButtonType.FLAT);
        closeBtn.setRipplerFill(Color.WHITE);
        rightStatusBar.getChildren().add(closeBtn);
    }


}
