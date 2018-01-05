package actcomponents;

/*
 * This class represents id of a legal act component.
 */
public class ComponentId {
    private int mainId;
    private String secondaryId; // letter after the numerical part of id eg. "Art. 1a"

    public ComponentId(int mainId, String secondaryId) {
        this.mainId = mainId;
        if (secondaryId == null){
            this.secondaryId = "";
        } else {
            this.secondaryId = secondaryId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentId that = (ComponentId) o;

        if (mainId != that.mainId) return false;
        return secondaryId != null ? secondaryId.equals(that.secondaryId) : that.secondaryId == null;
    }

    @Override
    public int hashCode() {
        int result = mainId;
        result = 31 * result + (secondaryId != null ? secondaryId.hashCode() : 0);
        return result;
    }

    /*
     * Returns true if @toCompare is after @this in ordering.
     */
    public boolean compareTo(ComponentId toCompare) {
        if(this.mainId == toCompare.mainId){
            return this.secondaryId == null || this.secondaryId.compareTo(toCompare.secondaryId) > 0;
        } else {
            return this.mainId >= toCompare.mainId;
        }
    }

    @Override
    public String toString() {
        return mainId + secondaryId;
    }
}
