package dk.cs.aau.huppaal.runconfig;

import com.jfoenix.controls.JFXButton;

import java.util.Optional;

public record RunConfigurationButton(
        Optional<RunConfiguration> runConfiguration,
        JFXButton button
){
    @Override
    public String toString() {
        if(runConfiguration.isPresent())
            return runConfiguration.get().name;
        return button.textProperty().get();
    }
}
