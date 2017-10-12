package cn.edu.fudan.dsm.tslrm.correlation;

import cn.edu.fudan.dsm.tslrm.ArrayUtil;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-9-15
 * Time: 下午11:14
 * To change this template use File | Settings | File Templates.
 */
public class MaxCorrBrute {
    double[] x = new double[]{68.57150099942037, 66.83332019271413, 65.2369936596907, 65.21942747719854, 66.22004573357219, 64.6499725028783, 63.88088351592477, 62.125252379622516, 62.26735227270236, 62.99844172614719};
    double[] y = new double[]{72.05562964202909, 72.1093795308268, 72.14007990452258, 72.11726571181764, 71.96563743876183, 71.77361701434936, 71.80383858734658, 71.77643162560582, 71.6178056283387, 71.90118530864231};

    @Test
    public void test() {
//        x = ArrayUtil.zNorm(x);
//        y = ArrayUtil.zNorm(y);
        int byBruteForce = getByBruteForce(x, y);
        System.out.println("byBruteForce = " + byBruteForce);
    }

    RegressionCorrelationTest regressionCorrelationTest = new RegressionCorrelationTest();

    @Test
    public void test2() {
        int removeIdxByRegression = regressionCorrelationTest.getRemoveIdxByRegression(x, y);
        System.out.println("removeIdxByRegression = " + removeIdxByRegression);

        int removeIdxByZNorm = regressionCorrelationTest.getRemoveIdxByZNorm(x, y);
        System.out.println("removeIdxByZNorm = " + removeIdxByZNorm);
    }

    @Test
    public void test3() {
        int round = 0;
        while (true) {
            round++;
            System.out.println("round = " + round);
            double[] x = regressionCorrelationTest.randomWalk(50, Math.random() * 5, Math.random() * 2);
            double[] y = regressionCorrelationTest.randomWalk(50, Math.random() * 5, Math.random() * 2);

            int idByBrute = getByBruteForce(x, y);

            int idxByZNorm = regressionCorrelationTest.getRemoveIdxByZNorm(x, y);
            int idxByRegression = regressionCorrelationTest.getRemoveIdxByRegression(x, y);

            if ((idByBrute != idxByZNorm) && (idByBrute != idxByRegression)) {
                System.out.println("idxByRegression = " + idxByRegression);
                System.out.println("idxByZNorm = " + idxByZNorm);
                System.out.println("idByBrute = " + idByBrute);
            }
        }
    }
    
    @Test
    public void testAnotherCorrAlgorithm()
    {
        double correlation = new PearsonsCorrelation().correlation(x, y);
        System.out.println("correlation = " + correlation);
        
        double sumXY = 0;
        double sumX = 0;
        double sumY = 0;
        for (int i = 0; i < x.length; i++) {
            sumX = sumX + x[i];
            sumY = sumY + y[i];
        }
        
        double barX = sumX / x.length;
        double barY = sumY / x.length;
        
        double sumXX = 0;
        double sumYY = 0;

        for (int i = 0; i < x.length; i++) {
            sumXX = sumXX + (x[i] - barX) * (x[i] - barX);
            sumYY = sumYY + (y[i] - barY) * (y[i] - barY);
            sumXY = sumXY + (x[i] - barX) * (y[i] - barY);
        }
        System.out.println("sumXX = " + sumXX);
        System.out.println("sumYY = " + sumYY);
        System.out.println("sumXY = " + sumXY);
        double corr1 = Math.sqrt(sumXY * sumXY / (sumXX * sumYY));
        System.out.println("corr1 = " + corr1);
    }

    @Test
    public void test4()
    {
        int removeCount = 10;
        int round = 0;
        while (true) {
            double[] x = regressionCorrelationTest.randomWalk(20, Math.random() * 5, Math.random() * 2);
            double[] y = regressionCorrelationTest.randomWalk(20, Math.random() * 5, Math.random() * 2);

            double v = new PearsonsCorrelation().correlation(x, y);
            System.out.println("before coefficient = " + v);
            if (v < 0)
                continue;

            round++;
            System.out.println("round = " + round);

            int[] tempIds = new int[0];
            for (int i = 1; i < removeCount + 1; i++) {
                int[] idsByBruteForce = getIdsByBruteForce(x, y, i);
                regressionCorrelationTest.printTimeSeries(idsByBruteForce);

                SimpleRegression regression = new SimpleRegression();
                for (int j = 0; j < x.length; j++) {
                    regression.addData(x[j],y[j]);
                }
                for (int j = 0; j < idsByBruteForce.length; j++) {
                    regression.removeData(x[idsByBruteForce[j]],y[idsByBruteForce[j]]);
                }
                double r = regression.getR();
                System.out.println("r = " + r);

                //check exists
                boolean exist;
                for (int j = 0; j < tempIds.length; j++) {
                    int tempId = tempIds[j];
                    exist = false;
                    for (int k = 0; k < idsByBruteForce.length; k++) {
                        int i1 = idsByBruteForce[k];
                        if (tempId == i1)
                        {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist)
                    {
//                        throw new RuntimeException("not exits!");
                        System.out.println("not existing ....................................................");
                    }
                }
                tempIds = idsByBruteForce;
            }
        }
    }

    @Test
    public void testBruteForce()
    {
        int[] idsByBruteForce = getIdsByBruteForce(x, y, 3);
        for (int i = 0; i < idsByBruteForce.length; i++) {
            int i1 = idsByBruteForce[i];
            System.out.println("i1 = " + i1);
        }
    }

    @Test
    public void testBruteForceForTwoParallel()
    {
        double[] x1 = new double[21];
        double[] y1 = new double[21];

        for (int i = 0; i < x1.length; i++) {
            if (i % 2 == 0)
            {
                x1[i] = Math.random() * 10;
                y1[i] = 2 * x1[i] + 1;
            }
            else
            {
                x1[i] = Math.random() * 10;
                y1[i] = 2 * x1[i] + 3;
            }

        }


        int[] idsByBruteForce = getIdsByBruteForce(x1, y1, 5);
        for (int i = 0; i < idsByBruteForce.length; i++) {
            int i1 = idsByBruteForce[i];
            System.out.println("i1 = " + i1 + " " + x1[i1] + "," + y1[i1]);
        }

        double[] x2 = new double[x1.length];
        double[] y2 = new double[y1.length];
        System.arraycopy(x1,0,x2,0,x1.length);
        System.arraycopy(y1,0,y2,0,y1.length);
        
        for (int i = 0; i < idsByBruteForce.length; i++) {
            int removeIdxByZNorm = regressionCorrelationTest.getRemoveIdxByZNorm(x2, y2);
            System.out.println("removeIdxByZNorm = " + removeIdxByZNorm + " " + x2[removeIdxByZNorm] + "," + y2[removeIdxByZNorm]);
            x2 = ArrayUtil.removeByIdx(x2, removeIdxByZNorm);
            y2 = ArrayUtil.removeByIdx(y2,removeIdxByZNorm);
        }

        double[] x3 = new double[x1.length];
        double[] y3 = new double[y1.length];
        System.arraycopy(x1,0,x3,0,x1.length);
        System.arraycopy(y1,0,y3,0,y1.length);

        for (int i = 0; i < idsByBruteForce.length; i++) {
            int idxByRegression = regressionCorrelationTest.getRemoveIdxByRegression(x3, y3);
            System.out.println("idxByRegression = " + idxByRegression + " " + x3[idxByRegression] + "," + y3[idxByRegression]);
            x3 = ArrayUtil.removeByIdx(x3, idxByRegression);
            y3 = ArrayUtil.removeByIdx(y3,idxByRegression);
        }
//        
    }

    public int[] getIdsByBruteForce(double[] x,double[] y, int removeCount)
    {
        int[] ret = new int[removeCount];
        int[] temp = new int[removeCount];

        //init
        for (int i =0; i<removeCount; i++)
        {
            temp[i] = i;
        }

        SimpleRegression simpleRegression = new SimpleRegression();
        double[][] xy = new double[x.length][2];
        for (int i = 0; i < xy.length; i++) {
            xy[i][0] = x[i];
            xy[i][1] = y[i];
        }
        simpleRegression.addData(xy);

        double maxR = -1;

        while (true)
        {
            //remove
            for (int i = 0; i < temp.length; i++) {
                simpleRegression.removeData(x[temp[i]],y[temp[i]]);
            }

            //calc and test
            double rTemp = simpleRegression.getR();
            if (rTemp > maxR)
            {
                System.arraycopy(temp,0,ret,0,temp.length);
                maxR = rTemp;
            }

            //add back
            for (int i = 0; i < temp.length; i++) {
                simpleRegression.addData(x[temp[i]], y[temp[i]]);
            }

            //no more test
            if (temp[0] == x.length - temp.length)
                break;

            //get next temp
            for (int i = temp.length -1; i >=0 ;i--) {
                if (temp[i] < x.length-(temp.length -i))
                {
                    temp[i] = temp[i] + 1;
                    for (int j = i + 1 ; j < temp.length; j++)
                    {
                        temp[j] = temp[i] + (j-i);  //increase one by one
                    }
                    break;
                }
            }
        }
        return ret;
    }

    public int getByBruteForce(double[] x, double[] y) {
        int ret = 0;

        SimpleRegression simpleRegression = new SimpleRegression();
        double[][] xy = new double[x.length][2];
        for (int i = 0; i < xy.length; i++) {
            xy[i][0] = x[i];
            xy[i][1] = y[i];
        }
        simpleRegression.addData(xy);
        double intercept = simpleRegression.getIntercept();
        double slope = simpleRegression.getSlope();
//        System.out.println("slope = " + slope);
//        System.out.println("intercept = " + intercept);
        double r = simpleRegression.getR();
//        System.out.println("r = " + r);

        double maxR = -1;
        for (int i = 0; i < xy.length; i++) {
            simpleRegression.removeData(x[i], y[i]);
            double r1 = simpleRegression.getR();
//            System.out.println("r1 = " + r1);
            if (r1 > maxR) {
                maxR = r1;
                ret = i;
//                System.out.println("maxR = " + maxR);
//                System.out.println("i = " + i);
            }

            simpleRegression.addData(x[i], y[i]);
            r = simpleRegression.getR();
//            System.out.println("r = " + r);
        }

        return ret;
    }
}
