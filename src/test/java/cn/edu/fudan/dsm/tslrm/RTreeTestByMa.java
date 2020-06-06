package cn.edu.fudan.dsm.tslrm;

import java.io.File;
import java.util.List;
import java.util.Properties;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygons2D;

import org.junit.Test;

import cn.edu.fudan.dsm.tslrm.data.DataGenerator;

import com.infomatiq.jsi.rtree.Rectangle;
import com.infomatiq.jsi.rtree.Node;
import com.infomatiq.jsi.rtree.RTree;

public class RTreeTestByMa {
	
	public void simpleTest() {
		Properties p = new Properties();
		p.setProperty("MinNodeEntries", "1");
		p.setProperty("MaxNodeEntries", "2");
		
		RTree rTree = new RTree();
		rTree.init(p);	
		double[] rects = {
				10, 20, 40, 70,
                30, 10, 70, 15,
                100, 70, 110, 80,
                0, 50, 30, 55,
                13, 21, 54, 78,
                3, 8, 23, 34,
                200, 29, 202, 50,
                34, 1, 35, 1,
                201, 200, 234, 203,
                56, 69, 58, 70,
                12, 67, 70, 102,
                1, 10, 10, 20
		}; 
		int length = rects.length - 1;
		System.out.println(length);
		Rectangle[] rectangles = new Rectangle[length];
		for(int i = 0; i < length; i++){
			
			rectangles[i] = new Rectangle(rects[i], rects[i + 1], rects[i], rects[i+1]);
			rTree.add(rectangles[i], i);
		}
		
		System.out.println("Entries add Done...............");
		System.out.println("RTree Size:" + rTree.size());
		
		Rectangle test1 = rTree.getBounds();
		System.out.println(test1);
		
		Node test2 = rTree.getNode(19);
		
		System.out.println(test2.getLevel());
		
		System.out.println(test2.getEntryCount());
		
		Rectangle testRect = new Rectangle(0, 0, 40, 40);
		
		List leafs = rTree.intersects(testRect);
		int kNum = 0;
		System.out.println("____________________");
		for(int i = 0; i < leafs.size(); i++){
			int index = (Integer) leafs.get(i);
			System.out.println(rectangles[index]);
			kNum++;
		}
		System.out.println("kNum:" + kNum);
		System.out.println("Intersected Leafs' Num is " + leafs.size());
		Rectangle deltRect = new Rectangle(10, 20, 10, 20);
		boolean isDel = rTree.delete(deltRect, 1);
		System.out.println("is deleted ?" + isDel);
		System.out.println(rTree.size());
		
	}
	
	
	public void testSegmentRect(){
		 	double x[] = {1, 2, 3, 6, 14, 1, 1, 2, 3, 4, 2, 1, 4, 2, 8, 7, 9, 6, 8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6, 5, 6, 1, 2, 3, 4, 1, 2, 3, 4,};
	        double y[] = {4, 3, 8, 13, 9, 1, 6, 8, 10, 12, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12, 20, 8, 6, 8, 10, 12, 6, 8, 10, 12,};
	        System.out.println("x.length = " + x.length);
	        System.out.println("y.length = " + y.length);
	        
	        double errorBound = 1;
	        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(x, y, errorBound);

	        List<PLASegment> allSegments = miner.buildAllSegments();
	        int length = allSegments.size();
	        PLASegment[] segs = new PLASegment[length];
	        
	        for(int i = 0; i < length; i++){
	        	segs[i] = allSegments.get(i);
	        }
	        RTree rTree = TSPLAPointBoundKBMiner.builtTree(segs);
	        System.out.println("Segment size is " + length);
	        System.out.println("RTree size is " + rTree.size());
	        
	        Rectangle rect = new Rectangle(0, 0, 2, 2);
	        
	        List<Node> leafs = rTree.intersects(rect);
	        
	        System.out.println("Intersect leafs is " + leafs.size());
	        
	        Rectangle delTest = TSPLAPointBoundKBMiner.getRectangle(segs[5]);
	        
	        boolean isDel = rTree.delete(delTest, 5);
	        System.out.println(isDel);
	}
	
	
	public void textAproiriRTree(){
		double x[] = {1, 2, 3, 6, 14, 1, 1, 2, 3, 4, 2, 1, 4, 2, 8, 7, 9, 6, 8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6, 5, 6, 1, 2, 3, 4, 1, 2, 3, 4,};
        double y[] = {4, 3, 8, 13, 9, 1, 6, 8, 10, 12, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12, 20, 8, 6, 8, 10, 12, 6, 8, 10, 12,};
        System.out.println("x.length = " + x.length);
        System.out.println("y.length = " + y.length);
        
        double errorBound = 1;
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(x, y, errorBound);
        TSPLAPointBoundKBMiner._max_Length = 15;
        List<PLASegment> allSegments = miner.buildAllSegments();
        TSPLAPointBoundKBMiner.findMaxIntersectionWithAprioriWithRTree(allSegments);
	}
	
	public void textAproiriRTree2() throws Exception{
		String fileName = "data\\test_1000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();
        TSPLAPointBoundKBMiner._max_Length = 90000;
        System.out.println("all segmentList.size() = " + segmentList.size());
        TSPLAPointBoundKBMiner.findMaxIntersectionWithAprioriWithRTree(segmentList);
        
	}
	
	public void testInitMatrixWithRTree() throws Exception{
		String fileName = "data\\test_1000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();
        long initMatrixTime = System.currentTimeMillis();
        TSPLAPointBoundKBMiner.initMatrixWithRTree(segmentList);
//        TSPLAPointBoundKBMiner.initMatrix(segmentList);
        initMatrixTime = System.currentTimeMillis() - initMatrixTime;
        System.out.println("initMatrix time is " + initMatrixTime);
	}
	
	public void testVerifyMatrix() throws Exception{
		String fileName = "data\\test_1000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();
        TSPLAPointBoundKBMiner.vertifyMatrix(segmentList);
	}
	
	public void testInitMatrixWithRTree2() throws Exception{
		String fileName = "data\\test_100_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();
        System.out.println("Original SegmentList length:" + segmentList.size());
        for(int i = 0; i < segmentList.size(); i++){
        	if(segmentList.get(i).getLength() > 5){
        		segmentList.remove(i);
        		i--;
        	}
        }
        
        System.out.println("Segment with Length :" + segmentList.size());
        long initMatrixTime = System.currentTimeMillis();
        TSPLAPointBoundKBMiner.initMatrixWithRTree(segmentList);
//        TSPLAPointBoundKBMiner.initMatrix(segmentList);
        initMatrixTime = System.currentTimeMillis() - initMatrixTime;
        System.out.println("initMatrix time is " + initMatrixTime);
	}
	
	public void testMatrixWithRTree2D() throws Exception{
		String fileName = "data\\test_10000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();

        for(int i = 0; i < segmentList.size(); i++){
        	if(segmentList.get(i).getLength() != 5){
        		segmentList.remove(i);
        		i--;
        	}
        }
        
        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            segment.idx = i;
        }
        
        System.out.println("SegmentList size is " + segmentList.size());
        
        boolean[][] matrix1 = new boolean[segmentList.size()][segmentList.size()];
        boolean[][] matrix2 = new boolean[segmentList.size()][segmentList.size()];
        
        //init matrix using nomal method
        System.out.println("Cost time of init matrix using nomal method");
        long nomalInitTime = System.currentTimeMillis();
        for (int i = 0; i < segmentList.size(); i++) {
          PLASegment segment = segmentList.get(i);
          for (int j = i; j < segmentList.size(); j++) {
              PLASegment plaSegment = segmentList.get(j);

              if (Polygons2D.intersection(segment.getPolygonKB(), plaSegment.getPolygonKB()).vertexNumber() > 0) {
                  matrix1[i][j] = true;
                  matrix1[j][i] = true;
              	}
          	}
        }
        nomalInitTime = System.currentTimeMillis() - nomalInitTime;
        System.out.println(nomalInitTime + "ms");
        long buildTreeTime = System.currentTimeMillis();
        Properties p = new Properties();
		p.setProperty("MinNodeEntries", "16");
		p.setProperty("MaxNodeEntries", "32");
		
    	RTree rTree = new RTree(segmentList); 
    	rTree.init(p);
    	rTree.buildRTree();
     	buildTreeTime = System.currentTimeMillis() - buildTreeTime;
     	System.out.println("Tree build Time: " + buildTreeTime);
     	
     	System.out.println("Cost time of init matrix using RTree");
     	long RTreeInitTime = System.currentTimeMillis();
     	rTree.initMatrix(matrix2);
     	RTreeInitTime = System.currentTimeMillis() - RTreeInitTime;
     	System.out.println(RTreeInitTime);
     	
     	int nonSame = 0;
     	for(int i =0; i < segmentList.size(); i++){
     		for(int j = 0; j < segmentList.size(); j++){
     			if(matrix1[i][j] != matrix2[i][j]){
     				nonSame++;
     			}
     		}
     	}
     	
     	System.out.println("nonSame :" + nonSame);
     	int[] ubs1 = new int[segmentList.size()];
     	int[] ubs2 = new int[segmentList.size()];
        
     	for (int i = 0; i < ubs1.length; i++) {
            ubs1[i] = 0;
            PLASegment lastSegment = null;
            for (int j = 0; j < ubs1.length; j++) {
                if (matrix1[i][j]) {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null) {
                        ubs1[i] = ubs1[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    } else {
                        //check start,end intersection
                        //last       -----
                        //current           -----
                        if (currentSegment.getStart() > lastSegment.getEnd()) {
                            ubs1[i] = ubs1[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        } else {
                            //last         -----
                            //current        ----
                            if (currentSegment.getEnd() > lastSegment.getEnd()) {
                                ubs1[i] = ubs1[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            } else {
                                //last          -------------
                                //current         ------
                                //do noting
                            }
                        }
                    }
                }
            }
        }
     	
     	for (int i = 0; i < ubs2.length; i++) {
            ubs2[i] = 0;
            PLASegment lastSegment = null;
            for (int j = 0; j < ubs2.length; j++) {
                if (matrix2[i][j]) {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null) {
                        ubs2[i] = ubs2[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    } else {
                        //check start,end intersection
                        //last       -----
                        //current           -----
                        if (currentSegment.getStart() > lastSegment.getEnd()) {
                            ubs2[i] = ubs2[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        } else {
                            //last         -----
                            //current        ----
                            if (currentSegment.getEnd() > lastSegment.getEnd()) {
                                ubs2[i] = ubs2[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            } else {
                                //last          -------------
                                //current         ------
                                //do noting
                            }
                        }
                    }
                }
            }
        }
     	int nonEqual = 0;
     	for(int i = 0; i < segmentList.size(); i++){
//     		System.out.println("ubs is" + ubs1[i] + "   " + ubs2[i]);
     		if(ubs1[i] != ubs2[i])
     			nonEqual++;
     	}
     	
     	System.out.println("nonEqual is" + nonEqual);
	}
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
        int maxLength = deepSearch.search(segmentList, 2000, 0.05);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed without RTree is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	public void testDeepSearch2WithLowerBound() throws Exception{
		String fileName = "data\\test_1000_4.txt";
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
        int maxLength = deepSearch.search(segmentList, 100, 0);
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed with LowerBound is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	public void testDeepSearch2() throws Exception{
		String fileName = "data\\test_10000_4.txt";
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(fileName));

        double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        
        List<PLASegment> segmentList = miner.buildAllSegments();

        for(int i = 0; i < segmentList.size(); i++){
        	if(segmentList.get(i).getLength() != 4){
        		segmentList.remove(i);
        		i--;
        	}
        }
        
        System.out.println("SegmentList size is " + segmentList.size());
        long timeTotal = System.currentTimeMillis();
        PLAExtendDeepSearch2 deepSearch = new PLAExtendDeepSearch2();
        int maxLength = deepSearch.search(segmentList, 100, 0.01);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed without RTree in deep search 2 is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
	}
	
	@Test
	public void testDeepSearchRTree2D() throws Exception{
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
        PLAExtendDeepSearchWithRTree2D deepSearch = new PLAExtendDeepSearchWithRTree2D();
        int maxLength = deepSearch.search(segmentList, 2000, 0.05);
        
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed with RTree2D is " + "   " + maxLength);
        System.out.println("Total Cost Time is " + timeTotal);
        
	}
	
	@Test
	public void testDeepSearchRTree3D() throws Exception{
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
        PLAExtendDeepSearchWithRTree3D deepSearch = new PLAExtendDeepSearchWithRTree3D();
        
        int maxLength = deepSearch.search(segmentList, 2000, 0.05);
        timeTotal = System.currentTimeMillis() - timeTotal;
        System.out.println("MaxLength computed with RTree3D is " + "   " + maxLength);
        
        System.out.println("Total Cost Time is " + timeTotal);
	}
}
