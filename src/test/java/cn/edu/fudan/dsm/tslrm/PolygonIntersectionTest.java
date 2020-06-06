package cn.edu.fudan.dsm.tslrm;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-14
 * Time: 下午10:07
 * To change this template use File | Settings | File Templates.
 */
public class PolygonIntersectionTest {
    public static void main(String[] args) {
        Polygon2D polygon1 = new SimplePolygon2D(new double[]{1,2,2,1},new double[]{1,1,2,2});
        Polygon2D polygon2 = new SimplePolygon2D(new double[]{1.5,2.5,2.5,1.5},new double[]{1.5,1.5,2.5,2.5});
        polygon1.boundingBox();
        //t1(polygon1, polygon2);

         polygon1 = new SimplePolygon2D(new double[]{-1,-2,-2,-1},new double[]{1,1,2,2});
         polygon2 = new SimplePolygon2D(new double[]{1.5,2.5,2.5,1.5},new double[]{1.5,1.5,2.5,2.5});

        t1(polygon1, polygon2);



    }

    private static void t1(Polygon2D polygon1, Polygon2D polygon2) {
        Polygon2D polygon3 = Polygons2D.intersection(polygon1, polygon2);
        Collection<Point2D> vertices = polygon3.vertices();
        for (Point2D next : vertices) {
            System.out.println("next.getX() = " + next.x());
            System.out.println("next.getY() = " + next.y());

        }

        String s = polygon3.toString();
        System.out.println("s = " + s);
    }
}
