package SW9.model_canvas;

import javafx.scene.Node;

public interface IParent {

    public void addChild(final Node child);

    public void removeChild(final Node child);

    public void addChildren(final Node... children);
}
