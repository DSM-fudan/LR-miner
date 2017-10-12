package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.DataGenerator;
import math.geom2d.Point2D;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-12
 * Time: 下午9:55
 * To change this template use File | Settings | File Templates.
 */
public class RegionSearchTestWithMultiModel {

    static Logger logger = Logger.getLogger(RegionSearchTestWithMultiModel.class);

    @Test
    public void test() throws IOException {
        double error = 0.0;
        double errorBound = 1;
        Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File("data/test_10000_4.txt"));

        logger.debug("begin ...");
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        logger.debug("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        List<PLASegment> segs = miner.buildSpecificSegments(3);
        logger.debug("all segmentList.size() = " + segs.size());
        for (int i = segs.size() - 1; i >= 0; i--) {
            if (segs.get(i).getPolygonKB().getRings().size() > 1) {
                segs.remove(i);
                logger.debug("Remove at " + i);
            }
        }

        int totalLength = point2Ds.length;

        Point2D[] point2DLeft = point2Ds;
        while (true) {
            PLARegionSearch plaRegionSearch = new PLARegionSearch(point2DLeft);
            plaRegionSearch.errorBound = errorBound;

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
            if (plaRegionSearch.finalLength < totalLength / 10)
            {
                logger.debug("no more model!");
                break;
            }
            else
            {
                logger.debug("try to find more model ...........................");
                //remove match segment from list
                for (int i = segs.size() - 1; i >= 0; i--) {
                    if (segs.get(i).getPolygonKB().contains(point2Ds1.getX(),point2Ds1.getY())) {
                        segs.remove(i);
//                        logger.debug("Remove at " + i);
                    }
                }

                logger.debug("segs.size()=" + segs.size());
//                //remove points from array
//                //get points from segment list
//                List<Point2D> list = new ArrayList<Point2D>();
//                for (int i = 0; i < segs.size(); i++) {
//                    PLASegment segment = segs.get(i);
//
//                }
//


            }
        }

    }
}
