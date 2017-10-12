package cn.edu.fudan.dsm.tslrm.data;

import math.geom2d.Point2D;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-17
 * Time: 上午11:38
 * To change this template use File | Settings | File Templates.
 */
public class DataGeneratorTest {
    @Test
    public void testGenerator100000() throws IOException {
        double[] slopes = {1, 2, -1, -3};
        double[] intercepts = {5, -2, -1, 4};
        double[] weights = {100, 20, 30, 50};
        int count = 100000;
        int maxSegLength = 10;
        double minX = -10;
        double maxX = 10;

        int[] models = new int[count];

        Point2D[] point2Ds = DataGenerator.randomLinearPoints(slopes, intercepts, weights, count, maxSegLength, minX, maxX, models);
        int[] modelLengths = new int[slopes.length];
        for (int i = 0; i < point2Ds.length; i++) {
            Point2D point2D = point2Ds[i];
            System.out.println("point2D = " + point2D + " belong to:" + models[i]);
            modelLengths[models[i]] = modelLengths[models[i]] + 1;
        }

        for (int i = 0; i < modelLengths.length; i++) {
            int modelLength = modelLengths[i];
            System.out.println("model " + i + ",modelLength = " + modelLength);
        }

        double yErrorBound = 1;
        Point2D[] points = DataGenerator.addError(point2Ds, 0, yErrorBound);

        String fileName = "data\\test_" + count + "_" + slopes.length;
        DataGenerator.savePoints2File(points, new File(fileName + ".txt"));

        DataGenerator.savePointsModel2File(slopes,intercepts,models,new File(fileName+".desc.txt"));
    }

    @Test
    public void testRandomLinearPoints() throws IOException {
        double[] slopes = {3.1, -3};
        double[] intercepts = {-50,  +100};
        double[] weights = {100, 200};
        int count = 100000;
        int maxSegLength = 20;
        double minX = -1000;
        double maxX = 1000;

        int[] models = new int[count];

        Point2D[] point2Ds = DataGenerator.randomLinearPoints(slopes, intercepts, weights, count, maxSegLength, minX, maxX, models);
        int[] modelLengths = new int[slopes.length];
        for (int i = 0; i < point2Ds.length; i++) {
            Point2D point2D = point2Ds[i];
            System.out.println("point2D = " + point2D + " belong to:" + models[i]);
            modelLengths[models[i]] = modelLengths[models[i]] + 1;
        }

        for (int i = 0; i < modelLengths.length; i++) {
            int modelLength = modelLengths[i];
            System.out.println("model " + i + ",modelLength = " + modelLength);
        }

        double yErrorBound = 1;
        Point2D[] points = DataGenerator.addError(point2Ds, 0, yErrorBound);


        String fileName = "data\\test_"+count+ "_" + slopes.length;
        DataGenerator.savePoints2File(points, new File(fileName + ".txt"));

        DataGenerator.savePointsModel2File(slopes, intercepts, models, new File(fileName + ".desc.txt"));
    }

    @Test
    public void testGenerator200() throws IOException {
        double[] slopes = {1, 2, -1, -3};
        double[] intercepts = {5, -2, -1, 4};
        double[] weights = {100, 20, 30, 50};
        int count = 200;
        int maxSegLength = 10;
        double minX = -10;
        double maxX = 10;

        int[] models = new int[count];

        Point2D[] point2Ds = DataGenerator.randomLinearPoints(slopes, intercepts, weights, count, maxSegLength, minX, maxX, models);
        int[] modelLengths = new int[slopes.length];
        for (int i = 0; i < point2Ds.length; i++) {
            Point2D point2D = point2Ds[i];
            System.out.println("point2D = " + point2D + " belong to:" + models[i]);
            modelLengths[models[i]] = modelLengths[models[i]] + 1;
        }

        for (int i = 0; i < modelLengths.length; i++) {
            int modelLength = modelLengths[i];
            System.out.println("model " + i + ",modelLength = " + modelLength);
        }

        double yErrorBound = 1;
        Point2D[] points = DataGenerator.addError(point2Ds, 0, yErrorBound);


        String fileName = "data\\test_200_4";
        DataGenerator.savePoints2File(points, new File(fileName + ".txt"));

        DataGenerator.savePointsModel2File(slopes,intercepts,models,new File(fileName+".desc.txt"));

    }

    @Test
    public void testGenerator() throws IOException {
        double[] slopes = {1, 2, -1, -3};
        double[] intercepts = {5, -2, -1, 4};
        double[] weights = {100, 20, 30, 50};
        int count = 10000;
        int maxSegLength = 20;
        double minX = -10;
        double maxX = 10;

        int[] models = new int[count];

        Point2D[] point2Ds = DataGenerator.randomLinearPoints(slopes, intercepts, weights, count, maxSegLength, minX, maxX, models);
        int[] modelLengths = new int[slopes.length];
        for (int i = 0; i < point2Ds.length; i++) {
            Point2D point2D = point2Ds[i];
//            System.out.println("point2D = " + point2D + " belong to:" + models[i]);
            modelLengths[models[i]] = modelLengths[models[i]] + 1;
        }

        for (int i = 0; i < modelLengths.length; i++) {
            int modelLength = modelLengths[i];
//            System.out.println("model " + i + ",modelLength = " + modelLength);
        }

        double yErrorBound = 1;
        Point2D[] points = DataGenerator.addError(point2Ds, 0, yErrorBound);


        String fileName = "data\\test_"+count + "_" + slopes.length;
        System.out.println("fileName = " + fileName);
        DataGenerator.savePoints2File(points, new File(fileName + ".txt"));

        DataGenerator.savePointsModel2File(slopes,intercepts,models,new File(fileName+".desc.txt"));

    }

    @Test
    public void testGenerator50() throws IOException {
        double[] slopes = {1, 2, -1, -3};
        double[] intercepts = {5, -2, -1, 4};
        double[] weights = {100, 20, 30, 50};
        int count = 50;
        int maxSegLength = 10;
        double minX = -10;
        double maxX = 6;

        int[] models = new int[count];

        Point2D[] point2Ds = DataGenerator.randomLinearPoints(slopes, intercepts, weights, count, maxSegLength, minX, maxX, models);
        int[] modelLengths = new int[slopes.length];
        for (int i = 0; i < point2Ds.length; i++) {
            Point2D point2D = point2Ds[i];
            System.out.println("point2D = " + point2D + " belong to:" + models[i]);
            modelLengths[models[i]] = modelLengths[models[i]] + 1;
        }

        for (int i = 0; i < modelLengths.length; i++) {
            int modelLength = modelLengths[i];
            System.out.println("model " + i + ",modelLength = " + modelLength);
        }

        double yErrorBound = 1;
        Point2D[] points = DataGenerator.addError(point2Ds, 0, yErrorBound);


        String fileName = "data\\test_50_4";
        DataGenerator.savePoints2File(points, new File(fileName + ".txt"));

        DataGenerator.savePointsModel2File(slopes,intercepts,models,new File(fileName+".desc.txt"));

    }

    @Test
    public void testRead200() throws IOException {
        File file = new File("data\\test_200_4.txt");
        Point2D[] points = DataGenerator.readPointsFromFile(file);

        System.out.println("points.length = " + points.length);
        System.out.println("=========");
        for (int i = 0; i < points.length; i++) {
            Point2D point2D = points[i];
            System.out.println("point2D = " + point2D);
        }

    }

    @Test
    public void testRead100() throws IOException {
        File file = new File("data\\test_100_4.txt");
        Point2D[] points = DataGenerator.readPointsFromFile(file);

        System.out.println("points.length = " + points.length);
        System.out.println("=========");
        for (int i = 0; i < points.length; i++) {
            Point2D point2D = points[i];
            System.out.println("point2D = " + point2D);
        }

    }

    @Test
    public void testReadFile() throws IOException {
        File file = new File("data\\test1.txt");
        Point2D[] points = DataGenerator.readPointsFromFile(file);

        System.out.println("points.length = " + points.length);
        System.out.println("=========");
        for (int i = 0; i < points.length; i++) {
            Point2D point2D = points[i];
            System.out.println("point2D = " + point2D);
        }
    }

    @Test
    public void test() {
        double x = 0.123;
        System.out.println("x = " + (int) x);
    }

    @Test
    public void testRandomWalk() throws IOException {
        int count = 2000;
        Point2D[] points = DataGenerator.randomWalk(count,1);

        String fileName = "data\\test_randomwalk_" +count ;
        DataGenerator.savePoints2File(points, new File(fileName + ".txt"));

    }
}
