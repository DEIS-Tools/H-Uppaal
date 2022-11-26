package dk.cs.aau.huppaal.presentations.logging;

import dk.cs.aau.huppaal.logging.LogLinkQuantifier;
import javafx.geometry.VPos;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.ReadOnlyStyledDocument;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.TextOps;
import org.reactfx.util.Either;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;

public class HyperlinkTextArea extends GenericStyledArea<Void, Either<String, Hyperlink>, TextStyle> {
    private static final TextOps<String, TextStyle> STYLED_TEXT_OPS = SegmentOps.styledTextOps();
    private static final HyperlinkOps<TextStyle> HYPERLINK_OPS = new HyperlinkOps<>();
    private static final TextOps<Either<String, Hyperlink>, TextStyle> EITHER_OPS = STYLED_TEXT_OPS._or(HYPERLINK_OPS, (s1, s2) -> Optional.empty());

    public HyperlinkTextArea(@Nonnull Consumer<Hyperlink> showLink) {
        super(null,
              (t, p) -> {},
              TextStyle.EMPTY,
              EITHER_OPS,
              e -> e.getSegment().unify(
              text -> createStyledTextNode(t -> {
                  t.setText(text);
                  t.setStyle(e.getStyle().toCss());
              }),
              hyperlink -> createStyledTextNode(t -> {
                if (hyperlink.isReal()) {
                    t.setText(hyperlink.getDisplayedText());
                    t.getStyleClass().add("hyperlink");
                    t.setStyle(e.getStyle().toCss());
                    t.setOnMouseClicked(ae -> showLink.accept(hyperlink));
                }})));
        getStyleClass().add("text-hyperlink-area");
    }

    public void append(@Nonnull String text,
                       @Nonnull TextStyle style) {
        replace(getLength(), getLength(), ReadOnlyStyledDocument.fromString(text, null, style, EITHER_OPS));
    }

    public void appendWithLink(@Nonnull String displayedText,
                               @Nonnull String link,
                               @Nonnull LogLinkQuantifier quantifier,
                               @Nonnull TextStyle style) {
        replaceWithLink(getLength(), getLength(), displayedText, link, quantifier, style);
    }

    public void appendWithLink(@Nonnull String displayedText,
                               @Nonnull String link,
                               @Nonnull LogLinkQuantifier quantifier) {
        appendWithLink(displayedText, link, quantifier, quantifier.getStyle());
    }

    public void replaceWithLink(int start, int end,
                                @Nonnull String displayedText,
                                @Nonnull String link,
                                @Nonnull LogLinkQuantifier quantifier,
                                @Nonnull TextStyle style) {
        replace(start, end, ReadOnlyStyledDocument.fromSegment(
                Either.right(new Hyperlink(displayedText, displayedText, link, quantifier)),
                null,
                style,
                EITHER_OPS
        ));
    }

    @Override
    public void replaceText(int start, int end, @Nonnull String text) {
        if (start > 0 && end > 0) {
            var s = Math.max(0, start-1);
            var e = Math.min(end+1, getLength()-1);
            var segList = getDocument().subSequence( s, e ).getParagraph(0).getSegments();
            if (!segList.isEmpty() && segList.get(0).isRight()) {
                var link = segList.get(0).getRight().getLink();
                var quantifier = segList.get(0).getRight().getQuantifier();
                replaceWithLink( start, end, text, link, quantifier, TextStyle.EMPTY );
                return;
            }
        }
        replace(start, end,
                ReadOnlyStyledDocument.fromString(text, getParagraphStyleForInsertionAt(start), getTextStyleForInsertionAt(start), EITHER_OPS));
    }

    public static TextExt createStyledTextNode(@Nonnull Consumer<TextExt> applySegment) {
        var t = new TextExt();
        t.setTextOrigin(VPos.TOP);
        applySegment.accept(t);
        return t;
    }
}
