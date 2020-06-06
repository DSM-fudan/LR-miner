package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import com.infomatiq.jsi.rtree.RTree;
import com.infomatiq.jsi.rtree.Rectangle;

/**
 * Created with IntelliJ IDEA.
 * User: MaYuanwen
 * Date: 12-11-30
 * Time: 下午9:10
 * To change this template use File | Settings | File Templates.
 * Without Matrix
 */
public class PLAExtendDeepSearch3 {
	private Point2D[] point2Ds;
	private RTree rTree;
	private double errorBound;
	private List<PLASegment> segmentList;
	public PLAExtendDeepSearch3(Point2D[] point2Ds, double errorBound){
		this.point2Ds = point2Ds;
		this.errorBound = errorBound;
	}

	public PLAExtendDeepSearch3(){}
	
	public double slope;
	public double intercept;
	public int maxLength;
	public Point2D maxKB = null;
	
	public int search(List<PLASegment> segmentList, int startLength, double error){
		this.segmentList = segmentList;
		maxLength = startLength;		
		int maxUpBound = initSegment();
		System.out.println("init MaxLength is " + maxLength);
    	List<PLASegment> stack = new ArrayList<PLASegment>();
    	PLASegment fakeSegment = new PLASegment();
        fakeSegment.idx = -1;
        fakeSegment.setStart(-10);
        fakeSegment.setEnd(-10);
        fakeSegment.setLength(0);
        fakeSegment.setDelete(false);
        fakeSegment.setPolygonKB(new SimplePolygon2D(TSPLAPointBoundKBMiner.X_INF, TSPLAPointBoundKBMiner.Y_INF));
        fakeSegment.totalLength = fakeSegment.getLength();
        fakeSegment.currentPolygon = fakeSegment.getPolygonKB();
        stack.add(fakeSegment);
        
        int c = 0;
        int startIdx = 0;
        while(stack.size() > 0){
        	double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                System.out.println("maxUpbound = " + maxUpBound);
                System.out.println("maxLength = " + maxLength);
                System.out.println("e = " + e);
                break;
            }
            
            PLASegment nextSegment = searchNext(stack, startIdx);
            ////add more segments into stack until end
            if(nextSegment !=null){
            	c++;
                if(c % 1000000 == 0)
                	System.out.println("C = " + c + "!!!!!!!");
                stack.add(nextSegment);
                Point2D tempKB = nextSegment.currentPolygon.centroid();
                int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.x(), tempKB.y(), errorBound, segmentList.get(0).getLength());
                if(lowerBound > maxLength){
                	maxKB = tempKB;
                	maxLength = lowerBound;
                	maxUpBound = adjustSegment(maxUpBound);
                	//check if the segment in stack has been deleted
                	int deleteIndex = -1;
                	for(int i = 1; i < stack.size(); i++){
                		if(stack.get(i).isDelete()){
                			deleteIndex = i;
                			break;
                		}
                	}
                	if((deleteIndex > -1) && (deleteIndex < stack.size())){
                		startIdx = stack.get(deleteIndex).idx + 1;
                		for(int i = stack.size() - 1; i >= deleteIndex; i--){
                			stack.remove(i);
                		}
                		continue;
                	}
                	
                }
                
                int upperBound = calUpBound(nextSegment.currentPolygon);
                if(upperBound <= maxLength){
                	//do pop
                	startIdx = segmentList.size();
                }else{
                	startIdx = nextSegment.idx + 1;
                }
            }else{
            	//nextSegment = null;
            	PLASegment removed = stack.remove(stack.size() - 1);
            	if(stack.size() == 0){
            		System.out.println("Statck Size is 0!!!!!!!!!!!!!!!");
                    break;
            	}else{
            		startIdx = removed.idx + 1;
            	}
            }
        }
        System.out.println("maxLength = " + maxLength);
        System.out.println("C = " + c + "!!!!!!!");
        
        if(maxKB != null){
        	System.out.println("k = " + maxKB.x());
            System.out.println("b = " + maxKB.y());
            int realLength = SegmentUtils.verifyTrueLength(point2Ds, maxKB.x(), maxKB.y(), errorBound, segmentList.get(0).getLength());
            System.out.println("RealLength with lowBound is " + realLength);
            this.slope = maxKB.x();
            this.intercept = maxKB.y();
        }
        
        return maxLength;
	}
	
	private PLASegment searchNext(List<PLASegment> stack, int startIdx){
		PLASegment topSegment = stack.get(stack.size() - 1);
        Polygon2D currentPolygon = topSegment.currentPolygon;
        for (int i = startIdx; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if(segment.isDelete())
            	continue;
            Polygon2D intersection = Polygons2D.intersection(currentPolygon, segment.getPolygonKB());
            if (intersection.vertexNumber() > 0) {
            	segment.currentPolygon = intersection;
                return segment;
            }
        }

        return null;
	}
	//initialize the information of each segment including upBound,lowerBound,isDelete
	//return the max lowerBound
	public int initSegment(){
		//init Segment
		int maxUpBound = -1;
		for(int i = 0; i < segmentList.size(); i++){
			PLASegment tempSeg = segmentList.get(i);
			Point2D tempKB = tempSeg.getPolygonKB().centroid();
            int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.x(), tempKB.y(), errorBound, segmentList.get(0).getLength());
            tempSeg.setLowerBound(lowerBound);
            if(lowerBound >= maxLength){
            	maxLength = lowerBound;
            	maxKB = tempKB;
            }
		}
		
		sortSegmentsBasedOnLowBound(segmentList);
		
		for(int i = 0; i < segmentList.size(); i++){
			PLASegment tempSeg = segmentList.get(i);
			tempSeg.idx = i;
			tempSeg.setDelete(false);
		}
		
		buildTree();
		//init upBound
		boolean needPrune = false;
		for(int i = 0; i < segmentList.size(); i++){
			PLASegment tempSeg = segmentList.get(i);
			Polygon2D probePoly = tempSeg.getPolygonKB();
			int upBound = calUpBound(probePoly);
			tempSeg.setUpBound(upBound);
			if(upBound <= maxLength){
				needPrune = true;
			}
			if(upBound > maxUpBound)
				maxUpBound = upBound;
		}
		if(needPrune){
			maxUpBound = adjustSegment(maxUpBound);
		}
		
		return maxUpBound;
	}
	//Given a maxLength, Delete the Segment in RTree whose UpBound is lower than maxLength.
	//after deletion, adjust the UpBound of each segment. Loop the process until
	// there is no change for the PuBound for each segment
	public int adjustSegment(int initUpBound){
		int loops = 0;
		int size = segmentList.size();
		int maxUpBound = initUpBound;
		boolean needPrune = true;
		while(needPrune){
			loops++;
			needPrune = false;
			for(int i = 0; i < segmentList.size(); i++){
				PLASegment tempSeg = segmentList.get(i);
				if((!tempSeg.isDelete()) && (tempSeg.getUpBound() <= maxLength)){
					tempSeg.setDelete(true);
					deleteSegment(tempSeg);
					size--;
					needPrune = true;
				}
			}
			// reCalculate the upBound for each Segment
			if(needPrune){
				maxUpBound = -1;
				for(int i = 0; i < segmentList.size(); i++){
					PLASegment tempSeg = segmentList.get(i);
					Polygon2D probePoly = tempSeg.getPolygonKB();
					int tempUpBound = calUpBound(probePoly);
					tempSeg.setUpBound(tempUpBound);
					if(tempUpBound > maxUpBound)
						maxUpBound = tempUpBound;
				}
			}
		}
		return maxUpBound;
	}
	
	//Given a probePoly, calculate its UpBound
	public int calUpBound(Polygon2D probePoly){
		int upBound = 0;
		List<PLASegment> segments = rTree.calUpBound(probePoly);
		if(segments.size() <=0){
			return upBound;
		}
		if(segments.size() == 1){
			return segments.get(0).getLength();
		}
		sortSegments(segments);
		PLASegment lastSeg = segments.get(0);
		upBound = lastSeg.getLength();
		for(int i = 1; i < segments.size(); i++){
			PLASegment curSeg = segments.get(i);
			if(curSeg.getStart() > lastSeg.getEnd()){
				upBound += curSeg.getLength();
			}else{
				upBound += curSeg.getEnd() - lastSeg.getEnd();
			}
			lastSeg = curSeg;
		}
		return upBound;
	}
	
	// sort Segment based on start
	public void sortSegments(List<PLASegment> segments){
		Collections.sort(segments, new Comparator<PLASegment>(){
			public int compare(PLASegment p1, PLASegment p2){
				return p1.getStart() - p2.getStart();
			}
		});
	}
	//sort Segment based on lowBound
	public void sortSegmentsBasedOnLowBound(List<PLASegment> segments){
		Collections.sort(segments, new Comparator<PLASegment>(){
			public int compare(PLASegment p1, PLASegment p2){
				return p2.getLowerBound() - p1.getLowerBound();
			}
		});
	}
	//delete segment in the RTree
	private void deleteSegment(PLASegment plaSegment){
		Polygon2D currentPoly = plaSegment.getPolygonKB();
		Box2D box = currentPoly.boundingBox();
		Rectangle probeRect = new Rectangle(box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY());
		rTree.delete(probeRect, plaSegment.idx);
	}
	
	private void buildTree(){
		
		Properties p = new Properties();
		p.setProperty("MinNodeEntries", "16");
		p.setProperty("MaxNodeEntries", "32");
		
    	rTree = new RTree(segmentList); 
    	rTree.init(p);
    	rTree.buildRTree();
	}
	
	
}
