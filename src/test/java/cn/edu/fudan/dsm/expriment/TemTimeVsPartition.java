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

public class TemTimeVsPartition {
	//Test the Origional PLARegionSearch
		public void PLARegionSearchTest(String fileName, double errorBound, double error, int length, int perLength) throws IOException{      
	        String resultFile = "data/experiments/Tem+GST_experiment_result_per" + perLength + "_Original(Bound=" + errorBound + "length=" + length + "error=" + error +").txt";
	        
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
	        	if(segs.get(i).getPolygonKB().getRings().size() > 1){
	        		segs.remove(i);
	        		System.out.println("Remove at " + i);
	        	}
	        }
	        
	        Point2D point2Ds1 = plaRegionSearch.searchByBox2DPerLength(segs, error, resultFile, perLength);
	        System.out.println("k = " + point2Ds1.getX() + " b = " + point2Ds1.getY());
	        System.out.println("FinalLegth = " + plaRegionSearch.finalLength);
		}
		
		//Test the PLARegionSearch which keep all intersect segment with Box2D in a list
		//and record whether the Box2d is contained by Segment
		public void PLARegionSearchWithListInsideTest(String fileName, double errorBound, double error, int length, int perLength) throws IOException{      
	        String resultFile = "data/experiments/Tem+GST_experiment_result_per" + perLength + "_ListInsides(Bound=" + errorBound + "length=" + length + "error=" + error + ").txt";
	        
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
	        	if(segs.get(i).getPolygonKB().getRings().size() > 1){
	        		segs.remove(i);
	        		System.out.println("Remove at " + i);
	        	}
	        }
	        
	        Point2D point2Ds1 = plaRegionSearch.searchByBox2DPerLengthInsides(segs, error, resultFile, perLength);
	        System.out.println("k = " + point2Ds1.getX() + " b = " + point2Ds1.getY());
	        System.out.println("FinalLegth = " + plaRegionSearch.finalLength);
		}
		
		public static void main(String[] args) throws IOException{
			String fileName = "data/experiments/Experiment_Tem+GST_Record_1960-2012(length=100000).txt";
			double errorBound = 1;
			double error = 0.05;
			int partitionNum = 1000;
			int length = 100000;
			
			TemTimeVsPartition expt = new TemTimeVsPartition();
			
			expt.PLARegionSearchWithListInsideTest(fileName, errorBound, error, length, partitionNum);
			System.out.println("**********************************************");
			expt.PLARegionSearchTest(fileName, errorBound, error, length, partitionNum);
		}
}
