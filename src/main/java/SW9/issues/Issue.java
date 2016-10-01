package SW9.issues;

import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.paint.Color;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;

import java.util.function.Predicate;

public abstract class Issue<T> {

    private String message = null;
    private BooleanBinding presentProperty;

    // Subclasses will override this, to provided us with the correct icon to use
    protected abstract GoogleMaterialDesignIcons getIcon();

    public Issue(final Predicate<T> presentPredicate, final T subject, final Observable... observables) {
        presentProperty = new BooleanBinding() {
            {
                // Bind to the provided observables (which may influence the "present" property
                super.bind(observables);
            }

            @Override
            protected boolean computeValue() {
                return presentPredicate.test(subject);
            }
        };
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public Boolean isPresent() {
        return presentProperty.get();
    }

    public ObservableBooleanValue presentPropertyProperty() {
        return presentProperty;
    }

    public IconNode generateIconNode() {
        final IconNode iconNode = new IconNode();

        // Set the style of the icon
        iconNode.setFill(Color.GRAY);
        iconNode.iconCodeProperty().set(getIcon());

        // The icon should only be visible when it is present
        iconNode.visibleProperty().bind(presentProperty);

        return iconNode;
    }
}
