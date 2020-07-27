package dk.cs.aau.huppaal.backend;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;

public final class UPPAALDriverManager {

    private static IUPPAALDriver instance = null;
    private static final SimpleStringProperty uppaalFilePath = new SimpleStringProperty(HUPPAAL.preferences.get("uppaalLocation", ""));

    private UPPAALDriverManager(){}

    public static synchronized IUPPAALDriver getInstance(){

        //If the instance is null this instantiates the correct IUPPAALDriver class
        if(instance == null){
            File serverFile = new File(uppaalFilePath.getValue());
            if(serverFile.exists()){
                instance = new UPPAALDriver(serverFile);
            } else {
                uppaalFilePath.set("dummy");
                instance = new DummyUPPAALDriver();
            }
        }

        return instance;
    }

    public static SimpleStringProperty getUppalFilePathProperty(){
        return uppaalFilePath;
    }

    public static String getUppaalFilePath() {
        return uppaalFilePath.getValue();
    }

    public static void setUppaalFilePath(String filePath) {
        //Set the instance to null to allow the correct UPPAALDriver to be instantiated
        //Todo: Insert check to see if the new value points to a UPPAAL server file
        instance = null;

        //Update uppaalFilePath and save the new value to preferences
        uppaalFilePath.set(filePath);
        HUPPAAL.preferences.put("uppaalLocation", filePath);

        //Let HUPPAAL know that the UPPAALDriver have been updated
        HUPPAAL.uppaalDriverUpdated();
    }
}
