package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by wujy on 17-2-11.
 */
public class SatelliteTimeVsLengthJocor {

    private static Logger logger = Logger.getLogger(SatelliteTimeVsLengthJocor.class);

    public static void main(String args[]) throws IOException {
        SatelliteTimeVsLengthJocor sss = new SatelliteTimeVsLengthJocor();
        double errorBound = 0.1;
        double error = 0.05;

//        sss.searchAllWithPointBasedOpti(4, 5, 2000, errorBound, error, true);
        //sss.searchAllWithPointBasedOpti(4, 5, 4000, errorBound, error, true);
        sss.searchAllWithPointBasedOpti(4, 5, 5000, errorBound, error, true);
//        sss.searchAllWithPointBasedOpti(4, 5, 6000, errorBound, error, true);
//        sss.searchAllWithPointBasedOpti(4, 5, 8000, errorBound, error, true);
//        sss.searchAllWithPointBasedOpti(4, 5, 10000, errorBound, error, true);
        sss.searchAllWithPointBasedOpti(4, 5, 20000, errorBound, error, true);
        sss.searchAllWithPointBasedOpti(4, 5, 30000, errorBound, error, true);
    }

    private void searchAllWithPointBasedOpti(int columnA, int columnB, int length, double errorBound, double error, boolean z_normalization) throws IOException {
        String filename = "data/satellite/data.txt";
        Point2D[] data = readFromFile(filename, columnA, columnB, length);

        if (z_normalization) {
            data = Point2DUtils.normalize(data);
        }

        double averageTime = 0;
        for (int i = 1; i <= 10; i++) {
            long startTime = System.currentTimeMillis();

            TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(data, errorBound);
            miner.process();
            List<PLASegment> segs = miner.buildSpecificSegments(3);

            PLARegionSearch plaRegionSearch = new PLARegionSearch(data);
            plaRegionSearch.errorBound = errorBound;
            for (int j = segs.size() - 1; j >= 0; j--) {
                if (segs.get(j).getPolygonKB().boundary().size() > 1) {
                    segs.remove(j);
                    System.out.println("Remove at " + j);
                }
            }

            Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
            //double k = point2Ds1.getX();
            //double b = point2Ds1.getY();

            long endTime = System.currentTimeMillis();
            averageTime = averageTime + (endTime - startTime - averageTime) / i;
        }

        System.out.println(length + "," + averageTime);
    }

    private Point2D[] readFromFile(String filename, int columnA, int columnB, int length) throws IOException {
        logger.debug("name = " + filename);
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        Point2D[] data;
        if (length == -1) {
            data = new Point2D[list.size() - 1];
        } else {
            data = new Point2D[length];
        }
        int cnt2 = 0;
        for (int i = 1; i < (length == -1 ? list.size() : length + 1); i++) {
            String s = (String) list.get(10000 + i);
            String[] strings = s.split(",");
            data[cnt2] = new Point2D(Double.parseDouble(strings[columnA]), Double.parseDouble(strings[columnB]));
            cnt2++;
        }
        logger.debug("length = " + data.length);
        return data;
    }
}
