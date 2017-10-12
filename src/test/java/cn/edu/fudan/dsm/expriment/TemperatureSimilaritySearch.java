package cn.edu.fudan.dsm.expriment;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;
import math.geom2d.Point2D;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * Created by wujy on 17-2-11.
 */
public class TemperatureSimilaritySearch {

    private static Logger logger = Logger.getLogger(TemperatureSimilaritySearch.class);

    private static String[][] stations;

    static {
        stations = new String[30][];

        // 0-19: 20 types from `KMeans_ResultByType(k=20).txt`, data of some stations in the list are not exist and will be auto ignored by the program
        stations[0] = new String[]{"57348", "57355", "57358", "57359", "57426", "57439", "57445", "57447", "57458", "57461", "57476", "57523", "57536", "57545", "57554", "57562", "57573", "57574", "57584", "57633", "57642", "57647", "57649", "57655", "57669", "57671", "57673", "57679", "57682", "57741", "57745", "57752", "57758", "57761", "57766", "57774", "57776", "57780", "57853", "57857", "57866", "57872"};
        stations[1] = new String[]{"56079", "56144", "56146", "56167", "56172", "56178", "56182", "56187", "56188", "56196", "56251", "56287", "56374", "56385", "56386", "56462", "56475", "56479", "56485", "56492", "56571", "57211", "57306", "57313", "57405", "57411", "57502", "57512", "57516", "57517", "57604"};
        stations[2] = new String[]{"50788", "50873", "50877", "50888", "50963", "50968", "50973", "50978", "50983", "54094", "54096", "54181", "54186", "54195", "54273", "54276", "54284", "54285", "54286", "54292", "54374", "54386"};
        stations[3] = new String[]{"57696", "57793", "57799", "58519", "58527", "58600", "58606", "58608", "58622", "58626", "58634", "58637", "58705", "58715", "58718", "58725", "58730", "58731", "58734", "58737", "58742", "58744", "58806", "58813", "58818", "58820", "58834", "58837", "58843", "58847", "58918", "58921", "58927", "58931", "58933", "58936", "58944", "59126", "59133", "59134", "59321"};
        stations[4] = new String[]{"53898", "54705", "54725", "54727", "54753", "54808", "54823", "54826", "54836", "54842", "54843", "54863", "54916", "54929", "54936", "54945", "58005", "58015", "58026", "58027", "58034", "58038", "58040", "58047", "58102", "58111", "58118", "58122", "58130", "58135", "58138", "58143", "58150", "58221", "58225"};
        stations[5] = new String[]{"57083", "57089", "57091", "57178", "57181", "57193", "57265", "57278", "57279", "57285", "57290", "57297", "57378", "57381", "57395", "57399", "57482", "57483", "57493", "57494", "57581", "57583", "57595", "57598", "58203", "58208", "58215", "58311", "58314", "58319", "58321", "58402", "58407", "58419", "58424", "58500", "58506", "58507"};
        stations[6] = new String[]{"50353", "50468", "50548", "50557", "50564", "50656", "50658", "50739", "50742", "50745", "50756", "50758", "50774", "50844", "50853", "50854", "50862", "50936", "50945", "50948", "50949", "50953", "50955", "54063", "54064"};
        stations[7] = new String[]{"52118", "52203", "52313", "52323", "52418", "52424", "52436", "52602", "52713", "52737", "52818", "52825", "52836", "52908", "55299", "55578", "55598", "55680", "55696", "55773", "56004", "56018", "56029", "56033", "56034", "56106", "56125", "56137", "56312"};
        stations[8] = new String[]{"51053", "51060", "51068", "51076", "51087", "51133", "51156", "51186", "51232", "51238", "51241", "51243", "51288", "51330", "51334", "51346", "51365", "51379", "51431", "51433", "51437", "51463", "51467", "51470", "51477", "51495", "51526", "51542", "51567", "51573", "51581", "51628", "51633", "51639", "51642", "51644", "51655", "51656", "51701", "51704", "51705", "51711", "51720", "51730", "51765", "51777", "51804", "51811", "51818", "51828", "51839", "51855", "51931"};
        stations[9] = new String[]{"56459", "56533", "56543", "56548", "56565", "56651", "56671", "56684", "56739", "56748", "56751", "56768", "56778", "56786", "56838", "56856", "56875", "56886", "56946", "56951", "56954", "56959", "56964", "56966", "56969", "56977", "56985", "56986", "56991"};
        stations[10] = new String[]{"58236", "58238", "58251", "58255", "58259", "58265", "58326", "58336", "58343", "58345", "58354", "58356", "58358", "58362", "58436", "58437", "58450", "58457", "58464", "58467", "58472", "58520", "58531", "58543", "58549", "58556", "58557", "58562", "58569", "58633", "58646", "58652", "58665", "58666", "58667"};
        stations[11] = new String[]{"52446", "52447", "52495", "52533", "52546", "52576", "52633", "52645", "52652", "52657", "52661", "52674", "52679", "52681", "52754", "52765", "52787", "52797", "52856", "52866", "52868", "52876", "52884", "52895", "52943", "52955", "52974", "52983", "52984", "52986", "52996", "53502", "53519", "53602", "53614", "53615", "53704", "53705", "53806", "53810", "53903", "56043", "56046", "56065", "56080", "56093"};
        stations[12] = new String[]{"56691", "56693", "56793", "57606", "57608", "57614", "57625", "57707", "57710", "57718", "57722", "57729", "57731", "57803", "57806", "57816", "57825", "57827", "57832", "57839", "57840", "57845", "57902", "57906", "57907", "57910", "57912", "57916", "57922", "57926", "57932", "57957", "59023", "59037", "59046", "59209", "59211", "59218", "59228"};
        stations[13] = new String[]{"53723", "53725", "53740", "53754", "53817", "53821", "53845", "53853", "53859", "53868", "53877", "53915", "53923", "53929", "53942", "53948", "53955", "53959", "53963", "53968", "53975", "57025", "57028", "57030", "57034", "57046", "57048", "57051", "57067", "57071", "57077", "57124", "57134", "57143", "57156", "57232", "57237", "57238", "57245", "57251"};
        stations[14] = new String[]{"54026", "54041", "54049", "54134", "54135", "54142", "54157", "54161", "54165", "54226", "54236", "54237", "54243", "54254", "54259", "54260", "54263", "54266", "54324", "54333", "54334", "54335", "54337", "54339", "54342", "54346", "54349", "54351", "54353", "54363", "54365", "54377", "54454", "54455", "54470", "54471", "54472", "54476", "54486", "54493", "54497", "54563", "54579", "54584", "54662", "54776"};
        stations[15] = new String[]{"59058", "59242", "59254", "59265", "59417", "59431", "59446", "59453", "59456", "59462", "59626", "59632", "59644", "59647", "59658", "59663", "59664", "59673", "59754", "59758", "59838", "59849", "59855", "59948", "59954", "59981"};
        stations[16] = new String[]{"53276", "53336", "53352", "53362", "53391", "53446", "53463", "53478", "53480", "53487", "53490", "53513", "53529", "53543", "53564", "53578", "53588", "53593", "53594", "53646", "53651", "53663", "53664", "53673", "53698", "53772", "53775", "53787", "53798", "53863", "53884"};
        stations[17] = new String[]{"50915", "53083", "53192", "53195", "53399", "54012", "54027", "54102", "54115", "54208", "54213", "54218", "54308", "54311", "54326", "54401", "54405", "54406", "54416", "54423", "54429", "54436", "54449", "54452", "54511", "54518", "54525", "54527", "54534", "54539", "54602", "54606", "54623", "54624"};
        stations[18] = new String[]{"50618", "50632", "50639", "50727", "50834", "50838"};
        stations[19] = new String[]{"57779", "57789", "57874", "57889", "57894", "57896", "57965", "57972", "57974", "57993", "57996", "59065", "59072", "59082", "59087", "59088", "59092", "59097", "59102", "59107", "59116", "59117", "59271", "59278", "59280", "59287", "59293", "59294", "59298", "59303", "59304", "59316", "59317", "59324", "59478", "59485", "59493", "59501"};

        // 20: each from a type
        stations[20] = new String[]{"57348", "57355", "50788", "57696", "53898", "57083", "50353", "52118", "51053", "56459", "58236", "52446", "56691", "53723", "54026", "59058", "53276", "50915", "50618", "57779"};
    }

    public static void main(String args[]) throws IOException {
        TemperatureSimilaritySearch tss = new TemperatureSimilaritySearch();
        double errorBound = 0.1;
        double error = 0.05;

        for (int i = 20; i < 21; i++) {
            tss.searchAllWithPointBasedOpti(i, stations[i][0], 19359, errorBound, error, true);
        }
    }

    private void searchAllWithPointBasedOpti(int type, String baseStation, int duration, double errorBound, double error, boolean z_normalization) throws IOException {
        String baseStationFilename = "data/temperature/SURF_CLI_CHN_MUL_DAY-TEM_" + baseStation + "-1960-2012.txt";
        double[] baseStationData = readFromFile(baseStationFilename, duration);

        double[][] allData = new double[stations[type].length][];
        String[] allStations = new String[stations[type].length];
        allData[0] = baseStationData;
        allStations[0] = baseStation;
        int cnt = 1;

        PrintWriter print = new PrintWriter(new File("data/temperature/" + type + "_" + duration + "_result.csv"));
        print.println("Base,Target,Pearson,k,b,Final length,Max up bound");
        for (String targetStation : stations[type]) {
            if (targetStation.equals(baseStation)) continue;
            logger.info(baseStation + "-" + targetStation);

            String targetStationFilename = "data/temperature/SURF_CLI_CHN_MUL_DAY-TEM_" + targetStation + "-1960-2012.txt";
            double[] targetStationData;
            try {
                targetStationData = readFromFile(targetStationFilename, duration);
            } catch (FileNotFoundException e) {
                logger.warn(targetStationFilename + " not found! Ignored.");
                continue;
            }
            allData[cnt] = targetStationData;
            allStations[cnt] = targetStation;
            cnt++;

            Point2D[] point2Ds = Point2DUtils.genFromXY(baseStationData, targetStationData);
            if (z_normalization) {
                point2Ds = Point2DUtils.normalize(point2Ds);
            }

            // Pearson correlation: use Matlab instead
            double pearsonCorrelation = new PearsonsCorrelation().correlation(baseStationData, targetStationData);

            TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
            miner.process();
            List<PLASegment> segs = miner.buildSpecificSegments(3);

            PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
            plaRegionSearch.errorBound = errorBound;
            for (int j = segs.size() - 1; j >= 0; j--) {
                if (segs.get(j).getPolygonKB().getRings().size() > 1) {
                    segs.remove(j);
                    System.out.println("Remove at " + j);
                }
            }

            Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
            double k = point2Ds1.getX();
            double b = point2Ds1.getY();

            //print 0-1 file
            printResultFile("data/temperature/" + baseStation + "_" + targetStation + "_segment.csv", point2Ds, k, b, errorBound);

            print.println(baseStation + "," + targetStation + "," + pearsonCorrelation + "," + k + "," + b + "," + plaRegionSearch.finalLength + "," + plaRegionSearch.maxUpBound);
        }
        print.close();

        // output csv containing all data to calculate Pearson correlation in Matlab
        print = new PrintWriter(new File("data/temperature/" + type + "_" + duration + "_data.csv"));
        for (int i = 0; i < cnt; i++) {
            print.print(allStations[i] + ",");
        }
        print.println();
        for (int i = 0; i < allData[0].length; i++) {
            for (int j = 0; j < cnt; j++) {
                print.print(allData[j][i] + ",");
            }
            print.println();
        }
        print.close();
    }

    private double[] readFromFile(String filename, int length) throws IOException {
        logger.debug("name = " + filename);
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        double[] data = new double[length];
        assert list.size() == 19359;
        for (int i = 19359 - length; i < list.size(); i++) {
            String s = (String) list.get(i);
            String[] strings = s.split(" ");
            data[i - 19359 + length] = Double.parseDouble(strings[5]);
            if (data[i-19359+length] > 500 || data[i-19359+length] < -500) {
                data[i - 19359 + length] = data[i - 19359 + length - 1];
            }
        }
        logger.debug("length = " + data.length);
        return data;
    }

    private void printResultFile(String outFile, Point2D[] points, double k, double b, double errorBound) throws IOException {
        int consecutiveNum = 0;
        PrintWriter pw = new PrintWriter(new FileWriter(outFile));
        for (Point2D point : points) {
            double x = point.getX();
            double y = point.getY();
            double estimateY = k * x + b;
            if (Math.abs(estimateY - y) < errorBound) {
                consecutiveNum++;
            } else {
                pw.println("0");
                if (consecutiveNum >= 3) {
                    while (consecutiveNum > 0) {
                        pw.println("1");
                        consecutiveNum--;
                    }
                } else {
                    while (consecutiveNum > 0) {
                        pw.println("0");
                        consecutiveNum--;
                    }
                }
            }
        }
        if (consecutiveNum >= 3) {
            while (consecutiveNum > 0) {
                pw.println("1");
                consecutiveNum--;
            }
        } else {
            while (consecutiveNum > 0) {
                pw.println("0");
                consecutiveNum--;
            }
        }
        pw.close();
    }

}
