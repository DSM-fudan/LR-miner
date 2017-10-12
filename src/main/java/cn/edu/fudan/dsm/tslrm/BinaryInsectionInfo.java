package cn.edu.fudan.dsm.tslrm;

import java.util.ArrayList;
import java.util.List;

import math.geom2d.polygon.Polygon2D;

public class BinaryInsectionInfo {
	
	private int start;
	private int end;
	private int currentLength;
	private int upBound;
	private int groupId = -1;
	private boolean isDelete = false;

	private List<BinaryInsectionInfo> children = new ArrayList<BinaryInsectionInfo>();
	private BinaryInsectionInfo[] parents = new BinaryInsectionInfo[2];
	private Polygon2D poly;
	
	public BinaryInsectionInfo(){
		
	}
	
	public BinaryInsectionInfo(int start, int end, int currentLength, Polygon2D poly){
		this.start = start;
		this.end = end;
		this.currentLength = currentLength;
		this.poly = poly;
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
	public int getCurrentLength() {
		return currentLength;
	}
	public void setCurrentLength(int currentLength) {
		this.currentLength = currentLength;
	}
	public int getUpBound() {
		return upBound;
	}
	public void setUpBound(int upBound) {
		this.upBound = upBound;
	}

	public List<BinaryInsectionInfo> getChildren(){
		return children;
	}
	
	public void addChild(BinaryInsectionInfo childInfo){
		children.add(childInfo);
	}
	
	public BinaryInsectionInfo[] getParents(){
		return parents;
	}
	
	public void setParents(BinaryInsectionInfo parent1, BinaryInsectionInfo parent2){
		parents[0] = parent1;
		parents[1] = parent2;
	}

	public Polygon2D getPoly() {
		return poly;
	}

	public void setPoly(Polygon2D poly) {
		this.poly = poly;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public void delete(){
		isDelete = true;
	}
	
	public boolean isDelete(){
		return isDelete;
	}
	
}
