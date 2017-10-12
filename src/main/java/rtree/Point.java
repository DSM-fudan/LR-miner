package rtree;

/**
 * A point in the n-dimensional space. All dimensions are stored in an array of Doubles.
 * <p>
 * Created: Tue May 18 15:01:38 1999
 * <p>
 * @author Hadjieleftheriou Marios
 * @version 1.1
 */
public class Point extends Object implements Cloneable {
    private double[] data;

    public Point(double[] d) {
	if (d == null) throw new
	    IllegalArgumentException("Coordinates cannot be null.");

	if (d.length < 2) throw new
	    IllegalArgumentException("Point dimension should be greater than 1.");

	data = new double[d.length];
	System.arraycopy(d, 0, data, 0, d.length);
    }

    public Point(int[] d) {
	if (d == null) throw new
	    IllegalArgumentException("Coordinates cannot be null.");

	if (d.length < 2) throw new
	    IllegalArgumentException("Point dimension should be greater than 1.");

	data = new double[d.length];

	for (int i = 0; i < d.length; i++) {
	    data[i] = (double) d[i];
	}
    }

    public double getDoubleCoordinate(int i) {
	return data[i];
    }

    public int getIntCoordinate(int i) { 
	return (int) data[i];
    }

    public int getDimension() {
	return data.length;
    }

    public boolean equals(Point p) {
	if (p.getDimension() != getDimension()) {
	    throw new IllegalArgumentException("Points must be of equal dimensions to be compared.");
	}

	boolean ret = true;
	for (int i = 0; i < getDimension(); i++) {
	    if (getDoubleCoordinate(i) != p.getDoubleCoordinate(i)) {
		ret = false;
		break;
	    }
	}
	return ret;
    }

    public Object clone() {
	double[] f = new double[data.length];
	System.arraycopy(data, 0, f, 0, data.length);

	return new Point(f);
    }

    public String toString() {
	String s = "(" + data[0];
	
	for (int i = 1; i < data.length; i++) {
	    s += ", " + data[i];
	}

	return s + ")";
    }
	
} // Point
