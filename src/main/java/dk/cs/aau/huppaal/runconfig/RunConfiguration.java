package dk.cs.aau.huppaal.runconfig;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class RunConfiguration {
    public String name;
    public String program;
    public List<String> arguments;
    public String executionDir;
    public static final TypeToken<ArrayList<RunConfiguration>> listTypeToken = new TypeToken<>() {};

    public RunConfiguration(String name, String program, List<String> arguments, String executionDir) {
        this.name = name;
        this.program = program;
        this.arguments = arguments;
        this.executionDir = executionDir;
    }

    @Override
    public String toString() {
        return name;
    }
}
