package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.correlation.PearsonsCorrelationTest;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jiaye Wu on 17-2-20.
 */
public class SatelliteBruteForce {

    private static Logger logger = Logger.getLogger(SatelliteBruteForce.class);

    public static void main(String args[]) throws IOException {
        SatelliteBruteForce sbf = new SatelliteBruteForce();
        double errorBound = 0.1;
        double error = 0.05;

        sbf.bruteForce(4, 5, 2000, 1000, 0.8, errorBound, error, true);
        sbf.bruteForce(4, 5, 5000, 1000, 0.8, errorBound, error, true);
        sbf.bruteForce(4, 5, 10000, 1000, 0.8, errorBound, error, true);
        sbf.bruteForce(4, 5, 20000, 1000, 0.8, errorBound, error, true);
        sbf.bruteForce(4, 5, 30000, 1000, 0.8, errorBound, error, true);
    }

    private void bruteForce(int columnA, int columnB, int length, int minLength, double minCorrelation, double errorBound, double error, boolean z_normalization) throws IOException {
        String filename = "data/satellite/data.txt";
        Point2D[] data = readFromFile(filename, columnA, columnB, length);
        if (z_normalization) {
            data = Point2DUtils.normalize(data);
        }

        double globalCorrelation = PearsonsCorrelationTest.correlation(data);

        long startTime = System.currentTimeMillis();

        int maxI = -1, maxLen = -1;
        double maxCorrelation = 0;
        for (int i = 0; i < data.length; i++) {
            for (int len = minLength; i + len - 1 < data.length; len++) {
                Point2D[] currentData = Arrays.copyOfRange(data, i, i + len);
                double currentCorrelation = PearsonsCorrelationTest.correlation(currentData);
                if (currentCorrelation > minCorrelation) {
                    //System.out.println(i + "," + len + ": max=" + currentCorrelation);
                    if (len > maxLen) {
                        maxLen = len;
                        maxI = i;
                        maxCorrelation = currentCorrelation;
                    } else if (len == maxLen && currentCorrelation > maxCorrelation) {
                        maxCorrelation = currentCorrelation;
                        maxI = i;
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println(length + "," + minLength + "," + minCorrelation + "," + (endTime - startTime));
        System.out.println("global correlation = " + globalCorrelation);
        System.out.println("max pearson correlation = " + maxCorrelation);
        System.out.println("max i = " + maxI);
        System.out.println("max len = " + maxLen);
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
