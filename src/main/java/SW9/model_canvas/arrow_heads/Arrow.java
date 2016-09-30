package SW9.model_canvas.arrow_heads;

import SW9.model_canvas.Parent;
import javafx.scene.shape.Line;


public class Arrow extends Parent {

    private ArrowHead head;
    private Line tail;

    public Arrow(final ArrowHead head, final Line tail) {
        this.head = head;
        this.tail = tail;

        // Add the head to the arrow
        addChild(head);
        addChild(tail);
    }

    public final Line getTail() {
        return tail;
    }

    public final ArrowHead getHead() {
        return head;
    }

}
