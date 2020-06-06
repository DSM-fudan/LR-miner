package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.ForexData;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import cn.edu.fudan.dsm.tslrm.log.DefaultFileAppender;
import math.geom2d.Point2D;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-30
 * Time: 上午10:21
 * To change this template use File | Settings | File Templates.
 */
public class ForexCorrelationTest {
    static Logger logger = Logger.getLogger(ForexDataReginSearchTest.class);

    @Test
    public void test() throws IOException {
        Logger.getRootLogger().addAppender(new DefaultFileAppender(this.getClass()));
        double errorBound = 0.1;
        double accuracy = 0.005;
        int consecutive = 3;
        logger.debug("errorBound = " + errorBound);
        logger.debug("accuracy = " + accuracy);
        logger.debug("consecutive = " + consecutive);

        File dir = new File("data/currency");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("60_");
            }
        });

        System.out.println("files.length = " + files.length);

        ForexData[] forexDatas = new ForexData[files.length];
        for (int i = 0; i < forexDatas.length; i++) {
            forexDatas[i] = ForexData.readFromFile(files[i].getAbsolutePath());
            logger.debug("read " + forexDatas[i].secondCurrency);
        }

        List<ForexCorrelation> forexCorrelations = new ArrayList<ForexCorrelation>();

        //do cross region search for each forexDatas
        for (int i = 0; i < forexDatas.length; i++) {
            ForexData dataX = forexDatas[i];
            logger.debug("==========dataX.secondCurrency = " + dataX.secondCurrency);
            for (int j = 0; j < forexDatas.length; j++) {
                ForexData dataY = forexDatas[j];

                if (!dataX.secondCurrency.equals(dataY.secondCurrency)) //do when the currency is not the same
                {
                    logger.debug("----------dataY.secondCurrency = " + dataY.secondCurrency);

                    ForexCorrelation forexCorrelation = regionSearch(dataX, dataY, errorBound, accuracy, consecutive);
                    logger.debug("forexCorrelation = " + forexCorrelation);
                    forexCorrelations.add(forexCorrelation);
                }
            }
        }

        logger.debug("forexCorrelations.size() = " + forexCorrelations.size());
        logger.debug("----------------do for cross relation ---------------");

        for (int i = 0; i < forexCorrelations.size(); i++) {
            ForexCorrelation forexCorrelation1 = forexCorrelations.get(i);

            logger.debug("forexCorrelation1.currencyX = " + forexCorrelation1.currencyX);
            for (int j = 0; j < forexCorrelations.size(); j++) {
                ForexCorrelation forexCorrelation2 = forexCorrelations.get(j);
                if (forexCorrelation1.currencyX.equals(forexCorrelation2.currencyX))
                {
                    //calc relative
                    logger.debug("forexCorrelation1.currencyY = " + forexCorrelation1.currencyY);
                    logger.debug("forexCorrelation2.currencyY = " + forexCorrelation2.currencyY);

                    double v = CorrelationUtil.calcCorrelation(forexCorrelation1.positions, forexCorrelation2.positions);
                    logger.debug("relative = " + v);
                }
            }
        }
    }

    public static ForexCorrelation regionSearch(ForexData dataX, ForexData dataY, double errorBound, double accuracy, int consecutive)
    {
        logger.debug("begin ...");
        Point2D[] point2Ds = Point2DUtils.genFromForexData(dataX, dataY, 1);   //for normalize value
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);

        List<PLASegment> segs = miner.buildSpecificSegments(consecutive);
        logger.debug("all segmentList.size() = " + segs.size());
        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;

        for(int i = segs.size() - 1; i >= 0; i--){
            if(segs.get(i).getPolygonKB().boundary().size() > 1){
                segs.remove(i);
                logger.debug("Remove at " + i);
            }
        }

        StopWatch stopWatch2 = new StopWatch();
        stopWatch2.start();
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, accuracy);
        stopWatch2.stop();

        logger.debug("point2Ds1.getX() k= " + point2Ds1.x());
        logger.debug("point2Ds1.getY() b= " + point2Ds1.y());
        logger.debug("PartitionNum = " + plaRegionSearch.partitionNum);
        logger.debug("RealLength = " + plaRegionSearch.finalLength);
        logger.debug("CountInsides = " + plaRegionSearch.countInsides);
        logger.debug("stopWatchInsides = " + plaRegionSearch.stopWatchInsides.getTime());
        Set<Integer> positions = SegmentUtils.verifyTrueLengthReturnPoints(point2Ds,point2Ds1.x(),point2Ds1.y(),errorBound,consecutive);
        logger.debug("positions.size() = " + positions.size());
        ForexCorrelation ret = new ForexCorrelation(dataX.secondCurrency,dataY.secondCurrency,errorBound,point2Ds1.x(),point2Ds1.y(),plaRegionSearch.finalLength,dataX,dataY,positions);

        return ret;
    }

}


