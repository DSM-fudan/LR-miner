package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.DataGenerator;
import math.geom2d.Point2D;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-15
 * Time: 上午2:02
 * To change this template use File | Settings | File Templates.
 */
public class PLAPointBoundMinerTest {
    private Logger logger = Logger.getLogger(PLAPointBoundMinerTest.class);

    @Test
    public void testRandomWalk()
    {
        Point2D[] point2Ds = DataGenerator.randomWalk(1000*10, 1);

        double errorBound = 1;
        PLAPointBoundMiner miner = new PLAPointBoundMiner(point2Ds,errorBound);
        miner.process();
        System.out.println("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        for (int i = 0; i < miner.plaSegmentList.size(); i++) {
            PLASegment segment = miner.plaSegmentList.get(i);
            System.out.println("segment = " + segment);
        }

        boolean verify = PLASegment.verify(miner.points, miner.plaSegmentList, errorBound);
        System.out.println("verify = " + verify);

        PLAPointBoundMiner.findMaxIntersection(miner.plaSegmentList);
    }
}
