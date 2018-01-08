package ui;

import java.util.Random;
import java.util.regex.Pattern;

public enum RangeType {
    ArticleRange ("^Art. ([0-9]+)([a-z]|)-([0-9]+)([a-z]|)$"),
    Component("^Art. ([0-9]+)([a-z]|)(.|)(,|)( |)(.+)$"),
    Section("^DZIAŁ ([IXV]+)([A-Z]|)$"),
    Chapter("^Rozdział ([0-9]+)([a-z]|)$"),
    ConstitutionalChapter("^Rozdział ([IVX]+)([a-z]|)$");

    Pattern pattern;
    RangeType(String pattern){
        this.pattern = Pattern.compile(pattern);
    }

    public Pattern getPattern() {
        return pattern;
    }
}
