package SW9.model_canvas;

import SW9.utility.DragHelper;

public interface Removable extends DragHelper.Draggable, IChild {

    public boolean select();

    public void deselect();

    public void remove();

    public void reAdd();
}