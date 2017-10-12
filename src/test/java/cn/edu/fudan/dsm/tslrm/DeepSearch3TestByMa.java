package cn.edu.fudan.dsm.tslrm;

import java.io.File;
import java.util.List;

import math.geom2d.Point2D;

import org.junit.Test;

import cn.edu.fudan.dsm.tslrm.data.DataGenerator;

public class DeepSearch3TestByMa {

	@Test
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
        int maxLength = deepSearch.search(segmentList, 0, 0.001);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed without RTree is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	@Test
	public void testDeepSearch2() throws Exception{
		String fileName = "data\\evaporator_100.txt";
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
        PLAExtendDeepSearch2 deepSearch = new PLAExtendDeepSearch2(point2Ds, 1.0);
        deepSearch.errorBound = errorBound;
        int maxLength = deepSearch.search(segmentList, 0, 0);
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("testDeepSearch2: MaxLength computed with LowerBound is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	@Test
	public void testDeepSearch3() throws Exception{
		String fileName = "data\\evaporator_1000.txt";
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
        PLAExtendDeepSearch3 deepSearch = new PLAExtendDeepSearch3(point2Ds, errorBound);
        int maxLength = deepSearch.search(segmentList, 0, 0.1);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("testDeepSearch3: MaxLength computed without Matrix is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	@Test
	public void testDeepSearch4() throws Exception{
		String fileName = "data\\evaporator_1000.txt";
//		String fileName = "data\\real_999.txt";
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
        PLAExtendDeepSearch4 deepSearch = new PLAExtendDeepSearch4(point2Ds, errorBound);
        int maxLength = deepSearch.search(segmentList, 0, 0.1);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("testDeepSearch4: MaxLength computed with Matrix  and sorteded segment by lowBoundis " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	@Test
	public void testDeepSearch5() throws Exception{
		String fileName = "data\\evaporator_1000.txt";
//		String fileName = "data\\test_randomwalk_100.txt";
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
        PLAExtendDeepSearch5 deepSearch = new PLAExtendDeepSearch5(point2Ds, errorBound);
        int maxLength = deepSearch.search(segmentList, 0, 0.1);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("testDeepSearch5: MaxLength computed with Matrix, RTree and sorteded segment by lowBoundies  " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	@Test
	public void testDeepSearch6() throws Exception{
//		String fileName = "data\\real_100.txt";
		String fileName = "data\\evaporator_1000.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));
        System.out.println("Point2D length = " + point2Ds.length + "!!!!!!!!");
        double errorBound = 0.1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();

        //keep length == 3
        for(int i = 0; i < segmentList.size(); i++){
        	if(segmentList.get(i).getLength() != 3){
        		segmentList.remove(i);
        		i--;
        	}
        }
        
        System.out.println("SegmentList size is " + segmentList.size());
        long timeTotal = System.currentTimeMillis();
        PLAExtendDeepSearch6 deepSearch = new PLAExtendDeepSearch6(point2Ds, errorBound);
        int maxLength = deepSearch.search(segmentList, 0, 0.1);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("testDeepSearch6: MaxLength computed with Matrix, RTree, LowerBound " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
}
