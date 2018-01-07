package actcomponents;

import java.util.LinkedHashMap;
import java.util.Optional;
/*
 * This class represents a component of legal act, and its textual representation.
 */
public class ActComponent {
    LinkedHashMap<ComponentId, ActComponent> children = new LinkedHashMap<>();
    private String content = "";
    private String title = "";
    private ComponentId id;
    private ActHierarchy hierarchyLevel;
    private ActComponent parent;

    public ActComponent(ComponentId id, ActHierarchy hierarchyLevel) {
        this.id = id;
        this.hierarchyLevel = hierarchyLevel;
    }

    public void addChild(ActComponent child) {
        children.put(child.id, child);
        child.setParent(this);
    }

    /* ----------------------------------------------------------------
     * Setters
     * ----------------------------------------------------------------
     */
    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParent(ActComponent parent) {
        this.parent = parent;
    }

    /* ----------------------------------------------------------------
     * Textual representation handlers
     * ----------------------------------------------------------------
     */

    /*
     * Print table of contents.
     * Content to print is defined by tableOfContentsToString method.
     */
    public void printTableOfContents(StringBuilder toPrint) {
        toPrint.append(this.tableOfContentsToString());
        children.values().forEach(c -> c.printTableOfContents(toPrint));
    }


    /*
     * Print recursively, from this component, then his children, with their children, and so on..
     * Printed content is defined by toString method.
     */
    public void print(StringBuilder toPrint) {
            toPrint.append(this);
            children.values().forEach(c -> c.print(toPrint));
    }


    /*
     * Defines textual representation of component.
     */
    @Override
    public String toString() {
        String text = "";
        switch (hierarchyLevel) {
            case Act:
                text = title == null ? "" : title;
                break;
            case Constitution:
                text = title == null ? "" : title;
                break;
            case Preamble:
                text = content;
                break;
            case Article:
                text = title + (content != null ? content:"");
                break;
            case Titlechapter:
                text = title;
                break;
            case Subparagraph:
                text = title + (content != null ? content:"");
                break;
            case Paragraph:
                text = title + (content != null ? content:"");
                break;
            case Chapter:
                text = title + (content != null ? content:"");
                break;
            case Section:
                text = title + (content != null ? content:"");
                break;
            case ConstitutionChapter:
                text = title;
                break;
            case Letter:
                text = title + content;

        }
        return text;
    }


    /*
     * Defines textual representation of a component for printing in table of contents.
     */

    private String tableOfContentsToString() {
        String text = "";
        ActStructureHandler actStructureHandler = new ActStructureHandler();
        switch (hierarchyLevel) {
            case Act:
                text = title == null ? "" : title;
                break;
            case Constitution:
                text = title == null ? "" : title;
                break;
            case Preamble:
                text = "";
                break;
            case Article:
                text = "";
                break;
            case Titlechapter:
                text = title + " (" + actStructureHandler.getArticlesRange(this) + ")" + "\n";;
                break;
            case Subparagraph:
                text = "";
                break;
            case Paragraph:
                text = "";
                break;
            case Chapter:
                text = title + (content != null ? content:"") + " (" + actStructureHandler.getArticlesRange(this) + ")" + "\n";
                break;
            case Section:
                text = title + (content != null ? content:"") + " (" + actStructureHandler.getArticlesRange(this) + ")" + "\n";
                break;
            case ConstitutionChapter:
                text = title + (content != null ? content:"") + " (" + actStructureHandler.getArticlesRange(this) + ")" + "\n";
                break;

        }
        return text;
    }


    /* ----------------------------------------------------------------
     * Getters
     * ----------------------------------------------------------------
     */
    public ActComponent getParent() {
        return parent;
    }

    public Optional<ActComponent> getChild(ComponentId id) {
        ActComponent child = children.get(id);
        return Optional.ofNullable(child);
    }

    public String getTitle() {
        return title;
    }

    public LinkedHashMap<ComponentId, ActComponent> getChildren() {
        return children;
    }

    public String getContent() {
        return content;
    }

    public ComponentId getId() {
        return id;
    }

    public ActHierarchy getHierarchyLevel() {
        return hierarchyLevel;
    }

    public int getLevel() {
        return hierarchyLevel.getLevel();
    }
}
