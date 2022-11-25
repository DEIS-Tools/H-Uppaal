package dk.cs.aau.huppaal.presentations.util;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;


public class PresentationFxmlLoader {
    public static <R> R load(String resource) {
        return load("../" + resource, PresentationFxmlLoader.class);
    }
    public static <T,R> R load(String resource, Class<T> clazz) {
        try {
            var url = clazz.getResource(resource);
            if(url == null)
                throw new NullPointerException("Could not find resource: '%s'".formatted(resource));
            return FXMLLoader.load(url);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T,R> R loadSetRoot(String resource, T root) {
        try {
            var url = PresentationFxmlLoader.class.getResource("../" + resource);
            if(url == null)
                throw new NullPointerException("Could not find resource: '%s'".formatted(resource));
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            fxmlLoader.setLocation(url);
            fxmlLoader.setRoot(root);
            fxmlLoader.load(url.openStream());
            return fxmlLoader.getController();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T,R> R loadSetRootGetElement(String resource, T root) {
        try {
            var url = PresentationFxmlLoader.class.getResource("../" + resource);
            if(url == null)
                throw new NullPointerException("Could not find resource: '%s'".formatted(resource));
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            fxmlLoader.setLocation(url);
            fxmlLoader.setRoot(root);
            return fxmlLoader.load(url.openStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
