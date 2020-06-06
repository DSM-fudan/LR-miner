package cn.edu.fudan.dsm.expriment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import math.geom2d.Point2D;

import org.apache.commons.lang.time.StopWatch;

import cn.edu.fudan.dsm.tslrm.PLAExtendDeepSearch;
import cn.edu.fudan.dsm.tslrm.PLAExtendDeepSearch6;
import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.RegionSearchBefore;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;

public class EvaporatorTimeVsLength {
	public void segmentBasedTest(String fileName, double errorBound, double error, int[] segLength) throws IOException{
		String outFile = "data/experiments/Evaporator_TimeVsLength_segmentBased(errorBound=" + errorBound + "error=" + error + ").txt";
		String line1 = "";	// Time series length
	    String line2 = ""; 	// Running time
	    PrintWriter pw = new PrintWriter(new FileWriter(outFile));
	    
	    for(int i = 0; i < segLength.length; i++){
	    	System.out.println("segmentBasedTest:i = " + i + "   ###############");
	      	Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), segLength[i]);
	      	TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	        miner.process();          
	        List<PLASegment> segs = miner.buildSpecificSegments(3);
	        
	        for(int j = segs.size() - 1; j >= 0; j--){
	          	if(segs.get(j).getPolygonKB().boundary().size() > 1){
	          		segs.remove(j);
	          		System.out.println("Remove at " + j);
	          	}
	          }
	        
	        int startLength = 0;
	        PLAExtendDeepSearch deepSearch = new PLAExtendDeepSearch();
	        
	        StopWatch stopWatch = new StopWatch();
	        stopWatch.start();
	        int maxLength = deepSearch.search(segs, startLength, error);
	        stopWatch.stop();
	        
	        long tempTime = stopWatch.getTime() / 1000;
	        
	        if(i == segLength.length - 1){
	        	line1 += segLength[i];
	            line2 += tempTime;
	        }else{
	        	line1 += segLength[i] + "\t";
	            line2 += tempTime + "\t";
	        }
	        
	        System.out.println("k = " + deepSearch.slope + " b = " + deepSearch.intercept);
	        System.out.println("RealLength = " + maxLength);
	    }
	    pw.println(line1);
	    pw.print(line2);
	    pw.close();
	}
	
	public void segmentBasedOptimizationTest(String fileName, double errorBound, double error, int[] segLength) throws IOException{
		String outFile = "data/experiments/Evaporator_TimeVsLength_segmentBasedOptimization(errorBound=" + errorBound + "error=" + error + ").txt";
		String line1 = "";	// Time series length
	    String line2 = ""; 	// Running time
	    PrintWriter pw = new PrintWriter(new FileWriter(outFile));
	    
	    for(int i = 0; i < segLength.length; i++){
	    	System.out.println("segmentBasedOptimizationTest:i = " + i + "   ###############");
	      	Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), segLength[i]);
	      	TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	        miner.process();          
	        List<PLASegment> segs = miner.buildSpecificSegments(3);
	        
	        for(int j = segs.size() - 1; j >= 0; j--){
	          	if(segs.get(j).getPolygonKB().boundary().size() > 1){
	          		segs.remove(j);
	          		System.out.println("Remove at " + j);
	          	}
	          }
	        
	        int startLength = 0;
	        PLAExtendDeepSearch6 deepSearch = new PLAExtendDeepSearch6(point2Ds, errorBound);
	        
	        StopWatch stopWatch = new StopWatch();
	        stopWatch.start();
	        int maxLength = deepSearch.search(segs, startLength, error);
	        stopWatch.stop();
	        
	        long tempTime = stopWatch.getTime() / 1000;
	        
	        if(i == segLength.length - 1){
	        	line1 += segLength[i];
	            line2 += tempTime;
	        }else{
	        	line1 += segLength[i] + "\t";
	            line2 += tempTime + "\t";
	        }
	        
	        System.out.println("k = " + deepSearch.slope + " b = " + deepSearch.intercept);
	        System.out.println("RealLength = " + maxLength);
	        System.out.println(tempTime);
	    }
	    pw.println(line1);
	    pw.print(line2);
	    pw.close();
	}
	
	public void pointBasedTest(String fileName, double errorBound, double error, int[] segLength) throws IOException{
		String outFile = "data/experiments/Evaporator_TimeVsLength_pointBased(errorBound=" + errorBound + "error=" + error + ").txt";
		String line1 = "";	// Time series length
	    String line2 = ""; 	// Running time
	    PrintWriter pw = new PrintWriter(new FileWriter(outFile));
	    
	    for(int i = 0; i < segLength.length; i++){
	    	System.out.println("pointBasedTest:i = " + i + "   ###############");
	      	Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), segLength[i]);
	      	TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	        miner.process();          
	        List<PLASegment> segs = miner.buildSpecificSegments(3);
	        
	        RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
	        plaRegionSearch.errorBound = errorBound;
	        
	        for(int j = segs.size() - 1; j >= 0; j--){
	          	if(segs.get(j).getPolygonKB().boundary().size() > 1){
	          		segs.remove(j);
	          		System.out.println("Remove at " + j);
	          	}
	          }
	        
	        StopWatch stopWatch = new StopWatch();
	        stopWatch.start();
	        Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segs, error);
	        stopWatch.stop();
	        
	        long tempTime = stopWatch.getTime() / 1000;
	        
	        if(i == segLength.length - 1){
	        	line1 += segLength[i];
	            line2 += tempTime;
	        }else{
	        	line1 += segLength[i] + "\t";
	            line2 += tempTime + "\t";
	        }
	        
	        System.out.println("k = " + point2Ds1.x() + " b = " + point2Ds1.y());
	        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	    }
	    pw.println(line1);
	    pw.print(line2);
	    pw.close();
	}
	
	public void pointBasedOptimizationTest(String fileName, double errorBound, double error, int[] segLength) throws IOException{
		String outFile = "data/experiments/Evaporator_TimeVsLength_pointBasedOptimization(errorBound=" + errorBound + "error=" + error + ").txt";
		String line1 = "";	// Time series length
	    String line2 = ""; 	// Running time
	    PrintWriter pw = new PrintWriter(new FileWriter(outFile));
	    
	    for(int i = 0; i < segLength.length; i++){
	    	System.out.println("pointBasedOptimizationTest:i = " + i + "   ###############");
	      	Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), segLength[i]);
	      	TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	        miner.process();          
	        List<PLASegment> segs = miner.buildSpecificSegments(3);
	        
	        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
	        plaRegionSearch.errorBound = errorBound;
	        
	        for(int j = segs.size() - 1; j >= 0; j--){
	          	if(segs.get(j).getPolygonKB().boundary().size() > 1){
	          		segs.remove(j);
	          		System.out.println("Remove at " + j);
	          	}
	          }
	        
	        StopWatch stopWatch = new StopWatch();
	        stopWatch.start();
	        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
	        stopWatch.stop();
	        
	        long tempTime = stopWatch.getTime() / 1000;
	        
	        if(i == segLength.length - 1){
	        	line1 += segLength[i];
	            line2 += tempTime;
	        }else{
	        	line1 += segLength[i] + "\t";
	            line2 += tempTime + "\t";
	        }
	        
	        System.out.println("k = " + point2Ds1.x() + " b = " + point2Ds1.y());
	        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	        System.out.println(tempTime);
	    }
	    pw.println(line1);
	    pw.print(line2);
	    pw.close();
	}
	
	public static void main(String[] args) throws IOException{
		String fileName = "data/experiments/evaporator_5000.txt";
		double errorBound = 0.05;
		double error = 0.05;
		int[] segLength = {100, 500, 1000, 2000, 5000};
		
		EvaporatorTimeVsLength expr = new EvaporatorTimeVsLength();
		expr.pointBasedOptimizationTest(fileName, errorBound, error, segLength);
		System.out.println("****************************");
		expr.pointBasedTest(fileName, errorBound, error, segLength);
		System.out.println("*****************************");
		expr.segmentBasedOptimizationTest(fileName, errorBound, error, segLength);
		System.out.println("*****************************");
//		expr.segmentBasedTest(fileName, errorBound, error, segLength);
		
	}
}
