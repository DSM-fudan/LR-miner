package cn.edu.fudan.dsm.tslrm.data;

import math.geom2d.Point2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-14
 * Time: 上午10:01
 * To change this template use File | Settings | File Templates.
 */
public class Point2DUtils {
    static Logger logger = Logger.getLogger(Point2DUtils.class);


    public static Point2D[] genFromForexData(ForexData forexData1, ForexData forexData2, int index) {
        assert (forexData1.times.length == forexData2.times.length);

        Point2D[] ret = new Point2D[forexData1.times.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Point2D(forexData1.rates[i][index], forexData2.rates[i][index]);
        }
        return ret;
    }

    public static Point2D[] genFromForexData(ForexData forexData1, ForexData forexData2, int index, int length) {
        assert (forexData1.times.length == forexData2.times.length);
        assert (length <= forexData1.times.length);

        Point2D[] ret = new Point2D[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new Point2D(forexData1.rates[i][index], forexData2.rates[i][index]);
        }
        return ret;
    }

    public static Point2D[] genFromForexData(ForexData forexData1, ForexData forexData2) {
        return genFromForexData(forexData1, forexData2, 0);
    }

    public static Point2D[] genFromXY(double[] x, double[] y) {
        assert x.length == y.length;

        Point2D[] ret = new Point2D[x.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Point2D(x[i], y[i]);
        }
        return ret;
    }

    public static Point2D[] normalize(Point2D[] org) {
        DescriptiveStatistics dsX = new DescriptiveStatistics();
        DescriptiveStatistics dsY = new DescriptiveStatistics();
        for (int i = 0; i < org.length; i++) {
            Point2D point2D = org[i];
            dsX.addValue(point2D.getX());
            dsY.addValue(point2D.getY());
        }

        //logger.debug("dsX = " + dsX);
        //logger.debug("dsY = " + dsY);

        Point2D[] ret = new Point2D[org.length];
        for (int i = 0; i < ret.length; i++) {
            double x = (org[i].getX() - dsX.getMean())/dsX.getStandardDeviation();
            double y = (org[i].getY() - dsY.getMean())/dsY.getStandardDeviation();
            ret[i] = new Point2D(x,y);
        }
        return ret;
    }

}
