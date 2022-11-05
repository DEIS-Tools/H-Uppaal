package dk.cs.aau.huppaal.controllers;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRippler;
import dk.cs.aau.huppaal.BuildConfig;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.runconfig.RunConfiguration;
import dk.cs.aau.huppaal.runconfig.RunConfigurationPreferencesKeys;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RunConfigurationEditorController implements Initializable {
    public JFXListView<RunConfiguration> savedConfigurationsList;
    public Label versionLabel;
    public JFXButton okButton, cancelButton, applyButton;
    public JFXRippler addNewRunConfigurationButton, removeSelectedRunConfigurationButton;
    public GridPane propertyGridPane;

    public RunConfigurationEditorController() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSavedConfigurationsList();
        initializeVersionLabel();
    }

    private void initializeSavedConfigurationsList() {
        var runConfigsJson = HUPPAAL.preferences.get(RunConfigurationPreferencesKeys.ConfigurationsList, "[]");
        var currentConfig = HUPPAAL.preferences.get(RunConfigurationPreferencesKeys.CurrentlySelected, "");
        var gson = new Gson();
        List<RunConfiguration> runConfigurations = gson.fromJson(runConfigsJson, RunConfiguration.listTypeToken);
        savedConfigurationsList.getItems().addAll(runConfigurations);
        // TODO: set that c is selected if c.isPresent()
        // var c = savedConfigurationsList.getItems().stream().filter(e -> e.name().equals(currentConfig)).findAny();
    }

    private void initializeVersionLabel() {
        versionLabel.setText("v"+BuildConfig.VERSION+"+"+BuildConfig.COMMIT_SHA_SHORT);
    }
}
