package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.ForexData;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Created by wujy on 17-2-11.
 */
public class CurrencyTimeVsSegmentLength {

    private static Logger logger = Logger.getLogger(CurrencyTimeVsSegmentLength.class);

    private static final String[] currencies = {"CAD", "CHF", "CZK", "DKK", "HKD", "HUF", "JPY", "MXN", "NOK", "PLN", "SEK", "SGD", "TRY", "ZAR"};

    public static void main(String args[]) throws IOException {
        CurrencyTimeVsSegmentLength css = new CurrencyTimeVsSegmentLength();
        double error = 0.05;
        double errorBound = 0.1;

//        css.searchAllWithPointBasedOpti("HKD", errorBound, error, 3, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", errorBound, error, 5, true, 50000);
        css.searchAllWithPointBasedOpti("HKD", errorBound, error, 8, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", errorBound, error, 10, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", errorBound, error, 20, true, 50000);
//        css.searchAllWithPointBasedOpti("HKD", errorBound, error, 50, true, 50000);
    }

    private void searchAllWithPointBasedOpti(String baseCurrency, double errorBound, double error, int segmentLength, boolean z_normalization, int length) throws IOException {
//        PrintWriter print = new PrintWriter(new File("data/currency/time_epsilon/" + baseCurrency + "_result.csv"));
//        print.println("Base,Target,Pearson,k,b,Final length,Max up bound");

        double averageTime = 0, all = 0;

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
            List<PLASegment> segs = miner.buildSpecificSegments(segmentLength);

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

            long endTime = System.currentTimeMillis();

            all++;
            averageTime = averageTime + (endTime - startTime - averageTime) / all;

            System.out.println(averageTime);

//            print.println(baseCurrency + "," + targetCurrency + ",," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
        }
        System.out.println(segmentLength + "," + averageTime);
//        print.close();
    }

}
