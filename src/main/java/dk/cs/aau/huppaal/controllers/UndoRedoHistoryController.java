package dk.cs.aau.huppaal.controllers;

import dk.cs.aau.huppaal.presentations.UndoRedoHistoryEntryPresentation;
import dk.cs.aau.huppaal.utility.UndoRedoStack;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class UndoRedoHistoryController implements Initializable {

    public VBox redoStackContainer;
    public VBox undoStackContainer;
    public VBox youAreHere;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        initializeYouAreHereIndicator();

        UndoRedoStack.setDebugRunnable((undoStack, redoStack) -> {
            undoStackContainer.getChildren().removeIf(node -> true);
            redoStackContainer.getChildren().removeIf(node -> true);

            undoStack.forEach(command -> {
                final UndoRedoHistoryEntryPresentation youAreHereIndicator = new UndoRedoHistoryEntryPresentation(command, true);
                undoStackContainer.getChildren().add(youAreHereIndicator);
                youAreHereIndicator.toBack();
            });

            redoStack.forEach(command -> {
                final UndoRedoHistoryEntryPresentation youAreHereIndicator = new UndoRedoHistoryEntryPresentation(command, false);
                redoStackContainer.getChildren().add(youAreHereIndicator);
            });
        });

    }

    private void initializeYouAreHereIndicator() {
        final UndoRedoHistoryEntryPresentation youAreHereIndicator = new UndoRedoHistoryEntryPresentation(null, false);
        youAreHere.getChildren().add(youAreHereIndicator);
    }

}
