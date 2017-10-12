package rtree;

/**
 * @author Hadjieleftheriou Marios
 * @version 1.0
 */
public interface Comparator {
    int compare(Object o1, Object o2);
    
    boolean equals(Object obj);
}
