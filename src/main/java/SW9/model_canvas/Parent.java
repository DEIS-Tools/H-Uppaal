package SW9.model_canvas;

import javafx.scene.Node;

public class Parent extends javafx.scene.Parent implements IParent {
    @Override
    public void addChild(Node child) {
        getChildren().add(child);
    }

    @Override
    public void removeChild(Node child) {
        getChildren().remove(child);
    }

    @Override
    public void addChildren(Node... children) {
        getChildren().addAll(children);
    }

}
