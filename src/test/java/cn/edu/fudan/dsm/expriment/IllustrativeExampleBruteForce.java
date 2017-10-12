package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.correlation.PearsonsCorrelationTest;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jiaye Wu on 17-2-20.
 */
public class IllustrativeExampleBruteForce {

    public static void main(String args[]) throws IOException {
        IllustrativeExampleBruteForce iess = new IllustrativeExampleBruteForce();

        Point2D[] data = iess.readFromFile("illustrative_example_data_4.csv", 0, 1);
        data = Point2DUtils.normalize(data);

        double globalCorrelation = PearsonsCorrelationTest.correlation(data);

        // given min length
        int minLength = 100;
        double minCorrelation = 0.8;

        int maxI = -1, maxLen = -1;
        double maxCorrelation = 0;
        for (int i = 0; i < data.length; i++) {
            for (int len = minLength; i + len - 1 < data.length; len++) {
                Point2D[] currentData = Arrays.copyOfRange(data, i, i + len);
                double currentCorrelation = PearsonsCorrelationTest.correlation(currentData);
                if (currentCorrelation > minCorrelation) {
                    System.out.println(i + "," + len + ": max=" + currentCorrelation);
                    if (len > maxLen) {
                        maxLen = len;
                        maxI = i;
                        maxCorrelation = currentCorrelation;
                    } else if (len == maxLen && currentCorrelation > maxCorrelation) {
                        maxCorrelation = currentCorrelation;
                        maxI = i;
                        maxLen = len;
                    }
                }
            }
        }

        System.out.println("global correlation = " + globalCorrelation);
        System.out.println("max pearson correlation = " + maxCorrelation);
        System.out.println("max i = " + maxI);
        System.out.println("max len = " + maxLen);
    }

    private Point2D[] readFromFile(String filename, int columnA, int columnB) throws IOException {
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        Point2D[] data;
        data = new Point2D[list.size()];
        int cnt2 = 0;
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            String[] strings = s.split(",");
            data[cnt2] = new Point2D(Double.parseDouble(strings[columnA]), Double.parseDouble(strings[columnB]));
            cnt2++;
        }
        return data;
    }
}
