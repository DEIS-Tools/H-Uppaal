package dk.cs.aau.huppaal.presentations.logging;

import dk.cs.aau.huppaal.logging.LogLinkQuantifier;

public class Hyperlink {
    private final String originalDisplayedText, displayedText, link;
    private final LogLinkQuantifier quantifier;

    Hyperlink(String originalDisplayedText, String displayedText, String link, LogLinkQuantifier quantifier) {
        this.originalDisplayedText = originalDisplayedText;
        this.displayedText = displayedText;
        this.link = link;
        this.quantifier = quantifier;
    }

    public LogLinkQuantifier getQuantifier() {
        return quantifier;
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public boolean isReal() {
        return length() > 0;
    }

    public boolean shareSameAncestor(Hyperlink other) {
        return link.equals(other.link);
    }

    public int length() {
        return displayedText.length();
    }

    public char charAt(int index) {
        return isEmpty() ? '\0' : displayedText.charAt(index);
    }

    public String getOriginalDisplayedText() {
        return originalDisplayedText;
    }

    public String getDisplayedText() {
        return displayedText;
    }

    public String getLink() {
        return link;
    }

    public Hyperlink subSequence(int start, int end) {
        return new Hyperlink(originalDisplayedText, displayedText.substring(start, end), link, quantifier);
    }

    public Hyperlink subSequence(int start) {
        return new Hyperlink(originalDisplayedText, displayedText.substring(start), link, quantifier);
    }

    public Hyperlink mapDisplayedText(String text) {
        return new Hyperlink(originalDisplayedText, text, link, quantifier);
    }

    @Override
    public String toString() {
        return isEmpty()
                ? String.format("EmptyHyperlink[original=%s link=%s]", originalDisplayedText, link)
                : String.format("RealHyperlink[original=%s displayedText=%s, link=%s]",
                originalDisplayedText, displayedText, link);
    }
}
