package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Edge;
import dk.cs.aau.huppaal.abstractions.Location;
import dk.cs.aau.huppaal.utility.colors.Color;
import dk.cs.aau.huppaal.utility.helpers.SelectHelper;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;


public class SpotlightSearchResultPresentation extends HBox {
    public SpotlightSearchResultPresentation(Component parent, Edge edge) {
        this("gmi-arrow-right-alt", edge.generatePeakyString());
        initializeContext(parent.getName());
        setOnMouseClicked((e) -> {
            if(e.getClickCount() >= 2) {
                SelectHelper.selectComponent(parent.getName());
                SelectHelper.select(edge);
            }
        });
    }

    public SpotlightSearchResultPresentation(Component parent, Location location) {
        this("gmi-adjust", location.getMostDescriptiveIdentifier());
        initializeContext(parent.getName());
        setOnMouseClicked((e) -> {
            if(e.getClickCount() >= 2) {
                SelectHelper.selectComponent(parent.getName());
                SelectHelper.select(location);
            }
        });
    }

    public SpotlightSearchResultPresentation(Component component) {
        this("gmi-branding-watermark", component.getName());
        setOnMouseClicked((e) -> {
            if(e.getClickCount() >= 2)
                SelectHelper.selectComponent(component.getName());
        });
    }

    public SpotlightSearchResultPresentation(String icon, String name) {
        initializeIcon(icon);
        initializeName(name);
        initializeHover();
    }

    private void initializeIcon(String icon) {
        var fi = new FontIcon();
        fi.setIconLiteral(icon);
        getChildren().add(fi);
    }

    private void initializeName(String name) {
        var l = new Label(name);
        getChildren().add(l);
    }

    private void initializeHover() {
        setBackground(Background.fill(Color.GREY.getColor(Color.Intensity.I50)));
        hoverProperty().addListener((e,o,n) -> {
            if(n) {
                setBackground(Background.fill(Color.BLUE.getColor(Color.Intensity.I50)));
            } else {
                setBackground(Background.fill(Color.GREY.getColor(Color.Intensity.I50)));
            }
        });
    }

    private void initializeContext(String context) {
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        var l = new Label(context);
        l.setStyle("-fx-text-fill: #323232");
        getChildren().add(l);
    }
}
