package cn.edu.fudan.dsm.tslrm;

import java.io.File;
import java.util.List;

import math.geom2d.Point2D;

import org.junit.Test;

import cn.edu.fudan.dsm.tslrm.data.DataGenerator;

public class BinaryInsectionSearchTest {

	@Test
	public void binarySearchTest() throws Exception{
		String fileName = "data\\evaporator_1000.txt";
//		String fileName = "data\\test_1000_4.txt";
//		String fileName = "data\\test_randomwalk_1000.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
        System.out.println("Point2D length = " + point2Ds.length + "!!!!!!!!");
        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();

        for(int i = 0; i < segmentList.size(); i++){
        	if(segmentList.get(i).getLength() != 3){
        		segmentList.remove(i);
        		i--;
        	}
        }
        
        System.out.println("SegmentList size is " + segmentList.size());
        long timeTotal = System.currentTimeMillis();
        BinaryInsectionSearch binarySearch = new BinaryInsectionSearch(point2Ds, errorBound, segmentList);
        binarySearch.binarySearch(0);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("BinaryInsectionSearch: maxLength =  " + "   " + binarySearch.getMaxLength());
        System.out.println("MaxK = " + binarySearch.getMaxK());
        System.out.println("MaxB = " + binarySearch.getMaxB());
        System.out.println("Total Cost Time is " + timeTotal);
	}
}
