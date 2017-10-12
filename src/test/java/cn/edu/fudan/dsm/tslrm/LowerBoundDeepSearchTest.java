package cn.edu.fudan.dsm.tslrm;

import java.io.File;
import java.util.List;

import org.junit.Test;

import math.geom2d.Point2D;
import cn.edu.fudan.dsm.tslrm.data.DataGenerator;

public class LowerBoundDeepSearchTest {

	
	public void testDeepSearch() throws Exception{
		String fileName = "data\\test_10000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

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
        PLAExtendDeepSearch deepSearch = new PLAExtendDeepSearch();
        int maxLength = deepSearch.search(segmentList, 2000, 0.1);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed without RTree is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	@Test
	public void testDeepSearch2WithLowerBound() throws Exception{
		String fileName = "data\\test_10000_4.txt";
		Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
		
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
        PLAExtendDeepSearch2 deepSearch = new PLAExtendDeepSearch2(point2Ds, errorBound);
        deepSearch.errorBound = errorBound;
        int maxLength = deepSearch.search(segmentList, 0, 0);
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed with LowerBound is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
}
