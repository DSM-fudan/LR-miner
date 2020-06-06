package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.correlation.PearsonsCorrelationTest;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by wujy on 17-2-11.
 */
public class SatelliteSimilaritySearch {

    private static Logger logger = Logger.getLogger(SatelliteSimilaritySearch.class);

    public static void main(String args[]) throws IOException {
        SatelliteSimilaritySearch sss = new SatelliteSimilaritySearch();
        double errorBound = 0.1;
        double error = 0.05;

//        sss.searchAllWithPointBasedOpti(4, 5, 60000, errorBound, error, true);
        //sss.searchAllWithPointBasedOpti(10, 11, 100000, errorBound, error, true);

        sss.calcSubstringPearsonCollrelation(4, 5, 60000, 1);
        sss.calcSubstringPearsonCollrelation(4, 5, 60000, 2);
    }

    private void calcSubstringPearsonCollrelation(int columnA, int columnB, int length, int top) throws IOException {
        String filename = "data/satellite/data.txt";
        Point2D[] data = readFromFileSelectResult(filename, columnA, columnB, length, top);
        double pearsonCorrelation = PearsonsCorrelationTest.correlation(data);
        System.out.println("Base,Target,Top,Length,Pearson");
        System.out.println(columnA + "," + columnB + "," + top + "," + data.length + "," + pearsonCorrelation);
    }

    private void searchAllWithPointBasedOpti(int columnA, int columnB, int length, double errorBound, double error, boolean z_normalization) throws IOException {
        String filename = "data/satellite/data.txt";
        Point2D[] data = readFromFile(filename, columnA, columnB, length);

        PrintWriter print = new PrintWriter(new File("data/satellite/" + columnA + "_" + columnB + "_" + data.length + "_result.csv"));
        print.println("Base,Target,Pearson,k,b,Final length,Max up bound");

        if (z_normalization) {
            data = Point2DUtils.normalize(data);
        }

        // Pearson correlation: use Matlab instead
        double pearsonCorrelation = PearsonsCorrelationTest.correlation(data);

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
        double k = point2Ds1.x();
        double b = point2Ds1.y();

        //print 0-1 file
        printResultFile("data/satellite/" + columnA + "_" + columnB + "_" + data.length + "_segment.csv", data, k, b, errorBound);

        print.println("电压" + "," + "充电电流" + "," + pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
        print.close();

        // output csv containing all data to calculate Pearson correlation in Matlab
        print = new PrintWriter(new File("data/satellite/" + columnA + "_" + columnB + "_" + data.length + "_data.csv"));
        for (Point2D aData : data) {
            print.println(aData.x() + "," + aData.y());
        }
        print.close();
    }

    private Point2D[] readFromFileSelectResult(String filename, int columnA, int columnB, int length, int top) throws IOException {
        Point2D[] original = readFromFile(filename, columnA, columnB, length);
        List list = FileUtils.readLines(new File("data/satellite/4_5_60000_segment_T" + top + ".csv"));
        int cnt = 0;
        for (Object line : list) {
            String s = (String) line;
            if (s.startsWith("1")) {
                cnt++;
            }
        }
        Point2D[] result = new Point2D[cnt];
        cnt = 0;
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            if (s.startsWith("1")) {
                result[cnt] = new Point2D(original[i]);
                cnt++;
            }
        }
        return result;
    }

    private Point2D[] readFromFile(String filename, int columnA, int columnB, int length) throws IOException {
        logger.debug("name = " + filename);
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        Point2D[] data;
//        List list2 = FileUtils.readLines(new File("data/satellite/4_5_60000_segment_T1.csv"));
//        int cnt = 0;
//        for (int i = 0; i < list2.size(); i++) {
//            String s = (String) list2.get(i);
//            if (s.startsWith("1")) {
//                cnt++;
//            }
//        }
        if (length == -1) {
//            data = new Point2D[list.size() - 1 - cnt];
            data = new Point2D[list.size() - 1];
        } else {
            data = new Point2D[length];
//            data = new Point2D[length - cnt];
        }
        int cnt2 = 0;
        for (int i = 1; i < (length == -1 ? list.size() : length + 1); i++) {
//            if (((String)list2.get(i-1)).startsWith("1")) continue;
            String s = (String) list.get(i);
            String[] strings = s.split(",");
            data[cnt2] = new Point2D(Double.parseDouble(strings[columnA]), Double.parseDouble(strings[columnB]));
            cnt2++;
        }
        logger.debug("length = " + data.length);
        return data;
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

}
