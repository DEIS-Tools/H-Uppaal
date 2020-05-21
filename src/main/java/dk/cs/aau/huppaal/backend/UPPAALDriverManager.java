package dk.cs.aau.huppaal.backend;

public final class UPPAALDriverManager {
    private static IUPPAALDriver instance = null;

    private UPPAALDriverManager(){}

    public static synchronized IUPPAALDriver getInstance(){
        if(instance == null){
            instance = new UPPAALDriver();
            if(!instance.getServerFile().exists()){
                instance = new DummyUPPAALDriver();
            }
        }

        return instance;
    }
}
