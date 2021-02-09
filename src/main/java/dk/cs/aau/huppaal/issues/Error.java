package dk.cs.aau.huppaal.issues;

import javafx.beans.Observable;
import javafx.scene.Node;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import java.util.function.Predicate;

public class Error<T extends Node> extends Issue<T> {

    @Override
    protected GoogleMaterialDesignIcons getIcon() {
        return GoogleMaterialDesignIcons.ERROR;
    }

    public Error(final Predicate<T> presentPredicate, final T subject, final Observable... observables) {
        super(presentPredicate, subject, observables);
    }
}
