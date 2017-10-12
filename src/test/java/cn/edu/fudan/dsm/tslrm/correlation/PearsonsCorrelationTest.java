package cn.edu.fudan.dsm.tslrm.correlation;

import math.geom2d.Point2D;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * Created by wujy on 17-2-12.
 */
public class PearsonsCorrelationTest {

    public static double correlation(Point2D[] data) {
        double[] x = new double[data.length];
        double[] y = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            x[i] = data[i].getX();
            y[i] = data[i].getY();
        }
        return new PearsonsCorrelation().correlation(x, y);
    }

    public static double correlation(double[] xs, double[] ys) {
        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        int n = xs.length;

        for (int i = 0; i < n; ++i) {
            double x = xs[i];
            double y = ys[i];
            sx += x;
            sy += y;
            sxx += x * x;
            syy += y * y;
            sxy += x * y;
        }

        // covariation
        double cov = sxy / n - sx * sy / n / n;
        // standard error of x
        double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
        // standard error of y
        double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

        // correlation is just a normalized covariation
        return cov / sigmax / sigmay;
    }

    public static void main(String[] args) {
        double[] x = {1, 1, 1, 1, 2};
        double[] y = {1, 2, 3, 2, 1};
        double corr = new PearsonsCorrelation().correlation(x, y);
        double corr1 = correlation(x, y);

        System.out.println(corr + "," + corr1);
    }

}