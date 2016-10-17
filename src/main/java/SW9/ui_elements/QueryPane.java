package SW9.ui_elements;

import SW9.Main;
import SW9.backend.UPPAALDriver;
import SW9.model_canvas.Component;
import SW9.utility.colors.Color;
import SW9.utility.helpers.DropShadowHelper;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.util.Duration;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryPane extends StackPane {

    private final VBox content = new VBox();
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox scrollPaneContent = new VBox();
    private Label queriesHeadlineCaption;
    private JFXButton addQueryButton;

    private final List<Query> queries = new ArrayList<>();

    public QueryPane() throws IOException {
        initialize();
    }

    @FXML
    private void initialize() throws IOException {
        StackPane.setAlignment(this, Pos.CENTER_RIGHT);

        // Add the content to us
        getChildren().add(content);

        // Load the stylesheet for the query pane
        getStylesheets().add("SW9/query_pane.css");

        // Add a drop shadow (to make the pane look floating)
        final DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5);
        dropShadow.setHeight(200);
        this.setEffect(dropShadow);

        // Set the background color for the pane
        setBackground(new Background(new BackgroundFill(
                Color.GREY.getColor(Color.Intensity.I300),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        // Set the width of the query box to 20% of the screen size
        final double width = Screen.getPrimary().getVisualBounds().getWidth() * 0.2;
        setMaxWidth(width);

        // Hide the query pane
        translateXProperty().set(width);

        // Initialize the toolbar
        initializeToolbar();

        // Initialize the scroll pane (will be used to handle the query children)
        initializeScrollPane();

        // Populate the list with queries
        final Query noDeadlockQuery = new Query(new SimpleStringProperty("A[] not deadlock"), new SimpleStringProperty("The model is deadlock free"));
        queries.add(noDeadlockQuery);
        scrollPaneContent.getChildren().add(queries.size() - 1, noDeadlockQuery.getView());

        final Query hasDeadlockQuery = new Query(new SimpleStringProperty("E<> deadlock"), new SimpleStringProperty("The model contains at least one deadlock"));
        queries.add(hasDeadlockQuery);
        scrollPaneContent.getChildren().add(queries.size() - 1, hasDeadlockQuery.getView());

        // Force update the label
        hasDeadlockQuery.updateQueriesHeadlineCaption();

        // Add the add new query button
        initializeAddButton();
    }

    private void initializeToolbar() throws IOException {
        // Load the toolbar fxml
        final HBox toolbar = FXMLLoader.load(getClass().getResource("/SW9/fxml/query_pane/toolbar.fxml"));
        content.getChildren().add(toolbar);

        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I800;

        // Set the background of the toolbar
        toolbar.setBackground(new Background(new BackgroundFill(
                color.getColor(colorIntensity),
                CornerRadii.EMPTY,
                Insets.EMPTY)));

        // Set the elevation of the toolbar
        toolbar.setEffect(DropShadowHelper.generateElevationShadow(8));

        // Find the labels in the toolbar and style them accordingly to the background used
        ((Label) toolbar.lookup("#queries-headline")).setTextFill(color.getTextColor(colorIntensity));
        queriesHeadlineCaption = ((Label) toolbar.lookup("#queries-headline-caption"));
        queriesHeadlineCaption.setTextFill(color.getTextColor(colorIntensity));

        // Find the buttons in the toolbar and style them accordingly to the background used
        final JFXButton clearButton = ((JFXButton) toolbar.lookup("#clear-button"));
        clearButton.setTextFill(color.getTextColor(colorIntensity));

        final JFXButton runAllButton = ((JFXButton) toolbar.lookup("#run-all-button"));
        runAllButton.setTextFill(color.getTextColor(colorIntensity));

        // Add listeners to the buttons
        clearButton.setOnMouseClicked(event -> queries.forEach(query -> query.queryState.set(QueryState.UNKNOWN)));
        runAllButton.setOnMouseClicked(event -> queries.forEach(Query::runQuery));

        // Make the buttons look clickable
        clearButton.setOnMouseEntered(event -> setCursor(Cursor.HAND));
        clearButton.setOnMouseExited(event -> setCursor(Cursor.DEFAULT));
        runAllButton.setOnMouseEntered(event -> setCursor(Cursor.HAND));
        runAllButton.setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

        toolbar.paddingProperty().set(new Insets(16));
    }

    private void initializeScrollPane() {
        content.getChildren().add(scrollPane);

        // Will make the scroll pane larger
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Will set the content of the scroll pane (just a VBox)
        scrollPane.setContent(scrollPaneContent);

        // Styling
        scrollPane.getStyleClass().addAll("edge-to-edge");
        scrollPane.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.TRANSPARENT,
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));

        // Make space for the floating action button by adding an empty region
        final Region spacer = new Region();
        spacer.setMinHeight(56 + 14 + 10);
        spacer.setMaxWidth(56 + 14 + 10);
        scrollPaneContent.getChildren().add(spacer);
    }

    private void initializeAddButton() {
        addQueryButton = new JFXButton();

        addQueryButton.setButtonType(JFXButton.ButtonType.RAISED);
        addQueryButton.getStyleClass().add("floating-action-button");

        final Color color = Color.GREY_BLUE;
        final Color.Intensity colorIntensity = Color.Intensity.I500;

        // Set the color of the button
        addQueryButton.setBackground(new Background(new BackgroundFill(
                color.getColor(colorIntensity),
                new CornerRadii(100),
                Insets.EMPTY
        )));

        // Generate and set the add icon
        final IconNode addIcon = new IconNode(GoogleMaterialDesignIcons.ADD);
        addIcon.setIconSize(28);
        addIcon.setFill(color.getTextColor(colorIntensity));
        addQueryButton.setGraphic(addIcon);

        // Style the rippler effect
        addQueryButton.setRipplerFill(color.getTextColor(colorIntensity));

        // Add a floating effect for the button
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(6));
        addQueryButton.setOnMousePressed(event -> addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(12)));
        addQueryButton.setOnMouseReleased(event -> addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(6)));

        // Add the floating action button
        getChildren().add(addQueryButton);

        // Align the floating action button to the bottom right part of the container
        StackPane.setAlignment(addQueryButton, Pos.BOTTOM_RIGHT);
        addQueryButton.setTranslateX(-24);
        addQueryButton.setTranslateY(-14);

        // Add the click listener (will add a new query)
        addQueryButton.setOnMouseClicked(event -> {
            try {
                // Check if the user actually used the previous query
                if (queries.get(queries.size() - 1).queryField.textProperty().get().equals("")) {
                    final Node queryView = scrollPaneContent.getChildren().get(queries.size() - 1);

                    final Timeline shakeAnimation = new Timeline();

                    final KeyValue shakeLeft = new KeyValue(queryView.translateXProperty(), -10);
                    final KeyValue shakeRight = new KeyValue(queryView.translateXProperty(), 10);
                    final KeyValue noShake = new KeyValue(queryView.translateXProperty(), 0);

                    final KeyFrame initial = new KeyFrame(Duration.millis(0), noShake);
                    final KeyFrame shake1 = new KeyFrame(Duration.millis(50), shakeLeft);
                    final KeyFrame shake2 = new KeyFrame(Duration.millis(100), shakeRight);
                    final KeyFrame shake3 = new KeyFrame(Duration.millis(150), shakeLeft);
                    final KeyFrame shake4 = new KeyFrame(Duration.millis(200), shakeRight);
                    final KeyFrame finish = new KeyFrame(Duration.millis(250), noShake);

                    shakeAnimation.getKeyFrames().addAll(initial, shake1, shake2, shake3, shake4, finish);
                    shakeAnimation.play();

                    return; // Do not add the query
                }

                final SimpleStringProperty query = new SimpleStringProperty("");
                final SimpleStringProperty comment = new SimpleStringProperty("");

                final Query newQuery = new Query(query, comment);
                queries.add(newQuery);
                newQuery.updateQueriesHeadlineCaption();
                scrollPaneContent.getChildren().add(queries.size() - 1, newQuery.getView());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        });

        // Make the button look clickable
        addQueryButton.setOnMouseEntered(event -> setCursor(Cursor.HAND));
        addQueryButton.setOnMouseExited(event -> setCursor(Cursor.DEFAULT));
    }

    private enum QueryState {
        SUCCESSFUL(Color.GREEN, Color.Intensity.I700, "✓"),
        ERROR(Color.RED, Color.Intensity.I700, "✘"),
        RUNNING(Color.GREY_BLUE, Color.Intensity.I600, "···"),
        UNKNOWN(Color.GREY, Color.Intensity.I600, "?"),
        SYNTAX_ERROR(Color.RED, Color.Intensity.I700, "!");

        private final Color color;
        private final Color.Intensity colorIntensity;
        private final String text;

        QueryState(final Color color, final Color.Intensity colorIntensity, final String text) {
            this.color = color;
            this.colorIntensity = colorIntensity;
            this.text = text;
        }
    }

    private class Query {

        private final HBox root;
        private final JFXTextField queryField;
        private final JFXTextField commentField;
        private final JFXButton openButton;
        private final JFXRippler indicatorContainer;
        private final Circle indicatorCircle;
        private final Label indicatorLabel;
        private final JFXSpinner indicatorSpinner;

        private final Color inputColor = Color.GREY_BLUE;
        private final Color.Intensity inputColorIntensity = Color.Intensity.I700;

        private final BooleanProperty queryIsOpened = new SimpleBooleanProperty(false);
        private final ObjectProperty<QueryState> queryState = new SimpleObjectProperty<>(null);

        Query(final StringProperty query, final StringProperty comment) throws IOException {
            // Load the element from fxml
            root = FXMLLoader.load(getClass().getResource("/SW9/fxml/query_pane/query.fxml"));

            final Color color = Color.GREY;
            final Color.Intensity colorIntensity = Color.Intensity.I50;

            // Set the background color
            setBackground(new Background(new BackgroundFill(
                    color.getColor(colorIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY)));

            // Initialize the query field
            queryField = (JFXTextField) root.lookup("#query-text-field");
            initializeTextInputField(queryField, query, color.toHexColor(inputColorIntensity));
            StackPane.setAlignment(queryField, Pos.TOP_LEFT);

            // Initialize the comment field
            commentField = (JFXTextField) root.lookup("#comment-text-field");
            initializeTextInputField(commentField, comment, Color.GREY.toHexColor(Color.Intensity.I600));
            StackPane.setAlignment(commentField, Pos.BOTTOM_LEFT);

            // Initialize the "open" button
            openButton = (JFXButton) root.lookup("#open-button");
            initializeOpenButton();

            // Initialize the indicator
            indicatorContainer = (JFXRippler) root.lookup("#indicator-container");
            indicatorCircle = (Circle) root.lookup("#indicator-circle");
            indicatorLabel = (Label) root.lookup("#indicator-label");
            indicatorSpinner = (JFXSpinner) root.lookup("#indicator-spinner");
            initializeIndicator();

            // Hover effect
            root.setOnMouseEntered(event -> {
                root.setBackground(new Background(new BackgroundFill(
                        Color.GREY_BLUE.getColor(colorIntensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)));
            });
            root.setOnMouseExited(event -> {
                root.setBackground(new Background(new BackgroundFill(
                        color.getColor(colorIntensity),
                        CornerRadii.EMPTY,
                        Insets.EMPTY)));
            });
        }

        private synchronized void updateQueriesHeadlineCaption() {
            final int[] running = {0};
            final int[] successful = {0};
            final int[] unknown = {0};
            final int[] error = {0};

            queries.forEach(query -> {
                final QueryState queryState = query.queryState.get();

                if (queryState.equals(QueryState.RUNNING)) {
                    running[0]++;
                } else if (queryState.equals(QueryState.SUCCESSFUL)) {
                    successful[0]++;
                } else if (queryState.equals(QueryState.UNKNOWN)) {
                    unknown[0]++;
                } else {
                    error[0]++;
                }
            });

            String resultString = "";

            if (successful[0] > 0) {
                resultString += successful[0] + " successful";
            }

            if (error[0] > 0) {
                if (!resultString.equals("")) resultString += ", ";
                resultString += error[0] + " unsuccessful";
            }

            if (unknown[0] > 0) {
                if (!resultString.equals("")) resultString += ", ";
                resultString += unknown[0] + " unknown";
            }

            if (running[0] > 0) {
                if (!resultString.equals("")) resultString += ", ";
                resultString += running[0] + " running";
            }

            queriesHeadlineCaption.setText(resultString);
        }

        private void runQuery() {
            queryState.set(QueryState.RUNNING);

            // Find the component and run the query on that
            for (final Node child : Main.getModelCanvas().getChildren()) {
                if (child instanceof Component) {
                    UPPAALDriver.verify(
                            queryField.getText(),
                            result -> {
                                // Handle result
                                if (result) {
                                    Platform.runLater(() -> {
                                        queryState.set(QueryState.SUCCESSFUL);
                                        indicatorLabel.setGraphic(null);
                                        updateQueriesHeadlineCaption();
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        queryState.set(QueryState.ERROR);
                                        indicatorLabel.setGraphic(null);
                                        updateQueriesHeadlineCaption();
                                    });
                                }
                            },
                            e -> Platform.runLater(() -> {
                                queryState.set(QueryState.SYNTAX_ERROR);
                                indicatorLabel.setGraphic(null);
                                updateQueriesHeadlineCaption();
                            })
                            ,
                            (Component) child
                    );

                    // Do not run on multiple components
                    return;
                }
            }

            // We found no component , i.e. we cannot run query
            queryState.set(QueryState.UNKNOWN);
        }

        private void stopQuery() {
            queryState.set(QueryState.UNKNOWN);

            // TODO: Stop the query
        }

        private void initializeIndicator() {
            // Style the spinner
            indicatorSpinner.radiusProperty().bind(indicatorContainer.heightProperty().divide(2).add(4));
            indicatorSpinner.setMouseTransparent(true);

            // Style the rippler effect
            indicatorContainer.setMaskType(JFXRippler.RipplerMask.CIRCLE);
            indicatorContainer.setMaxHeight(indicatorCircle.getRadius() * 2);

            // When the query state changes, change the style of the indicator
            queryState.addListener((observable, oldQueryState, newQueryState) -> {
                // Set the color of the indicator
                indicatorCircle.setFill(newQueryState.color.getColor(newQueryState.colorIntensity));
                indicatorCircle.setStroke(newQueryState.color.getColor(newQueryState.colorIntensity.next(2)));
                indicatorLabel.setTextFill(newQueryState.color.getTextColor(newQueryState.colorIntensity));

                // Style the rippler effect
                indicatorContainer.setRipplerFill(newQueryState.color.getTextColor(newQueryState.colorIntensity));

                // Set the label
                indicatorLabel.setText(newQueryState.text);

                // Hide the spinner if the query is not running
                if (newQueryState.equals(QueryState.RUNNING)) {
                    indicatorSpinner.setVisible(true);
                } else {
                    indicatorSpinner.setVisible(false);
                }
            });

            // Set the query state to force an update
            queryState.set(QueryState.UNKNOWN);

            // Icons to use for hover effect
            final IconNode runQueryIcon = new IconNode(GoogleMaterialDesignIcons.PLAY_ARROW);
            runQueryIcon.setFill(queryState.get().color.getTextColor(queryState.get().colorIntensity));
            final IconNode stopQueryIcon = new IconNode(GoogleMaterialDesignIcons.STOP);
            stopQueryIcon.setFill(queryState.get().color.getTextColor(queryState.get().colorIntensity));

            // Hover effect
            indicatorContainer.setOnMouseEntered(event -> {
                indicatorLabel.setText("");

                if (queryState.get().equals(QueryState.RUNNING)) {
                    indicatorLabel.setGraphic(stopQueryIcon);
                } else {
                    indicatorLabel.setGraphic(runQueryIcon);
                }

                setCursor(Cursor.HAND);
            });
            indicatorContainer.setOnMouseExited(event -> {
                indicatorLabel.setText(queryState.get().text);
                indicatorLabel.setGraphic(null);
                setCursor(Cursor.DEFAULT);
            });

            final boolean[] eventIsRaisedTwiceFix = {false};

            // When clicked, run the query
            indicatorContainer.setOnMouseClicked(event -> {
                // This event is raised twice, ignore the second one (event.consume() does not work)
                if (eventIsRaisedTwiceFix[0]) {
                    eventIsRaisedTwiceFix[0] = false;
                    return;
                } else {
                    eventIsRaisedTwiceFix[0] = true;
                }

                // If we are running a query, stop it and re-indicate that you can run the query
                if (queryState.get().equals(QueryState.RUNNING)) {
                    stopQuery();

                    indicatorLabel.setText("");
                    indicatorLabel.setGraphic(runQueryIcon);
                }
                // If we are not running a query, start it and indicate that you can stop the query
                else {
                    runQuery();

                    indicatorLabel.setText("");
                    indicatorLabel.setGraphic(stopQueryIcon);
                }
            });
        }

        private void initializeOpenButton() {
            // Generate the icon to use
            final IconNode openCloseIcon = new IconNode(GoogleMaterialDesignIcons.EXPAND_MORE);
            openCloseIcon.setIconSize(35);
            openCloseIcon.setFill(Color.GREY.getColor(Color.Intensity.I400));

            // Set the icon of the button
            openButton.setGraphic(openCloseIcon);

            // Set the ripple effect color
            openButton.setRipplerFill(Color.GREY_BLUE.getColor(Color.Intensity.I200));

            // Set the cursor to make the button look clickable
            openButton.setOnMouseEntered(event -> setCursor(Cursor.HAND));
            openButton.setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

            // Toggle the "open" property when the button is pressed
            openButton.setOnMouseClicked(event -> {
                // Animation for the icon
                final Timeline animation = new Timeline();

                final KeyValue openedState = new KeyValue(openCloseIcon.rotateProperty(), -180, Interpolator.EASE_IN);
                final KeyValue closedState = new KeyValue(openCloseIcon.rotateProperty(), 0, Interpolator.EASE_OUT);

                // Initialize the animation accordingly to the property
                if (queryIsOpened.get()) {
                    final KeyFrame openedKeyFrame = new KeyFrame(Duration.millis(0), openedState);
                    final KeyFrame closedKeyFrame = new KeyFrame(Duration.millis(250), closedState);

                    animation.getKeyFrames().add(openedKeyFrame);
                    animation.getKeyFrames().add(closedKeyFrame);
                } else {
                    final KeyFrame closedKeyFrame = new KeyFrame(Duration.millis(0), closedState);
                    final KeyFrame openedKeyFrame = new KeyFrame(Duration.millis(250), openedState);

                    animation.getKeyFrames().add(closedKeyFrame);
                    animation.getKeyFrames().add(openedKeyFrame);
                }

                // Start the animation (will rotate the icon)
                animation.play();

                // Toggle the open status of the query
                queryIsOpened.set(!queryIsOpened.get());

                // TODO: Open the query to show additional information
            });
        }

        private void initializeTextInputField(final JFXTextField textField, final StringProperty query, final String color) {
            // Set the text to whatever the text property we got, is
            textField.setText(query.get());

            // Bind the property we got to the property of the input field (will notify original listeners when user updated query)
            query.bind(textField.textProperty());

            // Set the focus and unfocused colors
            textField.setFocusColor(inputColor.getColor(inputColorIntensity));
            textField.setUnFocusColor(javafx.scene.paint.Color.TRANSPARENT);

            // Set the text color
            textField.setStyle("-fx-text-fill: " + color + ";");
        }

        HBox getView() {
            return root;
        }

    }

}
