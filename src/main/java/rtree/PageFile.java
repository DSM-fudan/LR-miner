package rtree;

/**
 * Abstract class for all classes implementing a storage manager for the RTree. Every node should be stored in
 * a unique page. The root node is always stored in page 0. The storage manager should have the control
 * over the page numbers where each node should be stored. 
 * <p>
 * Created: Tue May 18 16:24:00 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.003
 */
public abstract class PageFile {
    protected RTree tree = null;

    /** Dimension of data inserted into the tree. */
    protected int dimension = -1;

    /** 
     * fillFactor specifies minimum node entries present in each node. It must be a double between
     * 0 and 0.5. 
     */
    protected double fillFactor = -1;

    /** Maximum node capacity. Each node will be able to hold at most nodeCapacity entries. */
    protected int nodeCapacity = -1;

    /** 
     * The page size needed in bytes to store a full node. Calculated using the following formula:
     * [nodeCapacity * (sizeof(HyperCube) + sizeof(Branch))] + parent + level + usedSpace =
     * {nodeCapacity * [(2 * dimension * sizeof(double)) + sizeof(int)]} +
     * sizeof(int) + sizeof(int) + sizeof(int)
     */
    protected int pageSize = -1;

    /** RTree variant used. Specified when creating a new tree. */
    protected int treeType = -1;


    /**
     * Returns the object stored in the requested page.
     */
    //    protected abstract byte[] readData(int page) throws PageFaultError;

    /**
     * if <i>page</i> is negative, writes the object into the first available page 
     * and returns that page. Else, the object is written into the given page. Objects
     * larger than one page size in length, are not supported yet!
     */ 
    //    protected abstract int writeData(byte[] d, int page) throws PageFaultError;

    /**
     * Returns the node stored in the requested page.
     */
    public abstract AbstractNode readNode(int page) throws PageFaultError;

    /**
     * Writes the node into the first available page and returns that page.
     */ 
    protected abstract int writeNode(AbstractNode o) throws PageFaultError;

    /**
     * Marks a specific page as empty.
     */
    protected abstract AbstractNode deletePage(int page) throws PageFaultError;

    protected void initialize(RTree tree, int dimension, double fillFactor, int capacity, int treeType) {
	this.dimension = dimension;
	this.fillFactor = fillFactor;
	this.nodeCapacity = capacity;
	this.treeType = treeType;
	this.tree = tree;

	this.pageSize = capacity * (16 * dimension + 4) + 12;
    }

    protected void finalize() throws Throwable {
	super.finalize();
    }

} // PageFile
