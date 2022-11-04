package dk.cs.aau.huppaal.presentations;

import com.google.gson.Gson;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.controllers.RunConfigurationEditorController;
import dk.cs.aau.huppaal.runconfig.RunConfiguration;
import dk.cs.aau.huppaal.runconfig.RunConfigurationPreferencesKeys;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class RunConfigurationEditorPresentation extends VBox {
    private final RunConfigurationEditorController controller;
    public final Parent parent;
    private final Stage stage;
    private final Gson gson;
    private Runnable onRunConfigsSaved = null;

    public RunConfigurationEditorPresentation(Stage stage) {
        try {
            var location = this.getClass().getResource("RunConfigurationEditorPresentation.fxml");
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
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
        controller.addNewRunConfigurationButton.setOnAction(e ->
                controller.savedConfigurationsList.getItems().add(
                        new RunConfiguration("new config", "", new ArrayList<>(), HUPPAAL.projectDirectory.get())));
        controller.removeSelectedRunConfigurationButton.setOnAction(e ->
                getCurrentlySelectedConfiguration().ifPresent(runConfiguration ->
                        controller.savedConfigurationsList.getItems().remove(runConfiguration)));
    }

    private void initializeRunConfigurationSelectionChange() {
        controller.savedConfigurationsList.setOnMouseClicked(e -> {
            var c = controller.savedConfigurationsList.getSelectionModel().getSelectedItem();
            if(c != null)
                initializeTextFields(c);
        });
    }

    private void initializeTextFields(RunConfiguration configuration) {
        controller.nameText.setText(configuration.name);
        controller.commandText.setText(configuration.program);
        controller.execDirText.setText(configuration.executionDir);
        if (!configuration.arguments.isEmpty())
            controller.argumentsText.setText(configuration.arguments.get(0)); // TODO: the argumentsText field should be a list of strings
        else
            controller.argumentsText.setText("");

        controller.nameText.textProperty().addListener((b, o, n) -> getCurrentlySelectedConfiguration().ifPresent(c -> c.name = n));
        controller.commandText.textProperty().addListener((b, o, n) -> getCurrentlySelectedConfiguration().ifPresent(c -> c.program = n));
        controller.execDirText.textProperty().addListener((b, o, n) -> getCurrentlySelectedConfiguration().ifPresent(c -> c.executionDir = n));
        controller.argumentsText.textProperty().addListener((b, o, n) -> {
            var c = controller.savedConfigurationsList.getSelectionModel().getSelectedItem();
            if(!c.arguments.isEmpty()) // TODO: the arguemtnsText field should be a list of strings
                c.arguments.clear();
            c.arguments.add(n);
        });
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
