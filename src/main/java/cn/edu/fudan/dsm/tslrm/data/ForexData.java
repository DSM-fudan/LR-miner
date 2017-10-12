package cn.edu.fudan.dsm.tslrm.data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-5
 * Time: 下午10:21
 * To change this template use File | Settings | File Templates.
 */
public class ForexData {
    static Logger logger = Logger.getLogger(ForexData.class);

    public String baseCurrency;
    public String secondCurrency;
    public long[] times;
    public double[][] rates;

    public ForexData(String baseCurrency, String secondCurrency, long[] times, double[][] rates) {
        this.baseCurrency = baseCurrency;
        this.secondCurrency = secondCurrency;
        this.times = times;
        this.rates = rates;
    }

    public ForexData() {
    }

    @Override
    public String toString() {
        return "ForexData{" +
                "baseCurrency='" + baseCurrency + '\'' +
                ", secondCurrency='" + secondCurrency + '\'' +
                ", length='" + times.length + '\'' +
                ", times=" + new Timestamp(times[0]) + "," + new Timestamp(times[times.length - 1]) +
                ", rates=" + rates[0][0] + "," + rates[rates.length - 1][0] +
                '}';
    }

    public String toCsv()
    {
//        2012-12-31 15:39:00,7.75010,7.75010,7.75009,7.75009,0
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times.length; i++) {
            long time = times[i];
            sb.append(new Timestamp(time)).append(",");
            for (int j = 0; j < rates[i].length; j++) {
                double v = rates[i][j];
                sb.append(v).append(",");
            }
            sb.append("0").append("\n");
        }

        return sb.toString();
    }

    public static ForexData readFromFile(String fileName) throws IOException {
        //this.fileName = fileName;
        File file = new File(fileName);
        return readFromFile(file);
    }

    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static ForexData readFromFile(File file) throws IOException {
        String name = file.getName();
        logger.debug("name = " + name);
        String[] split = name.split("[.]");
//        logger.debug("split[0] = " + split[0]);

        String[] ss = split[0].split("_");
        ForexData forexData = new ForexData();
        if (!isNumber(ss[0]))
        {
            forexData.baseCurrency = ss[0];
            forexData.secondCurrency = ss[1];
        }
        else
        {
            forexData.baseCurrency = ss[1];
            forexData.secondCurrency = ss[2];
        }
        List list = FileUtils.readLines(file);
        forexData.times = new long[list.size()];
        forexData.rates = new double[list.size()][4];
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            String[] strings = s.split(",");
            try {
                forexData.times[i] = df.parse(strings[0]).getTime();
                forexData.rates[i][0] = Double.parseDouble(strings[1]);
                forexData.rates[i][1] = Double.parseDouble(strings[2]);
                forexData.rates[i][2] = Double.parseDouble(strings[3]);
                forexData.rates[i][3] = Double.parseDouble(strings[4]);
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        logger.debug("forexData.times.length = " + forexData.times.length);

        return forexData;
    }

    private static boolean isNumber(String s) {
        return StringUtils.isNumeric(s);
    }

    static long OneMinute = 60 * 1000;

    public ForexData fillMinuteData(long startTime, long endTime) {
        if (startTime < times[0])
            throw new RuntimeException("the start time should large than " + new Date(times[0]));

        ForexData ret = new ForexData();
        ret.baseCurrency = this.baseCurrency;
        ret.secondCurrency = this.secondCurrency;

        int l = (int) ((endTime - startTime) / OneMinute);
        ret.times = new long[l];
        ret.rates = new double[l][4];
        int c = 0;
        startTime = startTime - (startTime % OneMinute);
        for (int i = 0; i < ret.times.length; i++) {
            ret.times[i] = startTime + i * OneMinute;
            long time = ret.times[i];

            while ((this.times[c] < time) && (c < this.times.length -1) && (this.times[c + 1] <= time)) {
                c = c + 1;
            }
            System.arraycopy(this.rates[c], 0, ret.rates[i], 0, this.rates[c].length);
        }

        return ret;
    }

    public ForexData reduceByMinutes(int minutes) {
        ForexData ret = new ForexData();
        ret.baseCurrency = this.baseCurrency;
        ret.secondCurrency = this.secondCurrency;

        int l = (int) (this.times.length / minutes);
        ret.times = new long[l];
        ret.rates = new double[l][4];
        int c = 0;
        long startTime = times[0];
        for (int i = 0; i < ret.times.length; i++) {
            ret.times[i] = startTime + i * OneMinute * minutes;
//            ret.rates[i][0] = this.rates[i*minutes][0];
            //use average rate
            double sum = 0;
            //sum
            for (int j = i * minutes; j < (i+1)*minutes ; j++)
            {
                sum = sum + this.rates[j][0];
            }
            ret.rates[i][0] = sum/minutes;

            ret.rates[i][1] = this.rates[i*minutes][1];
            ret.rates[i][2] = this.rates[i*minutes][2];
            for (int j = i * minutes; j < (i+1)*minutes ; j++)
            {
                if (ret.rates[i][1] < this.rates[j][1])  //for max
                    ret.rates[i][1] = this.rates[j][1];

                if (ret.rates[i][2] > this.rates[j][2]) //for min
                    ret.rates[i][2] = this.rates[j][2];
            }
            ret.rates[i][3] = this.rates[i*minutes+minutes-1][3];
        }
        return ret;
    }


    public static void main(String[] args) throws IOException {
        ForexData forexData = ForexData.readFromFile("../data/AUD_CAD.csv");
        System.out.println("forexData = " + forexData);

        System.out.println("forexData.rates[0][0] = " + forexData.rates[0][0]);
        System.out.println("forexData.rates[0][1] = " + forexData.rates[0][1]);
        System.out.println("forexData.rates[0][2] = " + forexData.rates[0][2]);
        System.out.println("forexData.rates[0][3] = " + forexData.rates[0][3]);

        System.out.println("forexData.rates[2][0] = " + forexData.rates[2][0]);
        System.out.println("forexData.rates[2][1] = " + forexData.rates[2][1]);
        System.out.println("forexData.rates[2][2] = " + forexData.rates[2][2]);
        System.out.println("forexData.rates[2][3] = " + forexData.rates[2][3]);

        System.out.println("forexData.rates[forexData.rates.length-1][0] = " + forexData.rates[forexData.rates.length - 1][0]);
        System.out.println("forexData.rates[forexData.rates.length-1][1] = " + forexData.rates[forexData.rates.length - 1][1]);
        System.out.println("forexData.rates[forexData.rates.length-1][2] = " + forexData.rates[forexData.rates.length - 1][2]);
        System.out.println("forexData.rates[forexData.rates.length-1][3] = " + forexData.rates[forexData.rates.length - 1][3]);
    }

    public ForexData z_normalize() {
        ForexData ret = new ForexData();
        ret.baseCurrency = this.baseCurrency;
        ret.secondCurrency = this.secondCurrency;
        ret.times = new long[this.times.length];
        ret.rates = new double[this.times.length][4];

        System.arraycopy(this.times,0,ret.times,0,this.times.length);

        for (int i =0; i <4; i++)
        {
            DescriptiveStatistics ds = new DescriptiveStatistics();
            for (int j =0; j < this.times.length; j ++)
            {
                ds.addValue(this.rates[j][i]);
            }

//            logger.debug("ds = " + ds);
//            logger.debug("ds.getMean() = " + ds.getMean());
//            logger.debug("ds.getStandardDeviation() = " + ds.getStandardDeviation());
            for (int j =0; j < this.times.length; j ++)
            {
                ret.rates[j][i]  = (this.rates[j][i] - ds.getMean()) / ds.getStandardDeviation();
            }
        }

        return ret;
    }

    public void saveToFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times.length; i++) {
            long time = times[i];
            sb.append(df.format(new Date(time))).append(",");

            for (int j = 0; j < rates[i].length; j++) {
                double rate = rates[i][j];
                sb.append(rate).append(",");
            }
            sb.append("0\n");
        }
        FileUtils.writeStringToFile(file,sb.toString());
    }

    public void saveToFile(String fileName) throws IOException {
        saveToFile(new File(fileName));
    }

    public ForexData removeByTimeRanges(List<LongRange> rangeList)
    {
        ForexData ret = new ForexData();
        ret.baseCurrency = this.baseCurrency;
        ret.secondCurrency = this.secondCurrency;

        List<Integer> timeList = new ArrayList<Integer>();
        for (int i = 0; i < times.length; i++) {
            long time = times[i];
            boolean ignore = false;
            for (int j = 0; j < rangeList.size(); j++) {
                LongRange range = rangeList.get(j);
                if (range.containsLong(time))
                {
                    ignore = true;
                    break;
                }
            }
            if (!ignore)
            {
                timeList.add(i);
            }
        }

        ret.times = new long[timeList.size()];
        ret.rates = new double[ret.times.length][4];

        int c = 0;
        for (int i = 0; i < timeList.size(); i++) {
            Integer position = timeList.get(i);
            ret.times[c] = this.times[position];
            for (int j = 0; j < ret.rates[c].length; j++) {
                ret.rates[c][j] = this.rates[position][j];
            }
            c++;
        }
        return ret;
    }

    public static ForexData generateUSDFrom(ForexData targetCurrencyData) {
        ForexData baseCurrencyData = new ForexData();
        baseCurrencyData.baseCurrency = baseCurrencyData.secondCurrency = "USD";
        baseCurrencyData.times = new long[targetCurrencyData.times.length];
        baseCurrencyData.rates = new double[targetCurrencyData.times.length][4];
        for (int i = 0; i < targetCurrencyData.times.length; i++) {
            baseCurrencyData.times[i] = targetCurrencyData.times[i];
            baseCurrencyData.rates[i][0] = 1.0;
            baseCurrencyData.rates[i][1] = 0.0;
            baseCurrencyData.rates[i][2] = 1.0;
            baseCurrencyData.rates[i][3] = 1.0;
        }
        return baseCurrencyData;
    }

}
