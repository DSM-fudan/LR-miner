package cn.edu.fudan.dsm.tslrm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import math.geom2d.Point2D;
import cn.edu.fudan.dsm.tslrm.data.DataGenerator;

public class DeepSearchArchitecture {
	public static void main(String[] args) throws Exception{
		String fileName = "data\\test_randomwalk_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		int pointNum = point2Ds.length;
		double errorBound = 1;
		TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		List<PLASegment> segmentList = miner.buildAllSegments();
		
		int startSegLength = pointNum;
		int endSegLength = 2;
		int maxLength = 0;
		double error = 0.001;
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
			PLAExtendDeepSearch2 deepSearch = new PLAExtendDeepSearch2(point2Ds, errorBound);
			maxLength = deepSearch.search(calSegmentList, maxLength, error);
			System.out.println("Current Segment Length = " + i);
			System.out.println("Current MaxLength = " + maxLength);
			if(i < 6 && i > 3){
				i = 3;
				continue;
			}
			i = i/2;
		}
		
	}

}
