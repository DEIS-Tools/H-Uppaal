package dk.cs.aau.huppaal.utility.helpers;

import dk.cs.aau.huppaal.utility.mouse.MouseTracker;

public interface MouseTrackable extends LocationAware {
    MouseTracker getMouseTracker();
}
