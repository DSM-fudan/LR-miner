package cn.edu.fudan.dsm.expriment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import math.geom2d.Point2D;

import org.apache.commons.lang.time.StopWatch;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.RegionSearchBefore;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;

public class CurTimeVsPartition {
	//Test the Origional PLARegionSearch
			public void PLARegionSearchTest(String fileName, double errorBound, double error, int length, int perLength) throws IOException{		  	
		        String outFile = "data/experiments/currency_experiment_result_per" + perLength + "_Original(errorBound=" + errorBound +"length=" + length + "error=" + error + ").txt";		        
		        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

		        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		        miner.process();        
		        StopWatch stopWatch1 = new StopWatch();
		        stopWatch1.start();
		        List<PLASegment> segs = miner.buildSpecificSegments(3);
		        stopWatch1.stop();
		        System.out.println("all segmentList.size() = " + segs.size());

		        RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
		        plaRegionSearch.errorBound = errorBound;
		        
		        for(int i = segs.size() - 1; i >= 0; i--){
		        	if(segs.get(i).getPolygonKB().boundary().size() > 1){
		        		segs.remove(i);
		        		System.out.println("Remove at " + i);
		        	}
		        }
		        
		        Point2D point2Ds1 = plaRegionSearch.searchByBox2DPerLength(segs, error, outFile, perLength);
		        System.out.println("k = " + point2Ds1.x() + " b = " + point2Ds1.y());
		        System.out.println("FinalLegth = " + plaRegionSearch.finalLength);
			}
			
			//Test the PLARegionSearch which keep all intersect segment with Box2D in a list
			//and record whether the Box2d is contained by Segment
			public void PLARegionSearchWithListInsideTest(String fileName, double errorBound, double error, int length, int perLength) throws IOException{     
		        String outFile = "data/experiments/currency_experiment_result_per" + perLength + "_ListInsides(errorBound=" + errorBound +"length=" + length + "error=" + error + ").txt";		        
		        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

		        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		        miner.process();        
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
		        
		        Point2D point2Ds1 = plaRegionSearch.searchByBox2DPerLengthInsides(segs, error, outFile, perLength);
		        System.out.println("k = " + point2Ds1.x() + " b = " + point2Ds1.y());
		        System.out.println("FinalLegth = " + plaRegionSearch.finalLength);
			}
			
			public static void main(String[] args) throws IOException{
				String fileName = "data/experiments/5_USD_CAD_MXN_1.txt";
				double errorBound = 0.1;
				double error = 0.05;
				int partitionNum = 200;
				int length = 10000;
				
				CurTimeVsPartition expt = new CurTimeVsPartition();
				
				expt.PLARegionSearchWithListInsideTest(fileName, errorBound, error, length, partitionNum);
				System.out.println("**********************************************");
				expt.PLARegionSearchTest(fileName, errorBound, error, length, partitionNum);
			}
}
