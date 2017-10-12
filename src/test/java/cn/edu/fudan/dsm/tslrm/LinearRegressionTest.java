package cn.edu.fudan.dsm.tslrm;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-9-24
 * Time: 下午1:46
 * To change this template use File | Settings | File Templates.
 */

import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Test;

import org.apache.log4j.Logger;

import java.io.*;

public class LinearRegressionTest {
    private Logger logger = Logger.getLogger(LinearRegressionTest.class);

    @Test
    public void testLinearRegression() {
        SimpleRegression simpleRegression = new SimpleRegression();
        //y = kx + b; k = 2 b = 3
//        for (int i = 0; i < 5; i++) {
//            double x = i;
//            double y = 2 * x + 3 + Math.random()/10;
//            simpleRegression.addData(x,y);
//        }

        double[][] xy = new double[][]{
                {208, 21.6},
                {152, 15.5},
                {113, 10.4},
                {227, 31.0},
                {137, 13.0},
                {238, 32.4},
                {178, 19.0},
                {104, 10.4},
                {191, 19.0},
                {130, 11.8}
        };

        simpleRegression.addData(xy);

//        simpleRegression.addData(208, 21.6);
//        simpleRegression.addData(152, 15.5);
//        simpleRegression.addData(113, 10.4);
//        simpleRegression.addData(227, 31.0);
//        simpleRegression.addData(137, 13.0);
//        simpleRegression.addData(238, 32.4);
//        simpleRegression.addData(178, 19.0);
//        simpleRegression.addData(104, 10.4);
//        simpleRegression.addData(191, 19.0);
//        simpleRegression.addData(130, 11.8);

//        simpleRegression.removeData();

//        RegressionResults regressionResults = simpleRegression.regress();
        double intercept = simpleRegression.getIntercept();
        System.out.println("intercept = " + intercept);
        double slope = simpleRegression.getSlope();
        System.out.println("slope = " + slope);

        double meanSquareError = simpleRegression.getMeanSquareError();
        System.out.println("meanSquareError = " + meanSquareError);
        System.out.println("meanSquareError*(xy.length-2) = " + meanSquareError*(xy.length-2));

        double regressionSumSquares = simpleRegression.getRegressionSumSquares();
        System.out.println("regressionSumSquares = " + regressionSumSquares);

        double sumSquaredErrors = simpleRegression.getSumSquaredErrors();
        System.out.println("sumSquaredErrors = " + sumSquaredErrors);

        simpleRegression.getSignificance();

        double mse = 0;
        for (int i = 0; i < xy.length; i++) {
            double x = xy[i][0];
            double y = xy[i][1];

            double ey = x * slope + intercept;
//            System.out.println("ey = " + ey);
//            System.out.println("y = " + y);

            mse += (ey-y)*(ey-y);
        }
        System.out.println("mse = " + mse);

        //mse = mse / xy.length;

        //System.out.println("mse = " + mse);

        double mse1 = 0;
        for (int i = 0; i < xy.length; i++) {
            double x = xy[i][0];
            double y = xy[i][1];

            double ex = (y-intercept) / slope;
//            System.out.println("ex = " + ex);
//            System.out.println("x = " + x);

            mse1 += (ex-x)*(ex-x);
        }
        System.out.println("mse1 = " + mse1);

        System.out.println("sqrt(mse/mse1) = " + Math.sqrt(mse/mse1));


    }


    @Test
    public void test2()
    {
        SimpleRegression reg = new SimpleRegression();
        reg.addData(10,19);
        reg.addData(9,19);
        reg.addData(6,12);

        double slope = reg.getSlope();
        System.out.println("slope = " + slope);
        double intercept = reg.getIntercept();
        System.out.println("intercept = " + intercept);
        double v = reg.getSumSquaredErrors() / reg.getN();
        System.out.println("v = " + v);

        System.out.println("reg.getN() = " + reg.getN());

    }

    @Test
    public void test3() throws IOException, UnsupportedEncodingException {
        String s = "123中国";
        File file = new File("1.txt");
        FileOutputStream fos = new FileOutputStream(file);
        System.out.println(file.getAbsolutePath() );
        byte[] bytes = s.getBytes("utf-8");
        for (int i = 0; i < bytes.length; i++) {
            byte aByte = bytes[i];
            System.out.println("aByte = " + aByte);
        }
        fos.write(bytes);
        fos.flush();
        fos.close();
    }
}
