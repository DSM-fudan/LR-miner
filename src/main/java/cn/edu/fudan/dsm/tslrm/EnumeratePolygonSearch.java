package cn.edu.fudan.dsm.tslrm;

import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;

import math.geom2d.Point2D;
import math.geom2d.polygon.MultiPolygon2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;

public class EnumeratePolygonSearch {
	
	private double maxK;
	private double maxB;
	private int maxLength;
	private int maxUpBound;
	private  Point2D[] point2Ds;
    private double errorBound;
    private List<PLASegment> segmentList;
    
    public EnumeratePolygonSearch(Point2D[] point2Ds, double errorBound, List<PLASegment> segmentList){
    	this.point2Ds = point2Ds;
    	this.errorBound = errorBound;
    	this.segmentList = segmentList;
    }
    
    public void enumeratePolySearch(){
    	List<IntersectPolyInfo> possiblePolys = new ArrayList<IntersectPolyInfo>();
    	int segmentSize = segmentList.size();
    	//enumerate all possible polygons
    	int i = 0;
    	while(i < segmentSize){
    		System.out.println("i = " + i);
    		PLASegment proSeg = segmentList.get(i);
    		Polygon2D proPoly = proSeg.getPolygonKB();
    		IntersectPolyInfo proPolyInfo = new IntersectPolyInfo(proPoly, proSeg.getStart(), proSeg.getEnd(), proSeg.getLength(), proSeg.getEnd());
    		int j = 0;
    		int possiblePolyLength = possiblePolys.size();
    		while(j < possiblePolyLength){
    			IntersectPolyInfo curPolyInfo = possiblePolys.get(j);
    			if(curPolyInfo.getUpBound() > maxLength){
    				Polygon2D intersectPolygon2D = Polygon2DUtils.intersection(curPolyInfo.getPoly(), proPoly);
    				if(intersectPolygon2D instanceof MultiPolygon2D){
//    					System.out.println("Multiple2D !!!!!!!!");
    					j++;
    					continue;
    				}
    				if(intersectPolygon2D.getVertexNumber() > 0){
        				IntersectPolyInfo tempPolyInfo = new IntersectPolyInfo();
        				tempPolyInfo.setPoly(intersectPolygon2D);
        				tempPolyInfo.setEnd(proSeg.getEnd());
        				tempPolyInfo.setUpBoundEnd(proSeg.getEnd());
        				if(curPolyInfo.getEnd() < proSeg.getStart()){
        					tempPolyInfo.setCurrentLength(curPolyInfo.getCurrentLength() + proSeg.getLength());
        				}else{
        					tempPolyInfo.setCurrentLength(curPolyInfo.getCurrentLength() + proSeg.getEnd() - curPolyInfo.getEnd());
        				}
        				if(curPolyInfo.getUpBoundEnd() < proSeg.getStart()){
        					curPolyInfo.setUpBound(curPolyInfo.getUpBound() - proSeg.getLength());
        				}else{
        					curPolyInfo.setUpBound(curPolyInfo.getUpBound() - (proSeg.getEnd() - curPolyInfo.getUpBoundEnd()));
        				}
        				curPolyInfo.setUpBoundEnd(proSeg.getEnd());
        				int upBound = computerUpBound(tempPolyInfo, i);
        				// decide if the curPolyInfo should be removed					
        				if(upBound >= curPolyInfo.getUpBound()){
        					possiblePolys.remove(curPolyInfo);
        					possiblePolyLength--;
        				}else{
        					j++;
        				}
        				if(upBound > maxLength){
        					// to get the lowerBound of the new Polygon
            				Point2D tempKB = intersectPolygon2D.getCentroid();
            				int lowBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segmentList.get(0).getLength());
            				if(lowBound > maxLength){
            					maxLength = lowBound;
            					maxK = tempKB.getX();
            					maxB = tempKB.getY();
            				}
            				if(lowBound < upBound){
            					possiblePolys.add(tempPolyInfo);
            					tempPolyInfo.setUpBound(upBound);
            					tempPolyInfo.setLowUpBound(lowBound);
            				}
        				}   				
        			}else{
        				j++;
        			}
    			}else{
    				possiblePolys.remove(j);
    				possiblePolyLength--;
    			}
    			
    		}
    		
    		// decide if the proPolyInfo should be added to possiblePolys
    		int proSegUpBound = computerUpBound(proPolyInfo, i);
    		if(proSegUpBound > maxLength){
    			possiblePolys.add(proPolyInfo);
    			proPolyInfo.setUpBound(proSegUpBound);
    		}
    		//find the maxUpBound
    		maxUpBound = 0;
    		for(int k = 0; k < possiblePolys.size(); k++){
    			int tempUpBound = possiblePolys.get(k).getUpBound();
    			if(maxUpBound < tempUpBound){
    				maxUpBound = tempUpBound;
    			}
    		}
    		System.out.println("PossiblePolys.size = " + possiblePolys.size());
    		System.out.println("maxUpBound = " + maxUpBound);
    		System.out.println("MaxLength = " + maxLength);
    		i++;
    	}
    	
    	for(int k = 0; k < possiblePolys.size(); k++){
    		IntersectPolyInfo printInfo = possiblePolys.get(k);
    		System.out.println("upBound = " + printInfo.getUpBound());
    	}
    }
    
    public int computerUpBound(IntersectPolyInfo polyInfo, int startId){
    	startId += 1;
    	int upBound = polyInfo.getCurrentLength();
    	int end = polyInfo.getEnd();
    	for(int i = startId; i < segmentList.size(); i++){
    		PLASegment curSeg = segmentList.get(i);
    		if(Polygon2DUtils.intersection(polyInfo.getPoly(), curSeg.getPolygonKB()).getVertexNumber() > 0){
    			if(end < curSeg.getStart()){
    				upBound += curSeg.getLength();
    			}else{
    				upBound += curSeg.getEnd() - end;
    			}
    			end = curSeg.getEnd();
    		}
    	}
    	
    	return upBound;
    }

	public double getMaxK() {
		return maxK;
	}

	public void setMaxK(double maxK) {
		this.maxK = maxK;
	}

	public double getMaxB() {
		return maxB;
	}

	public void setMaxB(double maxB) {
		this.maxB = maxB;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
    
    
}
