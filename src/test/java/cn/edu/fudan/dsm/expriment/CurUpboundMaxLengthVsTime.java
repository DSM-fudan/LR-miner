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
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;

public class CurUpboundMaxLengthVsTime {
	
	public void pointBasedOptimizationTest(String fileName, double errorBound, double error, int length, int period)
			throws IOException{
		int tempPriod = period / 1000;
		String outFile1 = "data/experiments/cur_UpBoundLengthVsTime_pointBasedOptimization(errorBound=" + errorBound + "error=" + error
				+ "length=" + length + "peroid=" + tempPriod + "s_upBound).txt";
		String outFile2 = "data/experiments/cur_UpBoundLengthVsTime_pointBasedOptimization(errorBound=" + errorBound + "error=" + error
				+ "length=" + length + "peroid=" + tempPriod + "s_maxlength).txt";
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
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithBounLengthInsides(segs, error, period);     
        stopWatch.stop();
        
        PrintWriter pw1 = new PrintWriter(new FileWriter(outFile1));
        PrintWriter pw2 = new PrintWriter(new FileWriter(outFile2));
        StringBuilder tempBuild1 = plaRegionSearch.builder1.append(stopWatch.getTime() / 1000);
        StringBuilder tempBuild2 = plaRegionSearch.builder2.append(plaRegionSearch.maxUpBound);
        StringBuilder tempBuild3 = plaRegionSearch.builder3.append(plaRegionSearch.finalLength);
        
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
		String fileName = "data/experiments/5_USD_CAD_MXN_1.txt";
		double errorBound = 0.1;
		double error = 0.05;
		int length = 20000;
		int period = 5000;
		
		CurUpboundMaxLengthVsTime expr = new CurUpboundMaxLengthVsTime();
		expr.pointBasedOptimizationTest(fileName, errorBound, error, length, period);
		System.out.println("**************************************");				
	}
}
