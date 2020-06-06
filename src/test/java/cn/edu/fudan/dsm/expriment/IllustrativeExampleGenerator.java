package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Jiaye Wu on 17-2-20.
 */
public class IllustrativeExampleGenerator {

    public static void main(String args[]) throws IOException {
        int length = 1000;
        double epsilon = 0.1;

        IllustrativeExampleGenerator ieg = new IllustrativeExampleGenerator();

        // Step 1: generate series 1 with random walk data
        double[] series1 = ieg.randomWalk(length);

        // Step 2: examine whether series 1 meet slope criterion
        while (!examineSlopeCriterion(series1)) {
            series1 = ieg.randomWalk(length);
        }

        // Step 3: determine (k, b, position, length) (two triples)
        double k1 = ThreadLocalRandom.current().nextDouble(1, 2);
        double b1 = ThreadLocalRandom.current().nextDouble(20, 30);
        int length1 = ThreadLocalRandom.current().nextInt((int)(length * 0.6 - 5), (int)(length * 0.6 + 5));
        int position1 = ThreadLocalRandom.current().nextInt((int)(length * 0.65 - length1 - 25), (int)(length * 0.65 - length1));
        double k2 = ThreadLocalRandom.current().nextDouble(1, 2);
        double b2 = ThreadLocalRandom.current().nextDouble(-60, -50);
        int length2 = ThreadLocalRandom.current().nextInt((int)(length * 0.3 - 5), (int)(length * 0.3 + 5));
        int position2 = ThreadLocalRandom.current().nextInt((int)(length * 0.65), (int)(length * 0.7 - 25));

        // Step 4: generate linear represented parts of series 2 by (k, b, position, length, epsilon)
        double[] series2 = new double[length];
        for (int i = position1; i < position1 + length1; i++) {
            double y = k1 * series1[i] + b1;
            series2[i] = ThreadLocalRandom.current().nextDouble(y - epsilon, y + epsilon);
        }
        for (int i = position2; i < position2 + length2; i++) {
            double y = k2 * series1[i] + b2;
            series2[i] = ThreadLocalRandom.current().nextDouble(y - epsilon, y + epsilon);
        }

        // Step 5: generate other blank parts of series 2
        series2 = ieg.fillBlank(series2, 0, position1, -1);  // [0, position1)
        series2 = ieg.fillBlank(series2, position1+length1, position2, 1);  // [position1+length1, position2)
        series2 = ieg.fillBlank(series2, position2+length2, length, 1);  // [position2+length2, length)

        // Step 6: calculate the global Pearson coefficient
        double pearsonCorrelation = new PearsonsCorrelation().correlation(series1, series2);

        // Step 7: calculate maximal significant linear representation
        Point2D[] point2Ds = Point2DUtils.genFromXY(series1, series2);
        point2Ds = Point2DUtils.normalize(point2Ds);
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, epsilon);
        miner.process();
        List<PLASegment> segs = miner.buildSpecificSegments(3);
        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = epsilon;
        for (int j = segs.size() - 1; j >= 0; j--) {
            if (segs.get(j).getPolygonKB().boundary().size() > 1) {
                segs.remove(j);
                System.out.println("Remove at " + j);
            }
        }
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, epsilon / 2.0);
        double k = point2Ds1.x();
        double b = point2Ds1.y();

        // Step 8: output the series and results
        long time = System.currentTimeMillis();
        PrintWriter print = new PrintWriter(new File(time + "_illustrative_example_data.csv"));
        for (int i = 0; i < length; i++) {
            print.println(series1[i] + "," + series2[i]);
        }
        print.close();
        print = new PrintWriter(new File(time + "_illustrative_example_result.csv"));
        print.println(k1 + "," + b1 + "," + position1 + "," + length1);
        print.println(k2 + "," + b2 + "," + position2 + "," + length2);
        print.println(pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength);
        print.close();
        ieg.printResultFile(time + "_illustrative_example_segment.csv", point2Ds, k, b, epsilon);
    }

    private void printResultFile(String outFile, Point2D[] points, double k, double b, double errorBound) throws IOException {
        int consecutiveNum = 0;
        PrintWriter pw = new PrintWriter(new FileWriter(outFile));
        for (Point2D point : points) {
            double x = point.x();
            double y = point.y();
            double estimateY = k * x + b;
            if (Math.abs(estimateY - y) < errorBound) {
                consecutiveNum++;
            } else {
                pw.println("0");
                if (consecutiveNum >= 3) {
                    while (consecutiveNum > 0) {
                        pw.println("1");
                        consecutiveNum--;
                    }
                } else {
                    while (consecutiveNum > 0) {
                        pw.println("0");
                        consecutiveNum--;
                    }
                }
            }
        }
        if (consecutiveNum >= 3) {
            while (consecutiveNum > 0) {
                pw.println("1");
                consecutiveNum--;
            }
        } else {
            while (consecutiveNum > 0) {
                pw.println("0");
                consecutiveNum--;
            }
        }
        pw.close();
    }

    private double[] fillBlank(double[] data, int begin, int end, int direction) {
        if (direction == 1) {  // left to right
            System.arraycopy(randomWalk(end-begin, data[begin-1], -1, direction), 0, data, begin, end-begin);
        } else if (direction == 0) {  // middle
            System.arraycopy(randomWalk(end-begin, data[begin-1], data[end], direction), 0, data, begin, end-begin);
        } else {  // right to left
            System.arraycopy(randomWalk(end-begin, data[end], -1, -1), 0, data, begin, end-begin);
        }
        return data;
    }

    private static boolean examineSlopeCriterion(double[] data) {
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < data.length; i++) {
            regression.addData(i, data[i]);
        }
        return regression.getSlope() > 0.015;
    }

    private double[] randomWalk(int length) {
        return randomWalk(length, -1, -1, 1);
    }

    private double[] randomWalk(int length, double initialValue, double finalValue, int direction) {
        double[] data = new double[length];
        double x;
        if (initialValue == -1) {
            x = ThreadLocalRandom.current().nextDouble(0, 5);
        } else {
            x = initialValue;
        }
        if (direction == 1) {
            for (int i = 0; i < length; i++) {
                data[i] = x;
                x += ThreadLocalRandom.current().nextDouble(-5, 5);
                while (x < 0 || x > 100) {
                    if (x < 0) {
                        x += ThreadLocalRandom.current().nextDouble(0, 20);
                    } else if (x > 100) {
                        x -= ThreadLocalRandom.current().nextDouble(0, 20);
                    }
                }
            }
        } else if (direction == 0) {  // for [position1 + length1, position2) only
            for (int i = 0; i < length; i++) {
                data[i] = x;
                x = ThreadLocalRandom.current().nextDouble(initialValue, finalValue);
            }
        } else {  // for [0, position1) only
            for (int i = length-1; i >= 0; i--) {
                data[i] = x;
                x += ThreadLocalRandom.current().nextDouble(-1, 5);
            }
        }
        return data;
    }

}
