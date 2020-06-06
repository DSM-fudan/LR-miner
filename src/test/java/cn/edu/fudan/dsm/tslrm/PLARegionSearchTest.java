package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;
import math.geom2d.Point2D;
import org.apache.commons.lang.time.StopWatch;
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
public class PLARegionSearchTest {
	
    public void testRegionSearchWithLengthRunning() throws IOException {
        String fileName = "D:\\data\\TEM-Record-1960-2012(Length = 100000).txt";
//        String fileName = "data\\evaporator_5000.txt";
//        String fileName = "data\\test_1000_4.txt";
        String fileResult = "D:\\data\\result_length-running_(initlength=";
        
        String line1 = "0";	// Time series length
        String line2 = "0"; 	// Build segment time
        String line3 = "0";	// Find maxLength time
        String line4 = "0";	// partitionNum
        String line5 = "0"; //MaxLength
        String line6 = "0"; //slope
        String line7 = "0"; //intercepter
        
        double error = 0.05;
        double errorBound = 1;
        String line8 = "" + error;	//error
        String line9 = "" + errorBound;	//errorBound
        String line10 = "0";
        
        int length = 2000;
        int step = 2000;
        fileResult += length + ",Step=" + step + ").txt";
        PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
        
        for(int k = 0; k < 10; k++){
        	Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

            TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
            miner.process();
            System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

            StopWatch stopWatch1 = new StopWatch();
            stopWatch1.start();
            List<PLASegment> segs = miner.buildSpecificSegments(3);
            stopWatch1.stop();
            System.out.println("all segmentList.size() = " + segs.size());

            PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
            plaRegionSearch.errorBound = errorBound;
            
            for(int i = segs.size() - 1; i >= 0; i--){
            	if(segs.get(i).getPolygonKB().boundary().size() > 1){
            		segs.remove(i);
            		System.out.println("Remove at " + i);
            	}
            }

            StopWatch stopWatch2 = new StopWatch();
            stopWatch2.start();
            Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
            stopWatch2.stop();
            
            line1 += "\t" + length;
            line2 += "\t" + stopWatch1.getTime();
            line3 += "\t" + stopWatch2.getTime();
            line4 += "\t" + plaRegionSearch.partitionNum;
            line5 += "\t" + plaRegionSearch.finalLength;
            line6 += "\t" + point2Ds1.x();
            line7 += "\t" + point2Ds1.y();
            line8 += "\t" + error;
            line9 += "\t" + errorBound;
            line10 += "\t" + plaRegionSearch.maxUpBound;
            length += step;
            
            System.out.println("stopWatch1.getTime() = " + stopWatch1.getTime());
            System.out.println("stopWatch2.getTime() = " + stopWatch2.getTime());
            System.out.println("point2Ds1.getX() = " + point2Ds1.x());
            System.out.println("point2Ds1.getY() = " + point2Ds1.y());
            System.out.println("PartitionNum = " + plaRegionSearch.partitionNum);
            System.out.println("RealLength = " + plaRegionSearch.finalLength);
        }
        
        pw.println(line1);
        pw.println(line2);
        pw.println(line3);
        pw.println(line4);
        pw.println(line5);
        pw.println(line6);
        pw.println(line7);
        pw.println(line8);
        pw.println(line9);
        pw.println(line10);
        pw.close();
    }
    
    
    public void testRegionSearch() throws Exception{
    	String fileName = "D:\\data\\TEM-Record-1960-2012(Length = 100000).txt";
    	double error = 0.05;
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), 40000);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        List<PLASegment> segs = miner.buildSpecificSegments(3);
        stopWatch1.stop();
        System.out.println("all segmentList.size() = " + segs.size());

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().boundary().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }

        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
        stopWatch2.stop();
        
        System.out.println("stopWatch1.getTime() = " + stopWatch1.getTime());
        System.out.println("stopWatch2.getTime() = " + stopWatch2.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.x());
        System.out.println("point2Ds1.getY() = " + point2Ds1.y());
        System.out.println("PartitionNum = " + plaRegionSearch.partitionNum);
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
        System.out.println("CountInsides = " + plaRegionSearch.countInsides);
        System.out.println("stopWatchInsides = " + plaRegionSearch.stopWatchInsides.getTime());
        
    }
    
    public void testRegionSearchWithAccuracyVsTime() throws IOException{
    	String fileName = "D:\\data\\TEM-Record-1960-2012(Length = 100000).txt";
    	String resultFile = "D:\\data\\accuracyVstime-maxUpBound(length=";
    	int length = 40000;
    	resultFile += length + ").txt";
    	double error = 0.05;
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        List<PLASegment> segs = miner.buildSpecificSegments(3);
        stopWatch1.stop();
        System.out.println("all segmentList.size() = " + segs.size());

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().boundary().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }

        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracy(segs, error, 20000, 5929);
        stopWatch2.stop();
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch2.getTime());
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        pw.println(tempBuild1);
        pw.println(tempBuild2);
        pw.close();
        
        System.out.println("stopWatch1.getTime() = " + stopWatch1.getTime());
        System.out.println("stopWatch2.getTime() = " + stopWatch2.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.x());
        System.out.println("point2Ds1.getY() = " + point2Ds1.y());
        System.out.println("PartitionNum = " + plaRegionSearch.partitionNum);
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
    }
    @Test
    public void testRegionSearchWithPer100() throws IOException{
    	String fileName = "D:\\data\\TEM-Record-1960-2012(Length = 100000).txt";
    	double error = 0;
        double errorBound = 1;
        int length = 40000;
        String resultFile = "D:\\data\\result_per100_Insides(length=" + length + "error=0).txt";
        
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        StopWatch stopWatch1 = new StopWatch();
        stopWatch1.start();
        List<PLASegment> segs = miner.buildSpecificSegments(3);
        stopWatch1.stop();
        System.out.println("all segmentList.size() = " + segs.size());

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().boundary().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }

        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DInsidesPer100(segs, error, resultFile);
        stopWatch2.stop();
        
        System.out.println("stopWatch1.getTime() = " + stopWatch1.getTime());
        System.out.println("stopWatch2.getTime() = " + stopWatch2.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.x());
        System.out.println("point2Ds1.getY() = " + point2Ds1.y());
        System.out.println("PartitionNum = " + plaRegionSearch.partitionNum);
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
        
    }
}
