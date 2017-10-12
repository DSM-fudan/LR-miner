package rtree;

/**
 * Created: Sat May 22 00:19:29 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.0
 */
public class PageFaultError extends Error {

    public PageFaultError() {
	super();
    }

    public PageFaultError(String s) {
	super(s);
    }
    
} // PageFaultError
