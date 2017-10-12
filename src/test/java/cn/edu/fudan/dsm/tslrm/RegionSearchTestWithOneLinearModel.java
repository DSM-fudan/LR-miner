package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;
import cn.edu.fudan.dsm.tslrm.data.DataGenerator;
import math.geom2d.Point2D;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-12
 * Time: 下午6:41
 * To change this template use File | Settings | File Templates.
 */
public class RegionSearchTestWithOneLinearModel {
    static Logger logger = Logger.getLogger(RegionSearchTestWithOneLinearModel.class);

    @Test
    public void test100000()
    {
        double error = 0.000;
        double errorBound = 0.1;
        Point2D[] point2Ds = new Point2D[1000*100];
        for (int i = 0; i < point2Ds.length; i++) {
            point2Ds[i] = new Point2D(i,i*2+10);
        }

        logger.debug("begin ...");
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        logger.debug("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        List<PLASegment> segs = miner.buildSpecificSegments(3);
        logger.debug("all segmentList.size() = " + segs.size());

        int[] values = new int[10];
        for (int i = 0; i < segs.size(); i++) {
            PLASegment plaSegment = segs.get(i);
            values[plaSegment.getPolygonKB().getVertexNumber()]++;
        }

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;

        for(int i = segs.size() - 1; i >= 0; i--){
            if(segs.get(i).getPolygonKB().getRings().size() > 1){
                segs.remove(i);
                logger.debug("Remove at " + i);
            }
        }

        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
        stopWatch2.stop();

        logger.debug("point2Ds1.getX() = " + point2Ds1.getX());
        logger.debug("point2Ds1.getY() = " + point2Ds1.getY());
        logger.debug("PartitionNum = " + plaRegionSearch.partitionNum);
        logger.debug("RealLength = " + plaRegionSearch.finalLength);
        logger.debug("CountInsides = " + plaRegionSearch.countInsides);
        logger.debug("stopWatchInsides = " + plaRegionSearch.stopWatchInsides.getTime());
    }

    @Test
    public void test1000SamePoint()
    {
        double error = 0.000;
        double errorBound = 0.1;
        Point2D[] point2Ds = new Point2D[1000];

        for (int i = 0; i < point2Ds.length; i++) {
            point2Ds[i] = new Point2D(3,3*2+10);
        }

        point2Ds = DataGenerator.addError(point2Ds,0.001,0.1);

        logger.debug("begin ...");
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        logger.debug("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        List<PLASegment> segs = miner.buildSpecificSegments(3);
        logger.debug("all segmentList.size() = " + segs.size());

        int[] values = new int[10];
        for (int i = 0; i < segs.size(); i++) {
            PLASegment plaSegment = segs.get(i);
            values[plaSegment.getPolygonKB().getVertexNumber()]++;
        }

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;

        for(int i = segs.size() - 1; i >= 0; i--){
            if(segs.get(i).getPolygonKB().getRings().size() > 1){
                segs.remove(i);
                logger.debug("Remove at " + i);
            }
        }

        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
        stopWatch2.stop();

        logger.debug("point2Ds1.getX() = " + point2Ds1.getX());
        logger.debug("point2Ds1.getY() = " + point2Ds1.getY());
        logger.debug("PartitionNum = " + plaRegionSearch.partitionNum);
        logger.debug("RealLength = " + plaRegionSearch.finalLength);
        logger.debug("CountInsides = " + plaRegionSearch.countInsides);
        logger.debug("stopWatchInsides = " + plaRegionSearch.stopWatchInsides.getTime());
    }

}
