package cn.edu.fudan.dsm.tslrm.data;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-30
 * Time: 上午10:18
 * To change this template use File | Settings | File Templates.
 */
public class AccuracyTest {
    @Test
    public void test()
    {
        double[] values = DataGenerator.generateData(-100, 100, 1000, 18, 1, 0);

        int bitLength = DataGenerator.detectBitLength(values,-100,100);
        System.out.println("bitLength = " + bitLength);

    }
}
