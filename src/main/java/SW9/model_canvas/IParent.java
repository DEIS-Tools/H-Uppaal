package SW9.model_canvas;

import javafx.scene.Node;

public interface IParent {

    void addChild(final Node child);

    void removeChild(final Node child);

    void addChildren(final Node... children);
}
