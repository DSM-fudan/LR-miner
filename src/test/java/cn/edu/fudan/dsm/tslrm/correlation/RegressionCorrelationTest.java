package cn.edu.fudan.dsm.tslrm.correlation;

import cn.edu.fudan.dsm.tslrm.ArrayUtil;
import cn.edu.fudan.dsm.tslrm.data.DataGenerator;
import math.geom2d.Point2D;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-9-15
 * Time: 下午10:27
 * To change this template use File | Settings | File Templates.
 */
public class RegressionCorrelationTest {
    @Test
    public void test()
    {
        int length = 10;

        while (true) {
            double[] x = randomWalk(length,Math.random()*100, Math.random() * 10);
            double[] y = randomWalk(length,Math.random()*100, Math.random() * 10);

            int idxByRegression = getRemoveIdxByRegression(x, y);
            int idxByZNorm = getRemoveIdxByZNorm(x, y);

            if (idxByRegression != idxByZNorm)
            {
                double v = new PearsonsCorrelation().correlation(x, y);
                System.out.println("before coefficient = " + v);
                if (v < 0)
                    continue;

                SimpleRegression simpleRegression = new SimpleRegression();
                double[][] xy = new double[x.length][2];
                for (int i = 0; i < xy.length; i++) {
                    xy[i][0] = x[i];
                    xy[i][1] = y[i];
                }
                simpleRegression.addData(xy);
                double intercept = simpleRegression.getIntercept();
                double slope = simpleRegression.getSlope();
                System.out.println("slope = " + slope);
                System.out.println("intercept = " + intercept);
                double r = simpleRegression.getR();
                System.out.println("r = " + r);


                System.out.println("idxByRegression = " + idxByRegression);
                System.out.println("x[idxByRegression] = " + x[idxByRegression]);
                System.out.println("y[idxByRegression] = " + y[idxByRegression]);
                simpleRegression.removeData(x[idxByRegression], y[idxByRegression]);
                double rByRegression = simpleRegression.getR();
                System.out.println("rByRegression = " + rByRegression);

                simpleRegression.addData(x[idxByRegression],y[idxByRegression]);
                System.out.println("idxByZNorm = " + idxByZNorm);
                System.out.println("x[idxByZNorm] = " + x[idxByZNorm]);
                System.out.println("y[idxByZNorm] = " + y[idxByZNorm]);
                simpleRegression.removeData(x[idxByZNorm], y[idxByZNorm]);
                double rByZNorm = simpleRegression.getR();
                System.out.println("rByZNorm = " + rByZNorm);

                System.out.println("x:");
                printTimeSeries(x);
                System.out.println("y:");
                printTimeSeries(y);
                if (Math.abs(rByRegression) < Math.abs(rByZNorm))
                {
                    throw new RuntimeException("rByRegression large than rByZNorm");
                }
            }
        }
    }

    public void printTimeSeries(double[] x) {
        for (int i = 0; i < x.length; i++) {
            double v = x[i];
            System.out.print(v);
            System.out.print(",");
        }
        System.out.println();
    }

    public void printTimeSeries(int[] x) {
        for (int i = 0; i < x.length; i++) {
            int v = x[i];
            System.out.print(v);
            System.out.print(",");
        }
        System.out.println();
    }

    public double[] randomWalk(int length,double start, double step)
    {
        double[] ret = new double[length];
        double x = start;
        for (int i = 0; i < length; i++) {
            double r = Math.random();
            x = x + (r - 0.5) * step;
            ret[i] = x;
        }
        return ret;
    }

    public int getRemoveIdxByRegression(double[] x, double[] y)
    {
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
//        simpleRegression.getR();

        double max = Integer.MIN_VALUE;
        double diff = 0;

        for (int i = 0; i < x.length; i++) {
            diff = Math.abs(y[i] - slope * x[i] - intercept);
            if (diff > max) {
                max = diff;
                ret = i;
            }
        }


        return  ret;
    }

    public int getRemoveIdxByZNorm(double[] x, double[] y)
    {
        int ret = 0;
        double[] zx = ArrayUtil.zNorm(x);
        double[] zy = ArrayUtil.zNorm(y);

        double diff = 0;
        double max = Integer.MIN_VALUE;

        for (int i = 0; i < zx.length; i++) {
            diff = (zx[i] - zy[i]) * (zx[i] - zy[i]);
            if (diff > max) {
                max = diff;
                ret = i;
            }
        }

        return  ret;
    }

}
