package SW9.presentations;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import SW9.backend.UPPAALDriver;
import SW9.controllers.CanvasController;
import SW9.utility.colors.Color;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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

                final Component mainComponent = HUPPAAL.getProject().getMainComponent();

                if (mainComponent == null) {
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
                        mainComponent
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

        // todo: move this to the model
        final SimpleBooleanProperty isPeriodic = new SimpleBooleanProperty(false);

        final DropDownMenu dropDownMenu = new DropDownMenu((Pane) getParent(), detailsButton, 230, true);

        dropDownMenu.addTogglableListElement("Run periodically", isPeriodic, event -> {
            // Toggle the property
            isPeriodic.set(!isPeriodic.get());
        });

        dropDownMenu.addClickableListElement("Clear Status", event -> {
            // Clear the state
            query.setQueryState(QueryState.UNKNOWN);

            // Close the menu
            dropDownMenu.close();
        });

        dropDownMenu.addSpacerElement();

        dropDownMenu.addClickableListElement("Delete", event -> {
            // Remove the query
            HUPPAAL.getProject().getQueries().remove(query);

            // Close the menu
            dropDownMenu.close();
        });

        detailsButton.getChildren().get(0).setOnMousePressed(event -> {
            // Show the popup
            dropDownMenu.show(JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.RIGHT, 310, 35);
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
