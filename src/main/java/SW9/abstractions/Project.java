package SW9.abstractions;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Project {

    private final ObservableList<Query> queries = FXCollections.observableArrayList();
    private final ObservableList<Component> components = FXCollections.observableArrayList();
    private final ObjectProperty<Component> mainComponent = new SimpleObjectProperty<>();

    public Project() {

    }

    public ObservableList<Query> getQueries() {
        return queries;
    }

    public ObservableList<Component> getComponents() {
        return components;
    }

    public Component getMainComponent() {
        return mainComponent.get();
    }

    public ObjectProperty<Component> mainComponentProperty() {
        return mainComponent;
    }

    public void setMainComponent(final Component mainComponent) {
        this.mainComponent.set(mainComponent);
    }

}
