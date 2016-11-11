package SW9.abstractions;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Project {

    private final ObservableList<Query> queries = FXCollections.observableArrayList();
    private final ObservableList<Component> components = FXCollections.observableArrayList();

    public Project() {

    }

    public ObservableList<Query> getQueries() {
        return queries;
    }

    public ObservableList<Component> getComponents() {
        return components;
    }

}
