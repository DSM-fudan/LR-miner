package cn.edu.fudan.dsm.expriment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;

import math.geom2d.Point2D;
import cn.edu.fudan.dsm.tslrm.PLAExtendDeepSearch6;
import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;

public class TemAccuracyVsTime {
	
	public void segmentBasedOptimizationTest(String fileName, double errorBound, double error, int baseBound, int length, int period)
			throws IOException{
		int tempPriod = period / 1000;
		String outFile1 = "data/experiments/tem+GST_AccuracyVsTime_pointBasedOptimization(errorBound=" + errorBound + "error=" + error
				+ "baseBound=" + baseBound + "length=" + length + "peroid=" + tempPriod + "s_trueAccuracy).txt";
		String outFile2 = "data/experiments/tem+GST_AccuracyVsTime_pointBasedOptimization(errorBound=" + errorBound + "error=" + error
				+ "baseBound=" + baseBound + "length=" + length + "peroid=" + tempPriod + "s_ourAccuracy).txt";
		Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().getRings().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        
        int startLength = 0;
        PLAExtendDeepSearch6 deepSearch = new PLAExtendDeepSearch6(point2Ds, errorBound);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int maxLength = deepSearch.searchWithAccuracy(segs, startLength, error, period, baseBound);
        stopWatch.stop();
        
        PrintWriter pw1 = new PrintWriter(new FileWriter(outFile1));
        PrintWriter pw2 = new PrintWriter(new FileWriter(outFile2));
        StringBuilder tempBuild1 = deepSearch.builder1.append(stopWatch.getTime() / 1000);
        double finalError = 1 - deepSearch.curError;
        StringBuilder tempBuild2 = deepSearch.builder2.append(finalError);
        StringBuilder tempBuild3 = deepSearch.builder3.append(finalError);
        
        pw1.println(tempBuild1.toString());
        pw1.print(tempBuild2.toString());
        
        pw2.println(tempBuild1.toString());
        pw2.print(tempBuild3.toString());
        pw1.close();
        pw2.close();
        System.out.println("k = " + deepSearch.maxKB.getX());
        System.out.println("b = " + deepSearch.maxKB.getY());       
        System.out.println("RealLength = " + maxLength);
	}
	
	public void pointBasedOptimizationTest(String fileName, double errorBound, double error, int baseBound, int length, int period)
			throws IOException{
		int tempPriod = period / 1000;
		String outFile1 = "data/experiments/tem+GST_AccuracyVsTime_pointBasedOptimization(errorBound=" + errorBound + "error=" + error
				+ "baseBound=" + baseBound + "length=" + length + "peroid=" + tempPriod + "s_trueAccuracy).txt";
		String outFile2 = "data/experiments/tem+GST_AccuracyVsTime_pointBasedOptimization(errorBound=" + errorBound + "error=" + error
				+ "baseBound=" + baseBound + "length=" + length + "peroid=" + tempPriod + "s_ourAccuracy).txt";
		Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();        
        
        List<PLASegment> segs = miner.buildSpecificSegments(3);

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        
        for(int i = segs.size() - 1; i >= 0; i--){
        	if(segs.get(i).getPolygonKB().getRings().size() > 1){
        		segs.remove(i);
        		System.out.println("Remove at " + i);
        	}
        }
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithAccuracyInsides(segs, error, period, baseBound);     
        stopWatch.stop();
        
        PrintWriter pw1 = new PrintWriter(new FileWriter(outFile1));
        PrintWriter pw2 = new PrintWriter(new FileWriter(outFile2));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.finalError);
        StringBuilder tempBuild3 = plaRegionSearch.builder3.append(plaRegionSearch.finalError);
        
        pw1.println(tempBuild1.toString());
        pw1.print(tempBuild2.toString());
        
        pw2.println(tempBuild1.toString());
        pw2.print(tempBuild3.toString());
        pw1.close();
        pw2.close();
        
        System.out.println("FinalUpperBound = " + plaRegionSearch.maxUpBound);
        System.out.println("stopWatch.getTime() = " + stopWatch.getTime());
        System.out.println("k = " + point2Ds1.getX());
        System.out.println("b = " + point2Ds1.getY());       
        System.out.println("RealLength = " + plaRegionSearch.finalLength);
	}
	
	public static void main(String[] args) throws IOException{
		String fileName = "data/experiments/Experiment_Tem+GST_Record_1960-2012(length=100000).txt";
		double errorBound = 1;
		double error = 0.05;
		int baseBound = 40087;
		int length = 100000;
		int period = 80000;
		
		TemAccuracyVsTime expr = new TemAccuracyVsTime();
		expr.pointBasedOptimizationTest(fileName, errorBound, error, baseBound, length, period);
		System.out.println("**************************************");
//		expr.segmentBasedOptimizationTest(fileName, errorBound, error, baseBound, length, period);		
	}
}
