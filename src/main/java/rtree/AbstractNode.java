package rtree;

import java.util.*;
import java.io.*;

/**
 * Implements basic functions of Node interface. Also implements splitting functions.
 * <p>
 * Created: Tue May 18 15:57:56 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.1
 */
public abstract class AbstractNode implements Node {
    /** Level of this node. Leaves always have a level equal to 0. */
    protected int level;

    /** Parent of all nodes. */
    protected transient RTree tree;

    /** The pageNumber where the parent of this node is stored. */
    protected int parent;

    /** The pageNumber where this node is stored. */
    public int pageNumber;

    /**
     * All node data are stored into this array. It must have a size of <B>nodeCapacity + 1</B> to hold
     * all data plus an overflow HyperCube, when the node must be split.
     */
    protected HyperCube[] data;

    /**
     * Holds the pageNumbers containing the children of this node. Always has same size with the data array.
     * If this is a Leaf node, than all branches should point to the real data objects.
     */
    public int[] branches;

    /** How much space is used up into this node. If equal to nodeCapacity then node is full. */
    public int usedSpace;

//
// Initialization.
//

    protected AbstractNode(RTree tree, int parent, int pageNumber, int level) {
	this.parent = parent;
	this.tree = tree;
	this.pageNumber = pageNumber;
	this.level = level;
	data = new HyperCube[tree.getNodeCapacity()+1];
	branches = new int[tree.getNodeCapacity()+1];
	usedSpace = 0;
    }

//
// Node interface.
//

    /**
     * Returns the node level. Always zero for leaf nodes.
     *
     * @return Level of node.
     */
    public int getLevel() {
	return level;
    }

    /**
     *  Returns true if this node is the root node.
     */
    public boolean isRoot() {
	return (parent == RTree.NIL);
    }

    /**
     *  Returns true if this node is an Index. Root node is an Index too, unless it is a Leaf.
     */
    public boolean isIndex() {
	return (level != 0);
    }

    /**
     * Returns true if this node is a Leaf. Root may be a Leaf too.
     */
    public boolean isLeaf() {
	return (level == 0);
    }

    /**
     * Returns the mbb of all HyperCubes present in this node.
     *
     * @return A new HyperCube object, representing the mbb of this node.
     */
    public HyperCube getNodeMbb() {
	if (usedSpace > 0) {
	    HyperCube[] h = new HyperCube[usedSpace];
	    System.arraycopy(data, 0, h, 0, usedSpace);
	    return HyperCube.getUnionMbb(h);
	} else {
	    return new HyperCube(new Point(new double[] {0, 0}), new Point(new double[] {0, 0}));
	}
    }

    /**
     * Returns a unique id for this node. The page number is unique for every node but
     * it doen't signify anything about where in the path from the root this node lies, it is random.
     * This function returns a <I>point<I> separated list of page numbers from the root up to the
     * current node. It is expensive thought, because it loads into main memory all the nodes lying
     * on the path from the root up to this node.
     *
     * @return A string representing a unique id for this node.
     *
    public String getUniqueId() {
	if (!isRoot()) {
	    AbstractNode n = tree.file.readNode(parent);
	    int[] b = n.branches;

	    int i;
	    for (i = 0; i < n.usedSpace; i++) {
		if (b[i] == pageNumber) break;
	    }

	    return n.getUniqueId() + "." + i;
	} else {
	    return "";
	}
    }
*/

    /**
     * Returns a unique id for this node. The page number is unique for every node.
     *
     * @return A string representing a unique id for this node.
     */
    public String getUniqueId() {
	return Integer.toString(pageNumber);
    }

    /**
     * Returns the parent of this node. If there is a parent, it must be an Index. 
     * If this node is the root, returns null. This function loads one disk page into
     * main memory.
     */
    public AbstractNode getParent() {
	if (isRoot()) {
	    return null;
	} else {
	    return tree.file.readNode(parent);
	}
    }

    /**
     * Return a copy of the HyperCubes present in this node.
     *
     * @return An array of HyperCubes containing copies of the original data.
     */
    public HyperCube[] getHyperCubes() {
	HyperCube[] h = new HyperCube[usedSpace];

	for (int i = 0; i < usedSpace; i++) {
	    h[i] = (HyperCube) data[i].clone();
	}

	return h;
    }

    public String toString() {
	String s = "< Page: " + pageNumber + ", Level: " + level + ", UsedSpace: " + usedSpace + ", Parent: " + parent + " >\n";

	for (int i = 0; i < usedSpace; i++) {
	    s += "  " + (i+1) + ") " + data[i].toString() + " --> " + " page: " + branches[i] + "\n";
	}
	
	return s;
    }

//
// Abstract functions.
//

    /**
     * chooseLeaf finds the most appropriate leaf where the given HyperCube should be stored.
     *
     * @param h The new HyperCube.
     *
     * @return The leaf where the new HyperCube should be inserted.
     */
    protected abstract Leaf chooseLeaf(HyperCube h);

    /**
     * findLeaf returns the leaf that contains the given hypercube, null if the hypercube is not
     * contained in any of the leaves of this node.
     *
     * @param h The HyperCube to search for.
     *
     * @return The leaf where the HyperCube is contained, null if such a leaf is not found.
     */
    protected abstract Leaf findLeaf(HyperCube h, int page);

//
// Insertion/Deletion functions.
//

    /**
     * Adds a child node into this node.
     * This function does not save the node into persistent storage. It is used for bulk loading
     * a node whith data. The user must make sure that she saves the node into persistent storage, after
     * calling this function.
     *
     * @param n The new node to insert as a child of the current node.
     *
     */
    protected void addData(AbstractNode n) {
	addData(n.getNodeMbb(), n.pageNumber);
    }

    /**
     * Adds a child node into this node.
     * This function does not save the node into persistent storage. It is used for bulk loading
     * a node whith data. The user must make sure that she saves the node into persistent storage, after
     * calling this function.
     *
     * @param h The HyperCube to add.
     * @param page The page where this HyperCube is located, if any.
     */
    protected void addData(HyperCube h, int page) {
	if (usedSpace == tree.getNodeCapacity()) {
	    throw new IllegalStateException("Node is full.");
	}

	data[usedSpace] = h;
	branches[usedSpace] = page;
	usedSpace++;
    }

    /**
     * Deletes a data entry from this node.
     * This function does not save the node into persistent storage. The user must make sure that 
     * she saves the node into persistent storage, after calling this function.
     *
     * @param i  The index of the data entry to be deleted.
     */
    protected void deleteData(int i) {
	System.arraycopy(data, i+1, data, i, usedSpace-i-1);
	System.arraycopy(branches, i+1, branches, i, usedSpace-i-1);
	usedSpace--;
    }

    /**
     * Quadratic algorithm for spliting a node.
     * [A. Guttman 'R-trees a dynamic index structure for spatial searching']
     *
     * @param  h  The overflow HyperCube that caused the split.
     * @param  page The page where the child node that caused the split is stored. -1 if the split
     *         occurs in a Leaf node.
     */
    protected int[][] quadraticSplit(HyperCube h, int page) {
	if (h == null) {
	    throw new IllegalArgumentException("Hypercube cannot be null.");
	}

	// temporarily insert new hypercube into data, for common manipulation. Data array is always
	// by one larger than node capacity.
	data[usedSpace] = h;
	branches[usedSpace] = page;
	int total = usedSpace + 1;

	// use this mask array for marking visited entries.
	int[] mask = new int[total];
	for (int i = 0; i < total; i++) {
	    mask[i] = 1;
	}

	// each group will have at most totla/2 entries. Account for odd numbers too.
	int c = total/2 + 1;
	// calculate minimun number of entries a node must contain.
	int min = (int)Math.round(tree.getNodeCapacity() * tree.getFillFactor());
	// at least two nodes (in case the user selects a zero fill factor.)
	if (min < 2) min = 2;
	// count how many more entries are left unchecked.
	int rem = total;

	// create two groups of entries, for spliting the node in two.
	int[] g1 = new int[c];
	int[] g2 = new int[c];
	// keep track of the last item inserted into each group.
	int i1 = 0, i2 = 0;

	// initialize each group with the seed entries.
	int[] seed = pickSeeds();
	g1[i1++] = seed[0];
	g2[i2++] = seed[1];
	rem -= 2;
	mask[g1[0]] = -1;
	mask[g2[0]] = -1;

	while (rem > 0) {
	    if (min - i1 == rem) {
		// all remaining entries must be assigned to g1 to comply with minimun fill factor.
		for (int i = 0; i < total; i++) {
		    if (mask[i] != -1) {
			g1[i1++] = i;
			mask[i] = -1;
			rem--;
		    }
		}
	    } else if (min - i2 == rem) {
		// all remaining entries must be assigned to g2 to comply with minimun fill factor.
		for (int i = 0; i < total; i++) {
		    if (mask[i] != -1) {
			g2[i2++] = i;
			mask[i] = -1;
			rem--;
		    }
		}
	    } else {
		// find mbr of each group.
		HyperCube mbr1 = (HyperCube) data[g1[0]].clone();
		for (int i = 1; i < i1; i++) {
		    mbr1 = mbr1.getUnionMbb(data[g1[i]]);
		}
		HyperCube mbr2 = (HyperCube) data[g2[0]].clone();
		for (int i = 1; i < i2; i++) {
		    mbr2 = mbr2.getUnionMbb(data[g2[i]]);
		}

		// for each entry not already assigned to a group, determine the cost of putting it in
		// either one and select the one with the maximun difference between the two costs.
		double dif = Double.NEGATIVE_INFINITY;
		double d1 = 0, d2 = 0;
		int sel = -1;
		for (int i = 0; i < total; i++) {
		    if (mask[i] != -1) {
			HyperCube a = mbr1.getUnionMbb(data[i]);
			d1 = a.getArea() - mbr1.getArea();
			HyperCube b = mbr2.getUnionMbb(data[i]);
			d2 = b.getArea() - mbr2.getArea();
			if (Math.abs(d1 - d2) > dif) {
			    dif = Math.abs(d1 - d2);
			    sel = i;
			}
		    }
		}

		// determine the group where we should add the new entry.
		if (d1 < d2) {
		    g1[i1++] = sel;
		} else if (d2 < d1) {
		    g2[i2++] = sel;
		} else if (mbr1.getArea() < mbr2.getArea()) {
		    g1[i1++] = sel;
		} else if (mbr2.getArea() < mbr1.getArea()) {
		    g2[i2++] = sel;
		} else if (i1 < i2) {
		    g1[i1++] = sel;
		} else if (i2 < i1) {
		    g2[i2++] = sel;
		} else {
		    g1[i1++] = sel;
		}
		mask[sel] = -1;
		rem--;
	    }
	}

	// return the two groups. Let the subclass decide what to do with them.
	int[][] ret = new int[2][];
	ret[0] = new int[i1];
	ret[1] = new int[i2];

	for (int i = 0; i < i1; i++) {
	    ret[0][i] = g1[i];
	}
	for (int i = 0; i < i2; i++) {
	    ret[1][i] = g2[i];
	}

	return ret;
    }

    /*
    protected Node[] rstarSplit(HyperCube h, int page) {
	// chooseSplitAxis.
	for (int i = 0; i < tree.getDimension(); i++) {
	    HyperCube[] data = getData();
	    final int dim = i;
	    Sort.mergeSort(
	        data,		
		new Comparator() {
			public int compare(Object o1, Object o2) {
			    double ret = ((HyperCube) o1).getP1().getDoubleCoordinate(dim) -
				        ((HyperCube) o2).getP1().getDoubleCoordinate(dim);
			    if (ret > 0) return 1;
			    else if (ret < 0) return -1;
			    else  return 0;
			}
		    }
		);
	    for (int j = 0; j < data.length; j++)
	        System.out.println(data[j]);
	}
	return null;
    }
    */

    /**
     * pickSeeds is used by  split to initialize the two groups of HyperCubes.
     * [A. Guttman 'R-trees a dynamic index structure for spatial searching']
     * 3.5.2. A Quadratic-Cost Algorithm
     *
     * @return  The two indices of the selected entries to be the first elements of the groups.
     */
    protected int[] pickSeeds() {
	double inefficiency = Double.NEGATIVE_INFINITY;
	int i1 = 0, i2 = 0;
	
	// for each pair of HyperCubes (account for overflow HyperCube too!)
	for (int i = 0; i < usedSpace; i++) {
	    for (int j = i+1; j <= usedSpace; j++) {
		// get the mbr of those two entries.
		HyperCube h = data[i].getUnionMbb(data[j]);
		
		// find the inefficiency of grouping these entries together.
		double d = h.getArea() - data[i].getArea() - data[j].getArea();
		
		if (d > inefficiency) {
		    inefficiency = d;
		    i1 = i;
		    i2 = j;
		}
	    }
	}
	
	return new int[] {i1, i2};
    }

    /**
     * Used to condense the tree after an entry has been deleted.
     * [A. Guttman 'R-trees a dynamic index structure for spatial searching']
     * 3.3. Deletion
     **/
    protected void condenseTree(Vector q) {
	if (isRoot()) {
	    // eliminate root if it has only one child.
	    if (! isLeaf() && usedSpace == 1) {
		AbstractNode n = tree.file.readNode(branches[0]);
		tree.file.deletePage(n.pageNumber);
		n.pageNumber = 0;
		n.parent = RTree.NIL;
		tree.file.writeNode(n);
		if (! n.isLeaf()) {
		    for (int i = 0; i < n.usedSpace; i++) {
			AbstractNode m = ((Index) n).getChild(i);
			m.parent = 0;
			tree.file.writeNode(m);
		    }
		}
	    }
	} else {
	    // find the parent.
	    AbstractNode p = getParent();
	    int e;

	    // find the entry in the parent, that points to this node.
	    for (e = 0; e < p.usedSpace; e++) {
		if (pageNumber == p.branches[e]) {
		    break;
		}
	    }

	    int min = (int)Math.round(tree.getNodeCapacity() * tree.getFillFactor());
	    if (usedSpace < min) {
		// eleminate node.
		p.deleteData(e);
		q.addElement(this);
	    } else {
		// adjust the entry in 'p' to contain the new bounding box of this node.
		p.data[e] = getNodeMbb();
	    }

	    tree.file.writeNode(p);
	    p.condenseTree(q);
	}
    }

} // AbstractNode


