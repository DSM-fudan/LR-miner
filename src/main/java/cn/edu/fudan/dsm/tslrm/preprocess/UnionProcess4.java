package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class UnionProcess4 {
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
		PrintWriter pw = new PrintWriter(new FileWriter(priBase + recordType + "_ROW-1960-2012.txt"));
		String line1 = "Identifier";
		
		String tempInfo = null;
		while((tempInfo = br[0].readLine()) != null){
			String[] tempStrs = tempInfo.split(" ");
			line1 += "\t" + tempStrs[1] + "-" + tempStrs[2] + "-" + tempStrs[3];
		}
		pw.println(line1);
		br[0].close();
		br[0] = new BufferedReader(new FileReader(dirBase + recordType + "_" + stationID[0] + "-1960-2012.txt"));
		
		for(int i = 0; i < br.length; i++){
			tempInfo = null;
			StringBuilder idf1 = new StringBuilder(stationID[i] + "equal_GST");
			StringBuilder idf2 = new StringBuilder(stationID[i] + "max_GST");
			StringBuilder idf3 = new StringBuilder(stationID[i] + "min_GST");
			
			while((tempInfo = br[i].readLine()) != null){
				String[] tempStrs = tempInfo.split(" ");
				idf1.append("\t" + tempStrs[4]);
				idf2.append("\t" + tempStrs[5]);
				idf3.append("\t" + tempStrs[6]);
				
//				idf1 += "\t" + tempStrs[4];
//				idf2 += "\t" + tempStrs[5];
//				idf3 += "\t" + tempStrs[6];
			}
			pw.println(idf1);
			pw.println(idf2);
			pw.println(idf3);
			
			System.out.println("File Number = " + i + "has been finished!!!");
		}
		
		for(int i = 0; i < stationNum; i++){
			br[i].close();
		}
		pw.close();
		System.out.println("Finished");
	}

}
