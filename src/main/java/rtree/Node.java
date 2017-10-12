package rtree;

/**
 * Interface for easy access to common Node information.
 * <p>
 * Created: Tue May 18 16:03:08 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.001
 */
public interface Node {

    // do not export any Set functions. We want to protect the state of the RTree from outside
    // tampering. Give only basic Get methods.

    //    public Object getChild(int i);

    public AbstractNode getParent();

    public HyperCube[] getHyperCubes();

    public int getLevel();

    public HyperCube getNodeMbb();
    
    public String getUniqueId();

    public boolean isLeaf();

    public boolean isRoot();

    public boolean isIndex();

    public String toString();

} // Node
