package rtree;

import java.util.*;

import math.geom2d.Box2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;

import cn.edu.fudan.dsm.tslrm.PLASegment;

/**
 * To create a new RTree use the first two constructors. You must specify the dimension, the fill factor as
 * a double between 0 and 0.5 (0 to 50% capacity) and the variant of the RTree which is one of:
 * <ul>
 * <li>RTREE_QUADRATIC</li>
 * </ul>
 * The first constructor creates by default a new memory resident page file. The second constructor takes
 * the page file as an argument. If the given page file is not empty, then all data are deleted.
 * <p/>
 * The third constructor initializes the RTree from an already filled page file. Thus, you may store the
 * RTree into a persistent page file and recreate it again at any time.
 * <p/>
 * Created: Tue May 18 12:57:35 1999
 * <p/>
 *
 * @author Hadjieleftheriou Marios
 * @version 1.003
 */
public class RTree {
    public final String version = "1.003";
    public final String date = "December 7th 1999";

    /**
     * Page file where data is stored.
     */
    public PageFile file = null;

    /**
     * static identifier used for the parent of the root node.
     */
    public static final int NIL = -1;

    /**
     * Available RTree variants.
     */
    public static final int RTREE_LINEAR = 0;
    public static final int RTREE_QUADRATIC = 1;
    public static final int RTREE_EXPONENTIAL = 2;
    public static final int RSTAR = 3;
    
    private List<PLASegment> segmentList;
    private PLASegment currentSeg;
    private Polygon2D currentPoly;

    /**
     * Creates a memory resident RTree with an empty root node, which is stored in page 0 and has a
     * parent identifier equal to NIL. A new memory page file is created and used as a storage manager
     * by default.
     *
     * @param dimension  The data dimension.
     * @param fillFactor A percentage between 0 and 0.5. Used to calculate minimum number of entries
     *                   present in each node.
     * @param capacity   The maximun number of entries that each node can hold.
     * @param type       The RTree variant to use.
     */
//    public RTree(int dimension, double fillFactor, int capacity, int type) {
//        this(dimension, fillFactor, capacity, new MemoryPageFile(), type);
//    }

    /**
     * Creates a new RTree with an empty root node, which is stored in page 0 and has
     * a parent identifier equal to NIL. The given page file is used for storing the rtree nodes.
     * If the page file is not empty, than all entries will be overwritten.
     *
     * @param dimension  The data dimension.
     * @param fillFactor A percentage between 0 and 0.5. Used to calculate minimum number of entries
     *                   present in each node.
     * @param capacity   The maximun number of entries that each node can hold.
     * @param file       The page file to use for storing the nodes of the rtree.
     * @param type       The RTree variant to use.
     */
    public RTree(int dimension, double fillFactor, int capacity, PageFile file, int type, List<PLASegment> segmentList) {
        if (dimension <= 1) {
            throw new IllegalArgumentException("Dimension must be larger than 1.");
        }

        if (fillFactor < 0 || fillFactor > 0.5) {
            throw new IllegalArgumentException("Fill factor must be between 0 and 0.5.");
        }

        if (capacity <= 1) {
            throw new IllegalArgumentException("Capacity must be larger than 1.");
        }

        if (type != RTREE_QUADRATIC /*&& type != RTREE_LINEAR && type != RTREE_EXPONENTIAL && type != RSTAR*/) {
            throw new IllegalArgumentException("Invalid tree type.");
        }

        if (file.tree != null) {
            throw new IllegalArgumentException("PageFile already in use by another rtree instance.");
        }

        file.initialize(this, dimension, fillFactor, capacity, type);
        this.file = file;

        this.segmentList = segmentList;
        Leaf root = new Leaf(this, NIL, 0);
        file.writeNode(root);
    }

    /**
     * Creates an rtree from an already initialized page file, probably stored into persistent storage.
     */
    public RTree(PageFile file) {
        if (file.tree != null) {
            throw new IllegalArgumentException("PageFile already in use by another rtree instance.");
        }

        if (file.treeType == -1) {
            throw new IllegalArgumentException("PageFile is empty. Use some other RTree constructor.");
        }

        file.tree = this;
        this.file = file;
    }
    
    public void buildRTree(){
    	int length = segmentList.size();
    	for(int i = 0; i < length; i++){
    		PLASegment tempSeg = segmentList.get(i);
    		Polygon2D tempPoly = tempSeg.getPolygonKB();
    		Box2D box = tempPoly.getBoundingBox();
    		Point p1 = new Point(new double[]{box.getMinX(), box.getMinY(), i});
    		Point p2 = new Point(new double[]{box.getMaxX(), box.getMaxY(), i});
    		HyperCube h = new HyperCube(p1, p2);
    		this.insert(h, i);
    	}
    }

    /**
     * Retruns the maximun capacity of each Node.
     */
    public int getNodeCapacity() {
        return file.nodeCapacity;
    }

    /**
     * Returns the percentage between 0 and 0.5, used to calculate minimum number of entries
     * present in each node.
     */
    public double getFillFactor() {
        return file.fillFactor;
    }

    /**
     * Returns the data dimension.
     */
    public int getDimension() {
        return file.dimension;
    }

    /**
     * Returns the page length.
     */
    public int getPageSize() {
        return file.pageSize;
    }

    /**
     * Returns the level of the root Node, which signifies the level of the whole tree. Loads one
     * page into main memory.
     */
    public int getTreeLevel() {
        return file.readNode(0).getLevel();
    }

    /**
     * Returns the RTree variant used.
     */
    public int getTreeType() {
        return file.treeType;
    }

    /**
     * Inserts a HyperCube into the tree, pointing to the data stored at the given page number.
     *
     * @param h    The hypercube to insert.
     * @param page The page where the real data is stored.
     * @return The page number of the Leaf where the hypercube was inserted (the parent of the
     *         data entry.)
     */
    public int insert(HyperCube h, int page) {
        if (h == null) {
            throw new IllegalArgumentException("HyperCube cannot be null.");
        }

        if (h.getDimension() != file.dimension) {
            throw new IllegalArgumentException("HyperCube dimension different than RTree dimension.");
        }

        AbstractNode root = file.readNode(0);

        Leaf l = root.chooseLeaf(h);
        return l.insert(h, page);
    }

    /**
     * Deletes a HyperCube from the leaf level of the tree. If there is no leaf containg a hypercube
     * that matches the given hypercube, the tree is left intact.
     *
     * @param h The HyperCube to delete.
     * @return The data pointer of the deleted entry, NIL if no matching entry was found.
     */
    public int delete(HyperCube h, int page) {
        if (h == null) {
            throw new IllegalArgumentException("HyperCube cannot be null.");
        }

        if (h.getDimension() != file.dimension) {
            throw new IllegalArgumentException("HyperCube dimension different than RTree dimension.");
        }

        AbstractNode root = file.readNode(0);

        Leaf l = root.findLeaf(h, page);
        if (l != null) {
            return l.delete(h, page);
        }
        return NIL;
    }

    /**
     * Returns a Vector containing all tree nodes from bottom to top, left to right.
     * CAUTION: If the tree is not memory resident, all nodes will be loaded into main memory.
     *
     * @param root The node from which the traverse should begin.
     * @return A Vector containing all Nodes in the correct order.
     */
    public Vector traverseByLevel(AbstractNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }

        Vector ret = new Vector();
        Vector v = traversePostOrder(root);

        for (int i = 0; i <= getTreeLevel(); i++) {
            Vector a = new Vector();
            for (int j = 0; j < v.size(); j++) {
                Node n = (Node) v.elementAt(j);
                if (n.getLevel() == i) {
                    a.addElement(n);
                }
            }
            for (int j = 0; j < a.size(); j++) {
                ret.addElement(a.elementAt(j));
            }
        }

        return ret;
    }

    /**
     * Returns an Enumeration containing all tree nodes from bottom to top, left to right.
     *
     * @return An Enumeration containing all Nodes in the correct order.
     */
    public Enumeration traverseByLevel() {
        class ByLevelEnum implements Enumeration {
            // there is at least one node, the root node.
            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public ByLevelEnum() {
                AbstractNode root = file.readNode(0);
                nodes = traverseByLevel(root);
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("traverseByLevel");
                }

                Object n = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return n;
            }
        }
        ;

        return new ByLevelEnum();
    }

    /**
     * Post order traverse of tree nodes.
     * CAUTION: If the tree is not memory resident, all nodes will be loaded into main memory.
     *
     * @param root The node where the traversing should begin.
     * @return A Vector containing all tree nodes in the correct order.
     */
    public Vector traversePostOrder(AbstractNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }

        Vector v = new Vector();
        v.addElement(root);

        if (root.isLeaf()) {
        } else {
            for (int i = 0; i < root.usedSpace; i++) {
                Vector a = traversePostOrder(((Index) root).getChild(i));
                for (int j = 0; j < a.size(); j++) {
                    v.addElement(a.elementAt(j));
                }
            }
        }
        return v;
    }

    /**
     * Post order traverse of all tree nodes, begging with root.
     * CAUTION: If the tree is not memory resident, all nodes will be loaded into main memory.
     *
     * @return An Enumeration containing all tree nodes in the correct order.
     */
    public Enumeration traversePostOrder() {
        class PostOrderEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public PostOrderEnum() {
                AbstractNode root = file.readNode(0);
                nodes = traversePostOrder(root);
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("traversePostOrder");
                }

                Object n = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return n;
            }
        }
        ;

        return new PostOrderEnum();
    }

    /**
     * Pre order traverse of tree nodes.
     * CAUTION: If the tree is not memory resident, all nodes will be loaded into main memory.
     *
     * @param root The node where the traversing should begin.
     * @return A Vector containing all tree nodes in the correct order.
     */
    public Vector traversePreOrder(AbstractNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Node cannot be null.");
        }

        Vector v = new Vector();

        if (root.isLeaf()) {
            v.addElement(root);
        } else {
            for (int i = 0; i < root.usedSpace; i++) {
                Vector a = traversePreOrder(((Index) root).getChild(i));
                for (int j = 0; j < a.size(); j++) {
                    v.addElement(a.elementAt(j));
                }
            }
            v.addElement(root);
        }
        return v;
    }

    /**
     * Pre order traverse of all tree nodes, begging with root.
     * CAUTION: If the tree is not memory resident, all nodes will be loaded into main memory.
     *
     * @return An Enumeration containing all tree nodes in the correct order.
     */
    public Enumeration traversePreOrder() {
        class PreOrderEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public PreOrderEnum() {
                AbstractNode root = file.readNode(0);
                nodes = traversePreOrder(root);
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("traversePreOrder");
                }

                Object n = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return n;
            }
        }
        ;

        return new PreOrderEnum();
    }

    /**
     * Returns a Vector with all nodes that intersect with the given HyperCube.
     * The nodes are returned in post order traversing
     *
     * @param h    The given HyperCube that is tested for overlapping.
     * @param root The node where the search should begin.
     * @return A Vector containing the appropriate nodes in the correct order.
     */
    public Vector intersection(HyperCube h, AbstractNode root) {
        if (h == null || root == null) {
            throw new IllegalArgumentException("Arguments cannot be null.");
        }

        if (h.getDimension() != file.dimension) {
            throw new IllegalArgumentException("HyperCube dimension different than RTree dimension.");
        }

        Vector v = new Vector();

        if (root.getNodeMbb().intersection(h)) {
            v.addElement(root);

            if (!root.isLeaf()) {
                for (int i = 0; i < root.usedSpace; i++) {
                    if (root.data[i].intersection(h)) {
                        Vector a = intersection(h, ((Index) root).getChild(i));
                        for (int j = 0; j < a.size(); j++) {
                            v.addElement(a.elementAt(j));
                        }
                    }
                }
            }
        }
        return v;
    }

    //init matrix
    public void initMatrix(boolean[][] matrix){
    	int length = segmentList.size();
    	for(int i = 0; i < length; i++){
    		matrix[i][i] = true;
    		PLASegment curSegment = segmentList.get(i);
    		Polygon2D probePoly = curSegment.getPolygonKB();
  		  	Box2D box = probePoly.getBoundingBox();
  		  	Point p1 = new Point(new double[]{box.getMinX(), box.getMinY(), -Double.MAX_VALUE});
  		  	Point p2 = new Point(new double[]{box.getMaxX(), box.getMaxY(), Double.MAX_VALUE});
  		  	HyperCube h = new HyperCube(p1, p2);
  		  	initMatrixInterSection(file.readNode(0), matrix, probePoly, h, i);
    	}
    	;
    }
    
    public void initMatrixInterSection(AbstractNode n, boolean[][] matrix, Polygon2D probePoly, HyperCube h, int start){
    		if(n.isLeaf()){
    			for(int i = 0; i < n.usedSpace; i++){
    				if(n.branches[i] > start && n.data[i].intersection(h)){
    					PLASegment tempSeg = segmentList.get(n.branches[i]);
    					Polygon2D tempPoly = tempSeg.getPolygonKB();
    					Polygon2D intersection = Polygon2DUtils.intersection(probePoly, tempPoly);
    					 if(intersection.getVertexNumber() > 0){
    						  matrix[start][tempSeg.idx] = true;
    						  matrix[tempSeg.idx][start] = true;
    					  }
    				}
    			}
    		}else{
    			for (int i = 0; i < n.usedSpace; i++) {
                    if (n.data[i].intersection(h)) {
                    	initMatrixInterSection(((Index) n).getChild(i), matrix, probePoly, h, start);                    	
                    }
                }
    		}
    }
    
    //given a segment, return its upperbound
    
    public int calUpperBound(PLASegment probeSeg, int currentIdx){
    	this.currentSeg = probeSeg;
  	  	this.currentPoly = probeSeg.getPolygonKB();
  	  	Box2D box = currentPoly.getBoundingBox();
  	  	Point p1 = new Point(new double[] {box.getMinX(), box.getMinY(), currentIdx});
  	  	Point p2 = new Point(new double[] {box.getMaxX(), box.getMaxY(), Double.MAX_VALUE});
  	  	HyperCube h = new HyperCube(p1, p2);
  	  	AbstractNode node = file.readNode(0);
  	  	return upperBoundInterSection(node, h);
    }
    
    public int upperBoundInterSection(AbstractNode n, HyperCube h){
    	int upperBound = 0;
    	if(n.isLeaf()){
    		for(int i = 0; i < n.usedSpace; i++){
    			if(n.data[i].intersection(h)){
    				PLASegment tempSeg = segmentList.get(n.branches[i]);
    				Polygon2D tempPoly = tempSeg.getPolygonKB();
    				Polygon2D intersection = Polygon2DUtils.intersection(this.currentPoly, tempPoly);
    				if(intersection.getVertexNumber() > 0){
				 		if (tempSeg.getStart() > currentSeg.getEnd()) {
				 			upperBound += tempSeg.getLength();
			            }else{
			            	upperBound += tempSeg.getEnd() - currentSeg.getEnd();
			            }
				 	}
    			}
    		}
    	}else{
    		for(int i = 0; i < n.usedSpace; i++) {
                if (n.data[i].intersection(h)) {
                	upperBound += upperBoundInterSection(((Index) n).getChild(i), h);                    	
                }
            }
    	}
    	return upperBound;
    }
    
    /**
     * Returns an Enumeration with all nodes present in the tree that intersect with the given
     * HyperCube. The nodes are returned in post order traversing
     *
     * @param h The given HyperCube that is tested for overlapping.
     * @return An Enumeration containing the appropriate nodes in the correct order.
     */
    public Enumeration intersection(HyperCube h) {
        class IntersectionEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector nodes;

            private int index = 0;

            public IntersectionEnum(HyperCube hh) {
                nodes = intersection(hh, file.readNode(0));
                if (nodes.isEmpty()) {
                    hasNext = false;
                }
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) {
                    throw new NoSuchElementException("intersection");
                }

                Object c = nodes.elementAt(index);
                index++;
                if (index == nodes.size()) {
                    hasNext = false;
                }
                return c;
            }
        }
        ;

        return new IntersectionEnum(h);
    }

    /**
     * Returns a Vector with all Hypercubes that completely contain HyperCube <B>h</B>.
     * The HyperCubes are returned in post order traversing, according to the Nodes where
     * they belong.
     *
     * @param h    The given HyperCube.
     * @param root The node where the search should begin.
     * @return A Vector containing the appropriate HyperCubes in the correct order.
     */
    public Vector enclosure(HyperCube h, AbstractNode root) {
        if (h == null || root == null) throw new
                IllegalArgumentException("Arguments cannot be null.");

        if (h.getDimension() != file.dimension) throw new
                IllegalArgumentException("HyperCube dimension different than RTree dimension.");

        Vector v = new Vector();

        if (root.getNodeMbb().enclosure(h)) {
            v.addElement(root);

            if (!root.isLeaf()) {
                for (int i = 0; i < root.usedSpace; i++) {
                    if (root.data[i].enclosure(h)) {
                        Vector a = enclosure(h, ((Index) root).getChild(i));
                        for (int j = 0; j < a.size(); j++) {
                            v.addElement(a.elementAt(j));
                        }
                    }
                }
            }
        }
        return v;
    }

    /**
     * Returns an Enumeration with all Hypercubes present in the tree that contain the given
     * HyperCube. The HyperCubes are returned in post order traversing, according to the Nodes where
     * they belong.
     *
     * @param h    The given HyperCube.
     * @param root The node where the search should begin.
     * @return An Enumeration containing the appropriate HyperCubes in the correct order.
     */
    public Enumeration enclosure(HyperCube h) {
        class ContainEnum implements Enumeration {
            private boolean hasNext = true;

            private Vector cubes;

            private int index = 0;

            public ContainEnum(HyperCube hh) {
                cubes = enclosure(hh, file.readNode(0));
                if (cubes.isEmpty()) {
                    hasNext = false;
                }
            }

            public boolean hasMoreElements() {
                return hasNext;
            }

            public Object nextElement() {
                if (!hasNext) throw new
                        NoSuchElementException("enclosure");

                Object c = cubes.elementAt(index);
                index++;
                if (index == cubes.size()) {
                    hasNext = false;
                }
                return c;
            }
        }
        ;

        return new ContainEnum(h);
    }

    /**
     * Returns a Vector with all Hypercubes that completely contain point <B>p</B>.
     * The HyperCubes are returned in post order traversing, according to the Nodes where
     * they belong.
     *
     * @param p    The given point.
     * @param root The node where the search should begin.
     * @return A Vector containing the appropriate HyperCubes in the correct order.
     */
    public Vector enclosure(Point p, AbstractNode root) {
        return enclosure(new HyperCube(p, p), root);
    }

    /**
     * Returns an Enumeration with all Hypercubes present in the tree that contain the given
     * point. The HyperCubes are returned in post order traversing, according to the Nodes where
     * they belong.
     *
     * @param p    The query point.
     * @param root The node where the search should begin.
     * @return An Enumeration containing the appropriate HyperCubes in the correct order.
     */
    public Enumeration enclosure(Point p) {
        return enclosure(new HyperCube(p, p));
    }

    /**
     * Returns the nearest HyperCube to the given point.
     * [King Lum Cheung and Ada Wai-chee Fu: Enhanced Nearest Neighbor Search on the R-Tree]
     *
     * @param p The query point.
     * @return A vector containing all the nodes lying in the search path until the nearest hypercube
     *         is found. Elements are instances of AbstractNode and Data classes. The last Data instance
     *         in the vector is the answer to the query.
     */
    public Vector nearestNeighbor(Point p) {
        return nearestNeighborSearch(file.readNode(0), p, Double.POSITIVE_INFINITY);
    }

    /**
     * Used for nearest neighbor recursive search into the RTree structure. <B>n</B> is the current
     * node of the active branch list, searched. <B>p</B> is the query point. <B>nearest</B> is the
     * distance of <B>p</B> from current nearest hypercube <B>h</B>.
     */
    protected Vector nearestNeighborSearch(AbstractNode n, Point p, double nearest) {
        Vector ret = new Vector();
        HyperCube h;

        if (n.isLeaf()) {
            for (int i = 0; i < n.usedSpace; i++) {
                double dist = n.data[i].getMinDist(p);
                if (dist < nearest) {
                    h = n.data[i];
                    nearest = dist;
                    ret.addElement(new Data(h, n.branches[i], n.pageNumber, i));
                }
            }
            return ret;
        } else {
            // generate Active Branch List.
            class ABLNode {
                AbstractNode node;
                double minDist;

                public ABLNode(AbstractNode node, double minDist) {
                    this.node = node;
                    this.minDist = minDist;
                }
            }

            ABLNode[] abl = new ABLNode[n.usedSpace];

            for (int i = 0; i < n.usedSpace; i++) {
                AbstractNode ch = ((Index) n).getChild(i);
                abl[i] = new ABLNode(ch, ch.getNodeMbb().getMinDist(p));
            }

            // sort ABL in ascending order of MINDIST from the query point.
            Sort.mergeSort(
                    abl,
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            double f = ((ABLNode) o1).minDist -
                                    ((ABLNode) o2).minDist;

                            // do not round the double here. It is wrong!
                            if (f > 0) return 1;
                            else if (f < 0) return -1;
                            else return 0;
                        }
                    }
            );

            // traverse all ABL nodes and prune irrelevant nodes according to the MINDIST heuristic.
            for (int i = 0; i < abl.length; i++) {
                if (abl[i].minDist <= nearest) {
                    // add node in the results vector, if it complies to the MINDIST heuristic.
                    ret.addElement(abl[i].node);
                    // recursively continue the search.
                    Vector v = nearestNeighborSearch(abl[i].node, p, nearest);

                    // find the new nearest distance and add all the nodes accessed, into the current
                    // results vector.
                    try {
                        Object o = v.lastElement();
                        if (o instanceof AbstractNode) {
                            for (int j = 0; j < v.size(); j++) {
                                ret.addElement(v.elementAt(j));
                            }
                            AbstractNode an = (AbstractNode) o;
                            h = (HyperCube) an.getNodeMbb();
                            nearest = h.getMinDist(p);
                        } else if (o instanceof Data) {
                            // if the current node searched was a leaf, than the resulting set definetly
                            // contains just one HyperCube, the nearest to the query point.
                            h = ((Data) o).mbb;
                            nearest = h.getMinDist(p);
                            ret.addElement(o);
                        }
                    } catch (NoSuchElementException e) {
                        // no nearest node or hypercube was found from this recursion step. Leave nearest
                        // distance intact.
                    }
                }
            }

            return ret;
        }
    }

} // RTree
