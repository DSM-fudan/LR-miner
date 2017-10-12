package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.correlation.PearsonsCorrelationTest;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by Jiaye Wu on 17-2-20.
 */
public class IllustrativeExampleSimilaritySearch {

    public static void main(String args[]) throws IOException {
        double epsilon = 0.1;

        IllustrativeExampleSimilaritySearch iess = new IllustrativeExampleSimilaritySearch();

        // Step 1: read data
        Point2D[] data = iess.readFromFile("illustrative_example_data_4.csv", 0, 1);

        List list = FileUtils.readLines(new File("illustrative_example_segment_4T1.csv"));
        int cnt = 0;
        for (Object aList : list) {
            String s = (String) aList;
            if (s.startsWith("1")) {
                cnt++;
            }
        }
        Point2D[] data2 = new Point2D[cnt];
        cnt = 0;
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            if (s.startsWith("1")) {
                data2[cnt] = data[i];
                cnt++;
            }
        }
        double pearsonCorrelation = PearsonsCorrelationTest.correlation(data2);
        System.out.println(pearsonCorrelation);
/*
        // Step 2: calculate the global Pearson coefficient
        double pearsonCorrelation = PearsonsCorrelationTest.correlation(data);
        System.out.println(pearsonCorrelation);

        // Step 3: calculate maximal significant linear representation
        data = Point2DUtils.normalize(data);
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(data, epsilon);
        miner.process();
        List<PLASegment> segs = miner.buildSpecificSegments(3);
        PLARegionSearch plaRegionSearch = new PLARegionSearch(data);
        plaRegionSearch.errorBound = epsilon;
        for (int j = segs.size() - 1; j >= 0; j--) {
            if (segs.get(j).getPolygonKB().getRings().size() > 1) {
                segs.remove(j);
                System.out.println("Remove at " + j);
            }
        }
        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, epsilon / 2.0);
        double k = point2Ds1.getX();
        double b = point2Ds1.getY();

        // Step 4: print results to files
        PrintWriter print = new PrintWriter(new File("illustrative_example_result_4T1.csv"));
        print.println(pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength);
        print.close();
        iess.printResultFile("illustrative_example_segment_4T1.csv", data, k, b, epsilon);
*/
    }

    private Point2D[] readFromFile(String filename, int columnA, int columnB) throws IOException {
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        Point2D[] data;
//        List list2 = FileUtils.readLines(new File("illustrative_example_segment_3T1.csv"));
//        int cnt = 0;
//        for (int i = 0; i < list2.size(); i++) {
//            String s = (String) list2.get(i);
//            if (s.startsWith("1")) {
//                cnt++;
//            }
//        }
//        data = new Point2D[list.size() - cnt];
        data = new Point2D[list.size()];
        int cnt2 = 0;
        for (int i = 0; i < list.size(); i++) {
//            if (((String)list2.get(i)).startsWith("1")) continue;
            String s = (String) list.get(i);
            String[] strings = s.split(",");
            data[cnt2] = new Point2D(Double.parseDouble(strings[columnA]), Double.parseDouble(strings[columnB]));
            cnt2++;
        }
        return data;
    }

    private void printResultFile(String outFile, Point2D[] points, double k, double b, double errorBound) throws IOException {
        int consecutiveNum = 0;
        PrintWriter pw = new PrintWriter(new FileWriter(outFile));
        for (Point2D point : points) {
            double x = point.getX();
            double y = point.getY();
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
