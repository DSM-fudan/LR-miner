package cn.edu.fudan.dsm.tslrm;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-9-24
 * Time: 下午3:09
 * To change this template use File | Settings | File Templates.
 */
public class LinearRegressionModelMiner {
    public static void main(String[] args) {
        double x[] = {1, 2, 3, 6, 4, 3, 2, 1, 4, 2, 8, 7, 9, 6, 8, 4, 5, 7, 3, 4, 1, 10, 5, 8, 4, 8, 6, 10, 9, 6};
        double y[] = {4, 3, 8, 13, 9, 2, 5, 4, 8, 1, 13, 10, 20, 13, 15, 7, 9, 13, 5, 4, 1, 15, 11, 17, 9, 18, 13, 19, 19, 12};
        LinearRegressionModelMiner miner = new LinearRegressionModelMiner(x,y,3);
        miner.process();
        List<Segment> candidateSegmentList1 = miner.candidateSegmentList;
        int size = candidateSegmentList1.size();
        System.out.println("size = " + size);

        for (int i = 0; i < candidateSegmentList1.size(); i++) {
            Segment segment = candidateSegmentList1.get(i);
            System.out.println("segment" + i + " = " + segment);

        }

    }


    double[] x;
    double[] y;

    double mseBound;

    public LinearRegressionModelMiner(double[] x, double[] y, double mseBound) {
        this.x = x;
        this.y = y;
        this.mseBound = mseBound;
    }

    double slope;
    double intercept;

    public void process() {
        //do mining
        buildCandidateSegmentList();




    }

    List<Segment> candidateSegmentList = new ArrayList<Segment>();

    private void buildCandidateSegmentList() {
        SimpleRegression simpleRegression = new SimpleRegression();

        for (int i = 0; i < x.length - 2; i++) {
            simpleRegression.clear();
            simpleRegression.addData(x[i], y[i]);
            simpleRegression.addData(x[i + 1], y[i + 1]);
            for (int j = i + 2; j < x.length; j++) {
                simpleRegression.addData(x[j], y[j]);
                double sumSquaredErrors = simpleRegression.getSumSquaredErrors();
                double mse = sumSquaredErrors / (j - i + 1);
                if (mse <= mseBound) {
                    double slope = simpleRegression.getSlope();
                    double intercept = simpleRegression.getIntercept();
                    Segment segment = new Segment(i, j, slope, intercept, mse);
                    candidateSegmentList.add(segment);
                } else {
                    break;
                }
            }
        }


    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    List<Segment> segmentList = new ArrayList<Segment>();

    public List<Segment> getSegmentList() {
        return segmentList;
    }

    public int getTotalLengthOfSegments() {
        int ret = 0;
        for (int i = 0; i < segmentList.size(); i++) {
            ret += segmentList.get(i).getLength();
        }
        return ret;
    }

    public int getMeasure() {
        return getTotalLengthOfSegments() - segmentList.size() * 2;
    }
}

