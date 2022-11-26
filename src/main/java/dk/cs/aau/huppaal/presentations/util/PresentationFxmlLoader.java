package dk.cs.aau.huppaal.presentations.util;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;


public class PresentationFxmlLoader {
    public static <T,R> R load(String resource, T that) {
        try {
            var url = that.getClass().getResource(resource);
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(url);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            fxmlLoader.load(url.openStream());
            return fxmlLoader.getController();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T,R> R loadSetRoot(String resource, T that) {
        return loadSetRoot(resource, that, that);
    }

    public static <T,R> R loadSetRoot(String resource, T that, T root) {
        try {
            var url = that.getClass().getResource(resource);
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(url);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            fxmlLoader.setRoot(root);
            fxmlLoader.load(url.openStream());
            return fxmlLoader.getController();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
