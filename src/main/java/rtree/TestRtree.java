package rtree;

import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: wangyang
 * Date: 12-2-24
 * Time: 上午11:20
 * To change this template use File | Settings | File Templates.
 */
public class TestRtree {
//    public static void main(String[] args) {
//        //CachedPersistentPageFile file = new CachedPersistentPageFile("TestRtree.dat", 100);
//        MemoryPageFile file = new MemoryPageFile();
//        RTree r = new RTree(4, 0.4f, 5, file, RTree.RTREE_QUADRATIC);
//        //RTree r = new RTree(file);
//        double[] f = {10, 20, 40, 70,
//        		10, 20, 40, 70,
//                30, 10, 70, 15,
//                100, 70, 110, 80,
//                0, 50, 30, 55,
//                13, 21, 54, 78,
//                3, 8, 23, 34,
//                200, 29, 202, 50,
//                34, 1, 35, 1,
//                201, 200, 234, 203,
//                56, 69, 58, 70,
//                12, 67, 70, 102,
//                1, 10, 10, 20
//        };
//
//        for (int i = 0; i < f.length;) {
//            Point p1 = new Point(new double[] {f[i++],f[i++],f[i++],f[i++]});
//            Point p2 = p1;
//            final HyperCube h = new HyperCube(p1, p2);
//            r.insert(h, i/4);
//        }
//
//        for (Enumeration e = r.traverseByLevel(); e.hasMoreElements();) {
//            System.out.println(e.nextElement());
//        }
        
//        Point delP1 = new Point(new double[] {10, 20, 40, 70});
//        final HyperCube delCube = new HyperCube(delP1, delP1);
////        int delNum = r.delete(delCube, 2);
////        System.out.println("del Cube Number:" + delNum);
//        System.out.println("InterSection Test:");
//        for (Enumeration e = r.intersection(delCube); e.hasMoreElements();) {
//            System.out.println(e.nextElement());
//        }
//      HyperCube h = new HyperCube(new Point(new double[] {10, 15}), new Point(new double[] {23, 24}));
//
//      for (Enumeration e = r.intersection(h); e.hasMoreElements();) {
//          System.out.println(e.nextElement());
//      }

//      Point p = new Point(new double[] {f[20],f[21],f[22],f[23]});
//      System.out.print("Nearest to " + p + " is ");
//      System.out.println(r.nearestNeighbor(p));
//    }
}
