package cn.edu.fudan.dsm.tslrm.data;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-30
 * Time: 下午12:44
 * To change this template use File | Settings | File Templates.
 */
public class MatlabUtilTest {
    public static void saveArray2File(double[][] data, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            double[] doubles = data[i];
            for (int j = 0; j < doubles.length; j++) {
                double aDouble = doubles[j];
                sb.append(String.format("%.5f",aDouble)).append(",");
            }
            sb.append("\n");
        }

        FileUtils.writeStringToFile(file,sb.toString());
    }

    public static void saveArray2File(double[]data, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
                double aDouble = data[i];
                sb.append(aDouble).append(",");
        }

        FileUtils.writeStringToFile(file,sb.toString());
    }

    @Test
    public void test() throws IOException {
        double[][] data = new double[100][4];
        for (int i = 0; i < data.length; i++) {
            double[] doubles = data[i];

            for (int j = 0; j < doubles.length; j++) {
                doubles[j] = 1 * j;
            }
        }

        saveArray2File(data,new File("data/data.txt"));
    }

    @Test
    public void saveForexData() throws IOException {
        File dir = new File("data/currency");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("60_");
            }
        });

        System.out.println("files.length = " + files.length);

        StringBuilder sbDesc = new StringBuilder();
        ForexData[] forexDatas = new ForexData[files.length];
        for (int i = 0; i < forexDatas.length; i++) {
            forexDatas[i] = ForexData.readFromFile(files[i].getAbsolutePath());
            sbDesc.append(forexDatas[i].secondCurrency).append(",");
        }

        double[][] data = new double[forexDatas[0].times.length][forexDatas.length];
        for (int i = 0; i < data.length; i++) {
            double[] doubles = data[i];
            for (int j = 0; j < doubles.length; j++) {
                data[i][j] = forexDatas[j].rates[i][1];
            }
        }

        saveArray2File(data,new File("data/currency/UsdBasedData.txt"));
        FileUtils.writeStringToFile(new File("data/currency/UsdBasedData.desc.txt"),sbDesc.toString());
    }
}
