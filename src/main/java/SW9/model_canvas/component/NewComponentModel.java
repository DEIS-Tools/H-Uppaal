package SW9.model_canvas.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NewComponentModel {

    private final StringProperty label;
    private final BooleanProperty isSelected;

    public NewComponentModel() {
        this(null, false);
    }

    public NewComponentModel(final String label) {
        this(label, false);
    }

    public NewComponentModel(final String label, final boolean isSelected) {
        this.label = new SimpleStringProperty(label);
        this.isSelected = new SimpleBooleanProperty(isSelected);
    }

    public String getLabel() {
        return label.get();
    }

    public StringProperty labelProperty() {
        return label;
    }

    public void setLabel(final String label) {
        this.label.set(label);
    }

    public boolean isIsSelected() {
        return isSelected.get();
    }

    public BooleanProperty isSelectedProperty() {
        return isSelected;
    }

    public void setIsSelected(final boolean isSelected) {
        this.isSelected.set(isSelected);
    }

}
