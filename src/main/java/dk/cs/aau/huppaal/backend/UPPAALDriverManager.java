package dk.cs.aau.huppaal.backend;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;

import java.io.File;

public final class UPPAALDriverManager {

    private static IUPPAALDriver instance = null;
    private static String uppaalFilePath = ""; //HUPPAAL.preferences.get("uppaalLocation", "");

    private UPPAALDriverManager(){}

    public static synchronized IUPPAALDriver getInstance(){

        //If the instance is null this instantiates the correct IUPPAALDriver class
        if(instance == null){
            File serverFile = new File(uppaalFilePath);
            if(serverFile.exists()){
                CodeAnalysis.removeMessage(null, new CodeAnalysis.Message("Please set the UPPAAL server location through the 'Preferences' tab.\n" +
                        "Make sure to have UPPAAL installed. This can be done at uppaal.org", CodeAnalysis.MessageType.WARNING));
                instance = new UPPAALDriver(serverFile);
                //Todo: the list of warnings is updated, but the Interface does not represent this change
            } else {
                CodeAnalysis.addMessage(null, new CodeAnalysis.Message("Please set the UPPAAL server location through the 'Preferences' tab.\n" +
                            "Make sure to have UPPAAL installed. This can be done at uppaal.org", CodeAnalysis.MessageType.WARNING));
                instance = new DummyUPPAALDriver();
            }
        }

        return instance;
    }

    public static String getUppaalFilePath() {
        return uppaalFilePath;
    }

    public static void setUppaalFilePath(String filePath) {
        //Set the instance to null to allow the correct UPPAALDriver to be instantiated
        instance = null;

        //Update uppaalFilePath and save the new value to preferences
        uppaalFilePath = filePath;
        HUPPAAL.preferences.put("uppaalLocation", filePath);

        //Let HUPPAAL know that the UPPAALDriver have been updated
        HUPPAAL.uppaalDriverUpdated();
    }
}
