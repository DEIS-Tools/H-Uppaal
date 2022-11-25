package dk.cs.aau.huppaal.presentations;

import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Edge;
import dk.cs.aau.huppaal.abstractions.Location;
import dk.cs.aau.huppaal.utility.colors.Color;
import dk.cs.aau.huppaal.utility.helpers.SelectHelper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;


public class SpotlightSearchResultPresentation extends HBox {
    private Runnable clickEvent;
    private Optional<Runnable> onClickEffect = Optional.empty();

    public SpotlightSearchResultPresentation(Component parent, Edge edge) {
        this("gmi-near-me", Color.GREEN.getColor(Color.Intensity.I800), edge.generatePeakyString());
        initializeContext(parent.getName());
        this.clickEvent = () -> {
            SelectHelper.selectComponent(parent.getName());
            SelectHelper.select(edge);
        };
        setOnMouseClicked((e) -> {
            if(e.getClickCount() >= 2)
                click();
        });
    }

    public SpotlightSearchResultPresentation(Component parent, Location location) {
        this("gmi-adjust", Color.PURPLE.getColor(Color.Intensity.I800), location.getMostDescriptiveIdentifier());
        initializeContext(parent.getName());
        this.clickEvent = () -> {
            SelectHelper.selectComponent(parent.getName());
            SelectHelper.select(location);
        };
        setOnMouseClicked((e) -> {
            if(e.getClickCount() >= 2)
                click();
        });
    }

    public SpotlightSearchResultPresentation(Component component) {
        this("gmi-branding-watermark", Color.BLUE.getColor(Color.Intensity.I800), component.getName());
        this.clickEvent = () -> SelectHelper.selectComponent(component.getName());
        setOnMouseClicked((e) -> {
            if(e.getClickCount() >= 2)
                click();
        });
    }

    private SpotlightSearchResultPresentation(String icon, javafx.scene.paint.Color iconColor, String name) {
        initializeIcon(icon, iconColor);
        initializeName(name);
        initializeHover();
        initializeSpacing();
    }

    private void initializeIcon(String icon, javafx.scene.paint.Color color) {
        var fi = new FontIcon();
        fi.setIconSize(15);
        fi.setIconLiteral(icon);
        fi.setIconColor(color);
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

    private void initializeSpacing() {
        setSpacing(10);
        setPadding(new Insets(5,10,5,10));
    }

    private void initializeContext(String context) {
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);
        var l = new Label(context);
        l.setTextFill(Color.GREY_BLUE.getColor(Color.Intensity.I300));
        getChildren().add(l);
    }

    public void click() {
        clickEvent.run();
        onClickEffect.ifPresent(Runnable::run);
    }

    public SpotlightSearchResultPresentation withClickEffect(Runnable r) {
        onClickEffect = Optional.ofNullable(r);
        return this;
    }
}
