package cn.edu.fudan.dsm.tslrm;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-14
 * Time: 下午11:44
 * To change this template use File | Settings | File Templates.
 */
public class PLASegment {
    public PLASegment()
    {
    }

    public PLASegment(PLASegment org)
    {
        this.idx = org.idx;
        this.totalLength = org.totalLength;
        this.start = org.start;
        this.end = org.end;
        this.currentPolygon = org.currentPolygon;
        this.polygonKB = org.polygonKB;
        this.length = org.length;
        this.polygonYM = org.polygonYM;
    }

    public int idx = -1;
    public int index = -1;
    public PLASegment nextSegment = null;
    public PLASegment nextDisjointSegment = null;
    public PLASegment nextStartSegment = null;
    public Polygon2D currentPolygon;
    private int start = -1;
    private int end = -1;
    private int length;

    public int totalLength;

    private double startX;
    private double startY;

    private Polygon2D polygonKB;

    private Polygon2D polygonYM;

    private Point2D centroid;

    private int lowerBound;
    private int upBound;
    private boolean isDelete;
    
    public double getY() {
        return getCentroid().getX();
    }

    public double getM() {
        return getCentroid().getY();
    }

    public double getSlope() {
        return getM();
    }

    public double getIntercept() {
        return getY() - getM() * getStartX();
    }

    public double estimate(double x) {
        return getSlope() * x + getIntercept();
    }

    public double getK() {
        return getPolygonKB().getCentroid().getX();
    }

    public double getB() {
        return getPolygonKB().getCentroid().getY();
    }

    @Override
    public String toString() {
        if (polygonYM != null) {
            double y = getCentroid().getX();
            double m = getCentroid().getY();
            return "[" + getStart() + "," + getEnd() + "],length=" + getLength() + ",[y=" + toString(y) + "," + "m=" + toString(m) + "],[k=" + toString(getSlope()) + "," + "b=" + toString(getIntercept()) + "],[" + toString(getPolygonYM()) + "]";
        } else
            return "[" + getStart() + "," + getEnd() + "],length=" + getLength();
    }

    public static String toString(Polygon2D polygon2D) {
        StringBuilder sb = new StringBuilder();
        Collection<Point2D> vertices = polygon2D.getVertices();
        for (Point2D next : vertices) {
            sb.append("(").append(toString(next.getX())).append(",").append(toString(next.getY())).append(")");
        }
        return sb.toString();
    }

    public static String toString(double value) {
        return String.format("%.4f", value);
    }

    public static void main(String[] args) {
        System.out.println(toString(1.23232323));
    }

    public static boolean verify(Point2D[] points, List<PLASegment> plaSegmentList, double errorBound) {
        for (int k = 0; k < plaSegmentList.size(); k++) {
            PLASegment next = plaSegmentList.get(k);
            for (int i = next.getStart(); i <= next.getEnd(); i++) {
                double x = points[i].getX();
                double y = points[i].getY();

                double ey = next.estimate(x);
                double error = Math.abs(ey - y);

                if (error > errorBound) {
                    System.out.println("error = " + error);
                    System.out.println("error k = " + k);
                    System.out.println("next = " + next);
                    System.out.println("next.getSlope() = " + next.getSlope());
                    System.out.println("next.getIntercept() = " + next.getIntercept());
                    System.out.println("i = " + i);
                    System.out.println("x = " + x);
                    System.out.println("y = " + y);
                    System.out.println("ey = " + ey);

                    System.out.println("error = " + error);
                    return false;
                }
            }
        }
        return true;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
        this.length = end - start + 1;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
        this.length = end - start + 1;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public Polygon2D getPolygonYM() {
        return polygonYM;
    }

    public void setPolygonYM(Polygon2D polygonYM) {
        this.polygonYM = polygonYM;
        this.centroid = null;
    }

    public Point2D getCentroid() {
        if (centroid == null) {
            centroid = polygonYM.getCentroid();
        }
        return centroid;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    
    public int getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

	
	public int getUpBound() {
		return upBound;
	}

	public void setUpBound(int upBound) {
		this.upBound = upBound;
	}

	
	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public boolean verify(Point2D[] points, double errorBound) {
        for (int i = start; i <= end; i++) {
            Point2D point = points[i];
            double x = point.getX();
            double y = point.getY();

            double estimate = estimate(x);
            double error = Math.abs(estimate - y);


            if (error > errorBound) {
                System.out.println("this = " + this);

                System.out.println("error = " + error);
                System.out.println("points i = " + i);
                System.out.println("x = " + x);
                System.out.println("y = " + y);
                System.out.println("estimate = " + estimate);

                return false;
            }
        }

        return true;
    }

    List<PLASegment> children = new ArrayList<PLASegment>();
    
    public Polygon2D getPolygonKB() {
        return polygonKB;
    }

    public void setPolygonKB(Polygon2D polygonKB) {
        this.polygonKB = polygonKB;
    }

    public String toStringKB() {
        return "[" + getStart() + "," + getEnd() + "],length=" + getLength() + ",[k=" + toString(getK()) + "," + "b=" + toString(getB()) + "],[" + toString(getPolygonKB()) + "]";

    }
}
