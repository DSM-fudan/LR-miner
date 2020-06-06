package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * Created by wujy on 17-2-11.
 */
public class Currency2SimilaritySearch {

    private static Logger logger = Logger.getLogger(Currency2SimilaritySearch.class);

    private static final String[] currencies = {"BRL", "CAD", "CHF", "CNY", "DKK", "HKD", "INR",
            "JPY", "KRW", "MXN", "MYR", "NOK", "SEK", "SGD", "THB", "TWD", "AUD", "EUR", "GBP", "NZD", "ZAR"};

    public static void main(String args[]) throws IOException {
        Currency2SimilaritySearch c2ss = new Currency2SimilaritySearch();
        double errorBound = 0.1;
        double error = 0.05;

//        c2ss.searchAllWithPointBasedOpti(errorBound, error, true);
        c2ss.calcSubstringPearsonCollrelation(1);
        c2ss.calcSubstringPearsonCollrelation(2);
        c2ss.calcSubstringPearsonCollrelation(3);
    }

    private void calcSubstringPearsonCollrelation(int top) throws IOException {
        double[] baseCurrencyData = readFromFileSelectResult("EUR", "USD", top);
        double[] targetCurrencyData = readFromFileSelectResult("CHF", "USD", top);
        double pearsonCorrelation = new PearsonsCorrelation().correlation(baseCurrencyData, targetCurrencyData);
        System.out.println("Base,Target,Top,Length,Pearson");
        System.out.println("EUR" + "," + "CHF" + "," + top + "," + baseCurrencyData.length + "," + pearsonCorrelation);
    }

    private void searchAllWithPointBasedOpti(double errorBound, double error, boolean z_normalization) throws IOException {
//        PrintWriter print = new PrintWriter(new File("data/currency2/EUR_CHF_result_0.1_T3.csv"));
        PrintWriter print = new PrintWriter(new File("data/currency2/EUR_CHF_result.csv"));
        print.println("Base,Target,Pearson,k,b,Final length,Max up bound");

//        double[][] data = new double[currencies.length][];
//        String[] label = new String[currencies.length];
//        int cnt = 0;

        for (String baseCurrency : currencies) {
            if (!baseCurrency.equals("EUR")) continue;
            double[] baseCurrencyData = readFromFile(baseCurrency, "USD");
//            data[cnt] = baseCurrencyData;
//            label[cnt] = baseCurrency;
//            cnt++;

            for (String targetCurrency : currencies) {
                if (targetCurrency.equals(baseCurrency)) continue;
                if (!targetCurrency.equals("CHF")) continue;
                logger.info(baseCurrency + "-" + targetCurrency);

                double[] targetCurrencyData = readFromFile(targetCurrency, "USD");
//                data[cnt] = targetCurrencyData;
//                label[cnt] = targetCurrency;
//                cnt++;

                Point2D[] point2Ds = Point2DUtils.genFromXY(baseCurrencyData, targetCurrencyData);
                if (z_normalization) {
                    point2Ds = Point2DUtils.normalize(point2Ds);
                }

                // Pearson correlation: use Matlab instead
                double pearsonCorrelation = new PearsonsCorrelation().correlation(baseCurrencyData, targetCurrencyData);

                TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
                miner.process();
                List<PLASegment> segs = miner.buildSpecificSegments(3);

                PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
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
//                printResultFile("data/currency2/" + baseCurrency + "_" + targetCurrency + "_segment_0.1_T3.csv", point2Ds, k, b, errorBound);
                printResultFile("data/currency2/" + baseCurrency + "_" + targetCurrency + "_segment.csv", point2Ds, k, b, errorBound);

                print.println(baseCurrency + "," + targetCurrency + "," + pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
            }
        }
        print.close();

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

    private double[] readFromFileSelectResult(String currency1, String currency2, int top) throws IOException {
        double[] original = readFromFile(currency1, currency2);
        List list = FileUtils.readLines(new File("data/currency2/EUR_CHF_segment_0.1_T" + top + ".csv"));
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

    private double[] readFromFile(String currency1, String currency2) throws IOException {
        String filename = "data/currency2/" + currency1 + "-" + currency2 + ".csv";
        List list;
        boolean changed = false;
        try {
            logger.debug("name = " + filename);
            list = FileUtils.readLines(new File(filename));
        } catch (FileNotFoundException e) {
            logger.debug(filename + " not found. Use " + currency2 + "-" + currency1 + ".csv instead.");
            changed = true;
            filename = "data/currency2/" + currency2 + "-" + currency1 + ".csv";
            logger.debug("name = " + filename);
            list = FileUtils.readLines(new File(filename));
        }
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
        double[] data = new double[list.size() - 1];
        int cnt2 = 0;
        for (int i = 1; i < list.size(); i++) {
//            if (((String)list2.get(i-1)).startsWith("1") || ((String)list3.get(i-1)).startsWith("1")) {
//                continue;
//            }
            String s = (String) list.get(i);
            String[] strings = s.split(",");
            if (strings[1].equals(".")) {
                data[cnt2] = data[cnt2-1];
            } else {
                data[cnt2] = Double.parseDouble(strings[1]);
                if (changed) {
                    data[cnt2] = 1.0 / data[cnt2];
                }
            }
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
