package cn.edu.fudan.dsm.tslrm;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-9-1
 * Time: 上午8:23
 * To change this template use File | Settings | File Templates.
 */
public class ArrayUtil {
    public static double[] removeByIdx(double[] doubles, int removeIdx)
    {
        return removeByIdxs(doubles, new int[]{removeIdx});
    }

    public static double[] removeByIdxs(double[] doubles, int[] ints) {
        double[] ret = new double[doubles.length - ints.length];
        Arrays.sort(ints);

        int start = 0;
        for (int i = 0; i < ints.length; i++) {
            int anInt = ints[i];
            System.arraycopy(doubles,start,ret,start - i,anInt - start);
            start = anInt + 1;
        }
        System.arraycopy(doubles,start,ret,start - ints.length, doubles.length - start);

        return ret;
    }

    public static double[] zNorm(double[] doubles)
    {

        Variance variance = new Variance(false);
        DescriptiveStatistics ds = new DescriptiveStatistics(doubles);

        double mean = ds.getMean();
        double standardDeviation = Math.sqrt(variance.evaluate(doubles));
        double [] ret = new double[doubles.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (doubles[i] - mean)/ standardDeviation;
        }

        return ret;
    }

    public static void main(String[] args) {
        double[] x = new double[]{0,1,2,3,4,5,6,7,8,9,10};
        double[] y = removeByIdxs(x, new int[]{1, 3, 9});
        for (int i = 0; i < y.length; i++) {
            double v = y[i];
            System.out.println("v = " + v);
        }

        double[] z = zNorm(x);
        for (int i = 0; i < z.length; i++) {
            double v = z[i];
            System.out.println("v = " + v);
        }
    }

    public static double distance(double[] x, double[] y)
    {
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            double xi = x[i];
            double yi = y[i];

            sum = sum + (xi-yi)*(xi-yi);
        }
        return Math.sqrt(sum);
    }

    // Add by Jiaye Wu on 2017/2/10
    public static SimpleRegression getSimpleRegression(double[] x, double[] y) {
        SimpleRegression ret = new SimpleRegression();
        for (int i = 0; i < x.length; i++) {
            ret.addData(x[i], y[i]);
        }
        return ret;
    }

}
