package SW9.presentations;

import SW9.HUPPAAL;
import SW9.abstractions.Component;
import SW9.abstractions.Location;
import SW9.controllers.CanvasController;
import SW9.controllers.ComponentController;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.helpers.MouseTrackable;
import SW9.utility.helpers.SelectHelper;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import org.fxmisc.richtext.StyleSpans;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.reactfx.util.TriConsumer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static SW9.presentations.CanvasPresentation.GRID_SIZE;
import static javafx.util.Duration.millis;

public class ComponentPresentation extends StackPane implements MouseTrackable, SelectHelper.Selectable {

    public final static double CORNER_SIZE = 4 * GRID_SIZE;
    public static final double TOOL_BAR_HEIGHT = CORNER_SIZE / 2;
    private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))" + "|(?<COMMENT><!--[^<>]+-->)");
    private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");
    private static final int GROUP_OPEN_BRACKET = 2;
    private static final int GROUP_ELEMENT_NAME = 3;
    private static final int GROUP_ATTRIBUTES_SECTION = 4;
    private static final int GROUP_CLOSE_BRACKET = 5;
    private static final int GROUP_ATTRIBUTE_NAME = 1;
    private static final int GROUP_EQUAL_SYMBOL = 2;
    private static final int GROUP_ATTRIBUTE_VALUE = 3;
    private static final String uppaalKeywords = "clock|chan|urgent|broadcast";
    private static final String cKeywords = "auto|bool|break|case|char|const|continue|default|do|double|else|enum|extern|float|for|goto|if|int|long|register|return|short|signed|sizeof|static|struct|switch|typedef|union|unsigned|void|volatile|while";
    private static final Pattern UPPAAL = Pattern.compile(""
            + "(" + uppaalKeywords + ")"
            + "|(" + cKeywords + ")"
            + "|(//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/)");
    private final ComponentController controller;
    private final List<BiConsumer<Color, Color.Intensity>> updateColorDelegates = new ArrayList<>();
    private LocationPresentation initialLocationPresentation = null;
    private LocationPresentation finalLocationPresentation = null;

    public ComponentPresentation() {
        this(new Component());
    }

    public ComponentPresentation(final Component component) {
        final URL location = this.getClass().getResource("ComponentPresentation.fxml");

        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

        try {
            fxmlLoader.setRoot(this);
            fxmlLoader.load(location.openStream());

            // Set the width and the height of the view to the values in the abstraction
            setMinWidth(component.getWidth());
            setMaxWidth(component.getWidth());
            setMinHeight(component.getHeight());
            setMaxHeight(component.getHeight());
            minHeightProperty().bindBidirectional(component.heightProperty());
            maxHeightProperty().bindBidirectional(component.heightProperty());
            minWidthProperty().bindBidirectional(component.widthProperty());
            maxWidthProperty().bindBidirectional(component.widthProperty());

            controller = fxmlLoader.getController();
            controller.setComponent(component);

            initializeDefaultLocationsContainer();

            // Initializer methods that is sensitive to width and height
            final Runnable onUpdateSize = () -> {
                initializeToolbar();
                initializeFrame();
                initializeInitialLocation();
                initializeFinalLocation();
                initializeBackground();
            };

            initializeName();
            initializeDragAnchors();
            onUpdateSize.run();
            initializeLocationTypeIndicatorArrows();


            // Re run initialisation on update of width and height property
            component.widthProperty().addListener(observable -> {
                onUpdateSize.run();
            });
            component.heightProperty().addListener(observable -> {
                onUpdateSize.run();
            });

            controller.declaration.textProperty().addListener((obs, oldText, newText) -> {
                controller.declaration.setStyleSpans(0, computeHighlighting(newText));
            });

        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void initializeLocationTypeIndicatorArrows() {

        // Initial indicator
        final Group initialLocationGuideContainer = controller.initialLocationGuideContainer;
        final Path initialLocationGuideArrow = controller.initialLocationGuideArrow;
        final Label initialLocationGuideLabel = controller.initialLocationGuideLabel;

        drawIndicatorArrow(initialLocationGuideArrow);
        initialLocationGuideContainer.setRotate(-45-90);
        initialLocationGuideContainer.setTranslateX(2.3 * GRID_SIZE);
        initialLocationGuideContainer.setTranslateY(3.8 * GRID_SIZE);
        initialLocationGuideLabel.setRotate(180);
        initialLocationGuideLabel.setTranslateY(10.5);
        initialLocationGuideContainer.toFront();
        initialLocationGuideContainer.setOpacity(0);

        // Final indicator
        final Group finalLocationGuideContainer = controller.finalLocationGuideContainer;
        final Path finalLocationGuideArrow = controller.finalLocationGuideArrow;
        final Label finalLocationGuideLabel = controller.finalLocationGuideLabel;

        drawIndicatorArrow(finalLocationGuideArrow);
        finalLocationGuideContainer.setRotate(45);
        finalLocationGuideContainer.setTranslateX(-2.3 * GRID_SIZE);
        finalLocationGuideContainer.setTranslateY(-3.8 * GRID_SIZE);
        finalLocationGuideLabel.setTranslateY(10.5);
        finalLocationGuideContainer.toFront();
        finalLocationGuideContainer.setOpacity(0);

        if (!controller.getComponent().isFirsTimeShown()) {
            initialLocationGuideContainer.setOpacity(1);
            finalLocationGuideContainer.setOpacity(1);
            playIndicatorAnimations(Location.Type.INITIAL, initialLocationGuideContainer);
            playIndicatorAnimations(Location.Type.FINAl, finalLocationGuideContainer);
            controller.getComponent().setFirsTimeShown(true);
        }


    }

    private void drawIndicatorArrow(final Path initialLocationGuideArrow) {
        final MoveTo i1 = new MoveTo(-1 * GRID_SIZE, GRID_SIZE);
        final LineTo i2 = new LineTo(4 * GRID_SIZE, GRID_SIZE);
        final LineTo i3 = new LineTo(4 * GRID_SIZE, 0);
        final LineTo i4 = new LineTo(6 * GRID_SIZE, 2 * GRID_SIZE);
        final LineTo i5 = new LineTo(4 * GRID_SIZE, 4 * GRID_SIZE);
        final LineTo i6 = new LineTo(4 * GRID_SIZE, 3 * GRID_SIZE);
        final LineTo i7 = new LineTo(-1 * GRID_SIZE, 3 * GRID_SIZE);
        final LineTo i8 = new LineTo(-1 * GRID_SIZE, GRID_SIZE);

        initialLocationGuideArrow.getElements().addAll(i1, i2, i3, i4, i5, i6, i7, i8);
        final Color componentColor = controller.getComponent().getColor();
        final Color.Intensity componentColorIntensity = controller.getComponent().getColorIntensity();

        initialLocationGuideArrow.setFill(componentColor.getColor(componentColorIntensity.next(-1)));
        initialLocationGuideArrow.setStroke(componentColor.getColor(componentColorIntensity.next(2)));
    }

    private void playIndicatorAnimations(final Location.Type type, final Group container) {
        final Timeline moveAnimation = new Timeline();
        final Timeline disappearAnimation = new Timeline();

        final double startX = container.getTranslateX();
        final double startY = container.getTranslateY();

        final Interpolator interpolator = Interpolator.SPLINE(0.645, 0.045, 0.355, 1);

        double otherX = 0, otherY = 0;

        if(type.equals(Location.Type.INITIAL))
        {
            otherX = startX + 2 * GRID_SIZE;
            otherY = startY + 2 * GRID_SIZE;
        } else {
            otherX = startX - 2 * GRID_SIZE;
            otherY = startY - 2 * GRID_SIZE;
        }

        final KeyValue kvx1 = new KeyValue(container.translateXProperty(), otherX, interpolator);
        final KeyValue kvx2 = new KeyValue(container.translateXProperty(), startX, interpolator);

        final KeyValue kvy1 = new KeyValue(container.translateYProperty(), otherY, interpolator);
        final KeyValue kvy2 = new KeyValue(container.translateYProperty(), startY, interpolator);

        final List<KeyFrame> frames = new ArrayList<>();

        frames.add(new KeyFrame(millis(400), kvx2, kvy2));

        for(int i = 1; i < 6; i ++) {
            if(i % 2 == 1) {
                frames.add(new KeyFrame(millis(400 * (i + 1)), kvx2, kvy2));
            } else {
                frames.add(new KeyFrame(millis(400 * (i + 1)), kvx1, kvy1));
            }
        }

        moveAnimation.getKeyFrames().addAll(frames);

        final KeyValue kvPresent = new KeyValue(container.opacityProperty(), 1, interpolator);
        final KeyValue kvGone = new KeyValue(container.opacityProperty(), 0, interpolator);
        final KeyFrame kf1 = new KeyFrame(millis(5000), kvPresent);
        final KeyFrame kf2 = new KeyFrame(millis(5500), kvGone);
        disappearAnimation.getKeyFrames().addAll(kf1, kf2);


        moveAnimation.play();
        disappearAnimation.play();
    }

    public static StyleSpans<Collection<String>> computeHighlighting(final String text) {
        final Matcher matcher = UPPAAL.matcher(text);
        int lastKwEnd = 0;
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            if (matcher.group(1) != null) {
                spansBuilder.add(Collections.singleton("uppaal-keyword"), matcher.end(1) - matcher.start(1));
            } else if (matcher.group(2) != null) {
                spansBuilder.add(Collections.singleton("c-keyword"), matcher.end(2) - matcher.start(2));
            } else if (matcher.group(3) != null) {
                spansBuilder.add(Collections.singleton("comment"), matcher.end(3) - matcher.start(3));
            }

            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void initializeDragAnchors() {
        final Component component = controller.getComponent();
        final BooleanProperty wasResized = new SimpleBooleanProperty(false);

        // Bottom anchor
        final Rectangle bottomAnchor = controller.bottomAnchor;

        bottomAnchor.setCursor(Cursor.S_RESIZE);

        // Bind the place and size of bottom anchor
        bottomAnchor.widthProperty().bind(component.widthProperty().subtract(CORNER_SIZE));
        bottomAnchor.setHeight(5);

        final DoubleProperty prevY = new SimpleDoubleProperty();
        final DoubleProperty prevHeight = new SimpleDoubleProperty();

        final Supplier<Double> componentMinHeight = () -> {

            final DoubleProperty minHeight = new SimpleDoubleProperty(10 * GRID_SIZE);

            component.getSubComponents().forEach(subComponent -> {
                minHeight.set(Math.max(minHeight.doubleValue(), subComponent.getHeight() + subComponent.getY() + GRID_SIZE));
            });

            component.getLocations().forEach(location -> {
                minHeight.set(Math.max(minHeight.doubleValue(), location.getY() + GRID_SIZE * 2));
            });

            component.getEdges().forEach(edge -> {
                edge.getNails().forEach(nail -> {
                    minHeight.set(Math.max(minHeight.doubleValue(), nail.getY() + GRID_SIZE));
                });
            });

            component.getJorks().forEach(jork-> {
                minHeight.set(Math.max(minHeight.doubleValue(), jork.getY() + JorkPresentation.JORK_HEIGHT + GRID_SIZE));
            });

            return minHeight.get();
        };

        bottomAnchor.setOnMousePressed(event -> {
            prevY.set(event.getScreenY());
            prevHeight.set(component.getHeight());
        });

        bottomAnchor.setOnMouseDragged(event -> {
            double diff = event.getScreenY() - prevY.get();
            diff -= diff % GRID_SIZE;

            final double newHeight = prevHeight.get() + diff;
            final double minHeight = componentMinHeight.get();

            component.setHeight(Math.max(newHeight, minHeight));
            wasResized.set(true);
        });

        bottomAnchor.setOnMouseReleased(event -> {
            if (!wasResized.get()) return;
            final double previousHeight = prevHeight.doubleValue();
            final double currentHeight = component.getHeight();

            // If no difference do not save change
            if (previousHeight == currentHeight) return;

            UndoRedoStack.push(() -> { // Perform
                        component.setHeight(currentHeight);
                    }, () -> { // Undo
                        component.setHeight(previousHeight);
                    },
                    "Component height resized", "settings-overscan");

            wasResized.set(false);
        });

        // Right anchor
        final Rectangle rightAnchor = controller.rightAnchor;

        rightAnchor.setCursor(Cursor.E_RESIZE);

        // Bind the place and size of bottom anchor
        rightAnchor.setWidth(5);
        rightAnchor.heightProperty().bind(component.heightProperty().subtract(CORNER_SIZE));

        final DoubleProperty prevX = new SimpleDoubleProperty();
        final DoubleProperty prevWidth = new SimpleDoubleProperty();

        final Supplier<Double> componentMinWidth = () -> {

            final DoubleProperty minWidth = new SimpleDoubleProperty(10 * GRID_SIZE);

            component.getSubComponents().forEach(subComponent -> {
                minWidth.set(Math.max(minWidth.doubleValue(), subComponent.getWidth() + subComponent.getX() + GRID_SIZE));
            });

            component.getLocations().forEach(location -> {
                minWidth.set(Math.max(minWidth.doubleValue(), location.getX() + GRID_SIZE * 2));
            });

            component.getEdges().forEach(edge -> {
                edge.getNails().forEach(nail -> {
                    minWidth.set(Math.max(minWidth.doubleValue(), nail.getX() + GRID_SIZE));
                });
            });

            component.getJorks().forEach(jork-> {
                minWidth.set(Math.max(minWidth.doubleValue(), jork.getX() + JorkPresentation.JORK_WIDTH + GRID_SIZE));
            });

            return minWidth.get();
        };

        rightAnchor.setOnMousePressed(event -> {
            prevX.set(event.getScreenX());
            prevWidth.set(component.getWidth());
        });

        rightAnchor.setOnMouseDragged(event -> {
            double diff = event.getScreenX() - prevX.get();
            diff -= diff % GRID_SIZE;

            final double newWidth = prevWidth.get() + diff;
            final double minWidth = componentMinWidth.get();
            component.setWidth(Math.max(newWidth, minWidth));
            wasResized.set(true);
        });

        rightAnchor.setOnMouseReleased(event -> {
            if (!wasResized.get()) return;
            final double previousWidth = prevWidth.doubleValue();
            final double currentWidth = component.getWidth();

            // If no difference do not save change
            if (previousWidth == currentWidth) return;

            UndoRedoStack.push(() -> { // Perform
                        component.setWidth(currentWidth);
                    }, () -> { // Undo
                        component.setWidth(previousWidth);
                    },
                    "Component width resized", "settings-overscan");

            wasResized.set(false);
        });


    }

    private void initializeDefaultLocationsContainer() {
        if (initialLocationPresentation != null) {
            controller.defaultLocationsContainer.getChildren().remove(initialLocationPresentation);
        }

        if (finalLocationPresentation != null) {
            controller.defaultLocationsContainer.getChildren().remove(finalLocationPresentation);
        }

        // Instantiate views for the initial and final location
        final Component component = controller.getComponent();
        initialLocationPresentation = new LocationPresentation(component.getInitialLocation(), component);
        finalLocationPresentation = new LocationPresentation(component.getFinalLocation(), component);

        // Add the locations to the view
        controller.defaultLocationsContainer.getChildren().addAll(initialLocationPresentation, finalLocationPresentation);
    }

    private void initializeName() {
        final Component component = controller.getComponent();
        final BooleanProperty initialized = new SimpleBooleanProperty(false);

        controller.name.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !initialized.get()) {
                controller.root.requestFocus();
                initialized.setValue(true);
            }
        });

        // Set the text field to the name in the model, and bind the model to the text field
        controller.name.setText(component.getName());
        controller.name.textProperty().addListener((obs, oldName, newName) -> {
            component.nameProperty().unbind();
            component.setName(newName);
        });

        final Runnable updateColor = () -> {
            final Color color = component.getColor();
            final Color.Intensity colorIntensity = component.getColorIntensity();

            // Set the text color for the label
            controller.name.setStyle("-fx-text-fill: " + color.getTextColorRgbaString(colorIntensity) + ";");
            controller.name.setFocusColor(color.getTextColor(colorIntensity));
            controller.name.setUnFocusColor(javafx.scene.paint.Color.TRANSPARENT);
        };

        controller.getComponent().colorProperty().addListener(observable -> updateColor.run());
        updateColor.run();

        // Center the text vertically and aff a left padding of CORNER_SIZE
        controller.name.setPadding(new Insets(2, 0, 0, CORNER_SIZE));
        controller.name.setOnKeyPressed(CanvasController.getLeaveTextAreaKeyHandler());
    }

    private void initializeInitialLocation() {
        initialLocationPresentation.setLocation(controller.getComponent().getInitialLocation());
        initialLocationPresentation.layoutXProperty().unbind();
        initialLocationPresentation.layoutYProperty().unbind();
        initialLocationPresentation.setLayoutX(CORNER_SIZE / 2);
        initialLocationPresentation.setLayoutY(CORNER_SIZE / 2);

        StackPane.setAlignment(initialLocationPresentation, Pos.TOP_LEFT);
    }

    private void initializeFinalLocation() {
        final Component component = controller.getComponent();

        finalLocationPresentation.setLocation(component.getFinalLocation());
        finalLocationPresentation.layoutXProperty().unbind();
        finalLocationPresentation.layoutYProperty().unbind();
        finalLocationPresentation.setLayoutX(component.getWidth() - CORNER_SIZE / 2);
        finalLocationPresentation.setLayoutY(component.getHeight() - CORNER_SIZE / 2);

        StackPane.setAlignment(finalLocationPresentation, Pos.BOTTOM_RIGHT);
    }

    private void initializeToolbar() {
        final Component component = controller.getComponent();

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Set the background of the toolbar
            controller.toolbar.setBackground(new Background(new BackgroundFill(
                    newColor.getColor(newIntensity),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));

            // Set the icon color and rippler color of the toggleDeclarationButton
            controller.toggleDeclarationButton.setRipplerFill(newColor.getTextColor(newIntensity));

            controller.toolbar.setPrefHeight(TOOL_BAR_HEIGHT);
            controller.toggleDeclarationButton.setBackground(Background.EMPTY);
        };

        updateColorDelegates.add(updateColor);

        controller.getComponent().colorProperty().addListener(observable -> updateColor.accept(component.getColor(), component.getColorIntensity()));

        updateColor.accept(component.getColor(), component.getColorIntensity());

        // Set a hover effect for the controller.toggleDeclarationButton
        controller.toggleDeclarationButton.setOnMouseEntered(event -> controller.toggleDeclarationButton.setCursor(Cursor.HAND));
        controller.toggleDeclarationButton.setOnMouseExited(event -> controller.toggleDeclarationButton.setCursor(Cursor.DEFAULT));

    }

    private void initializeFrame() {
        final Component component = controller.getComponent();

        final Shape[] mask = new Shape[1];
        final Rectangle rectangle = new Rectangle(component.getWidth(), component.getHeight());

        // Generate first corner (to subtract)
        final Polygon corner1 = new Polygon(
                0, 0,
                CORNER_SIZE + 2, 0,
                0, CORNER_SIZE + 2
        );

        // Generate second corner (to subtract)
        final Polygon corner2 = new Polygon(
                component.getWidth(), component.getHeight(),
                component.getWidth() - CORNER_SIZE - 2, component.getHeight(),
                component.getWidth(), component.getHeight() - CORNER_SIZE - 2
        );

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Mask the parent of the frame (will also mask the background)
            mask[0] = Path.subtract(rectangle, corner1);
            mask[0] = Path.subtract(mask[0], corner2);
            controller.frame.setClip(mask[0]);
            controller.background.setClip(Path.union(mask[0], mask[0]));
            controller.background.setOpacity(0.5);

            // Bind the missing lines that we cropped away
            controller.line1.setStartX(CORNER_SIZE);
            controller.line1.setStartY(0);
            controller.line1.setEndX(0);
            controller.line1.setEndY(CORNER_SIZE);
            controller.line1.setStroke(newColor.getColor(newIntensity.next(2)));
            controller.line1.setStrokeWidth(1.25);
            StackPane.setAlignment(controller.line1, Pos.TOP_LEFT);

            controller.line2.setStartX(CORNER_SIZE);
            controller.line2.setStartY(0);
            controller.line2.setEndX(0);
            controller.line2.setEndY(CORNER_SIZE);
            controller.line2.setStroke(newColor.getColor(newIntensity.next(2)));
            controller.line2.setStrokeWidth(1.25);
            StackPane.setAlignment(controller.line2, Pos.BOTTOM_RIGHT);

            // Set the stroke color to two shades darker
            controller.frame.setBorder(new Border(new BorderStroke(
                    newColor.getColor(newIntensity.next(2)),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(1),
                    Insets.EMPTY
            )));
        };

        updateColorDelegates.add(updateColor);

        component.colorProperty().addListener(observable -> {
            updateColor.accept(component.getColor(), component.getColorIntensity());
        });

        updateColor.accept(component.getColor(), component.getColorIntensity());
    }

    private void initializeBackground() {
        final Component component = controller.getComponent();

        // Bind the background width and height to the values in the model
        controller.background.widthProperty().bindBidirectional(component.widthProperty());
        controller.background.heightProperty().bindBidirectional(component.heightProperty());

        final BiConsumer<Color, Color.Intensity> updateColor = (newColor, newIntensity) -> {
            // Set the background color to the lightest possible version of the color
            controller.background.setFill(newColor.getColor(newIntensity.next(-10).next(2)));
        };

        updateColorDelegates.add(updateColor);

        component.colorProperty().addListener(observable -> {
            updateColor.accept(component.getColor(), component.getColorIntensity());
        });

        updateColor.accept(component.getColor(), component.getColorIntensity());
    }

    @Override
    public DoubleProperty xProperty() {
        return layoutXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return layoutYProperty();
    }

    @Override
    public double getX() {
        return xProperty().get();
    }

    @Override
    public double getY() {
        return yProperty().get();
    }

    @Override
    public MouseTracker getMouseTracker() {
        return controller.getMouseTracker();
    }

    @Override
    public void select() {
        updateColorDelegates.forEach(colorConsumer -> colorConsumer.accept(SelectHelper.SELECT_COLOR, SelectHelper.SELECT_COLOR_INTENSITY_NORMAL));
    }

    @Override
    public void deselect() {
        updateColorDelegates.forEach(colorConsumer -> {
            final Component component = controller.getComponent();

            colorConsumer.accept(component.getColor(), component.getColorIntensity());
        });
    }
}
