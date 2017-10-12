package cn.edu.fudan.dsm.tslrm;

import math.geom2d.polygon.Polygon2D;

public class IntersectPolyInfo {
	
	private Polygon2D poly;
	private int lowUpBound;
	private int upBound;
	private int start;
	private int end;
	private int currentLength;
	private int upBoundEnd;
	
	public IntersectPolyInfo(){
		
	}

	public IntersectPolyInfo(Polygon2D poly, int lowUpBound, int upBound,
			int start, int end, int currentLength, int upBoundEnd) {
		this.poly = poly;
		this.lowUpBound = lowUpBound;
		this.upBound = upBound;
		this.start = start;
		this.end = end;
		this.currentLength = currentLength;
		this.upBoundEnd = upBoundEnd;
	}
	
	

	public IntersectPolyInfo(Polygon2D poly, int start, int end,
			int currentLength, int upBoundEnd) {
		this.poly = poly;
		this.start = start;
		this.end = end;
		this.currentLength = currentLength;
		this.upBoundEnd = upBoundEnd;
	}

	public Polygon2D getPoly() {
		return poly;
	}

	public void setPoly(Polygon2D poly) {
		this.poly = poly;
	}

	public int getLowUpBound() {
		return lowUpBound;
	}

	public void setLowUpBound(int lowUpBound) {
		this.lowUpBound = lowUpBound;
	}

	public int getUpBound() {
		return upBound;
	}

	public void setUpBound(int upBound) {
		this.upBound = upBound;
	}

	public int getCurrentLength() {
		return currentLength;
	}

	public void setCurrentLength(int currentLength) {
		this.currentLength = currentLength;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getUpBoundEnd() {
		return upBoundEnd;
	}

	public void setUpBoundEnd(int upBoundEnd) {
		this.upBoundEnd = upBoundEnd;
	}
		
}
