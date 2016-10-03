package SW9.model_canvas;

import SW9.utility.DragHelper;

public interface Selectable extends DragHelper.Draggable, IChild {

    public void select();

    public void deselect();

    public void remove();

}
