package cn.edu.fudan.dsm.expriment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import math.geom2d.Point2D;

import org.apache.commons.lang.time.StopWatch;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.RegionSearchBefore;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;

public class AccuracyVSTimeExperiment {
	
	//Test the Origional PLARegionSearch
	public void PLARegionSearchTest(String fileName, double error, int baseBound, int length, int period) throws IOException{		    	
		String resultFile = "D:\\data\\experiments\\accuracyVstime_Original(length=";
    	resultFile += length + ",error=" + error + ").txt";
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);                

        RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().boundary().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracy(segs, error, period, baseBound);       
        stopWatch.stop();
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        pw.println(tempBuild1);
        pw.println(tempBuild2);
        pw.close();
                
        System.out.println("stopWatch2.getTime() = " + stopWatch.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.x());
        System.out.println("point2Ds1.getY() = " + point2Ds1.y());       
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	}
	
	//Test the PLARegionSearch which keep all intersect segment with Box2D in a List
	public void PLARegionSearchWithListTest(String fileName, double error, int baseBound, int length, int period) throws IOException{
		String resultFile = "D:\\data\\experiments\\accuracyVstime_List(length=";
    	resultFile += length + ",error=" + error + ").txt";
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);                

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().boundary().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracy(segs, error, period, baseBound);       
        stopWatch.stop();
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        pw.println(tempBuild1);
        pw.println(tempBuild2);
        pw.close();
                
        System.out.println("stopWatch.getTime() = " + stopWatch.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.x());
        System.out.println("point2Ds1.getY() = " + point2Ds1.y());       
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	}
	
	//Test the PLARegionSearch which keep all intersect segment with Box2D in a list
	//and record whether the Box2d is contained by Segment
	public void PLARegionSearchWithListInsideTest(String fileName, double error, int baseBound, int length, int period) throws IOException{
		String resultFile = "D:\\data\\experiments\\accuracyVstime_ListInsides(length=";
    	resultFile += length + ",error=" + error + ").txt";
        double errorBound = 1;
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);                

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().boundary().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracyInsides(segs, error, period, baseBound);       
        stopWatch.stop();
        
        PrintWriter pw = new PrintWriter(new FileWriter(resultFile));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        pw.println(tempBuild1);
        pw.println(tempBuild2);
        pw.close();
        
        System.out.println("FinalUpperBound = " + plaRegionSearch.maxUpBound);
        System.out.println("stopWatch.getTime() = " + stopWatch.getTime());
        System.out.println("point2Ds1.getX() = " + point2Ds1.x());
        System.out.println("point2Ds1.getY() = " + point2Ds1.y());       
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	}
	
	public static void main(String[] args) throws IOException{
		String fileName = "D:\\data\\experiments\\Experiment_Tem_Record_1960-2012(length=100000).txt";
		double error = 0.01;
		int baseBound = 22225;
		int length = 80000;
		int period = 40000;
		AccuracyVSTimeExperiment expt = new AccuracyVSTimeExperiment();	
		expt.PLARegionSearchWithListInsideTest(fileName, error, baseBound, length, period);
		System.out.println("****************************************");
		expt.PLARegionSearchWithListTest(fileName, error, baseBound, length, period);
		System.out.println("****************************************");
		expt.PLARegionSearchTest(fileName, error, baseBound, length, period);
	}

}
