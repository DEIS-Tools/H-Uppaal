package SW9.presentations;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import SW9.backend.UPPAALDriver;
import SW9.controllers.CanvasController;
import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.When;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import static javafx.scene.paint.Color.TRANSPARENT;

public class QueryPresentation extends AnchorPane {

    private final Query query;
    private JFXRippler actionButton;
    private Runnable runQuery;

    public QueryPresentation(final Query query) {
        final URL location = this.getClass().getResource("QueryPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            this.query = query;

            initializeRunQuery();

            initializeStateIndicator();
            initializeProgressIndicator();
            initializeActionButton();
            initializeDetailsButton();
            initializeTextFields();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
    private void initializeRunQuery() {
        runQuery = () -> {
            if (query.getQueryState().equals(QueryState.RUNNING)) {
                // todo: Stop the query
                query.setQueryState(QueryState.UNKNOWN);
            } else {
                query.setQueryState(QueryState.RUNNING);

                final Component[] mainComponent = {null};
                HUPPAAL.getProject().getComponents().forEach(component -> {
                    if (component.isIsMain()) {
                        mainComponent[0] = component;
                    }
                });

                if (mainComponent[0] == null) {
                    return; // We cannot generate a UPPAAL file without a main component
                }

                UPPAALDriver.verify(query.getQuery(),
                        aBoolean -> {
                            if(aBoolean) {
                                query.setQueryState(QueryState.SUCCESSFUL);
                            } else {
                                query.setQueryState(QueryState.ERROR);
                            }
                        },
                        e -> {
                            query.setQueryState(QueryState.SYNTAX_ERROR);
                        },
                        mainComponent[0]
                );
            }
        };
    }

    private void initializeTextFields() {
        final JFXTextField queryTextField = (JFXTextField) lookup("#query");
        final JFXTextField commentTextField = (JFXTextField) lookup("#comment");

        queryTextField.setText(query.getQuery());
        commentTextField.setText(query.getComment());

        query.queryProperty().bind(queryTextField.textProperty());
        query.commentProperty().bind(commentTextField.textProperty());


        queryTextField.setOnKeyPressed(CanvasController.getEnterKeyHandler(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                runQuery.run();
            }
        }));
        commentTextField.setOnKeyPressed(CanvasController.getEnterKeyHandler());
    }

    private void initializeDetailsButton() {
        final JFXRippler detailsButton = (JFXRippler) lookup("#detailsButton");
        final FontIcon detailsButtonIcon = (FontIcon) lookup("#detailsButtonIcon");

        detailsButtonIcon.setIconColor(Color.GREY.getColor(Color.Intensity.I900));

        detailsButton.setCursor(Cursor.HAND);
        detailsButton.setRipplerFill(Color.GREY.getColor(Color.Intensity.I500));
        detailsButton.setMaskType(JFXRippler.RipplerMask.CIRCLE);
        detailsButton.getChildren().get(0).setOnMousePressed(event -> {
            // todo: show query information
        });

        // Bind the y scale to the x scale
        detailsButton.scaleYProperty().bind(detailsButton.scaleXProperty());

        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        final KeyValue scale0x = new KeyValue(detailsButton.scaleXProperty(), 0, interpolator);
        final KeyValue scale2x = new KeyValue(detailsButton.scaleXProperty(), 1.1, interpolator);
        final KeyValue scale1x = new KeyValue(detailsButton.scaleXProperty(), 1, interpolator);

        final KeyFrame kf1 = new KeyFrame(Duration.millis(0), scale0x);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(180), scale2x);
        final KeyFrame kf3 = new KeyFrame(Duration.millis(200), scale1x);

        final KeyFrame kf4 = new KeyFrame(Duration.millis(0), scale1x);
        final KeyFrame kf5 = new KeyFrame(Duration.millis(200), scale0x);

        final Timeline enterAnimation = new Timeline();
        enterAnimation.getKeyFrames().addAll(kf1, kf2, kf3);

        final Timeline exitAnimation = new Timeline();
        exitAnimation.getKeyFrames().addAll(kf4, kf5);

        // When the query is not not unknown, show the details button, otherwise hide it
        query.queryStateProperty().addListener((obs, oldQueryState, newQueryState) -> {
            if (newQueryState.equals(QueryState.UNKNOWN)) {
                exitAnimation.play();
            } else {
                if (oldQueryState.equals(QueryState.UNKNOWN)) {
                    enterAnimation.play();
                }
            }
        });
    }

    private void initializeActionButton() {
        // Find the action icon
        actionButton = (JFXRippler) lookup("#actionButton");
        final FontIcon actionButtonIcon = (FontIcon) lookup("#actionButtonIcon");

        actionButtonIcon.setIconColor(Color.GREY.getColor(Color.Intensity.I900));

        actionButton.setCursor(Cursor.HAND);
        actionButton.setRipplerFill(Color.GREY.getColor(Color.Intensity.I500));

        // Delegate that based on the query state updated the action icon
        final Consumer<QueryState> updateIcon = (queryState) -> {
            if (queryState.equals(QueryState.RUNNING)) {
                actionButtonIcon.setIconLiteral("gmi-stop");
                actionButtonIcon.setIconSize(24);
            } else {
                actionButtonIcon.setIconLiteral("gmi-play-arrow");
                actionButtonIcon.setIconSize(24);
            }
        };

        // Update the icon initially
        updateIcon.accept(query.getQueryState());

        // Update the icon when ever the query state is updated
        query.queryStateProperty().addListener((observable, oldValue, newValue) -> updateIcon.accept(newValue));

        actionButton.setMaskType(JFXRippler.RipplerMask.CIRCLE);

        actionButton.getChildren().get(0).setOnMousePressed(event -> {
            runQuery.run();
        });
    }

    private void initializeProgressIndicator() {
        // Find the progress indicator
        final JFXSpinner progressIndicator = (JFXSpinner) lookup("#progressIndicator");

        // If the query is running show the indicator, otherwise hide it
        progressIndicator.visibleProperty().bind(new When(query.queryStateProperty().isEqualTo(QueryState.RUNNING)).then(true).otherwise(false));
    }

    private void initializeStateIndicator() {
        // Find the state indicator from the inflated xml
        final StackPane stateIndicator = (StackPane) lookup("#stateIndicator");

        // Delegate that based on a query state updates the color of the state indicator
        final Consumer<QueryState> updateColor = (queryState) -> {
            final Color color = queryState.getColor();
            final Color.Intensity colorIntensity = queryState.getColorIntensity();

            if (queryState.equals(QueryState.UNKNOWN) || queryState.equals(QueryState.RUNNING)) {
                stateIndicator.setBackground(new Background(new BackgroundFill(TRANSPARENT,
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));
            } else {
                stateIndicator.setBackground(new Background(new BackgroundFill(color.getColor(colorIntensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)
                ));
            }
        };

        // Update the initial color
        updateColor.accept(query.getQueryState());

        // Ensure that the color is updated when ever the query state is updated
        query.queryStateProperty().addListener((observable, oldValue, newValue) -> updateColor.accept(newValue));
    }
}
