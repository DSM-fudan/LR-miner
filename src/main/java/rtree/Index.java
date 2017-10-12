package rtree;

/**
 * Internal node of the RTree. Used to access Leaf nodes, where real data lies.
 * <p>
 * Created: Tue May 18 13:04:25 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.001
 */
public class Index extends AbstractNode {
    
    protected Index(RTree tree, int parent, int pageNumber, int level) {
	super(tree, parent, pageNumber, level);
    }

    protected Leaf chooseLeaf(HyperCube h) {
	int i;

	switch (tree.getTreeType()) {
	case RTree.RTREE_LINEAR:
	case RTree.RTREE_QUADRATIC:
	case RTree.RTREE_EXPONENTIAL:
	    i = findLeastEnlargement(h);
	    break;
	case RTree.RSTAR:
	    if (level == 1) {
		// if this node points to leaves...
		i = findLeastOverlap(h);
	    } else {
		i = findLeastEnlargement(h);
	    }
	    break;
	default:
	    throw new IllegalStateException("Invalid tree type.");
	}
	
	return ((AbstractNode) getChild(i)).chooseLeaf(h);
    }

    protected Leaf findLeaf(HyperCube h, int page) {
	for (int i = 0; i < usedSpace; i++) {
	    if (data[i].enclosure(h)) {
		Leaf l = ((AbstractNode) getChild(i)).findLeaf(h, page);
		if (l != null) {
		    return l;
		}
	    }
	}

	return null;
    }

    /**
     * Add the new HyperCube to all mbbs present in this node. Calculate the area difference and
     * choose the entry with the least enlargement. Based on that metric we choose the path that
     * leads to the leaf that will hold the new HyperCube.
     * [A. Guttman 'R-trees a dynamic index structure for spatial searching']
     *
     * @return The index of the branch of the path that leads to the Leaf where the new HyperCube 
     *         should be inserted.
     */
    private int findLeastEnlargement(HyperCube h) {
	double area = Double.POSITIVE_INFINITY;
	int sel = -1;

	for (int i = 0; i < usedSpace; i++) {
	    double enl = data[i].getUnionMbb(h).getArea() - data[i].getArea();
	    if (enl < area) {
		area = enl;
		sel = i;
	    } else if (enl == area) {
		sel = (data[sel].getArea() <= data[i].getArea()) ? sel : i;
	    }
	}
	return sel;
    }

    /**
     * R*-tree criterion for choosing the best branch to follow.
     * [Beckmann, Kriegel, Schneider, Seeger 'The R*-tree: An efficient and Robust Access Method
     *  for Points and Rectangles]
     *
     * @return The index of the branch of the path that leads to the Leaf where the new HyperCube
     *         should be inserted.
     */
    private int findLeastOverlap(HyperCube h) {
	double overlap = Double.POSITIVE_INFINITY;
	int sel = -1;

	for (int i = 0; i < usedSpace; i++) {
	    AbstractNode n = (AbstractNode) getChild(i);
	    double o = 0;
	    for (int j = 0; j < n.data.length; j++) {
		o += h.intersectingArea(n.data[j]);
	    }
	    if (o < overlap) {
		overlap = o;
		sel = i;
	    } else if (o == overlap) {
		double area1 = data[i].getUnionMbb(h).getArea() - data[i].getArea();
		double area2 = data[sel].getUnionMbb(h).getArea() - data[sel].getArea();

		if (area1 == area2) {
		    sel = (data[sel].getArea() <= data[i].getArea()) ? sel : i;
		} else {
		    sel = (area1 < area2) ? i : sel;
		}
	    }
	}
	return sel;
    }

    /**
     * Inserts a new node into the tree. If enought space is available
     * the insertion is straighforward. If not, a split must occur.
     *
     * @param  node The node that should be inserted.
     *
     * @return True if a split occurred, false otherwise.
     */
    protected boolean insert(AbstractNode node) {
	if (usedSpace < tree.getNodeCapacity()) {
	    data[usedSpace] = node.getNodeMbb();
	    branches[usedSpace] = node.pageNumber;
	    usedSpace++;
	    node.parent = pageNumber;
	    tree.file.writeNode(node);
	    tree.file.writeNode(this);
	    Index p = (Index) getParent();
	    if (p != null) {
		p.adjustTree(this, null);
	    }
	    return false;
	} else {
	    Index[] a = splitIndex(node);
	    Index n = a[0];
	    Index nn = a[1];

	    if (isRoot()) {
		n.parent = 0;
		n.pageNumber = -1;
		nn.parent = 0;
		nn.pageNumber = -1;
		int p = tree.file.writeNode(n);
		for (int i = 0; i < n.usedSpace; i++) {
		    AbstractNode ch = (AbstractNode) n.getChild(i);
		    ch.parent = p;
		    tree.file.writeNode(ch);
		}
		p = tree.file.writeNode(nn);
		for (int i = 0; i < nn.usedSpace; i++) {
		    AbstractNode ch = (AbstractNode) nn.getChild(i);
		    ch.parent = p;
		    tree.file.writeNode(ch);
		}
		Index r = new Index(tree, RTree.NIL, 0, level+1);
		r.addData(n.getNodeMbb(), n.pageNumber);
		r.addData(nn.getNodeMbb(), nn.pageNumber);
		tree.file.writeNode(r);
	    } else {
		n.pageNumber = pageNumber;
		n.parent = parent;
		nn.pageNumber = -1;
		nn.parent = parent;
		tree.file.writeNode(n);
		int j = tree.file.writeNode(nn);
		for (int i = 0; i < nn.usedSpace; i++) {
		    AbstractNode ch = (AbstractNode) nn.getChild(i);
		    ch.parent = j;
		    tree.file.writeNode(ch);
		}
		Index p = (Index) getParent();
		p.adjustTree(n, nn);
	    }

	    return true;
	}
    }

    /**
     * Called by insert to adjust the parents of the node that was modified by the insertion. Stops
     * when it reaches the root.
     *
     * @param  n1  The child node that caused the need for adjustment.
     * @param  n2  If a split occured in the child node, this is the new node that must be inserted. 
     *             Otherwise, this is null.
     */
    protected void adjustTree(AbstractNode n1, AbstractNode n2) {
	// find entry pointing to old node;
	for (int i = 0; i < usedSpace; i++) {
	    if (branches[i] == n1.pageNumber) {
		data[i] = n1.getNodeMbb();
		break;
	    }
	}

	// if a split has occured we must insert the new node, else we must continue adjusting the tree
	// until we hit the root.
	if (n2 != null) {
	    insert(n2);
	} else if (! isRoot()) {
	    Index p = (Index) getParent();
	    p.adjustTree(this, null);
	}
    }

    private Index[] splitIndex(AbstractNode n) {
	int[][] group = null;

	switch (tree.getTreeType()) {
	case RTree.RTREE_LINEAR:
	    break;
	case RTree.RTREE_QUADRATIC:
	    group = quadraticSplit(n.getNodeMbb(), n.pageNumber);
	    break;
	case RTree.RTREE_EXPONENTIAL:
	    break;
	case RTree.RSTAR:
	    break;
	default:
	    throw new IllegalStateException("Invalid tree type.");
	}

	Index i1 = new Index(tree, parent, pageNumber, level);
	Index i2 = new Index(tree, parent, -1, level);

	int[] g1 = group[0];
	int[] g2 = group[1];

	for (int i = 0; i < g1.length; i++) {
	    i1.addData(data[g1[i]], branches[g1[i]]);
	}
	    
	for (int i = 0; i < g2.length; i++) {
	    i2.addData(data[g2[i]], branches[g2[i]]);
	}

	return new Index[] {i1, i2};
    }

    /**
     * Retrieves the <B>i-th</B> child node. Loads one page into main memory.
     *
     * @param  i The index of the child in the data array.
     * @return The i-th child.
     */
    public AbstractNode getChild(int i) {
	if (i < 0 || i >= usedSpace) {
	    throw new IndexOutOfBoundsException("" + i);
	}

	return tree.file.readNode(branches[i]);
    }
    
} // Index
