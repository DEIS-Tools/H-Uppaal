package SW9.model_canvas.component;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class NewComponentController {

    // The model that this controller listens to
    private NewComponentModel model;

    // Views that should be updated based on the model
    public CheckBox isSelectedView;
    public Label labelView;

    /*
     * Getters and setters below
     */

    public void setModel(final NewComponentModel model) {
        this.model = model;

        // Bind the label view to the model property
        labelView.textProperty().bind(model.labelProperty());
    }

    public NewComponentModel getModel() {
        return model;
    }
}
