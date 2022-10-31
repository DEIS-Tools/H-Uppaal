import dk.cs.aau.huppaal.BuildConfig;

public class HUPPAAL {
    //For JavaFX Runtime Components are Missing https://edencoding.com/runtime-components-error/ - 2021-02-09
	public static void main(String[] args) {
        System.out.printf("Welcome to %s version v%s+%s%n", BuildConfig.NAME, BuildConfig.VERSION, BuildConfig.COMMIT_SHA_SHORT);
        dk.cs.aau.huppaal.HUPPAAL.main(args);
    }
}
