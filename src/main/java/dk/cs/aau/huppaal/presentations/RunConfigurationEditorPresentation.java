package dk.cs.aau.huppaal.presentations;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.RunConfigurationEditorController;
import dk.cs.aau.huppaal.runconfig.RunConfiguration;
import dk.cs.aau.huppaal.runconfig.RunConfigurationPreferencesKeys;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class RunConfigurationEditorPresentation extends BorderPane {
    private final RunConfigurationEditorController controller;
    public final Parent parent;
    private final Stage stage;
    private final Gson gson;
    private Runnable onRunConfigsSaved = null;
    private final TextField nameField = new TextField();
    private final JFXButton programFieldButton = new JFXButton("File...");
    private final FileChooser programField = new FileChooser(); // TODO: should be a file-picker (check for executable)
    private final TextField argumentsField = new TextField(); // TODO: should be a list-editor
    private final TextField execDirField = new TextField();  // TODO: should be a folder-picker (DirectoryChooser)

    public RunConfigurationEditorPresentation(Stage stage) {
        try {
            var location = this.getClass().getResource("RunConfigurationEditorPresentation.fxml");
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            fxmlLoader.setRoot(this);
            this.stage = stage;
            this.gson = new Gson();
            parent = fxmlLoader.load();
            controller = fxmlLoader.getController();
            initialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void setOnRunConfigsSaved(Runnable r) {
        onRunConfigsSaved = r;
    }

    private void initialize() {
        initializeOkCancelApplyButtons();
        initializeAddAndRemoveButtons();
        initializeRunConfigurationSelectionChange();
        initializePropertyGridPane();
    }

    private void initializePropertyGridPane() {
        // TODO: For each (reflection) field on a RunConfiguration
        // TODO: Depending on the field, use a different javafx thingy to edit the value
        var rc = controller.propertyGridPane.getRowCount();
        // Add name text field
        nameField.textProperty().addListener((b,o,n) -> getCurrentlySelectedConfiguration().ifPresent(c -> c.name = n));
        controller.propertyGridPane.addRow(rc++, new StackPane(new Label("name")), new StackPane(nameField));

        // Add program text field
        //programField.textProperty().addListener((b,o,n) -> getCurrentlySelectedConfiguration().ifPresent(c -> c.program = n));
        programFieldButton.setOnAction(e -> {
            var f = programField.showOpenDialog(getScene().getWindow());
            if(!(f.exists() && f.isFile() && f.canExecute())) {
                HUPPAAL.showToast("Program file must exist, be a file and executable");
                return;
            }
            getCurrentlySelectedConfiguration().ifPresent(c -> c.program = f.getAbsolutePath());
        });

        controller.propertyGridPane.addRow(rc++, new StackPane(new Label("program")), new StackPane(programFieldButton));

        // Add arguments text field
        argumentsField.textProperty().addListener((b,o,n) -> getCurrentlySelectedConfiguration().ifPresent(c -> {
            if(!c.arguments.isEmpty())
                c.arguments.clear();
            c.arguments.add(n);
        }));
        controller.propertyGridPane.addRow(rc++, new StackPane(new Label("arguments")), new StackPane(argumentsField));

        // Add execution dir text field
        execDirField.textProperty().addListener((b,o,n) -> getCurrentlySelectedConfiguration().ifPresent(c -> c.executionDir = n));
        controller.propertyGridPane.addRow(rc++, new StackPane(new Label("execution directory")), new StackPane(execDirField));

        // TODO: Add environment variables editor
    }

    private void populatePropertyGridPane(RunConfiguration r) {
        nameField.setText(r.name);
        if(!Strings.isNullOrEmpty(r.program))
            programFieldButton.setText(r.program);
        else
            programFieldButton.setText("File");
        argumentsField.setText(String.join(" ", r.arguments));
        execDirField.setText(r.executionDir);
    }

    private void initializeOkCancelApplyButtons() {
        controller.cancelButton.setOnAction(e -> closeWindow());
        controller.applyButton.setOnAction(e -> saveRunConfigurations());
        controller.okButton.setOnAction(e -> {
            saveRunConfigurations();
            closeWindow();
        });
    }

    private void initializeAddAndRemoveButtons() {
        controller.addNewRunConfigurationButton.setOnMouseClicked(e ->
                controller.savedConfigurationsList.getItems().add(
                        new RunConfiguration("new config", "", new ArrayList<>(), HUPPAAL.projectDirectory.get())));
        controller.removeSelectedRunConfigurationButton.setOnMouseClicked(e ->
                getCurrentlySelectedConfiguration().ifPresent(runConfiguration ->
                        controller.savedConfigurationsList.getItems().remove(runConfiguration)));
    }

    private void initializeRunConfigurationSelectionChange() {
        controller.savedConfigurationsList.setOnMouseClicked(e -> getCurrentlySelectedConfiguration().ifPresent(this::populatePropertyGridPane));
    }

    private Optional<RunConfiguration> getCurrentlySelectedConfiguration() {
        var c = controller.savedConfigurationsList.getSelectionModel().getSelectedItem();
        if(c == null)
            return Optional.empty();
        return Optional.of(c);
    }

    private void saveRunConfigurations() {
        HUPPAAL.preferences.put(RunConfigurationPreferencesKeys.ConfigurationsList, gson.toJson(controller.savedConfigurationsList.getItems()));
        HUPPAAL.showToast("Saved RunConfigurations");
        if(onRunConfigsSaved != null)
            onRunConfigsSaved.run();
    }

    private void closeWindow() {
        stage.close();
    }
}
