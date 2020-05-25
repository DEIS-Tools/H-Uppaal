package dk.cs.aau.huppaal.backend;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.code_analysis.CodeAnalysis;

import java.io.File;

public final class UPPAALDriverManager {

    public static boolean isServerCheckingEnabled = true;
    private static IUPPAALDriver instance = null;

    private UPPAALDriverManager(){}

    public static synchronized IUPPAALDriver getInstance(){

        //If the instance is null this instantiates the correct IUPPAALDriver class
        if(instance == null){
            File serverFile = findServerFile("server");
            if(serverFile.exists()){
                if(isServerCheckingEnabled) {
                    //If the UPPAAL server file exists and server computation is on
                    instance = new UPPAALDriver(serverFile);
                } else {
                    //If the UPPAAL server file exists and server computation is off
                    instance = new LocalUPPAALDriver();
                }
            } else {
                //If the UPPAAL server file does not exist
                CodeAnalysis.addMessage(null, new CodeAnalysis.Message("The UPPAAL server file: '" + UPPAALDriverManager.getServerFilePath("server") + "' does not exist.\nMake sure to have UPPAAL installed and the binaries copied to the location.", CodeAnalysis.MessageType.WARNING));
                instance = new DummyUPPAALDriver(getServerFilePath("server"));
            }
        }

        return instance;
    }

    private static File findServerFile(final String serverName) {
        return new File(getServerFilePath(serverName));
    }

    public static String getServerFilePath(final String serverName){
        final String os = System.getProperty("os.name");

        if (os.contains("Mac")) {
            return HUPPAAL.serverDirectory + File.separator + "bin-MacOS" + File.separator + serverName;
        } else if (os.contains("Linux")) {
            return HUPPAAL.serverDirectory + File.separator + "bin-Linux" + File.separator + serverName;
        } else {
            return HUPPAAL.serverDirectory + File.separator + "bin-Win32" + File.separator + serverName + ".exe";
        }
    }
}
