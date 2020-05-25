package dk.cs.aau.huppaal.backend;

import dk.cs.aau.huppaal.HUPPAAL;

import java.io.File;

public final class UPPAALDriverManager {
    private static IUPPAALDriver instance = null;

    private UPPAALDriverManager(){}

    public static synchronized IUPPAALDriver getInstance(){
        //If the instance is null this instantiates the correct IUPPAALDriver class
        if(instance == null){
            File serverFile = findServerFile("server");
            if(serverFile.exists()){
                if(true) {
                    instance = new UPPAALDriver(serverFile);
                } else {
                    instance = new LocalUPPAALDriver();
                }
            } else {
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
