package dk.cs.aau.huppaal.presentations.logging;

import javafx.scene.paint.Color;
import org.fxmisc.richtext.model.Codec;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Holds information about the style of a text fragment.
 */
public class TextStyle {
    public static final TextStyle EMPTY = new TextStyle();
    public static final TextStyle WHITE = new TextStyle().updateTextColor(Color.WHITE);
    public static final Codec<TextStyle> CODEC = new TextStyleCodec();

    public static TextStyle bold(boolean bold) { return EMPTY.updateBold(bold); }
    public static TextStyle italic(boolean italic) { return EMPTY.updateItalic(italic); }
    public static TextStyle underline(boolean underline) { return EMPTY.updateUnderline(underline); }
    public static TextStyle strikethrough(boolean strikethrough) { return EMPTY.updateStrikethrough(strikethrough); }
    public static TextStyle fontSize(int fontSize) { return EMPTY.updateFontSize(fontSize); }
    public static TextStyle fontFamily(String family) { return EMPTY.updateFontFamily(family); }
    public static TextStyle textColor(Color color) { return EMPTY.updateTextColor(color); }
    public static TextStyle backgroundColor(Color color) { return EMPTY.updateBackgroundColor(color); }
    public static TextStyle randomTextColor() {
        var r = new Random();
        return EMPTY.updateTextColor(Color.color(
                r.nextDouble(),
                r.nextDouble(),
                r.nextDouble()));
    }

    static String cssColor(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return "rgb(" + red + ", " + green + ", " + blue + ")";
    }

    final Optional<Boolean> bold;
    final Optional<Boolean> italic;
    final Optional<Boolean> underline;
    final Optional<Boolean> strikethrough;
    final Optional<Integer> fontSize;
    final Optional<String> fontFamily;
    final Optional<Color> textColor;
    final Optional<Color> backgroundColor;

    public TextStyle() {
        this(Optional.empty(),
             Optional.empty(),
             Optional.empty(),
             Optional.empty(),
             Optional.empty(),
             Optional.empty(),
             Optional.empty(),
             Optional.empty());
    }

    public TextStyle(Optional<Boolean> bold,
                     Optional<Boolean> italic,
                     Optional<Boolean> underline,
                     Optional<Boolean> strikethrough,
                     Optional<Integer> fontSize,
                     Optional<String> fontFamily,
                     Optional<Color> textColor,
                     Optional<Color> backgroundColor) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.fontSize = fontSize;
        this.fontFamily = fontFamily;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bold, italic, underline, strikethrough,
                            fontSize, fontFamily, textColor, backgroundColor);
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof TextStyle that)
            return Objects.equals(this.bold,            that.bold) &&
                   Objects.equals(this.italic,          that.italic) &&
                   Objects.equals(this.underline,       that.underline) &&
                   Objects.equals(this.strikethrough,   that.strikethrough) &&
                   Objects.equals(this.fontSize,        that.fontSize) &&
                   Objects.equals(this.fontFamily,      that.fontFamily) &&
                   Objects.equals(this.textColor,       that.textColor) &&
                   Objects.equals(this.backgroundColor, that.backgroundColor);
        return false;
    }

    @Override
    public String toString() {
        var styles = new ArrayList<String>();
        bold           .ifPresent(b -> styles.add(b.toString()));
        italic         .ifPresent(i -> styles.add(i.toString()));
        underline      .ifPresent(u -> styles.add(u.toString()));
        strikethrough  .ifPresent(s -> styles.add(s.toString()));
        fontSize       .ifPresent(s -> styles.add(s.toString()));
        fontFamily     .ifPresent(f -> styles.add(f.toString()));
        textColor      .ifPresent(c -> styles.add(c.toString()));
        backgroundColor.ifPresent(b -> styles.add(b.toString()));
        return String.join(",", styles);
    }

    public String toCss() {
        var sb = new StringBuilder();
        bold.ifPresent(b -> sb.append("-fx-font-weight: ").append(b ? "bold" : "normal").append(";"));
        italic.ifPresent(b -> sb.append("-fx-font-style: ").append(b ? "italic" : "normal").append(";"));
        underline.ifPresent(b -> sb.append("-fx-underline: ").append(b ? "true" : "false").append(";"));
        strikethrough.ifPresent(b -> sb.append("-fx-strikethrough: ").append(b ? "true" : "false").append(";"));
        fontSize.ifPresent(integer -> sb.append("-fx-font-size: ").append(integer).append("pt;"));
        fontFamily.ifPresent(s -> sb.append("-fx-font-family: ").append(s).append(";"));
        textColor.ifPresent(color -> sb.append("-fx-fill: ").append(cssColor(color)).append(";"));
        backgroundColor.ifPresent(color -> sb.append("-rtfx-background-color: ").append(cssColor(color)).append(";"));
        return sb.toString();
    }

    public TextStyle updateWith(TextStyle mixin) {
        return new TextStyle(
                mixin.bold.isPresent() ? mixin.bold : bold,
                mixin.italic.isPresent() ? mixin.italic : italic,
                mixin.underline.isPresent() ? mixin.underline : underline,
                mixin.strikethrough.isPresent() ? mixin.strikethrough : strikethrough,
                mixin.fontSize.isPresent() ? mixin.fontSize : fontSize,
                mixin.fontFamily.isPresent() ? mixin.fontFamily : fontFamily,
                mixin.textColor.isPresent() ? mixin.textColor : textColor,
                mixin.backgroundColor.isPresent() ? mixin.backgroundColor : backgroundColor);
    }

    public TextStyle updateBold(boolean bold) {
        return new TextStyle(Optional.of(bold), italic, underline, strikethrough, fontSize, fontFamily, textColor, backgroundColor);
    }

    public TextStyle updateItalic(boolean italic) {
        return new TextStyle(bold, Optional.of(italic), underline, strikethrough, fontSize, fontFamily, textColor, backgroundColor);
    }

    public TextStyle updateUnderline(boolean underline) {
        return new TextStyle(bold, italic, Optional.of(underline), strikethrough, fontSize, fontFamily, textColor, backgroundColor);
    }

    public TextStyle updateStrikethrough(boolean strikethrough) {
        return new TextStyle(bold, italic, underline, Optional.of(strikethrough), fontSize, fontFamily, textColor, backgroundColor);
    }

    public TextStyle updateFontSize(int fontSize) {
        return new TextStyle(bold, italic, underline, strikethrough, Optional.of(fontSize), fontFamily, textColor, backgroundColor);
    }

    public TextStyle updateFontFamily(String fontFamily) {
        return new TextStyle(bold, italic, underline, strikethrough, fontSize, Optional.of(fontFamily), textColor, backgroundColor);
    }

    public TextStyle updateTextColor(Color textColor) {
        return new TextStyle(bold, italic, underline, strikethrough, fontSize, fontFamily, Optional.of(textColor), backgroundColor);
    }

    public TextStyle updateTextColorWeb(String webColor) {
        return updateTextColor(Color.web(webColor));
    }

    public TextStyle updateBackgroundColor(Color backgroundColor) {
        return new TextStyle(bold, italic, underline, strikethrough, fontSize, fontFamily, textColor, Optional.of(backgroundColor));
    }
}
