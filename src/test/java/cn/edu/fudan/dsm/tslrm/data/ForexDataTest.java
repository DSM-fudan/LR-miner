package cn.edu.fudan.dsm.tslrm.data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-11
 * Time: 上午10:22
 * To change this template use File | Settings | File Templates.
 */
public class ForexDataTest {
    @Test
    public void testUsd() throws IOException {
        File dir = new File("data/currency");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains("USD");
            }
        });

        System.out.println("files.length = " + files.length);

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println("file.getName() = " + file.getName());

            ForexData forexData = ForexData.readFromFile(file);
            System.out.println("forexData = " + forexData.times.length);
        }

    }

    static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testUsdBased() throws IOException {
        File dir = new File("../data");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("USD");
            }
        });

        System.out.println("files.length = " + files.length);

        ArrayList<LongRange> list = new ArrayList<LongRange>();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
//            System.out.println("file.getName() = " + file.getName());

            ForexData forexData = ForexData.readFromFile(file);
            System.out.println(i + "  -----file = " + file.getName());

            long[] times = forexData.times;

            int c = 1;
            for (int j = 0; j < times.length; j++) {

                if (j < times.length - 1) {
                    if (times[j + 1] - times[j] > 1000 * 60 * 60 * 2)  //two hour
                    {
                        LongRange range = new LongRange(times[j],times[j+1]);
                        list.add(range);
                        System.out.println(c + "  time[j] = " + sf.format(new Date(times[j])) + " to " + "time[j+1] = " + sf.format(new Date(times[j + 1])));
                        c++;
                    }
                }
            }
        }

        System.out.println("list.size() = " + list.size());

        Collections.sort(list,new Comparator<LongRange>() {
            @Override
            public int compare(LongRange o1, LongRange o2) {
                return (o1.getMaximumLong() - o2.getMaximumLong() > 0?1:-1);
            }
        });

        for (int i = 0; i < list.size(); i++) {
            LongRange range = list.get(i);
            System.out.print("start:" + long2Str(range.getMinimumLong()));
            System.out.println("  to: " + long2Str(range.getMaximumLong()));
        }

        List<LongRange> mergeList = new ArrayList<LongRange>();
        for (int i = 0; i < list.size(); i++) {
            LongRange range = list.get(i);
            boolean isMerge = false;
            for (int j = 0; j < mergeList.size(); j++) {
                LongRange longRange = mergeList.get(j);
                if (longRange.overlapsRange(range))
                {
                    //do merge
                    long min = Math.min(range.getMinimumLong(),longRange.getMinimumLong());
                    long max = Math.max(range.getMaximumLong(),longRange.getMaximumLong());
                    mergeList.remove(j);
                    LongRange newRange = new LongRange(min,max);
                    mergeList.add(j,newRange);

                    isMerge = true;
                    break;
                }
            }
            if (!isMerge)
            {
                mergeList.add(range);
            }
        }

        System.out.println("------------------------mergeList = " + mergeList);
        for (int i = 0; i < mergeList.size(); i++) {
            LongRange range = mergeList.get(i);
            System.out.print(i + "  start:" + long2Str(range.getMinimumLong()));
            System.out.println("  to: " + long2Str(range.getMaximumLong()));
        }


    }

    static String long2Str(long time)
    {
        return sf.format(new Date(time));
    }

    @Test
    public void testUsdBased1() throws IOException {
        File dir = new File("../data");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("USD");
            }
        });

        System.out.println("files.length = " + files.length);

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
//            System.out.println("file.getName() = " + file.getName());

            ForexData forexData = ForexData.readFromFile(file);
            System.out.println(i + "  -----file = " + file.getName());

            long[] times = forexData.times;

            int c = 1;
            double rate = 0;
            double maxDiff = 0;
            long maxDiffStartTime = 0;
            long maxDiffEndTime = 0;
            for (int j = 0; j < times.length; j++) {
                long time1 = times[j];

                for (int k = j; k < times.length; k++) {
                    long time2 = times[k];

                    if (time2 - time1 < 1000 * 60 * 60) //10 min
                    {
                        double diff = Math.abs(forexData.rates[j][0] - forexData.rates[k][0]);
                        if (diff > maxDiff) {
                            rate = forexData.rates[j][0];
                            maxDiff = diff;
                            maxDiffStartTime = time1;
                            maxDiffEndTime = time2;
                        }
                    }
                    else
                        break;
                }
            }
            System.out.println("maxDiffStartTime = " + sf.format(new Date(maxDiffStartTime)));
            System.out.println("maxDiffEndTime = " + sf.format(new Date(maxDiffEndTime)));
            System.out.println("rate = " + rate);
            System.out.println("maxDiff = " + maxDiff);
            System.out.println("maxDiff/rate = " + maxDiff / rate);
        }
    }


    public List<LongRange> genTimeRangList(long[] times, long maxInterval) {
        List<LongRange> ret = new ArrayList<LongRange>();
        boolean isStart = true;
        for (int i = 0; i < times.length; i++) {
            long time = times[i];
        }
        return ret;
    }

    @Test
    public void testLongRange() {
        LongRange range = new LongRange(100, 200);
        range.getMaximumLong();
        range.getMaximumLong();
        System.out.println("range = " + range);
    }

    @Test
    public void testNorm() throws IOException {
        String fileName = "../data/USD_HKD.csv";

        ForexData forexData = ForexData.readFromFile(fileName);

        System.out.println("forexData.times[0] = " + new Date(forexData.times[0]));
        System.out.println("forexData.times[0] = " + new Date(forexData.times[forexData.times.length - 1]));

        String start = "2012-01-02 02:10:00";
        String end = "2012-12-31 17:00:00";

        long startTime = Timestamp.valueOf(start).getTime();
        long endTime = Timestamp.valueOf(end).getTime();

        ForexData fullData = forexData.fillMinuteData(startTime, endTime);
        System.out.println("fullData.times.length = " + fullData.times.length);
        System.out.println("fullData = " + fullData);

        ForexData forexData1 = fullData.reduceByMinutes(10);
        System.out.println("forexData1 = " + forexData1);
        System.out.println("forexData1 = " + forexData1.toCsv());
    }

    @Test
    public void testReadTimeRangeToRemove() throws IOException, ParseException {
        List<LongRange> longRanges = readTimeRangeToRemove(new File("data/currency/timeRangeToRemove.txt"));
        for (int i = 0; i < longRanges.size(); i++) {
            LongRange range = longRanges.get(i);
            System.out.print("start:" + long2Str(range.getMinimumLong()));
            System.out.println("  to: " + long2Str(range.getMaximumLong()));
        }
    }

    public List<LongRange> readTimeRangeToRemove(File file) throws IOException, ParseException {
        List<LongRange> ret = new ArrayList<LongRange>();
        List list = FileUtils.readLines(file);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            String[] split = s.split(",");
            long start = df.parse(split[0]).getTime();
            long end = df.parse(split[1]).getTime();
            LongRange range = new LongRange(start,end);
            ret.add(range);
        }

        return ret;
    }

    @Test
    public void testSaveFile() throws IOException, ParseException {
        File file = new File("../data/USD_CAD.csv");
        ForexData forexData = ForexData.readFromFile(file);
        System.out.println("forexData = " + forexData.times.length);
        String start = "2012-01-02 03:00:00";
        String end = "2012-12-31 17:00:00";
        long startTime = Timestamp.valueOf(start).getTime();
        long endTime = Timestamp.valueOf(end).getTime();
        ForexData forexData1 = forexData.fillMinuteData(startTime,endTime);
        System.out.println("forexData1.times.length = " + forexData1.times.length);
        forexData1.saveToFile("data/currency/" + file.getName());

        List<LongRange> longRanges = readTimeRangeToRemove(new File("data/currency/timeRangeToRemove.txt"));
        ForexData forexData2 = forexData1.reduceByMinutes(10).removeByTimeRanges(longRanges);
        System.out.println("forexData2.times.length = " + forexData2.times.length);

        forexData2.saveToFile("data/currency/10_" + file.getName());
    }

    @Test
    public void testSaveAllUsdFiles() throws IOException, ParseException {
        File dir = new File("data/min1_forex2012");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("USD");
            }
        });

        System.out.println("files.length = " + files.length);

        String start = "2012-01-02 03:00:00";
        String end = "2012-12-31 17:00:00";
        long startTime = Timestamp.valueOf(start).getTime();
        long endTime = Timestamp.valueOf(end).getTime();
        List<LongRange> longRanges = readTimeRangeToRemove(new File("data/currency/timeRangeToRemove.txt"));

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println("file.getName() = " + file.getName());

            ForexData forexData = ForexData.readFromFile(file);
            System.out.println("forexData = " + forexData.times.length);
            ForexData forexData1 = forexData.fillMinuteData(startTime,endTime);
            System.out.println("forexData1.times.length = " + forexData1.times.length);
            ForexData forexData2 = forexData1.reduceByMinutes(5).removeByTimeRanges(longRanges);  //sample by every 60 minutes,and remove weekend
            System.out.println("forexData2.times.length = " + forexData2.times.length);
            //do normalize
            //rate[i][0] is the origin : mean value of start value
            //rate[i][1] is the normalize value of origin
            //get the mean and standard and deviation
            DescriptiveStatistics ds = new DescriptiveStatistics();
            for (int j =0; j < forexData2.times.length; j ++)
            {
                ds.addValue(forexData2.rates[j][0]);
            }

            for (int j =0; j < forexData2.times.length; j ++)
            {
                forexData2.rates[j][1] = (forexData2.rates[j][0] - ds.getMean())/ds.getStandardDeviation();
            }

            forexData2.saveToFile("data/currency/5_" + file.getName());
        }
    }
}
