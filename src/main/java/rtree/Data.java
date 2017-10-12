package rtree;

/**
 * Created: Tue Dec 07 16:04:30 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.000
 */
public class Data {
    public HyperCube mbb;
    public int dataPointer;
    public int parent;
    public int position;

    public Data(HyperCube h, int d, int p, int pos) {
	mbb = h;
	dataPointer = d;
	parent = p;
	position = pos;
    }
    
} // Data
