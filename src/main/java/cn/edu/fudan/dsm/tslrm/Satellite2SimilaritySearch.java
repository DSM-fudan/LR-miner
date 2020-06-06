package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * Created by wujy on 17-2-11.
 */
public class Satellite2SimilaritySearch {

    private static Logger logger = Logger.getLogger(Satellite2SimilaritySearch.class);
    private int offset;

    public static void main(String args[]) throws IOException {
        Satellite2SimilaritySearch sss = new Satellite2SimilaritySearch();
        double errorBound = 0.1;
        double error = 0.05;

        Scanner scanner = new Scanner(System.in);
        System.out.println("Subsystem=");
        int subSystem = scanner.nextInt();
        logger.info(subSystem);
        System.out.println("Start part=");
        int startPart = scanner.nextInt();
        System.out.println("End part=");
        int endPart = scanner.nextInt();
        logger.info("subsystem=" + subSystem + "; start part=" + startPart + "; end part=" + endPart);

        if (subSystem == 1) {  // GPS
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 8, 50000, errorBound, error, true);
        } else if (subSystem == 2) {  // 测控
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 40, 50000, errorBound, error, true);
        } else if (subSystem == 3) {  // 搭载
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 9, 50000, errorBound, error, true);
        } else if (subSystem == 4) {  // 热控
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 22, 50000, errorBound, error, true);
        } else if (subSystem == 5) {  // 通信
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 46, 50000, errorBound, error, true);
        } else if (subSystem == 6) {  // 星务
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 80, 50000, errorBound, error, true);
        } else if (subSystem == 7) {  // 姿控
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 54, 50000, errorBound, error, true);
        } else if (subSystem == 8) {  // 能源
            sss.searchAllWithPointBasedOpti(subSystem, startPart, endPart, 25, 50000, errorBound, error, true);
        }

        scanner.close();
    }

    private void searchAllWithPointBasedOpti(int subSystem, int startPart, int endPart, int numColumns, int length, double errorBound, double error, boolean z_normalization) throws IOException {
        String filename = "data/satellite2/" + subSystem + "_data.txt";
        logger.info("filename = " + filename);

        PrintWriter print = new PrintWriter(new File("data/satellite2/subsystem" + subSystem + "_part" + startPart + "-" + endPart + "_result.csv"));
        print.println("Base,Target,Pearson,k,b,Final length,Max up bound");

        int start = (startPart-1) * ((numColumns+7) / 8);
        int end = endPart * ((numColumns+7) / 8);
        for (int columnA = start; columnA < Math.min(numColumns, end); columnA++) {
            for (int columnB = 0; columnB < numColumns; columnB++) {
                if (columnB == columnA) continue;
                logger.info("columns = " + columnA + "-" + columnB);

                double pearsonCorrelation = 0;
                try {
                    Point2D[] data = readFromFile(filename, columnA, columnB, length);
                    if (z_normalization) {
                        data = Point2DUtils.normalize(data);
                    }

                    PrintWriter print2 = new PrintWriter(new File("data/satellite2/" + subSystem + "_" + columnA + "_" + columnB + "_" + offset + "_" + data.length +"_data.csv"));
                    for (Point2D aData : data) {
                        print2.println(aData.x() + "," + aData.y());
                    }
                    print2.close();

                    pearsonCorrelation = correlation(data);

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
                    printResultFile("data/satellite2/" + subSystem + "_" + columnA + "_" + columnB + "_" + data.length + "_segment.csv", data, k, b, errorBound);

                    logger.info("result = " + columnA + "," + columnB + "," + pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
                    print.println(columnA + "," + columnB + "," + pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
                } catch (Exception e) {
                    logger.error("error = " + columnA + "-" + columnB);
                    print.println(columnA + "," + columnB + "," + pearsonCorrelation + "," + "error!" + ",,,");
                } catch (StackOverflowError e) {
                    logger.error("error = " + columnA + "-" + columnB);
                    print.println(columnA + "," + columnB + "," + pearsonCorrelation + "," + "stack overflow!" + ",,,");
                } finally {
                    print.flush();
                }
            }
        }
        print.close();
    }

    private Point2D[] readFromFile(String filename, int columnA, int columnB, int length) throws IOException {
        logger.debug("name = " + filename);
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        Point2D[] data = new Point2D[Math.min(list.size(), length)];
        offset = (int) (((double)list.size() / (double)length / 8.0) * list.size());
        logger.info("length = " + list.size() + "; offset = " + offset);
        for (int i = offset; i < Math.min(list.size(), offset + length); i++) {
            String s = (String) list.get(i);
            String[] strings = s.split(",");
            data[i - offset] = new Point2D(Double.parseDouble(strings[columnA]), Double.parseDouble(strings[columnB]));
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

    private double correlation(Point2D[] data) {
        double[] x = new double[data.length];
        double[] y = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            x[i] = data[i].x();
            y[i] = data[i].y();
        }
        return new PearsonsCorrelation().correlation(x, y);
    }

}
