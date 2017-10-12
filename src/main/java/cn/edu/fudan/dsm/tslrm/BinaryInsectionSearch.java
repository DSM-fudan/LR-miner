package cn.edu.fudan.dsm.tslrm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;

public class BinaryInsectionSearch {
	
	private int maxLength = 0;
	private double maxK;
	private double maxB;
	private  Point2D[] point2Ds;
    private double errorBound;
    private List<PLASegment> segmentList;
	
	public BinaryInsectionSearch(Point2D[] point2Ds, double errorBound, List<PLASegment> segmentList){
		this.point2Ds = point2Ds;
		this.errorBound = errorBound;
		this.segmentList = segmentList;
	}
	
	public void binarySearch(double error){
		int segLength = segmentList.get(0).getLength();
		//initialize the BinarayInsectionInfo
		List<ArrayList> binaryList = new ArrayList<ArrayList>();
		int maxUpBound = 0;
		for(int i = 0; i < segmentList.size(); i++){
			PLASegment seg = segmentList.get(i);
			BinaryInsectionInfo binaryInfo = new BinaryInsectionInfo(seg.getStart(), seg.getEnd(), seg.getLength(), seg.getPolygonKB());
			Point2D tempKB = seg.getPolygonKB().getCentroid();
			int lowBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segLength);
			if(lowBound > maxLength){
				maxLength = lowBound;
				maxK = tempKB.getX();
				maxB = tempKB.getY();
			}
			ArrayList<BinaryInsectionInfo> segList = new ArrayList<BinaryInsectionInfo>();
			segList.add(binaryInfo);
			binaryList.add(segList);
		}
		prunePoly(binaryList);
		maxUpBound = findMaxUpBound(binaryList); 
		int loopsNum = 0;
		while(true){
			loopsNum++;
			System.out.println("LoopsNum = " + loopsNum);
			int binaryListLenth = binaryList.size();
			double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if ((e <= error) || (binaryList.size() == 1)){
                System.out.println("e = " + e);
                break;
            }
			List<ArrayList> tempBinaryList = new ArrayList<ArrayList>();
			for(int i = 0; i < binaryListLenth; i += 2){
				ArrayList<BinaryInsectionInfo> group1 = binaryList.get(i);
				ArrayList<BinaryInsectionInfo> newGroup = new ArrayList<BinaryInsectionInfo>();
				if(i + 1 < binaryListLenth){
					ArrayList<BinaryInsectionInfo> group2 = binaryList.get(i + 1);
					for(int j = 0; j < group1.size(); j++){
						BinaryInsectionInfo tempgroup1 = group1.get(j);
						for(int k = 0; k < group2.size(); k++){
							BinaryInsectionInfo tempgroup2 = group2.get(k);
							Polygon2D intersection = null;
							if(NewPolyUtils.isSame(tempgroup1.getPoly(), tempgroup2.getPoly())){
								tempgroup1.delete();
								tempgroup2.delete();
							}
							intersection = NewPolyUtils.intersection(tempgroup1.getPoly(), tempgroup2.getPoly());
							if(intersection.getVertexNumber() > 0){
								BinaryInsectionInfo newInsectionInfo = new BinaryInsectionInfo();
								newGroup.add(newInsectionInfo);
								tempgroup1.addChild(newInsectionInfo);
								tempgroup2.addChild(newInsectionInfo);
								
								newInsectionInfo.setStart(tempgroup1.getStart());
								newInsectionInfo.setEnd(tempgroup2.getEnd());
								newInsectionInfo.setParents(tempgroup1, tempgroup2);
								newInsectionInfo.setPoly(intersection);
								newInsectionInfo.setGroupId(i);
								if(tempgroup1.getEnd() < tempgroup2.getStart()){
									newInsectionInfo.setCurrentLength(tempgroup1.getCurrentLength() + tempgroup2.getCurrentLength());
								}else{
									newInsectionInfo.setCurrentLength(tempgroup1.getCurrentLength() + tempgroup2.getCurrentLength() - 
											(tempgroup1.getEnd() - tempgroup2.getStart() + 1));
								}
								Point2D tempKB = intersection.getCentroid();
								int lowBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.getX(), tempKB.getY(), errorBound, segLength);
	            				if(lowBound > maxLength){
	            					maxLength = lowBound;
	            					maxK = tempKB.getX();
	            					maxB = tempKB.getY();
	            				}
							}
						}
					}
					for(int l = 0; l < group1.size(); l++){
						BinaryInsectionInfo groupInfo1 = group1.get(l);
						if(!groupInfo1.isDelete())
							newGroup.add(groupInfo1);
					}
					for(int l = 0; l < group2.size(); l++){
						BinaryInsectionInfo groupInfo2 = group2.get(l);
						if(!groupInfo2.isDelete())
							newGroup.add(groupInfo2);
					}
					
					sortGroup(newGroup);					
				}else{
					newGroup.addAll(group1);
				}
				tempBinaryList.add(newGroup);
			}
			binaryList = tempBinaryList;
			//prune poly whose upBound is lower than maxLength
			prunePoly(binaryList);
			maxUpBound = findMaxUpBound(binaryList);
			System.out.println("Now : maxLength = " + maxLength);
			System.out.println("Now : maxUpBound = " + maxUpBound);
		}
	}
	
	public void prunePoly(List<ArrayList> binaryList){
		boolean needPrune = true;
		while(needPrune){
			needPrune = false;
			for(int i = 0; i < binaryList.size();){
				ArrayList<BinaryInsectionInfo> group = binaryList.get(i);
				for(int j = 0; j < group.size(); j++){
					BinaryInsectionInfo proInfo = group.get(j);
					int tempUpBound = calUpBound(binaryList, proInfo);
					if(tempUpBound <= maxLength){
						deletePoly(proInfo, group);
						needPrune = true;
					}
					proInfo.setUpBound(tempUpBound);
				}
				if(group.size() == 0){
					binaryList.remove(i);
				}else{
					i++;
				}
			}
			if(needPrune){
				System.out.println("NeedPrune = true");
			}
		}
	}
	
	public int findMaxUpBound(List<ArrayList> binaryList){
		int upbound = 0;
		int polyNum = 0;
		for(int i = 0; i < binaryList.size(); i++){
			ArrayList<BinaryInsectionInfo> group = binaryList.get(i);
			for(int j = 0; j < group.size(); j++){
				BinaryInsectionInfo proInfo = group.get(j);
				polyNum++;
				if(upbound < proInfo.getUpBound()){
					upbound = proInfo.getUpBound();
				}
			}
		}
		System.out.println("Poly Num = " + polyNum);
		return upbound;
	}
	
	public void sortGroup(ArrayList<BinaryInsectionInfo> group){
		Collections.sort(group, new Comparator<BinaryInsectionInfo>(){
			public int compare(BinaryInsectionInfo info1, BinaryInsectionInfo info2){
				int indicator = info1.getCurrentLength() - info2.getCurrentLength();
				if(indicator != 0){
					return indicator;
				}else{
					indicator = info1.getStart() - info2.getStart();
					if(indicator != 0){
						return indicator;
					}else{
						return info2.getEnd() -info1.getEnd();
					}
				}
			}
		});
	}
	
	public int calUpBound(List<ArrayList> binaryList, BinaryInsectionInfo proInfo){
		int upBound = 0;
		int lastEnd = -1;
		if(binaryList.size() == 0)
			return upBound;
		ArrayList<BinaryInsectionInfo> group0 = binaryList.get(0);
		
		for(int i = 0; i < binaryList.size(); i++){
			if(i == proInfo.getGroupId()){
				lastEnd = proInfo.getEnd();
				upBound += proInfo.getCurrentLength();
				continue;
			}
			ArrayList<BinaryInsectionInfo> tempGroup = binaryList.get(i);
			for(int j = tempGroup.size() - 1; j > -1; j--){
				BinaryInsectionInfo tempInfo = tempGroup.get(j);
				Polygon2D intersection = null;
				try{
				intersection = NewPolyUtils.intersection(tempInfo.getPoly(), proInfo.getPoly());
				}catch(Exception ex){
					try {
						PrintWriter print1 = new PrintWriter(new FileWriter("data\\Exception1.txt"));
						PrintWriter print2 = new PrintWriter(new FileWriter("data\\Exception2.txt"));
						for(int l = 0; l < tempInfo.getPoly().getVertexNumber(); l++){
							print1.println(tempInfo.getPoly().getVertex(l).getX() + " " + proInfo.getPoly().getVertex(l).getX());
							print2.println(tempInfo.getPoly().getVertex(l).getY() + " " + proInfo.getPoly().getVertex(l).getY());
						}
						print1.println(tempInfo.getPoly().getVertex(0).getX() + " " + proInfo.getPoly().getVertex(0).getX());
						print2.println(tempInfo.getPoly().getVertex(0).getY() + " " + proInfo.getPoly().getVertex(0).getY());
						print1.close();
						print2.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for(int l = 0; l < tempInfo.getPoly().getVertexNumber(); l++){
						System.out.println("vetex Num " + l +": X = " + tempInfo.getPoly().getVertex(l).getX() + "  Y = "+ tempInfo.getPoly().getVertex(l).getY());
					}
					System.out.println("*******************");
					for(int l = 0; l < proInfo.getPoly().getVertexNumber(); l++){
						System.out.println("vetex Num " + l +": X = " + proInfo.getPoly().getVertex(l).getX() + "  Y = "+ proInfo.getPoly().getVertex(l).getY());
					}
				}
//				Polygon2D intersection = Polygon2DUtils.intersection(tempInfo.getPoly(), proInfo.getPoly());
				if(intersection.getVertexNumber() > 0){
					if(lastEnd < tempInfo.getStart()){
						upBound += tempInfo.getCurrentLength();
					}else{
						upBound += tempInfo.getCurrentLength() - (lastEnd - tempInfo.getStart() + 1);
					}
					lastEnd = tempInfo.getEnd();
					break;
				}
			}
		}
		return upBound;
	}
	
	public void deletePoly(BinaryInsectionInfo deletePoly, ArrayList<BinaryInsectionInfo> group){
		group.remove(deletePoly);
		int childrenNum = deletePoly.getChildren().size();
		for(int i = 0; i < childrenNum; i++){
			BinaryInsectionInfo child = deletePoly.getChildren().get(i);
			deletePoly(child, group);
			BinaryInsectionInfo[] parents = child.getParents();
			//parent num is 2
			if(parents[0] == deletePoly){
				parents[1].getChildren().remove(child);
			}else{
				parents[0].getChildren().remove(child);
			}
		}
	}

	public int getMaxLength() {
		return maxLength;
	}

	public double getMaxK() {
		return maxK;
	}

	public double getMaxB() {
		return maxB;
	}
	
}
