package actcomponents;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActStructureHandler {
    /*
     * Returns all of @parent children, which are on @level of component hierarchy.
     */
    private LinkedList<ActComponent> filterHierarchy(ActComponent parent, ActHierarchy level) {
        return parent.getChildren().entrySet().stream()
                .filter(entry -> entry.getValue().getHierarchyLevel().equals(level))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /*
     * Returns article range for given chapter or constitutional chapter.
     */
    String getArticlesRange(ActComponent parent) {
        // find possible parents of articles
        List<ActComponent> parentsToSearch = new LinkedList<>();
        parentsToSearch.add(parent);
        Arrays.stream(ActHierarchy.values())
                .filter(level -> canHaveArticle(level))
                .map(hl -> parentsToSearch.addAll(filterHierarchy(parent, hl)));

        //find all children that are articles
        LinkedList<ActComponent> articlesToSearch = parentsToSearch.stream()
                .map(toSearch -> filterHierarchy(toSearch, ActHierarchy.Article))
                .flatMap(List::stream)
                .collect(Collectors.toCollection(LinkedList::new));

        //find range of articles
        if (articlesToSearch.isEmpty()) return "";
        ComponentId first = articlesToSearch.getFirst().getId();
        ComponentId last = first;
        first = articlesToSearch.stream()
                .map(ActComponent::getId)
                .reduce(first, (id, acc) -> acc.compareTo(id) ? id:acc);

        last = articlesToSearch.stream()
                .map(ActComponent::getId)
                .reduce(last, (id, acc) -> acc.compareTo(id) ? acc:id);
        return "Art. " + first.toString() + (first.equals(last)  ? "" : "-" + last.toString());
    }

    public LinkedList<ActComponent> getSectionsList(ActComponent actRoot){
        return filterHierarchy(actRoot, ActHierarchy.Section);
    }
    public LinkedList<ActComponent> getChaptersList(ActComponent actRoot) {
        LinkedList<ActComponent> parentsToSearch = filterHierarchy(actRoot, ActHierarchy.Section);
        parentsToSearch.add(actRoot);
        if(actRoot.getHierarchyLevel().equals(ActHierarchy.Act)) {
            return parentsToSearch.stream()
                    .map(p -> filterHierarchy(p, ActHierarchy.Chapter))
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(LinkedList::new));
        } else {
            return parentsToSearch.stream()
                    .map(p -> filterHierarchy(p, ActHierarchy.ConstitutionChapter))
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(LinkedList::new));
        }
    }

    public LinkedList<ActComponent> getArticleList(ActComponent actRoot) {
        List<ActComponent> parents =  actRoot.children.values().stream()
                .filter(c -> canHaveArticle(c.getHierarchyLevel()))
                .collect(Collectors.toCollection(LinkedList::new));
        List<ActComponent> parentsToSearch = parents.stream()
                .map(ActComponent::getChildren)
                .map(HashMap::values)
                .flatMap(v -> v.stream())
                .filter(c -> canHaveArticle(c.getHierarchyLevel()))
                .collect(Collectors.toCollection(LinkedList::new));
        parentsToSearch.addAll(parents);


        return parentsToSearch.stream()
                .map(parent -> filterHierarchy(parent, ActHierarchy.Article))
                .flatMap(List::stream)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean canHaveArticle(ActHierarchy level) {
        boolean can = false;
        switch (level) {
            case Act:
                can = true;
                break;
            case Constitution:
                can = true;
                break;
            case Titlechapter:
                can = true;
                break;
            case Chapter:
                can = true;
                break;
            case Section:
                can = true;
                break;
            case ConstitutionChapter:
                can = true;
                break;
        }
        return can;
    }
}
