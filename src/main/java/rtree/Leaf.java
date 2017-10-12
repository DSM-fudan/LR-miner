package rtree;

import java.util.*;

/**
 * A Leaf node. Containts pointers to the real data.
 * <p>
 * Created: Tue May 18 13:04:08 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.002
 */
public class Leaf extends AbstractNode {
    
    protected Leaf(RTree tree, int parent, int pageNumber) {
	// Leaf nodes belong by default to level 0.
	super(tree, parent, pageNumber, 0);
    }

    protected Leaf(RTree tree, int parent) {
	// Leaf nodes belong by default to level 0.
	super(tree, parent, -1, 0);
    }

    protected Leaf chooseLeaf(HyperCube h) {
	return this;
    }

    protected Leaf findLeaf(HyperCube h, int page) {
	for (int i = 0; i < usedSpace; i++) {
	    if (data[i].enclosure(h) && branches[i] == page) {
		return this;
	    }
	}

	return null;
    }

    /**
     * Inserts a new HyperCube into the Leaf. If there is space left, then the insertion is straightforward and
     * adjustTree must be called. Otherwise, a split must occur and two new leaves must be created.
     *
     * @param h The HyperCube that represents the MBB of the data.
     * @param page The page number where the real data resides.
     * @return The page number of the Leaf where the hypercube was inserted.
     */
    protected int insert(HyperCube h, int page) {
	if (usedSpace < tree.getNodeCapacity()) {
	    data[usedSpace] = h;
	    branches[usedSpace] = page;
	    usedSpace++;
	    tree.file.writeNode(this);
	    Index p = (Index) getParent();
	    if (p != null) {
		p.adjustTree(this, null);
	    }
	    return pageNumber;
	} else {
	    Leaf[] a = splitLeaf(h, page);
	    Leaf l = a[0];
	    Leaf ll = a[1];

	    if (isRoot()) {
		// root is full, so we must split it. From now on root will be
		// an Index and not a Leaf.
		l.parent = 0;
		l.pageNumber = -1;
		ll.parent = 0;
		ll.pageNumber = -1;
		tree.file.writeNode(l);
		tree.file.writeNode(ll);
		// create the new root node. It belongs to pageNumber 0 and level 1.
		Index r = new Index(tree, RTree.NIL, 0, 1);
		r.addData(l.getNodeMbb(), l.pageNumber);
		r.addData(ll.getNodeMbb(), ll.pageNumber);
		tree.file.writeNode(r);
	    } else {
		// use old page number for left child, a new page number for the right child.
		l.pageNumber = pageNumber;
		ll.pageNumber = -1;
		tree.file.writeNode(l);
		tree.file.writeNode(ll);
		Index p = (Index) getParent();
		p.adjustTree(l, ll);
	    }

	    // Find which is the parent Leaf of the inserted hypercube and return it's page number.
	    for (int i = 0; i < l.usedSpace; i++) {
		if (l.branches[i] == page) {
		    return l.pageNumber;
		}
	    }

	    for (int i = 0; i < ll.usedSpace; i++) {
		if (ll.branches[i] == page) {
		    return ll.pageNumber;
		}
	    }

	    return -1;
	}
    }

    /**
     * Deletes an entry for the leaf.
     * [A. Guttman 'R-trees a dynamic index structure for spatial searching']
     * 3.3. Deletion
     *
     * @param h  The hypercube that corresponds to the entry to be deleted.
     * @return The data pointer of the deleted entry.
     **/
    protected int delete(HyperCube h, int page) {
	for (int i = 0; i < usedSpace; i++) {
	    if (data[i].equals(h) && branches[i] == page) {
		int pointer = branches[i];
		deleteData(i);
		tree.file.writeNode(this);
		Vector q = new Vector();
		condenseTree(q);

		// reinsert eliminated leaves.
		for (int l = 0; l < q.size(); l++) {
		    AbstractNode n = (AbstractNode) q.elementAt(l);
		    if (n.isLeaf()) {
			for (int j = 0; j < n.usedSpace; j++) {
			    tree.insert(n.data[j], n.branches[j]);
			}
		    } else {
			// this is not what Gutman says, but it will work anyway...
			Vector v = tree.traversePostOrder(n);
			for (int j = 0; j < v.size(); j++) {
			    AbstractNode m = (AbstractNode) v.elementAt(j);
			    if (m.isLeaf()) {
				for (int k = 0; k < m.usedSpace; k++) {
				    tree.insert(m.data[k], m.branches[k]);
				}
			    }
			    tree.file.deletePage(m.pageNumber);
			}
		    }
		    tree.file.deletePage(n.pageNumber);
		}
		
		return pointer;
	    }
	}
	return RTree.NIL;
    }

    private Leaf[] splitLeaf(HyperCube h, int page) {
	int[][] group = null;

	switch (tree.getTreeType()) {
	case RTree.RTREE_LINEAR:
	    break;
	case RTree.RTREE_QUADRATIC:
	    group = quadraticSplit(h, page);
	    break;
	case RTree.RTREE_EXPONENTIAL:
	    break;
	case RTree.RSTAR:
	    break;
	default:
	    throw new IllegalStateException("Invalid tree type.");
	}

	Leaf l = new Leaf(tree, parent);
	Leaf ll = new Leaf(tree, parent);

	int[] g1 = group[0];
	int[] g2 = group[1];

	for (int i = 0; i < g1.length; i++) {
	    l.addData(data[g1[i]], branches[g1[i]]);
	}
	    
	for (int i = 0; i < g2.length; i++) {
	    ll.addData(data[g2[i]], branches[g2[i]]);
	}

	return new Leaf[] {l, ll};
    }

    /**
     * Returns the pointer of the <i>i-th</i> data entry.
     *
     * @param  i The index of the child in the data array.
     * @return The pointer of the <i>i-th</i> child.
     */
    public int getDataPointer(int i) {
	if (i < 0 || i >= usedSpace) {
	    throw new IndexOutOfBoundsException("" + i);
	}

	return branches[i];
    }

} // Leaf
