package ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ViewerMode {
    tableOfContents("Table of contents",
            Stream.of(
                    "toc",
                    "table",
                    "contents").collect(Collectors.toList())),
    viewRange("View content of particular part or range",
            Stream.of(
                    "range",
                    "elements").collect(Collectors.toList()));
    private final String description;
    private final List<String> textRepresentations;

    ViewerMode (String description, List<String> textRepresentatnions) {
        this.description = description;
        this.textRepresentations = textRepresentatnions;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTextRepresentations() {
        return textRepresentations;
    }

    public static ViewerMode fromText(String text) {
        return Arrays.stream(ViewerMode.values())
                .filter(l -> l.getTextRepresentations().contains(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no such representation as " + text +"."));
    }
}
