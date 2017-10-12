package cn.edu.fudan.dsm.tslrm;

import java.io.File;
import java.util.List;

import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;
import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import math.geom2d.Point2D;

public class ClimateDataTest {
	
	public void climateDateRegionSearch() throws Exception{
		String fileName = "E:\\climateByType\\SURF_CLI_CHN_MUL_DAY-TEM-1960-2012.txt";
		Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), 4, 5);
		double errorBound = 1;

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

//        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
//            PLASegment segment = miner.plaSegmentList.get(i);
//        }

        List<PLASegment> segmentList = miner.buildAllSegments();
        System.out.println("all segmentList.size() = " + segmentList.size());

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;

        List<PLASegment> segs = SegmentUtils.filter(segmentList, 3, 3);
        System.out.println("segs.size() = " + segs.size());

        Point2D point2Ds1 = plaRegionSearch.searchByBox2D(segs, 0.05);
        System.out.println("point2Ds1.getX() = " + point2Ds1.getX());
        System.out.println("point2Ds1.getY() = " + point2Ds1.getY());
        int realLength = SegmentUtils.verifyTrueLength(point2Ds, point2Ds1.getX(), point2Ds1.getY(), errorBound, 3);
        System.out.println("RealLength = " + realLength);
	}
	
	public static void main(String[] args){
		ClimateDataTest test = new ClimateDataTest();
		try {
			test.climateDateRegionSearch();
		} catch (Exception e) {
			System.out.println("Error detected !!!!!!!!!!!!!!");
			e.printStackTrace();
		}
	}

}
