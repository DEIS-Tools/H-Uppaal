package SW9.presentations;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Edge;
import SW9.abstractions.Location;
import SW9.abstractions.SubComponent;
import SW9.code_analysis.CodeAnalysis;
import SW9.code_analysis.Nearable;
import SW9.controllers.CanvasController;
import SW9.utility.helpers.SelectHelper;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;

public class MessagePresentation extends HBox {

    private final CodeAnalysis.Message message;

    public MessagePresentation(final CodeAnalysis.Message message) {
        this.message = message;

        final URL location = this.getClass().getResource("MessagePresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            // Initialize
            initializeMessage();
            initializeNearLabel();

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeMessage() {
        final Label messageLabel = (Label) lookup("#messageLabel");
        messageLabel.textProperty().bind(message.messageProperty());
    }

    private void initializeNearLabel() {
        final InvalidationListener listener = observable -> {
            String nearString = "Near: ";

            final HBox nearLabels = (HBox) lookup("#nearLabels");
            nearLabels.getChildren().clear(); // Remove all children currently in the container

            if (message.getNearables().size() == 0) {
                nearString = ""; // Do not display any "near"
            } else {
                // Add all "near" strings
                for (final Nearable nearable : message.getNearables()) {
                    final Label newNearLabel = new Label(nearable.generateNearString());

                    final boolean isClickable = nearable instanceof Location
                            || nearable instanceof Edge
                            || nearable instanceof SubComponent
                            || nearable instanceof Component;

                    if (isClickable) {
                        // Set styling
                        newNearLabel.setStyle("-fx-underline: true;");
                        newNearLabel.getStyleClass().add("body1");

                        // On mouse entered/exited
                        newNearLabel.setOnMouseEntered(event -> setCursor(Cursor.HAND));
                        newNearLabel.setOnMouseExited(event -> setCursor(Cursor.DEFAULT));

                        // On mouse pressed
                        newNearLabel.setOnMousePressed(event -> {
                            final Component[] openComponent = {null};

                            // We are pressing a location, find the location and open the corresponding component
                            if (nearable instanceof Location) {
                                HUPPAAL.getProject().getComponents().forEach(component -> {
                                    if (component.getLocations().contains(nearable)) {
                                        openComponent[0] = component;
                                    }
                                });
                            } else if (nearable instanceof Edge) { // We are pressing an edge, find the edge and open the corresponding component
                                HUPPAAL.getProject().getComponents().forEach(component -> {
                                    if (component.getEdges().contains(nearable)) {
                                        openComponent[0] = component;
                                    }
                                });
                            } else if (nearable instanceof SubComponent) { // We are pressing a subcomponent, find the subcomponent and open the corresponding component
                                HUPPAAL.getProject().getComponents().forEach(component -> {
                                    if (component.getSubComponents().contains(nearable)) {
                                        openComponent[0] = component;
                                    }
                                });
                            }

                            if (openComponent[0] != null) {
                                if (!CanvasController.getActiveComponent().equals(openComponent[0])) {
                                    SelectHelper.elementsToBeSelected = FXCollections.observableArrayList();
                                    CanvasController.setActiveComponent(openComponent[0]);
                                }

                                SelectHelper.clearSelectedElements();
                                SelectHelper.select(nearable);
                            }
                        });
                    }

                    nearLabels.getChildren().add(newNearLabel);

                    final Region spacer = new Region();
                    spacer.setMinWidth(10);

                    nearLabels.getChildren().add(spacer);
                }
            }

            final Label nearLabel = (Label) lookup("#nearLabel");
            nearLabel.setText(nearString);
        };

        // Run the listener now
        listener.invalidated(null);

        // Whenever the list is updated
        message.getNearables().addListener(listener);
    }

}
