package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ForexData;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Created by wujy on 17-2-11.
 */
public class CurrencyTimeVsEpsilon {

    private static Logger logger = Logger.getLogger(CurrencyTimeVsEpsilon.class);

    private static final String[] currencies = {"CAD", "CHF", "CZK", "DKK", "HKD", "HUF", "JPY", "MXN", "NOK", "PLN", "SEK", "SGD", "TRY", "ZAR"};

    public static void main(String args[]) throws IOException {
        CurrencyTimeVsEpsilon css = new CurrencyTimeVsEpsilon();
        double error = 0.05;

        css.searchAllWithPointBasedOpti("HKD", 0.01, error, true, 50000);
        css.searchAllWithPointBasedOpti("HKD", 0.02, error, true, 50000);
        css.searchAllWithPointBasedOpti("HKD", 0.03, error, true, 50000);
        css.searchAllWithPointBasedOpti("HKD", 0.04, error, true, 50000);

//        css.searchAllWithPointBasedOpti("HKD", 0.05, error, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", 0.10, error, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", 0.15, error, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", 0.20, error, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", 0.50, error, true, 50000);
    }

    private void searchAllWithPointBasedOpti(String baseCurrency, double errorBound, double error, boolean z_normalization, int length) throws IOException {
//        PrintWriter print = new PrintWriter(new File("data/currency/time_epsilon/" + baseCurrency + "_result.csv"));
//        print.println("Base,Target,Pearson,k,b,Final length,Max up bound");

        double averageTime = 0, all = 0;
        double averageTime1 = 0, all1 = 0;
        double averageSegs = 0;
        double averageTimes = 0;
        double averageVerifyTime = 0;
        double averageCalcUpperBoundTime = 0;

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
            Point2D[] point2Ds = Point2DUtils.genFromForexData(baseCurrencyData, targetCurrencyData, z_normalization ? 1 : 0, length);

            // Pearson correlation: use Matlab instead
            //double pearsonCorrelation = new PearsonsCorrelation().correlation(baseCurrencyData.rates[1], targetCurrencyData.rates[1]);
            //double pearsonCorrelation2 = PearsonsCorrelationTest.correlation(baseCurrencyData.rates[1], targetCurrencyData.rates[1]);
            //System.out.println(baseCurrency + "," + targetCurrency + "," + pearsonCorrelation + "," + pearsonCorrelation2);

            long startTime = System.currentTimeMillis();

            TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
            miner.process();
            List<PLASegment> segs = miner.buildSpecificSegments(3);

            long endTime1 = System.currentTimeMillis();
            all1++;
            averageTime1 = averageTime1 + (endTime1 - startTime - averageTime1) / all1;
            System.out.println(averageTime1);
            averageSegs = averageSegs + (segs.size() - averageSegs) / all1;

            PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
            plaRegionSearch.errorBound = errorBound;
            for (int j = segs.size() - 1; j >= 0; j--) {
                if (segs.get(j).getPolygonKB().getRings().size() > 1) {
                    segs.remove(j);
                    System.out.println("Remove at " + j);
                }
            }

            Pair<Long, Pair<Long, Long>> ret = plaRegionSearch.searchByBox2DWithInsideCountTimes(segs, error);
//            double k = point2Ds1.getX();
//            double b = point2Ds1.getY();

            long endTime = System.currentTimeMillis();

            all++;
            averageTime = averageTime + (endTime - startTime - averageTime) / all;
            averageTimes = averageTimes + (ret.getKey() - averageTimes) / all;
            averageVerifyTime = averageVerifyTime + (ret.getValue().getKey() - averageVerifyTime) / all;
            averageCalcUpperBoundTime = averageCalcUpperBoundTime + (ret.getValue().getValue() - averageCalcUpperBoundTime) / all;

            System.out.println(averageTime);
            System.out.println(averageTimes);

//            print.println(baseCurrency + "," + targetCurrency + ",," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
        }
        System.out.println(errorBound + "," + averageTime1 + ","  + averageSegs + "," + averageTime + "," + averageTimes + "," + averageVerifyTime + "," + averageCalcUpperBoundTime);
//        print.close();
    }

}
