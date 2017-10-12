package cn.edu.fudan.dsm.tslrm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import math.geom2d.Point2D;
import cn.edu.fudan.dsm.tslrm.data.DataGenerator;

public class DeepSearchTestWithArchitectureByMa {
	
	public void testDeepSearchArchitecture() throws Exception{
		String fileName = "data\\test_randomwalk_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		int pointNum = point2Ds.length;
		double errorBound = 1;
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		List<PLASegment> segmentList = miner.buildAllSegments();
		
		int startSegLength = pointNum;
		int maxLength = 0;
		double error = 0.01;
		double lastK = -1;
		double lastB = -1;
		for(int i = startSegLength; i > 2;){
			//select the segment with length i
			List<PLASegment> calSegmentList = new ArrayList<PLASegment>();
			for(int j = 0; j < segmentList.size(); j++){
				PLASegment tempSeg = segmentList.get(j);
				if(tempSeg.getLength() == i){
					calSegmentList.add(tempSeg);
				}
			}
			if(calSegmentList.size() == 0){
				i = i/2;
				continue;
			}
			PLAExtendDeepSearch deepSearch = new PLAExtendDeepSearch();
			int tempLength = maxLength;
			maxLength = deepSearch.search(calSegmentList, maxLength, error);
			if(maxLength > tempLength){
				lastK = deepSearch.slope;
				lastB = deepSearch.intercept;
			}
			if(i < 6 && i > 3){
				i = 3;
				continue;
			}
			i = i/2;
		}
		System.out.println("K = " + lastK + " B = " + lastB);
		System.out.println("MaxLength = " + maxLength);
	}
	
	
	public void testDeepSearchArchitecture2() throws Exception{
		String fileName = "data\\test_randomwalk_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		int pointNum = point2Ds.length;
		double errorBound = 1;
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		List<PLASegment> segmentList = miner.buildAllSegments();
		
		int startSegLength = pointNum;
		int maxLength = 0;
		double error = 0.01;
		double lastK = -1;
		double lastB = -1;
		long time = System.currentTimeMillis();
		for(int i = startSegLength; i > 2;){
			//select the segment with length i
			List<PLASegment> calSegmentList = new ArrayList<PLASegment>();
			for(int j = 0; j < segmentList.size(); j++){
				PLASegment tempSeg = segmentList.get(j);
				if(tempSeg.getLength() == i){
					calSegmentList.add(tempSeg);
				}
			}
			if(calSegmentList.size() == 0){
				i = i/2;
				continue;
			}
			PLAExtendDeepSearch2 deepSearch = new PLAExtendDeepSearch2(point2Ds, 1.0);
			int tempLength = maxLength;
			maxLength = deepSearch.search(calSegmentList, maxLength, error);
			if(maxLength > tempLength){
				lastK = deepSearch.slope;
				lastB = deepSearch.intercept;
			}
			if(i < 6 && i > 3){
				i = 3;
				continue;
			}
			i = i/2;
		}
		time = System.currentTimeMillis() - time;
		System.out.println("**************************");
		System.out.println("PLAExtendDeepSearch2");
		System.out.println("K = " + lastK + " B = " + lastB);
		System.out.println("MaxLength = " + maxLength);
		System.out.println("Total Time = " + time);
	}
	
	
	public void testDeepSearchArchitecture3() throws Exception{
		String fileName = "data\\test_randomwalk_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		int pointNum = point2Ds.length;
		double errorBound = 1;
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		List<PLASegment> segmentList = miner.buildAllSegments();
		
		int startSegLength = pointNum;
		int maxLength = 0;
		double error = 0.01;
		double lastK = -1;
		double lastB = -1;
		long time = System.currentTimeMillis();
		for(int i = startSegLength; i > 2;){
			//select the segment with length i
			List<PLASegment> calSegmentList = new ArrayList<PLASegment>();
			for(int j = 0; j < segmentList.size(); j++){
				PLASegment tempSeg = segmentList.get(j);
				if(tempSeg.getLength() == i){
					calSegmentList.add(tempSeg);
				}
			}
			if(calSegmentList.size() == 0){
				i = i/2;
				continue;
			}
			PLAExtendDeepSearch3 deepSearch = new PLAExtendDeepSearch3(point2Ds, 1.0);
			int tempLength = maxLength;
			maxLength = deepSearch.search(calSegmentList, maxLength, error);
			if(maxLength > tempLength){
				lastK = deepSearch.slope;
				lastB = deepSearch.intercept;
			}
			if(i < 6 && i > 3){
				i = 3;
				continue;
			}
			i = i/2;
		}
		time = System.currentTimeMillis() - time;
		System.out.println("**************************");
		System.out.println("PLAExtendDeepSearch3");
		System.out.println("K = " + lastK + " B = " + lastB);
		System.out.println("MaxLength = " + maxLength);
		System.out.println("Total Time = " + time);
	}
	
	@Test
	public void testDeepSearchArchitecture4() throws Exception{
		String fileName = "data\\evaporator_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		int pointNum = point2Ds.length;
		double errorBound = 1;
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		List<PLASegment> segmentList = miner.buildAllSegments();
		
		int startSegLength = pointNum;
		int maxLength = 0;
		double error = 0.01;
		double lastK = -1;
		double lastB = -1;
		long time = System.currentTimeMillis();
		for(int i = startSegLength; i > 2;){
			//select the segment with length i
			List<PLASegment> calSegmentList = new ArrayList<PLASegment>();
			for(int j = 0; j < segmentList.size(); j++){
				PLASegment tempSeg = segmentList.get(j);
				if(tempSeg.getLength() == i){
					calSegmentList.add(tempSeg);
				}
			}
			if(calSegmentList.size() == 0){
				i = i/2;
				continue;
			}
			PLAExtendDeepSearch4 deepSearch = new PLAExtendDeepSearch4(point2Ds, 1.0);
			int tempLength = maxLength;
			maxLength = deepSearch.search(calSegmentList, maxLength, error);
			if(maxLength > tempLength){
				lastK = deepSearch.slope;
				lastB = deepSearch.intercept;
			}
			if(i < 6 && i > 3){
				i = 3;
				continue;
			}
			i = i/2;
		}
		time = System.currentTimeMillis() - time;
		System.out.println("**************************");
		System.out.println("PLAExtendDeepSearch4");
		System.out.println("K = " + lastK + " B = " + lastB);
		System.out.println("MaxLength = " + maxLength);
		System.out.println("Total Time = " + time);
	}
	
	
	public void testDeepSearchArchitecture5() throws Exception{
		String fileName = "data\\test_randomwalk_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		int pointNum = point2Ds.length;
		double errorBound = 1;
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		List<PLASegment> segmentList = miner.buildAllSegments();
		
		int startSegLength = pointNum;
		int maxLength = 0;
		double error = 0.01;
		double lastK = -1;
		double lastB = -1;
		long time = System.currentTimeMillis();
		for(int i = startSegLength; i > 2;){
			//select the segment with length i
			List<PLASegment> calSegmentList = new ArrayList<PLASegment>();
			for(int j = 0; j < segmentList.size(); j++){
				PLASegment tempSeg = segmentList.get(j);
				if(tempSeg.getLength() == i){
					calSegmentList.add(tempSeg);
				}
			}
			if(calSegmentList.size() == 0){
				i = i/2;
				continue;
			}
			PLAExtendDeepSearch5 deepSearch = new PLAExtendDeepSearch5(point2Ds, 1.0);
			int tempLength = maxLength;
			maxLength = deepSearch.search(calSegmentList, maxLength, error);
			if(maxLength > tempLength){
				lastK = deepSearch.slope;
				lastB = deepSearch.intercept;
			}
			if(i < 6 && i > 3){
				i = 3;
				continue;
			}
			i = i/2;
		}
		time = System.currentTimeMillis() - time;
		System.out.println("**************************");
		System.out.println("PLAExtendDeepSearch5");
		System.out.println("K = " + lastK + " B = " + lastB);
		System.out.println("MaxLength = " + maxLength);
		System.out.println("Total Time = " + time);
	}
	
	
	public void testDeepSearchArchitecture6() throws Exception{
		String fileName = "data\\test_randomwalk_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		int pointNum = point2Ds.length;
		double errorBound = 1;
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		List<PLASegment> segmentList = miner.buildAllSegments();
		
		int startSegLength = pointNum;
		int maxLength = 0;
		double error = 0.01;
		double lastK = -1;
		double lastB = -1;
		long time = System.currentTimeMillis();
		for(int i = startSegLength; i > 2;){
			//select the segment with length i
			List<PLASegment> calSegmentList = new ArrayList<PLASegment>();
			for(int j = 0; j < segmentList.size(); j++){
				PLASegment tempSeg = segmentList.get(j);
				if(tempSeg.getLength() == i){
					calSegmentList.add(tempSeg);
				}
			}
			if(calSegmentList.size() == 0){
				i = i/2;
				continue;
			}
			System.out.println("Current segmentList size = " + calSegmentList.size());
			System.out.println("Iterator " + i + " SegmentList size = " + calSegmentList.size());
			PLAExtendDeepSearch6 deepSearch = new PLAExtendDeepSearch6(point2Ds, 1.0);
			int tempLength = maxLength;
			maxLength = deepSearch.search(calSegmentList, maxLength, error);
			if(maxLength > tempLength){
				lastK = deepSearch.slope;
				lastB = deepSearch.intercept;
			}
			if(i < 6 && i > 3){
				i = 3;
				continue;
			}
			i = i/2;
		}
		time = System.currentTimeMillis() - time;
		System.out.println("**************************");
		System.out.println("PLAExtendDeepSearch6");
		System.out.println("K = " + lastK + " B = " + lastB);
		System.out.println("MaxLength = " + maxLength);
		System.out.println("Total Time = " + time);
	}
}
