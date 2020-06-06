package cn.edu.fudan.dsm.tslrm;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-14
 * Time: 下午11:01
 * To change this template use File | Settings | File Templates.
 */
public class PLAPointBoundKBMiner {
    public PLAPointBoundKBMiner(Point2D[] point2Ds, double errorBound) {
        this.points = point2Ds;
        this.pointErrorBound = errorBound;
    }

    public static void main(String[] args) {
        double x[] = {1, 2, 3, 6, 14, 1, 1, 2, 3, 4, 2, 1, 4, 2, 8, 7, 9, 6, 8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6, 5, 6, 1, 2, 3, 4, 1, 2, 3, 4,};
        double y[] = {4, 3, 8, 13, 9, 1, 6, 8, 10, 12, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12, 20, 8, 6, 8, 10, 12, 6, 8, 10, 12,};
        System.out.println("x.length = " + x.length);
        System.out.println("y.length = " + y.length);

        double errorBound = 1;
        PLAPointBoundKBMiner miner = new PLAPointBoundKBMiner(x, y, errorBound);

        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
            PLASegment segment = miner.plaSegmentList.get(i);
            System.out.println("segment = " + segment.toStringKB());
        }

//        boolean verify = PLASegment.verify(miner.points, miner.plaSegmentList, errorBound);
//        System.out.println("verify = " + verify);

        findMaxIntersection(miner.plaSegmentList);
    }

    /**
     * sort segment list by length asc
     *
     * @param segments
     */
    public static void sortSegmentList(List<PLASegment> segments) {
        //sort asc
        Collections.sort(segments, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o1.getLength() - o2.getLength();
            }
        });
    }

    public static void findMaxIntersection(List<PLASegment> segments) {
        List<PLASegment> list = new ArrayList<PLASegment>();
        //remove segment whose length <= 2
        for (PLASegment next : segments) {
            next.children.add(next);

            if (next.getLength() > 2) {
                list.add(next);
            }
        }

        //sort list by segment length
        sortSegmentList(list);

        System.out.println("==================");
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list.get(i) = " + list.get(i).toStringKB());
        }

        //find O(n*n)
        while (list.size() > 1) {
            PLASegment remove = list.remove(0);
            Polygon2D removePolygon = remove.getPolygonKB();

            for (int i = 0; i < list.size(); i++) {
                PLASegment seg = list.get(i);

                Polygon2D segPolygonKB = seg.getPolygonKB();
                Polygon2D intersection = Polygons2D.intersection(removePolygon, segPolygonKB);

                if (intersection.vertexNumber() > 0) //has intersected
                {
                    PLASegment newSeg = new PLASegment();

                    newSeg.setPolygonKB(intersection);
                    newSeg.children.addAll(seg.children);
                    newSeg.setLength(seg.getLength());

                    //remove duplicated segment
                    for (PLASegment child : remove.children) {
                        if (!newSeg.children.contains(child)) {
                            newSeg.setLength(newSeg.getLength() + child.getLength());
                            newSeg.children.add(child);
                        }
                    }
                    list.set(i, newSeg); //replace the old
                }
            }

            sortSegmentList(list);
            System.out.println("==================");
            for (int i = 0; i < list.size(); i++) {
                System.out.println("list.get(i) = " + list.get(i).toStringKB());
            }
        }

        PLASegment plaSegment = list.get(0);
        System.out.println("plaSegment = " + plaSegment.toStringKB());
        for(PLASegment child : plaSegment.children)
        {
            System.out.println("child = " + child.toStringKB());
        }
    }

    Point2D[] points;

    List<PLASegment> plaSegmentList;

    public void process() {

        plaSegmentList = new ArrayList<PLASegment>();

        //algorithm begin
        int i = 0;
        int j = i + 1;
        PLASegment plaSegment = new PLASegment();
        plaSegment.setStart(i);
        plaSegment.setEnd(i);
        plaSegment.setStartX(points[i].x());
        plaSegment.setStartY(points[i].y());
        plaSegment.setPolygonKB(polygonKBOfPoint(points[i]));

//        boolean verify = plaSegment.verify(points, pointErrorBound);

        while (j < points.length) {
            Polygon2D p1 = polygonKBOfPoint(points[j]);
            Polygon2D p = Polygons2D.intersection(plaSegment.getPolygonKB(), p1);

            if (p.vertexNumber() <= 0) //intersection is null
            {
                plaSegmentList.add(plaSegment);

                i = j;
                j = j + 1;
                plaSegment = new PLASegment();
                plaSegment.setStart(i);
                plaSegment.setEnd(i);
                plaSegment.setStartX(points[i].x());
                plaSegment.setStartY(points[i].y());
                plaSegment.setPolygonKB(polygonKBOfPoint(points[i]));
            } else {
                plaSegment.setEnd(j);
                plaSegment.setPolygonKB(p);

//                verify = plaSegment.verify(points, pointErrorBound);

                j = j + 1;
            }
        }

        plaSegmentList.add(plaSegment);
    }

    private Polygon2D polygonKBOfPoint(Point2D point) {
        double x = point.x();
        double y = point.y();
        double k[] = X_INF;
        double b[] = {(y - pointErrorBound - D_MIN * x), (y - pointErrorBound - D_MAX * x), (y + pointErrorBound - D_MAX * x), (y + pointErrorBound - D_MIN * x)};
        return new SimplePolygon2D(k, b);
    }

    public final double D_MAX = 100000;
//    public final double D_MAX = Long.MAX_VALUE / 2;
    public final double D_MIN = -D_MAX;
//    public final double D_MIN = Long.MIN_VALUE / 2;

    public final double[] X_INF = {D_MIN, D_MAX, D_MAX, D_MIN};
    public final double[] Y_INF = {D_MIN, D_MIN, D_MAX, D_MAX};

/*
    private Polygon2D polygonYMOfPoint(Point2D point) {
        double y[] = {point.getY() - pointErrorBound, point.getY() + pointErrorBound, point.getY() + pointErrorBound, point.getY() - pointErrorBound};
        double m[] = Y_INF;
        return new SimplePolygon2D(y, m);
    }

    private Polygon2D polygonYMOf2Points(Point2D point1, Point2D point2) {
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();

        double y[] = {y1 - pointErrorBound, y1 + pointErrorBound, y1 + pointErrorBound, y1 - pointErrorBound};
        if (x1 == x2) {
            Interval interval1 = new Interval(y1 - pointErrorBound, y1 + pointErrorBound);
            Interval interval2 = new Interval(y2 - pointErrorBound, y2 + pointErrorBound);

            Interval interval3 = intersection(interval1, interval2);
            if (interval3 != null) {                    //the range of y is intersected
                y[0] = interval3.getLower();
                y[1] = interval3.getUpper();
                y[2] = interval3.getUpper();
                y[3] = interval3.getLower();
                return new SimplePolygon2D(y, Y_INF);
            } else {
                return new SimplePolygon2D();
            }
        } else if (x1 < x2) {
            //y2 - e < y1i + m * (x2-x1) < y2 + e
            //left-bottom,right-bottom,right-top,left-top

            //m0:y1-e
            double m0 = (y2 - y1) / (x2 - x1);
            //m1:y1+e
            double m1 = (y2 - y1 - 2 * pointErrorBound) / (x2 - x1);
            //m2:y1+e
            double m2 = (y2 - y1) / (x2 - x1);
            //m3:y1-e
            double m3 = (y2 - y1 + 2 * pointErrorBound) / (x2 - x1);

            double m[] = {m0, m1, m2, m3};
            return new SimplePolygon2D(y, m);
        } else {    //x1 > x2
            //y2 - e < y1i + m * (x2-x1) < y2 + e
            //left-bottom,right-bottom,right-top,left-top

            //m0:y1-e
            double m0 = (y2 - y1 + 2 * pointErrorBound) / (x2 - x1);
            //m1:y1+e
            double m1 = (y2 - y1) / (x2 - x1);
            //m2:y1+e
            double m2 = (y2 - y1 - 2 * pointErrorBound) / (x2 - x1);
            //m3:y1-e
            double m3 = (y2 - y1) / (x2 - x1);

            double m[] = {m0, m1, m2, m3};
            return new SimplePolygon2D(y, m);
        }
    }
*/

    double[] x;
    double[] y;

    double pointErrorBound;

    public PLAPointBoundKBMiner(double[] x, double[] y, double pointErrorBound) {
        this.x = x;
        this.y = y;
        this.pointErrorBound = pointErrorBound;
        //init points
        points = new Point2D[x.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point2D(x[i], y[i]);
        }
    }

/*
    public Interval intersection(Interval interval1, Interval interval2) {
        double lower = Math.max(interval1.getLower(), interval2.getLower());
        double upper = Math.min(interval1.getUpper(), interval2.getUpper());

        if (upper >= lower) {
            return new Interval(lower, upper);
        } else {
            return null;
        }
    }
*/
}
