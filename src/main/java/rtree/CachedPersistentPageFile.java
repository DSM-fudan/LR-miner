package rtree;

import java.util.*;

/**
 * A cached persistent page file with a Least Recently Used (LRU) caching strategy.<br>
 * (PENDING: Use a sorted list on rankings here, for better performance.)
 * <p>
 * Created: Sun Oct 31 19:22:05 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.001
 */
public class CachedPersistentPageFile extends PersistentPageFile {
    private Hashtable cache;
    private int usedSpace = 0;
    private int cacheSize;

    public CachedPersistentPageFile(String fileName, int cacheSize) {
	super(fileName);
	this.cacheSize = cacheSize;
	cache = new Hashtable(cacheSize);
    }

    /*
    protected byte[] readData(int page) throws PageFaultError {
	Object o = readFromCache(page);
	if (null != o) {
	    return (byte[]) o;
	} else {
	    return super.readData(page);
	}
    }

    protected int writeData(byte[] d, int page) throws PageFaultError {
	int i = super.writeData(d, page);
	writeToCache(d, i);
	return i;
    }
    */

    public AbstractNode readNode(int page) throws PageFaultError {
	AbstractNode n = (AbstractNode) readFromCache(page);
	if (null != n) {
	    return n;
	} else {
	    return super.readNode(page);
	}
    }

    protected int writeNode(AbstractNode n) throws PageFaultError {
	int page = super.writeNode(n);
	writeToCache(n, page);
	return page;
    }

    private Object readFromCache(int page) {
	CachedObject c = (CachedObject) cache.get(new Integer(page));
	if (c != null) {
	    int rank = c.rank;
	    for (Enumeration e = cache.elements(); e.hasMoreElements();) {
		CachedObject co = (CachedObject) e.nextElement();
		if (co.rank > rank) {
		    co.rank--;
		} else if (co.rank == rank) {
		    co.rank = usedSpace-1;
		}
	    }
	    return c.object;
	} else {
	    return null;
	}
    }

    private void writeToCache(Object o, int page) {
	CachedObject c = (CachedObject) cache.get(new Integer(page));

	if (null != c) {
	    // if object already in cache.
	    c.object = o;
	    int rank = c.rank;
	    for (Enumeration e = cache.elements(); e.hasMoreElements();) {
		CachedObject co = (CachedObject) e.nextElement();
		if (co.rank > rank) {
		    co.rank--;
		} else if (co.rank == rank) {
		    co.rank = usedSpace-1;
		}
	    }
	} else if (usedSpace < cacheSize) {
	    // if cache is not full yet.
	    cache.put(new Integer(page), new CachedObject(o, page, usedSpace));
	    usedSpace++;
	    return;
	} else {
	    // if cache is full.
	    for (Enumeration e = cache.elements(); e.hasMoreElements();) {
		c = (CachedObject) e.nextElement();
		if (c.rank == 0) {
		    cache.remove(new Integer(c.page));
		    break;
		}
	    }

	    for (Enumeration e = cache.elements(); e.hasMoreElements();) {
		c = (CachedObject) e.nextElement();
		c.rank--;
	    }

	    cache.put(new Integer(page), new CachedObject(o, page, usedSpace-1));
	}
    }

    protected AbstractNode deletePage(int page) throws PageFaultError {
	CachedObject c = (CachedObject) cache.get(new Integer(page));
	if (c != null) {
	    int rank = c.rank;

	    for (Enumeration e = cache.elements(); e.hasMoreElements();) {
		c = (CachedObject) e.nextElement();

		if (c.rank > rank) {
		    c.rank--;
		}
	    }

	    cache.remove(new Integer(page));
	    usedSpace--;
	}

	return super.deletePage(page);
    }

    class CachedObject {
	int rank = 0;
	int page = -1;
	Object object;

	public CachedObject(Object o, int page, int rank) {
	    this.object = o;
	    this.page = page;
	    this.rank = rank;
	}
    }
    
} // CachedPersistentPageFile
