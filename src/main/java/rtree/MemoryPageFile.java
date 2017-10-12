package rtree;

import java.util.*;

/**
 * Implements a memory page file. Holds all pages into a Hashtable with keys equal to each object's
 * page number.
 * <p>
 * Created: Wed May 19 09:33:26 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.002
 */
public class MemoryPageFile extends PageFile {
    private Hashtable file = new Hashtable();

    protected void initialize(RTree tree, int dimension, double fillFactor, int capacity, int treeType) {
	super.initialize(tree, dimension, fillFactor, capacity, treeType);
	file.clear();
    }

    /*
    protected byte[] readData(int page) throws PageFaultError {
	if (page < 0) {
	    throw new IllegalArgumentException("Page number cannot be negative.");
	}

	byte[] ret = (byte[]) file.get(new Integer(page));

	if (ret == null) {
	    throw new PageFaultError("Invalid page number request.");
	}

	return ret;
    }

    protected int writeData(byte[] d, int page) throws PageFaultError {
	if (d == null) {
	    throw new IllegalArgumentException("Data cannot be null.");
	}

	if (page < 0) {
	    page = 0;
	    while (true) {
		if (! file.containsKey(new Integer(page))) {
		    break;
		}
		page++;
	    }
	}
	
	file.put(new Integer(page), d);

	return page;
    }
    */

    public AbstractNode readNode(int page) throws PageFaultError {
	if (page < 0) {
	    throw new IllegalArgumentException("Page number cannot be negative.");
	}

	AbstractNode ret = (AbstractNode) file.get(new Integer(page));

	if (ret == null) {
	    throw new PageFaultError("Invalid page number request.");
	}

	return ret;
    }

    protected int writeNode(AbstractNode n) throws PageFaultError {
	if (n == null) {
	    throw new IllegalArgumentException("Node cannot be null.");
	}

	int i = 0;
	if (n.pageNumber < 0) {
	    while (true) {
		if (! file.containsKey(new Integer(i))) {
		    break;
		}
		i++;
	    }
	    n.pageNumber = i;
	} else {
	    i = n.pageNumber;
	}

	file.put(new Integer(i), n);

	return i;
    }

    protected AbstractNode deletePage(int page) throws PageFaultError {
	return (AbstractNode) file.remove(new Integer(page));
    }

    public void dumpMemory() {
	for (Enumeration e = file.elements(); e.hasMoreElements();) {
	    System.out.println(e.nextElement());
	}
    }

} // MemoryPageFile
