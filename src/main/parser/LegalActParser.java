package parser;

import actcomponents.ActComponent;
import actcomponents.ActHierarchy;
import actcomponents.ComponentId;
import filehandler.FileHandler;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LegalActParser {

    /*
     * Defines regexps of lines to remove in pre-processing.
     */
    private static final List<Predicate> toRemove = Stream.of(
            "\\d+-\\d+-\\d+\\b",
            "^©Kancelaria Sejmu",
            ".*(uchylony)",
            ".*(pominięte)",
            "^Dz\\.U\\. ([0-9]+) Nr ([0-9]+) poz\\. ([0-9]+)$"
    ).map(Pattern::compile).map(Pattern::asPredicate).collect(Collectors.toList());

    /*
     * Defines regexp for word split into two lines.
     */
    private static final Pattern twoLineWord = Pattern.compile("([A-zĘęÓóĄąŚśŁłŻżŹźĆćŃń]+)-$");


    /*
     * Removes from provided list of lines based on toRemove list of predicates.
     */
    public void preparseRemove(List<String> lines) {
        toRemove.forEach(lines::removeIf);
    }

    /*
     * Concatenates words split into two lines, based on toLineWord regexp.
     */
    public void concatTwoLineWords(List<String> lines) {
        String wordPartAcc = "";
        Boolean found = false;
        ListIterator<String> iterator = lines.listIterator();
        while (iterator.hasNext()){
            String l = iterator.next();
            if(found) {
                iterator.set(wordPartAcc+l);
                l=wordPartAcc+l;
                found = false;
            }
            Matcher m = twoLineWord.matcher(l);
            if(m.find()) {
                wordPartAcc = m.group(1);
                found=true;
                iterator.set(l.replace(m.group(),""));
            }
        }
    }

    /*
     * Extracts root of an Act or Constitution, based on title line.
     */
    public ActComponent getActRoot(List<String> lines) {
        String titleLine = lines.get(0);
        ActComponent actRoot = null;
        ComponentId rootId = new ComponentId(0, null);
        if (titleLine.matches(ActHierarchy.Constitution.getPattern().pattern())) {
            actRoot = new ActComponent(rootId, ActHierarchy.Constitution);
            parseConstitutionRoot(lines, actRoot);
        } else {
            if (titleLine.matches(ActHierarchy.Act.getPattern().pattern())) {
                actRoot = new ActComponent(rootId, ActHierarchy.Act);
                parseActRoot(lines, actRoot);
            } else {
                actRoot = new ActComponent(rootId, ActHierarchy.Act);
            }
        }
        return actRoot;
    }

    /*
     * Parses root of Constitution, and calls method to parse Preamble.
     */
    public void parseConstitutionRoot(List<String> lines, ActComponent actRoot) {
        // Title parsing
        ListIterator<String> linesIterator = lines.listIterator();
        String line;
        String title = "";
        while (linesIterator.hasNext()) {
            line = linesIterator.next();
            //Part of title
            if (line.matches(ActStructureRegexp.polishUppercaseWords)) {
                title += line + "\n";
                linesIterator.remove();
            }
            // Constitution date, end title parsing
            if (line.matches(ActStructureRegexp.constitutionDate)) {
                title += line + "\n";
                linesIterator.remove();
                parsePreamble(linesIterator, actRoot);
                break;
            }
        }
        actRoot.setTitle(title);
    }

    /*
     * Parses Preamble, is called when parsing Constitution root.
     * @linesIterator is iterator from parsing Constitution root, is at the position of end of the root.
     */
    public void parsePreamble(ListIterator<String> linesIterator, ActComponent actRoot){
        String line = "";
        String content = "";
        while (linesIterator.hasNext()) {
            line = linesIterator.next();
            if (line.matches(ActHierarchy.ConstitutionChapter.getPattern().pattern())) break;
            content += line + "\n";
            linesIterator.remove();
        }
        ActComponent preamble = new ActComponent(new ComponentId(0, null), ActHierarchy.Preamble);
        preamble.setContent(content);
        actRoot.addChild(preamble);
    }


    /*
     * Parses Act root.
     */
    public void parseActRoot(List<String> lines, ActComponent actRoot) {
        ListIterator<String> linesIterator = lines.listIterator();
        String line = "";
        String title = "";
        while (linesIterator.hasNext() ) {
            line = linesIterator.next();
            //Begining of act components, end of title
            if (line.matches(ActHierarchy.Section.getPattern().pattern())) {
                break;
            }
            // part of act title
            if (line.matches(ActStructureRegexp.polishWords) || line.matches(ActStructureRegexp.constitutionDate)) {
                title += line + "\n";
                linesIterator.remove();
            }
        }
        actRoot.setTitle(title);
    }

    /*
     * Parses given list of lines, extracts ActComponents with their title and content.
     * In the first call, recursionParent should be the Act or Constitution root.
     * @id parameter is for creating ids for components that don't have it in textual representation.
     */
    public void parseComponents(List<String> lines, ActComponent recursionParent, int id) {
        ListIterator<String> linesIterator = lines.listIterator();
        Boolean ifFoundComponent = false;
        boolean titleLine = false;
        while (linesIterator.hasNext() && !ifFoundComponent) {
            String line = linesIterator.next();
            while(!line.equals("") && !ifFoundComponent) {
                Pattern titlePattern = recursionParent.getHierarchyLevel().getTitlePattern();
                Matcher titleMatcher = titlePattern.matcher(line);
                if (!titlePattern.pattern().equals("") && titleMatcher.find()
                        && (recursionParent.getHierarchyLevel().equals(ActHierarchy.ConstitutionChapter)
                            && !titleLine)) {
                    if (recursionParent.getTitle() == null) {
                        recursionParent.setTitle(titleMatcher.group() + "\n");
                    } else {
                        recursionParent.setTitle(recursionParent.getTitle() + titleMatcher.group() + "\n");
                    }
                    line = line.replace(titleMatcher.group(), "");
                    linesIterator.set(line);
                    titleLine = true;
                    continue;
                }
                Boolean ifFound = false;
                Matcher foundComponent = null;
                ActHierarchy foundLevel = null;
                for (ActHierarchy level : ActHierarchy.values()) {
                    foundComponent = level.getPattern().matcher(line);
                    if (foundComponent.find() && !level.getPattern().pattern().equals("")) {
                        foundLevel = level;
                        ifFound = true;
                        break;
                    }
                }
                if (!ifFound) {
                    if(recursionParent.getContent() == null){
                        recursionParent.setContent(line + "\n");
                    } else {
                        recursionParent.setContent(recursionParent.getContent() + line + "\n");
                    }
                    line = "";
                    linesIterator.set(line);
                } else {
                    //create component
                    ActComponent newComponent = new ActComponent(idFromString(foundComponent, foundLevel, id), foundLevel);
                    newComponent.setTitle(foundComponent.group() + "\n");
                    line = line.replace(foundComponent.group(0), "");
                    linesIterator.set(line);
                    ActComponent newParent = recursionParent;
                    ifFoundComponent = true;
                    if(newParent.getLevel() == newComponent.getLevel()) {
                        newParent = recursionParent.getParent();
                    } else {
                        if(newParent.getLevel() > newComponent.getLevel()) {
                            while(newParent.getLevel() >= newComponent.getLevel() ) {
                                newParent = newParent.getParent();
                            }
                        }
                    }
                    newParent.addChild(newComponent);
                    parseComponents(lines, newComponent, id+1);
                }
            }
        }
    }

    /*
     * Extracts and creates ComponentId from found textual representation.
     */
    private ComponentId idFromString(Matcher foundComponent, ActHierarchy level, int id) {
        int main = 0;
        String secondary = null;
        switch (level) {
            case Section:
                main = new RomanConverter().convertToInt(foundComponent.group(1));
                secondary = foundComponent.group(2);
                break;
            case Chapter:
                main = Integer.parseInt(foundComponent.group(1));
                secondary = foundComponent.group(2);
                break;
            case Titlechapter:
                main = id;
                break;
            case Article:
                main = Integer.parseInt(foundComponent.group(1));
                secondary = foundComponent.group(2);
                break;
            case Paragraph:
                main = Integer.parseInt(foundComponent.group(1));
                secondary = foundComponent.group(2);
                break;
            case Subparagraph:
                main = Integer.parseInt(foundComponent.group(1));
                secondary = foundComponent.group(2);
                break;
            case Letter:
                secondary = foundComponent.group(1);
                break;
            case ConstitutionChapter:
                main = new RomanConverter().convertToInt(foundComponent.group(1));
                secondary = foundComponent.group(2);
        }
        return new ComponentId(main, secondary)
;    }


    public static void main(String[] args) {
        FileHandler fileHandler = new FileHandler();
        LegalActParser legalActParser = new LegalActParser();
        List<String> lines = fileHandler.getLines(args[0]);
        legalActParser.preparseRemove(lines);
        legalActParser.concatTwoLineWords(lines);
        ActComponent root = legalActParser.getActRoot(lines);
        legalActParser.parseComponents(lines, root, 0);

        StringBuilder all = new StringBuilder();
        root.print(all);
        System.out.print(all.toString());

    }

}
