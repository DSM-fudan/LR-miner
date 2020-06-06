package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.DataGenerator;
import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import math.geom2d.Point2D;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-17
 * Time: 下午1:07
 * To change this template use File | Settings | File Templates.
 */
public class TSPLAPointBoundKBMinerTest {
    @Test
    public void testSearch() {
        double x[] = {1, 2, 3, 6, 14, 1, 1, 2, 3, 4, 2, 1, 4, 2, 8, 7, 9, 6, 8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6, 5, 6, 1, 2, 3, 4, 1, 2, 3, 4,};
        double y[] = {4, 3, 8, 13, 9, 1, 6, 8, 10, 12, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12, 20, 8, 6, 8, 10, 12, 6, 8, 10, 12,};
        double errorBound = 1;
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(x, y, errorBound);
        miner.process();

        List<PLASegment> segmentList = miner.buildAllSegments();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());
        TSPLAPointBoundKBMiner.findMaxIntersectionWithApriori(segmentList);

//        TSPLAPointBoundKBMiner.findMaxIntersectionWithStack(segmentList);
    }

    @Test
    public void testVerifyTrueLength() throws IOException {
        String fileName = "data\\test_10000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
        int len = SegmentUtils.verifyTrueLength(point2Ds, 1, 5, 1, 3);
        System.out.println("len = " + len);
    }

    @Test
    public void testSegmentSearch() throws IOException {
        String fileName = "data\\test_1000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
            PLASegment segment = miner.plaSegmentList.get(i);
        }

        List<PLASegment> segmentList = miner.buildAllSegments();
        System.out.println("all segmentList.size() = " + segmentList.size());

        int m = (int) (Math.log(point2Ds.length / 3.0) / Math.log(2) - 1);
        PLAExtendDeepSearch plaExtendDeepSearch = new PLAExtendDeepSearch(point2Ds);

        double error = 0;
        int startLength = 0;
        //按最小segment长度递降求解
        for (int i = m; i >= 0; i--) {
            int minSegmentLength = (int) (3 * Math.pow(2, i));
            System.out.println("minSegmentLength = " + minSegmentLength);
            List<PLASegment> segs = SegmentUtils.filter(segmentList, minSegmentLength, minSegmentLength);
            System.out.println("segs.size() = " + segs.size());
            startLength = plaExtendDeepSearch.search(segs, startLength, error);
            System.out.println("maxLength of  " + minSegmentLength + " :" + startLength);
            if (startLength > 0) {
                startLength = SegmentUtils.verifyTrueLength(point2Ds, plaExtendDeepSearch.slope, plaExtendDeepSearch.intercept, errorBound, 3);
                System.out.println("true maxLength of  " + minSegmentLength + " :" + startLength);
            }
        }
        System.out.println("maxLength = " + startLength);
    }

    @Test
    public void testFindAllCandidateSegment() throws IOException {
        String fileName = "data\\test_randomwalk_1000.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 0.1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
            PLASegment segment = miner.plaSegmentList.get(i);
//            System.out.println("segment = " + segment.toStringKB());
        }

//        System.out.println("==================search with findMaxIntersection======================");
//        TSPLAPointBoundKBMiner.findMaxIntersection(miner.plaSegmentList);


        List<PLASegment> segmentList = miner.buildAllSegments();
        System.out.println("segmentList.size() = " + segmentList.size());

//        TSPLAPointBoundKBMiner.findMaxIntersection(miner.plaSegmentList);
//        for (int i = 0; i < segmentList.size(); i++) {
//            PLASegment segment = segmentList.get(i);
//            System.out.println("segment = " + segment.toStringKB());
//        }
        System.out.println("==================search with apriori====================");
        TSPLAPointBoundKBMiner.findMaxIntersectionWithApriori(segmentList);
//        System.out.println("==================search with stack======================");
//        TSPLAPointBoundKBMiner.findMaxIntersectionWithStack(segmentList);
        System.out.println("==================search with approximate======================");
        TSPLAPointBoundKBMiner.findMaxIntersectionApproximate(segmentList);
        System.out.println("==================search with stack of shortsegmentlist======================");
        List<PLASegment> shortSegmentList = new ArrayList<PLASegment>();
        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if (segment.getLength() <= 5) {
                shortSegmentList.add(segment);
            }
        }
        System.out.println("shortSegmentList.size() = " + shortSegmentList.size());
        TSPLAPointBoundKBMiner.findMaxIntersectionWithStack(shortSegmentList);
        System.out.println("==================search with stack of findMaxIntersectionWithStackWithMatrix======================");

        TSPLAPointBoundKBMiner.findMaxIntersectionWithStackWithMatrix(shortSegmentList);

    }

    @Test
    public void testFindAllCandidateSegment1() throws IOException {
        String fileName = "data\\test1.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

//        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
//            PLASegment segment = miner.plaSegmentList.get(i);
//            System.out.println("segment = " + segment.toStringKB());
//        }

        List<PLASegment> segmentList = miner.buildAllSegments();
        System.out.println("segmentList.size() = " + segmentList.size());
//        for (int i = 0; i < segmentList.size(); i++) {
//            PLASegment segment = segmentList.get(i);
//            System.out.println("segment = " + segment.toStringKB());
//        }


    }
}
