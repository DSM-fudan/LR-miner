package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class UnionProcess3 {
	/*
	 * 根据UnionProcess2处理后生成的气象文件，对于同一种气象类型，将所有的气象站整理到一个文件中
	 * 输出的文件格式为：year,month,day,every station's record split by tab
	 */
	public static void main(String[] args) throws Exception{
		String dirBase = "E:\\climateById\\";
		String priBase = "E:\\climateByType\\";
		String recordType = "SURF_CLI_CHN_MUL_DAY-GST";
		String stationName = "E:\\SURF_CLI_CHN_MUL_DAY-GST_1960_1_1";
		int stationNum = 703;
		BufferedReader brStation = new BufferedReader(new FileReader(stationName));
		brStation.readLine();
		brStation.readLine();
		String[] stationID = new String[stationNum];
		String stationInfo = null;
		int idNum = 0;
		while((stationInfo = brStation.readLine()) != null){
			String[] infos = stationInfo.split(" ");
			stationID[idNum] = infos[0];
			idNum++;
		}
		BufferedReader[] br = new BufferedReader[stationNum];
		for(int i = 0; i < stationNum; i++){
			br[i] = new BufferedReader(new FileReader(dirBase + recordType + "_" + stationID[i] + "-1960-2012.txt"));
		}
		PrintWriter pw = new PrintWriter(new FileWriter(priBase + recordType + "-1960-2012.txt"));
		String firstLine = "";
		firstLine = firstLine + "year\t" + "month\t" + "day";
		for(int i = 0; i < stationNum; i++){
			firstLine += "\t" + stationID[i] + "_equal GST" + "\t" + stationID[i] + "_max GST" + "\t" + stationID[i] + "_min GST";
		}
		pw.println(firstLine);
		String station1 = null;
		int printNow = 0;
		while((station1 = br[0].readLine()) != null){
			String[] stationStrs1 = station1.split(" ");
			String printInfo = stationStrs1[1];
			for(int i = 2; i < stationStrs1.length; i++){
				printInfo += "\t" + stationStrs1[i];
			}
			for(int i = 1; i < stationNum; i++){
				String tempInfo = br[i].readLine();
				String[] tempStrs = tempInfo.split(" ");
				for(int j = 4; j < tempStrs.length; j++){
					printInfo += "\t" + tempStrs[j];
				}
			}
			pw.println(printInfo);
			System.out.println("print Now = " + printNow);
			printNow++;
		}
		for(int i = 0; i < stationNum; i++){
			br[i].close();
		}
		pw.close();
		System.out.println("Finished");
	}

}
