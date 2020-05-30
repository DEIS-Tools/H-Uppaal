package dk.cs.aau.huppaal.backend;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;

import java.io.File;

public final class UPPAALDriverManager {

    private static IUPPAALDriver instance = null;
    private static String uppaalFilePath = HUPPAAL.preferences.get("uppaalLocation", "");

    private UPPAALDriverManager(){}

    public static synchronized IUPPAALDriver getInstance(){

        //If the instance is null this instantiates the correct IUPPAALDriver class
        if(instance == null){
            File serverFile = new File(uppaalFilePath);
            if(serverFile.exists()){
                instance = new UPPAALDriver(serverFile);
            } else {
                if(CodeAnalysis.getWarnings().contains(new CodeAnalysis.Message("Please set the UPPAAL server location through the 'Preferences' tab.\n" +
                        "Make sure to have UPPAAL installed. This can be done at uppaal.org", CodeAnalysis.MessageType.WARNING))){
                    HUPPAAL.showToast("What");
                } else{
                    CodeAnalysis.addMessage(null, new CodeAnalysis.Message("Please set the UPPAAL server location through the 'Preferences' tab.\n" +
                            "Make sure to have UPPAAL installed. This can be done at uppaal.org", CodeAnalysis.MessageType.WARNING));
                }
                instance = new DummyUPPAALDriver();
            }
        }

        return instance;
    }

    public static String getUppaalFilePath() {
        return uppaalFilePath;
    }

    public static void setUppaalFilePath(String uppaalFilePath) {
        instance = null;
        HUPPAAL.uppaalDriverUpdated();
        HUPPAAL.preferences.put("uppaalLocation", uppaalFilePath);
        UPPAALDriverManager.uppaalFilePath = uppaalFilePath;
    }
}
