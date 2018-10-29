package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
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

/**
 * Created by wujy on 18-4-20.
 */
public class RailwaySimilaritySearch {

    private static Logger logger = Logger.getLogger(RailwaySimilaritySearch.class);

    private static final String[] STATION_NAMES = {"A积水潭-鼓楼大街1加速", "A积水潭-鼓楼大街2匀速", "A积水潭-鼓楼大街3减速",
            "B鼓楼大街-安定门1加速", "B鼓楼大街-安定门2匀速", "B鼓楼大街-安定门3减速",
            "C安定门-雍和宫1加速", "C安定门-雍和宫2减速",
            "D雍和宫-东直门1加速", "D雍和宫-东直门2转弯 R=300", "D雍和宫-东直门2转弯 R=300_out", "D雍和宫-东直门3减速"};

    private static final String[] SENSOR_NAMES = {"cegun", "chilunxiang", "dianjichui", "dianjiheng", "fuchen",
            "hengjian", "hengxiang ", "lingxing", "niuzhuan", "qianyin", "zhidong", "速度", "角速度"};

    private static void mergeData() throws IOException {
        for (String sensor : SENSOR_NAMES) {
            PrintWriter print = new PrintWriter(new File("data/railway/all/all_" + sensor + ".csv"));

            int cnt = 0;
            String lastTime = "";
            for (int j = 0; j < STATION_NAMES.length; j++) {
                String station = STATION_NAMES[j];
                String filename = "data/railway/" + station + "/" + station + "_" + sensor + ".csv";
                logger.debug("name = " + filename);
                List<String> list = FileUtils.readLines(new File(filename));

                if (j == 0) {
                    for (int k = 0; k < 8; k++) {
                        print.println(list.get(k));
                    }
                }

                for (int k = 8; k < list.size(); k++) {
                    String[] s = list.get(k).split(",");
                    if (s[1].equals(lastTime)) continue;
                    print.println(++cnt + "," + s[1] + "," + s[2]);
                    lastTime = s[1];
                }
                print.flush();
            }
            print.close();
        }
    }

    public static void main(String args[]) throws IOException {
//        mergeData();
//        System.exit(0);

        RailwaySimilaritySearch c2ss = new RailwaySimilaritySearch();
        double errorBound = 0.4;
        double error = 0.05;

//        for (String station : STATION_NAMES) {
            String station = "all";
            PrintWriter print = new PrintWriter(new File("data/railway/" + station + "/" + station + "_results_hengxiang_lingxing_" + errorBound + ".csv"));
            print.println("Base,Target,Pearson,k,b,Length,Pearson(sub)");

            for (String sensor1 : SENSOR_NAMES) {
                for (String sensor2 : SENSOR_NAMES) {
                    if (sensor1.equals(sensor2) || sensor1.endsWith("速度") || sensor2.endsWith("速度")) continue;
                    if (sensor1.equals("hengxiang ") && sensor2.equals("lingxing")) {
                        c2ss.searchAllWithPointBasedOpti(print, station, sensor1, sensor2, errorBound, error, true);
                        print.flush();
                    }
                }
            }
//            c2ss.searchAllWithPointBasedOpti(print, station, "角速度", "速度", errorBound, error, true);
//            c2ss.searchAllWithPointBasedOpti(print, station, "速度", "角速度", errorBound, error, true);

            print.close();
        }
//    }

//    private void calcSubstringPearsonCorrelation(String base, String target, int top) throws IOException {
//        double[] baseData = readFromFileSelectResult(base, base, target);
//        double[] targetData = readFromFileSelectResult(target, base, target);
//        double pearsonCorrelation = new PearsonsCorrelation().correlation(baseData, targetData);
//        System.out.println("Base,Target,Top,Length,Pearson");
//        System.out.println(base + "," + target + "," + top + "," + baseData.length + "," + pearsonCorrelation);
//    }

    private void searchAllWithPointBasedOpti(PrintWriter print, String station, String base, String target, double errorBound, double error, boolean z_normalization) throws IOException {
        double[] baseData = readFromFile(station, base);
        double[] targetData = readFromFile(station, target);

        logger.info(base + "-" + target);

        Point2D[] point2Ds = Point2DUtils.genFromXY(baseData, targetData);
        if (z_normalization) {
            point2Ds = Point2DUtils.normalize(point2Ds);
        }

        // Pearson correlation: use Matlab instead
        double pearsonCorrelation = new PearsonsCorrelation().correlation(baseData, targetData);

        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
        miner.process();
        List<PLASegment> segs = miner.buildSpecificSegments(3);

        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
        plaRegionSearch.errorBound = errorBound;
        for (int j = segs.size() - 1; j >= 0; j--) {
            if (segs.get(j).getPolygonKB().getRings().size() > 1) {
                segs.remove(j);
                System.out.println("Remove at " + j);
            }
        }

        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
        if (point2Ds1 == null) return;
        double k = point2Ds1.getX();
        double b = point2Ds1.getY();

        //print 0-1 file
        printResultFile("data/railway/" + station + "/" + station + "_" + base + "-" + target + "_segment_" + errorBound + ".csv", point2Ds, k, b, errorBound);

        // calculate Top 1 statistics
        double[] top1BaseData = readFromFileSelectResult(station, base, base, target);
        double[] top1TargetData = readFromFileSelectResult(station, target, base, target);
        double top1PearsonCorrelation = new PearsonsCorrelation().correlation(top1BaseData, top1TargetData);

        print.println(base + "," + target + "," + pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength + "," + top1PearsonCorrelation);

//        PrintWriter print = new PrintWriter(new File("data/currency2/data.csv"));
//        for (int i = 0; i < cnt; i++) {
//            print.print(label[i] + ",");
//        }
//        print.println();
//        for (int i = 0; i < data[0].length; i++) {
//            for (int j = 0; j < cnt; j++) {
//                print.print(data[j][i] + ",");
//            }
//            print.println();
//        }
//        print.close();
    }

    private double[] readFromFileSelectResult(String station, String aim, String base, String target) throws IOException {
        double[] original = readFromFile(station, aim);
        List list = FileUtils.readLines(new File("data/railway/" + station + "/" + station + "_" + base + "-" + target + "_segment.csv"));
        int cnt = 0;
        for (Object line : list) {
            String s = (String) line;
            if (s.startsWith("1")) {
                cnt++;
            }
        }
        double[] result = new double[cnt];
        cnt = 0;
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            if (s.startsWith("1")) {
                result[cnt] = original[i];
                cnt++;
            }
        }
        return result;
    }

    private double[] readFromFile(String station, String aim) throws IOException {
        String filename = "data/railway/" + station + "/" + station + "_" + aim + ".csv";
        logger.debug("name = " + filename);
        List list = FileUtils.readLines(new File(filename));

//        List list2 = FileUtils.readLines(new File("data/currency2/EUR_CHF_segment_0.1_T1.csv"));
//        int cnt = 0;
//        for (int i = 0; i < list2.size(); i++) {
//            String s = (String) list2.get(i);
//            if (s.startsWith("1")) {
//                cnt++;
//            }
//        }
//        List list3 = FileUtils.readLines(new File("data/currency2/EUR_CHF_segment_0.1_T2.csv"));
//        for (int i = 0; i < list3.size(); i++) {
//            String s = (String) list3.get(i);
//            if (s.startsWith("1")) {
//                cnt++;
//            }
//        }
//        double[] data = new double[list.size() - 1 - cnt];
        double[] data = new double[list.size()-8];
        int cnt2 = 0;
        for (int i = 8; i < list.size(); i++) {
            String s = (String) list.get(i);
            String[] values = s.split(",");
            data[cnt2] = Double.parseDouble(values[2]);
            cnt2++;
        }
        logger.debug("length = " + cnt2);
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
