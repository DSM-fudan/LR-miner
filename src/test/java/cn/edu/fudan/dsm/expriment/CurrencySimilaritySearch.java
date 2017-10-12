package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ForexData;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by wujy on 17-2-11.
 */
public class CurrencySimilaritySearch {

    private static Logger logger = Logger.getLogger(CurrencySimilaritySearch.class);

    private static final String[] currencies = {"CAD", "CHF", "CZK", "DKK", "HKD", "HUF", "JPY", "MXN", "NOK", "PLN", "SEK", "SGD", "TRY", "ZAR"};

    public static void main(String args[]) throws IOException {
        CurrencySimilaritySearch css = new CurrencySimilaritySearch();
        double errorBound = 0.1;
        double error = 0.05;

        css.searchAllWithPointBasedOpti("HKD", errorBound, error, true);
    }

    private void searchAllWithPointBasedOpti(String baseCurrency, double errorBound, double error, boolean z_normalization) throws IOException {
        PrintWriter print = new PrintWriter(new File("data/currency/" + baseCurrency + "_result.csv"));
        print.println("Base,Target,Pearson,k,b,Final length,Max up bound");
        for (String targetCurrency : currencies) {
            if (targetCurrency.equals(baseCurrency)) continue;
            logger.info(baseCurrency + "-" + targetCurrency);

            String targetCurrencyFilename = "data/currency/5_USD_" + targetCurrency + ".csv";
            ForexData targetCurrencyData = ForexData.readFromFile(targetCurrencyFilename);
            ForexData baseCurrencyData;
            if (baseCurrency.equals("USD")) {
                baseCurrencyData = ForexData.generateUSDFrom(targetCurrencyData);
            } else {
                String baseCurrencyFilename = "data/currency/5_USD_" + baseCurrency + ".csv";
                baseCurrencyData = ForexData.readFromFile(baseCurrencyFilename);
            }
            Point2D[] point2Ds = Point2DUtils.genFromForexData(baseCurrencyData, targetCurrencyData, z_normalization ? 1 : 0);

            // Pearson correlation: use Matlab instead
            //double pearsonCorrelation = new PearsonsCorrelation().correlation(baseCurrencyData.rates[1], targetCurrencyData.rates[1]);
            //double pearsonCorrelation2 = PearsonsCorrelationTest.correlation(baseCurrencyData.rates[1], targetCurrencyData.rates[1]);
            //System.out.println(baseCurrency + "," + targetCurrency + "," + pearsonCorrelation + "," + pearsonCorrelation2);

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
            double k = point2Ds1.getX();
            double b = point2Ds1.getY();

            //print 0-1 file
            printResultFile("data/currency/" + baseCurrency + "_" + targetCurrency + "_segment.csv", point2Ds, k, b, errorBound);

            print.println(baseCurrency + "," + targetCurrency + ",," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
        }
        print.close();
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
