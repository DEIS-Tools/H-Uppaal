package SW9.model_canvas;

import SW9.utility.helpers.MouseTrackable;

public interface Removable extends MouseTrackable {

    public boolean select();

    public void deselect();

    public void remove();

    public void reAdd();
}
