package actcomponents;

import parser.ActStructureRegexp;

import java.util.regex.Pattern;
/*
 * This enum represents level in legal act components hierarchy.
 */
public enum ActHierarchy {
    Act("USTAWA", "", 0),
    Constitution("^KONSTYTUCJA$", "", 0),
    Preamble("", "", 1),
    Chapter("^Rozdział ([0-9]+)([a-z]|)$", "^" +  ActStructureRegexp.polishWords + "$", 2),
    ConstitutionChapter("^Rozdział ([IVX]+)([a-z]|)$", "^" +  ActStructureRegexp.polishUppercaseWords + "$",2),
    Section("^DZIAŁ ([IXV]+)([A-Z]|)$","^" +  ActStructureRegexp.polishWords + "$", 1),
    Titlechapter("^" + ActStructureRegexp.polishUppercaseWords + "$", "", 3),
    Article("^Art" + ActStructureRegexp.componentId, "", 4),
    Paragraph("^([0-9]+)([a-z]||)\\.", "", 5),
    Subparagraph("^([0-9]+)([a-z]||)\\)", "", 6),
    Letter("(^[a-z])\\)", "", 7);

    private final Pattern pattern;
    private final Pattern titlePattern;
    private final int level;

    ActHierarchy(String regExp, String titleRegExp, int level) {
        pattern = Pattern.compile(regExp);
        titlePattern = Pattern.compile(titleRegExp);
        this.level = level;
    }

    /* ----------------------------------------------------------------
     * Getters
     * ----------------------------------------------------------------
     */

    public Pattern getPattern() {
        return pattern;
    }

    public int getLevel() {
        return level;
    }

    public Pattern getTitlePattern() {
        return titlePattern;
    }
}
