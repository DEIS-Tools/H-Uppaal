package dk.cs.aau.huppaal.backend;

public final class UPPAALDriverManager {
    private static IUPPAALDriver instance = null;

    private UPPAALDriverManager(){}

    public static synchronized IUPPAALDriver getInstance(){
        if(instance == null){
            //Todo: find better way to check if the server file exists
            instance = new UPPAALDriver();
            if(!instance.getServerFile().exists()){
                instance = new DummyUPPAALDriver();
            } else {
                //Todo: handle checks set to local only (possibly by adding a variable that can be set)
                if(false){
                    instance = new LocalUPPAALDriver();
                }
            }
        }

        return instance;
    }
}
