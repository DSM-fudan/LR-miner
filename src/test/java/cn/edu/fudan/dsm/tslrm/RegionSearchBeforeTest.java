package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;
import cn.edu.fudan.dsm.tslrm.data.DataGenerator;
import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import math.geom2d.Point2D;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-11-12
 * Time: 下午6:49
 * To change this template use File | Settings | File Templates.
 */
public class RegionSearchBeforeTest {
    @Test
    public void testRegionSearch() throws IOException {
        String fileName = "D:\\data\\TEM-Record-1960-2012(Length = 20000).txt";
//        String fileName = "E:\\climateByType\\SURF_CLI_CHN_MUL_DAY-TEM-1960-2012.txt";
//        String fileName = "data\\test_1000_4.txt";
//        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromTem(new File(fileName), 0, 3);
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
        System.out.println("PointNum = " + point2Ds.length);
        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

//        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
//            PLASegment segment = miner.plaSegmentList.get(i);
//        }

        long time = System.currentTimeMillis();
        List<PLASegment> segmentList = miner.buildSpecificSegments(3);
        time = System.currentTimeMillis() - time;
        System.out.println("Build Segment Time = " + time);
        System.out.println("all segmentList.size() = " + segmentList.size());

        RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
        plaRegionSearch.errorBound = errorBound;

//        List<PLASegment> segs = SegmentUtils.filter(segmentList, 3, 3);
        System.out.println("segs.size() = " + segmentList.size());

        time = System.currentTimeMillis();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segmentList, 0.05);
        time = System.currentTimeMillis() - time;
        System.out.println("Region Search Time = " + time);
        System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
        System.out.println("point2Ds1.getY() = " + point2Ds1.getY());
        int realLength = SegmentUtils.verifyTrueLength(point2Ds, point2Ds1.getX(), point2Ds1.getY(), errorBound, 3);
        System.out.println("RealLength = " + realLength);
//        plaRegionSearch.search()
//
//        double error = 0;
//        int startLength = 0;
//        //按最小segment长度递降求解
//        for (int i = m; i >= 0; i--) {
//            int minSegmentLength = (int) (3 * Math.pow(2, i));
//            System.out.println("minSegmentLength = " + minSegmentLength);
//            List<PLASegment> segs = SegmentUtils.filter(segmentList, minSegmentLength, minSegmentLength);
//            System.out.println("segs.size() = " + segs.size());
//            startLength = plaExtendDeepSearch.search(segs, startLength, error);
//            System.out.println("maxLength of  " + minSegmentLength + " :" + startLength);
//            if (startLength > 0) {
//                startLength = SegmentUtils.verifyTrueLength(point2Ds, plaExtendDeepSearch.slope, plaExtendDeepSearch.intercept, errorBound, 3);
//                System.out.println("true maxLength of  " + minSegmentLength + " :" + startLength);
//            }
//        }
//        System.out.println("maxLength = " + startLength);
    }
    
    public void testRegionSearchWithSpecific() throws IOException {
        String fileName = "D:\\data\\TEM-1960-2012.txt";
        String fileResult = "D:\\data\\result_length-running.txt";
        PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
        String line1 = "0";	// Time series length
        String line2 = "0"; 	// Build segment time
        String line3 = "0";	// Find maxLength time
//        String fileName = "E:\\climateByType\\SURF_CLI_CHN_MUL_DAY-TEM-1960-2012.txt";
//        String fileName = "data\\test_1000_4.txt";
        int length = 2000;
        for(int i = 0; i < 10; i++){
        	Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);
            System.out.println("PointNum = " + point2Ds.length);
            double errorBound = 1;

            TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
            miner.process();
            System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

//            for (int i = 0; i < miner.plaSegmentList.size(); i++) {
//                PLASegment segment = miner.plaSegmentList.get(i);
//            }

            long time1 = System.currentTimeMillis();
            List<PLASegment> segmentList = miner.buildSpecificSegments(3);
            time1 = System.currentTimeMillis() - time1;
            System.out.println("Build Segment Time = " + time1);
            System.out.println("all segmentList.size() = " + segmentList.size());

            RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
            plaRegionSearch.errorBound = errorBound;

//            List<PLASegment> segs = SegmentUtils.filter(segmentList, 3, 3);
            System.out.println("segs.size() = " + segmentList.size());

            long time2 = System.currentTimeMillis();
            Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segmentList, 0.05);
            time2 = System.currentTimeMillis() - time2;
            System.out.println("Region Search Time = " + time2);
            System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
            System.out.println("point2Ds1.getY() = " + point2Ds1.getY());
            int realLength = SegmentUtils.verifyTrueLength(point2Ds, point2Ds1.getX(), point2Ds1.getY(), errorBound, 3);
            System.out.println("RealLength = " + realLength);
            
            line1 += " " + length;
            line2 += " " + time1;
            line3 += " " + time2;
            length += 2000;
        }
        
        pw.println(line1);
        pw.println(line2);
        pw.println(line3);
        
//        plaRegionSearch.search()
//
//        double error = 0;
//        int startLength = 0;
//        //按最小segment长度递降求解
//        for (int i = m; i >= 0; i--) {
//            int minSegmentLength = (int) (3 * Math.pow(2, i));
//            System.out.println("minSegmentLength = " + minSegmentLength);
//            List<PLASegment> segs = SegmentUtils.filter(segmentList, minSegmentLength, minSegmentLength);
//            System.out.println("segs.size() = " + segs.size());
//            startLength = plaExtendDeepSearch.search(segs, startLength, error);
//            System.out.println("maxLength of  " + minSegmentLength + " :" + startLength);
//            if (startLength > 0) {
//                startLength = SegmentUtils.verifyTrueLength(point2Ds, plaExtendDeepSearch.slope, plaExtendDeepSearch.intercept, errorBound, 3);
//                System.out.println("true maxLength of  " + minSegmentLength + " :" + startLength);
//            }
//        }
//        System.out.println("maxLength = " + startLength);
    }


}
