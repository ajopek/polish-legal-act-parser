package ui;

import actcomponents.ActComponent;
import actcomponents.ActHierarchy;
import actcomponents.ActStructureHandler;
import actcomponents.ComponentId;
import filehandler.FileHandler;
import parser.LegalActParser;
import parser.RomanConverter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * This class represents request from the viewer to parse and print legal act in given range and mode,
 * from given filePath.
 */
public class Viewer {
    private String filePath;
    private ViewerMode mode;
    private String range = null;

    private final static String articlePattern = "^Art. ([0-9]+)([a-z]|)(\\.|)(,|)( |)";
    private final static String paragraphPattern = "^ust. ([0-9]+)([a-z]|)(\\.|)(,|)( |)";
    private final static String subparagraphPattern = "^pkt ([0-9]+)([a-z]|)\\)(,|)( |)";
    private final static String letterPattern = "^lit. ([a-z])\\)$";

    /*
     * Reads, parses and extracts legal act from file, prints content from the act, based on mode, and range.
     */
    public void applyViewer() {
        FileHandler fileHandler = new FileHandler();
        LegalActParser legalActParser = new LegalActParser();
        List<String> lines = fileHandler.getLines(filePath);
        legalActParser.preparseRemove(lines);
        legalActParser.concatTwoLineWords(lines);
        ActComponent root = legalActParser.getActRoot(lines);
        legalActParser.parseComponents(lines, root, 0);
        try {
            switch (mode) {
                case tableOfContents:
                    if (range.equals("")) {
                        printTableOfContents(root);
                    } else {
                        printTableOfContents(findSection(root));
                    }
                    break;
                case viewRange:
                    StringBuilder toPrint = new StringBuilder();
                    LinkedList<ActComponent> toPrintList;
                    RangeType type = Arrays.stream(RangeType.values())
                            .filter(r -> range.matches(r.getPattern().pattern()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No such range type: "+range));
                    switch (type) {
                        case ArticleRange:
                            toPrintList = findArticlesInRange(root);
                            break;
                        case Component:
                            toPrintList = findComponent(root);
                            break;
                        case Chapter:
                            toPrintList = findChapter(root);
                            break;
                        case ConstitutionalChapter:
                            toPrintList = findConstitutionalChapter(root);
                            break;
                        default:
                            throw new IllegalArgumentException("No such range type: "+range);
                    }
                    toPrintList.forEach(a -> a.print(toPrint));
                    System.out.print(toPrint.toString());
                    break;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Viewing act failed.");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    /*
     * Updates given viewer, with arguments read from @args.
    */
    public void updateViewer(String[] args) {
        CommandLineParser commandLineParser = new CommandLineParser();
        try {
            String filePath = commandLineParser.parseFilePath(args);
            String modeText = commandLineParser.parseMode(args);
            this.setFilePath(filePath);
            ViewerMode mode = ViewerMode.fromText(modeText);
            this.setMode(mode);
            try {
                String range = commandLineParser.parseRange(args);
                this.setRange(range);
            } catch (IllegalArgumentException e){
                if(this.mode.equals(ViewerMode.viewRange)) {
                    System.out.println("Parsing arguments failed.");
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
                this.setRange("");
            }

        }catch (IllegalArgumentException e){
            System.out.println("Parsing arguments failed.");
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    /* ----------------------------------------------------------------
     * Setters
     * ----------------------------------------------------------------
     */

    private void setFilePath(String filePathText) {
        filePath = filePathText;
    }

    private void setMode(ViewerMode mode) {
       this.mode = mode;
    }

    private void setRange(String range) {
        this.range = range;
    }

    /* ----------------------------------------------------------------
     * Utils
     * ----------------------------------------------------------------
     */

    private ActComponent findSection(ActComponent actRoot) {
        Matcher  sectionMatcher = RangeType.Section.getPattern().matcher(range);
        if (sectionMatcher.find()) {
            int main = new RomanConverter().convertToInt(sectionMatcher.group(1));
            String secondary = sectionMatcher.group(2);
            ComponentId id = new ComponentId(main, secondary);
            return new ActStructureHandler().getSectionsList(actRoot).stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No such section: " + range + "."));
        } else {
            throw new IllegalArgumentException("Wrong range format.");
        }
    }

    private LinkedList<ActComponent> findArticlesInRange(ActComponent actRoot) {
        Matcher rangeMatcher = RangeType.ArticleRange.getPattern().matcher(range);
        if(!rangeMatcher.find()) throw new IllegalArgumentException("Wrong range struct: "+range);
        ComponentId first = new ComponentId(Integer.parseInt(rangeMatcher.group(1)), rangeMatcher.group(2));
        ComponentId last = new ComponentId(Integer.parseInt(rangeMatcher.group(3)), rangeMatcher.group(4));
        LinkedList<ActComponent> articles =
                new ActStructureHandler().getArticleList(actRoot).stream()
                .filter(a -> a.getId().compareTo(first))
                .filter(a -> !a.getId().compareTo(last))
                .collect(Collectors.toCollection(LinkedList::new));
        if(articles.isEmpty()) {
            throw new IllegalArgumentException("No articles in range: "+range);
        } else {
            return articles;
        }
    }

    private LinkedList<ActComponent> findArticle(ActComponent actRoot, ComponentId id) {
        return new ActStructureHandler().getArticleList(actRoot).stream()
                .filter(a -> a.getId().equals(id))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private LinkedList<ActComponent> findChapter(ActComponent actRoot) {
        Matcher rangeMatcher = RangeType.Chapter.getPattern().matcher(range);
        if(!rangeMatcher.find()) throw new IllegalArgumentException("Wrong range struct: "+range);
        int main = Integer.parseInt(rangeMatcher.group(1));
        String secondary = rangeMatcher.group(2);
        ComponentId id = new ComponentId(main, secondary);
        LinkedList<ActComponent> chapter =
                new ActStructureHandler().getChaptersList(actRoot).stream()
                        .filter(ch -> ch.getId().equals(id))
                        .collect(Collectors.toCollection(LinkedList::new));
        if(chapter.isEmpty()) {
            throw new IllegalArgumentException("No chapter: "+range);
        } else {
            return chapter;
        }
    }

    private LinkedList<ActComponent> findConstitutionalChapter(ActComponent actRoot) {
        Matcher rangeMatcher = RangeType.ConstitutionalChapter.getPattern().matcher(range);
        if(!rangeMatcher.find()) throw new IllegalArgumentException("Wrong range struct: "+range);
        int main = new RomanConverter().convertToInt(rangeMatcher.group(1));
        String secondary = rangeMatcher.group(2);
        ComponentId id = new ComponentId(main, secondary);
        LinkedList<ActComponent> chapter =
                new ActStructureHandler().getChaptersList(actRoot).stream()
                .filter(ch -> ch.getId().equals(id))
                .collect(Collectors.toCollection(LinkedList::new));
        if(chapter.isEmpty()) {
            throw new IllegalArgumentException("No chapter: "+range);
        } else {
            return chapter;
        }
    }
    
    private LinkedList<ActComponent> findComponent(ActComponent actRoot) {
        ActComponent searchParent;
        Matcher componentMatcher = Pattern.compile(articlePattern).matcher(range);
        if(componentMatcher.find()) {
            ComponentId articleId = new ComponentId(Integer.parseInt(componentMatcher.group(1)), componentMatcher.group(2));
            LinkedList<ActComponent> article = findArticle(actRoot, articleId);
            if (article.isEmpty()) throw new IllegalArgumentException("No such article:" + range);
            else searchParent = article.getFirst();
            range = range.replace(componentMatcher.group(0), "");
            if(range.isEmpty()) return article;
            componentMatcher = Pattern.compile(paragraphPattern).matcher(range);
            if (componentMatcher.find()) {
                ComponentId paragraphId = new ComponentId(Integer.parseInt(componentMatcher.group(1)), componentMatcher.group(2));
                LinkedList<ActComponent> paragraph = searchParent.getChildren().values().stream()
                        .filter(ch -> ch.getId().equals(paragraphId) && ch.getHierarchyLevel().equals(ActHierarchy.Paragraph))
                        .collect(Collectors.toCollection(LinkedList::new));
                if (paragraph.isEmpty()) throw new IllegalArgumentException("No such paragraph:" + range);
                else searchParent = paragraph.getFirst();
                range = range.replace(componentMatcher.group(0), "");
                if(range.isEmpty()) return paragraph;
            }
            componentMatcher = Pattern.compile(subparagraphPattern).matcher(range);
            if (componentMatcher.find()) {
                ComponentId subparagraphId = new ComponentId(Integer.parseInt(componentMatcher.group(1)), componentMatcher.group(2));
                LinkedList<ActComponent> subparagraph = searchParent.getChildren().values().stream()
                        .filter(ch -> ch.getId().equals(subparagraphId) && ch.getHierarchyLevel().equals(ActHierarchy.Subparagraph))
                        .collect(Collectors.toCollection(LinkedList::new));
                if (subparagraph.isEmpty()) throw new IllegalArgumentException("No such subparagraph:" + range);
                else searchParent = subparagraph.getFirst();
                range = range.replace(componentMatcher.group(0), "");
                if(range.isEmpty()) return subparagraph;
            }
            componentMatcher = Pattern.compile(letterPattern).matcher(range);
            if (componentMatcher.find()) {
                ComponentId letterId = new ComponentId(0, componentMatcher.group(1));
                LinkedList<ActComponent> letter = searchParent.getChildren().values().stream()
                        .filter(ch -> ch.getId().equals(letterId) && ch.getHierarchyLevel().equals(ActHierarchy.Letter))
                        .collect(Collectors.toCollection(LinkedList::new));
                if (letter.isEmpty()) throw new IllegalArgumentException("No such letter:" + range);
                range = range.replace(componentMatcher.group(0), "");
                if(range.isEmpty()) return letter;
            }
        }
        throw new IllegalArgumentException("Bad range structure: "+ range);
    }
    /*
     * Print table of contents for a given root, based on printTableOfContents method in ActComponent.
     */
    private void printTableOfContents(ActComponent actRoot) {
            StringBuilder tableOfContents = new StringBuilder();
            actRoot.printTableOfContents(tableOfContents);
            System.out.print(tableOfContents.toString());
    }
}
