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

public class TimeVSLengthExperiment {
	
	//Test the Origional PLARegionSearch
	public void PLARegionSearchTest(String fileName, double error) throws IOException{
      String fileResult = "D:\\data\\experiments\\result_TimeVsLength_Origional(error=" + error + ").txt";
      
      String line1 = "";	// Time series length
      String line2 = ""; 	// Running time      
       
      double errorBound = 1;
      
      int length = 10000;
      int step = 20000;
      
      PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
      
      for(int k = 0; k < 6; k++){
    	  System.out.println("k = " + k + "   ###############");
      	  Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

          TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
          miner.process();          

          StopWatch stopWatch = new StopWatch();
          stopWatch.start();
          List<PLASegment> segs = miner.buildSpecificSegments(3);          

          RegionSearchBefore plaRegionSearch = new RegionSearchBefore(point2Ds);
          plaRegionSearch.errorBound = errorBound;
          
          for(int i = segs.size() - 1; i >= 0; i--){
          	if(segs.get(i).getPolygonKB().getRings().size() > 1){
          		segs.remove(i);
          		System.out.println("Remove at " + i);
          	}
          }

          Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segs, error);
          stopWatch.stop();
          long tempTime = stopWatch.getTime() / 1000;
          if(k == 5){
        	  line1 += length;
              line2 += tempTime;
          }else{
        	  line1 += length + "\t";
              line2 += tempTime + "\t"; 
          }
          
          if(k == 0){
        	  length = 20000;
          }else{
        	  length += step;
          }
          
          System.out.println("PointNum = " + point2Ds.length);
          System.out.println("RealLength = " + plaRegionSearch.finalLength);
      }
      
      pw.println(line1);
      pw.println(line2);      
      pw.close();
	}
	//Test the PLARegionSearch which keep all intersect segment with Box2D in a List
	public void PLARegionSearchWithListTest(String fileName, double error) throws IOException{		  
	      String fileResult = "D:\\data\\experiments\\result_TimeVsLength_List(error=" + error + ").txt";
	      
	      String line1 = "0";	// Time series length
	      String line2 = "0"; 	// Running time      
	       
	      double errorBound = 1;
	      
	      int length = 10000;
	      int step = 20000;
	      
	      PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
	      
	      for(int k = 0; k < 6; k++){
	    	  System.out.println("k = " + k + "   ###############");
	      	  Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

	          TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	          miner.process();          

	          StopWatch stopWatch = new StopWatch();
	          stopWatch.start();
	          List<PLASegment> segs = miner.buildSpecificSegments(3);          

	          PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
	          plaRegionSearch.errorBound = errorBound;
	          
	          for(int i = segs.size() - 1; i >= 0; i--){
	          	if(segs.get(i).getPolygonKB().getRings().size() > 1){
	          		segs.remove(i);
	          		System.out.println("Remove at " + i);
	          	}
	          }

	          Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segs, error);
	          stopWatch.stop();
	          long tempTime = stopWatch.getTime() / 1000;
	          if(k == 5){
	        	  line1 += length;
	              line2 += tempTime;
	          }else{
	        	  line1 += length + "\t";
	              line2 += tempTime + "\t"; 
	          }
	          if(k == 0){
	        	  length = 20000;
	          }else{
	        	  length += step;
	          }
	          System.out.println("PointNum = " + point2Ds.length);	      
	          System.out.println("RealLength = " + plaRegionSearch.finalLength);
	      }
	      
	      pw.println(line1);
	      pw.println(line2);      
	      pw.close();
	}
	//Test the PLARegionSearch which keep all intersect segment with Box2D in a list
	//and record whether the Box2d is contained by Segment
	public void PLARegionSearchWithListInsideTest(String fileName, double error) throws IOException{
	      String fileResult = "D:\\data\\experiments\\result_TimeVsLength_ListInside(error=" + error + ").txt";
	      
	      String line1 = "0";	// Time series length
	      String line2 = "0"; 	// Running time      
	       
	      double errorBound = 1;
	      
	      int length = 10000;
	      int step = 20000;
	      
	      PrintWriter pw = new PrintWriter(new FileWriter(fileResult));
	      
	      for(int k = 0; k < 6; k++){
	    	  System.out.println("k = " + k + "   ###############");
	      	  Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

	          TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	          miner.process();          

	          StopWatch stopWatch = new StopWatch();
	          stopWatch.start();
	          List<PLASegment> segs = miner.buildSpecificSegments(3);          

	          PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
	          plaRegionSearch.errorBound = errorBound;
	          
	          for(int i = segs.size() - 1; i >= 0; i--){
	          	if(segs.get(i).getPolygonKB().getRings().size() > 1){
	          		segs.remove(i);
	          		System.out.println("Remove at " + i);
	          	}
	          }

	          Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
	          stopWatch.stop();
	          
	          long tempTime = stopWatch.getTime() / 1000;
	          if(k == 5){
	        	  line1 += length;
	              line2 += tempTime;
	          }else{
	        	  line1 += length + "\t";
	              line2 += tempTime + "\t"; 
	          }
	          
	          if(k == 0){
	        	  length = 20000;
	          }else{
	        	  length += step;
	          }
	          
	          System.out.println("PointNum = " + point2Ds.length);
	          System.out.println("RealLength = " + plaRegionSearch.finalLength);
	          System.out.println("Time = " + stopWatch.getTime());
	      }
	      
	      pw.println(line1);
	      pw.println(line2);      
	      pw.close();
	}
	
	public static void main(String[] args) throws IOException{
		String fileName = "D:\\data\\experiments\\Experiment_Tem_Record_1960-2012(length=100000).txt";
		double error = 0.05;
		TimeVSLengthExperiment expt = new TimeVSLengthExperiment();
		expt.PLARegionSearchWithListInsideTest(fileName, error);
		System.out.println("**********************************************");
		expt.PLARegionSearchWithListTest(fileName, error);
		System.out.println("**********************************************");
		expt.PLARegionSearchTest(fileName, error);
		
	};

}
