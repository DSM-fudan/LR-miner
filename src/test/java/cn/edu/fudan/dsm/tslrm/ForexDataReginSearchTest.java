package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.ForexData;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import cn.edu.fudan.dsm.tslrm.log.DefaultFileAppender;
import math.geom2d.Point2D;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-14
 * Time: 上午9:50
 * To change this template use File | Settings | File Templates.
 */
public class ForexDataReginSearchTest {
    static Logger logger = Logger.getLogger(ForexDataReginSearchTest.class);

    @Test
    public void testUsdHKD2JPY() throws IOException {
        //test USD ---> other currency
        //for 10 minute
        String fileName1 = "../data/USD_HKD.csv";
        ForexData forexData1 = ForexData.readFromFile(fileName1);

        String fileName2 = "../data/USD_JPY.csv";
        ForexData forexData2 = ForexData.readFromFile(fileName2);

        String start = "2012-01-04 02:00:00";
        String end = "2012-12-30 17:00:00";

        long startTime = Timestamp.valueOf(start).getTime();
        long endTime = Timestamp.valueOf(end).getTime();

        ForexData hkdData = forexData1.fillMinuteData(startTime, endTime).reduceByMinutes(60).z_normalize();
        ForexData jpyData = forexData2.fillMinuteData(startTime, endTime).reduceByMinutes(60).z_normalize();

        logger.debug("hkdData = " + hkdData.times.length);
        logger.debug("jpyData = " + jpyData.times.length);

        Point2D[] point2Ds = Point2DUtils.genFromForexData(hkdData, jpyData, 0);

        logger.debug(points2string(point2Ds));

        regionSearch(point2Ds, 0.01, 0.05);
    }

    @Test
    public void testUsdBased() throws IOException {
        Logger.getRootLogger().addAppender(new DefaultFileAppender(this.getClass()));
        double errorBound = 0.5;
        double accuracy = 0.05;
        int consecutive = 3;
        logger.debug("errorBound = " + errorBound);
        logger.debug("accuracy = " + accuracy);
        logger.debug("consecutive = " + consecutive);

        File dir = new File("../data");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("USD");
            }
        });

        System.out.println("files.length = " + files.length);

        //test USD ---> other currency
        //for 10 minute
        String fileName1 = "../data/USD_HUF.csv";
        String fileName2 = "../data/USD_JPY.csv";
        for (int i = 0; i < files.length - 1; i++) {
            File file1 = files[i];
            fileName1 = file1.getAbsolutePath();
            for (int j = i + 1; j < files.length; j ++ )
            {
                fileName2 = files[j].getAbsolutePath();
                logger.debug("fileName1 = " + fileName1);
                logger.debug("fileName2 = " + fileName2);
                doCurrencySearch(errorBound, accuracy, consecutive, fileName1, fileName2);
            }
        }
    }

    private void doCurrencySearch(double errorBound, double accuracy, int consecutive, String fileName1, String fileName2) throws IOException {
        ForexData forexData1 = ForexData.readFromFile(fileName1);
        logger.debug("forexData1.secondCurrency = " + forexData1.secondCurrency);
        ForexData forexData2 = ForexData.readFromFile(fileName2);
        logger.debug("forexData2.secondCurrency = " + forexData2.secondCurrency);

        String start = "2012-01-04 02:00:00";
        String end = "2012-12-30 17:00:00";

        long startTime = Timestamp.valueOf(start).getTime();
        long endTime = Timestamp.valueOf(end).getTime();

        ForexData data1 = forexData1.fillMinuteData(startTime, endTime).reduceByMinutes(60).z_normalize();
        ForexData data2 = forexData2.fillMinuteData(startTime, endTime).reduceByMinutes(60).z_normalize();

        logger.debug("data1 = " + data1.times.length);
        logger.debug("data2 = " + data2.times.length);

        Point2D[] point2Ds = Point2DUtils.genFromForexData(data1, data2, 0);

//        logger.debug(points2string(point2Ds));

        regionSearch(point2Ds, errorBound, accuracy, consecutive);
    }

    @Test
    public void testUsdHUF2JPY() throws IOException {
        //test USD ---> other currency
        //for 10 minute
        String fileName1 = "../data/USD_HUF.csv";
        ForexData forexData1 = ForexData.readFromFile(fileName1);

        String fileName2 = "../data/USD_JPY.csv";
        ForexData forexData2 = ForexData.readFromFile(fileName2);

        String start = "2012-01-04 02:00:00";
        String end = "2012-12-30 17:00:00";

        long startTime = Timestamp.valueOf(start).getTime();
        long endTime = Timestamp.valueOf(end).getTime();

        ForexData data1 = forexData1.fillMinuteData(startTime, endTime).reduceByMinutes(60).z_normalize();
        ForexData data2 = forexData2.fillMinuteData(startTime, endTime).reduceByMinutes(60).z_normalize();

        logger.debug("data1 = " + data1.times.length);
        logger.debug("data2 = " + data2.times.length);

        Point2D[] point2Ds = Point2DUtils.genFromForexData(data1, data2, 0);

        //do z_norm
        logger.debug(points2string(point2Ds));

        regionSearch(point2Ds, 0.01, 0.005, 3);
    }


    public String points2string(Point2D[] point2Ds)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < point2Ds.length; i++) {
            Point2D point2D = point2Ds[i];
            sb.append(point2D.getX()).append(" ").append(point2D.getY()).append("\n");
        }

        return sb.toString();
    }

    public void regionSearch(Point2D[] point2Ds, double errorBound, double accuracy, int consecutive)
    {
        logger.debug("begin ...");
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        //miner.process();
        //logger.debug("miner.plaSegmentList.size() = " + miner.plaSegmentList.size());

        List<PLASegment> segs = miner.buildSpecificSegments(consecutive);
        logger.debug("all segmentList.size() = " + segs.size());

//        int[] values = new int[30];
//        for (int i = 0; i < segs.size(); i++) {
//            PLASegment plaSegment = segs.get(i);
//            values[plaSegment.getPolygonKB().getVertexNumber()]++;
//        }

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
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, accuracy);
        stopWatch2.stop();

        logger.debug("point2Ds1.getX() = " + point2Ds1.getX());
        logger.debug("point2Ds1.getY() = " + point2Ds1.getY());
        logger.debug("PartitionNum = " + plaRegionSearch.partitionNum);
        logger.debug("RealLength = " + plaRegionSearch.finalLength);
        logger.debug("CountInsides = " + plaRegionSearch.countInsides);
        logger.debug("stopWatchInsides = " + plaRegionSearch.stopWatchInsides.getTime());

    }

    public void regionSearch(Point2D[] point2Ds, double errorBound, double accuracy)
    {
        regionSearch(point2Ds,errorBound,accuracy,3);
    }
}
