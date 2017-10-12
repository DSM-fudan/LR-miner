package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class UnionProcess2 {

	/*
	 * 从原始的气象文件中，对于每一个气象站，整理出从1960.1——2012.12月的气象记录
	 */
	public static void main(String[] args) throws Exception{
		String dirBase = "E:\\Chinaclimate1951-2012\\1951.1-2012.12";
		String unionFileFilter = "SURF_CLI_CHN_MUL_DAY-PRS";
		String stationName = "E:\\SURF_CLI_CHN_MUL_DAY-PRS_1960_1_1";
		int stationNum = 703;
		File dir = new File(dirBase);
		String[] fileNames = dir.list(new ClimateFileNameFilter(unionFileFilter));
		BufferedReader stationNameReader = new BufferedReader(new FileReader(stationName));
		stationNameReader.readLine();
		stationNameReader.readLine();
		String stationInfo = null;
		String[] stationID = new String[stationNum];
		int[] records = new int[stationNum];
		int idNum = 0;
		while((stationInfo = stationNameReader.readLine()) != null){
			String[] infos = stationInfo.split(" ");
			stationID[idNum] = infos[0];
			idNum++;
		}
		stationNameReader.close();
		PrintWriter[] pw = new PrintWriter[stationNum];
		for(int i = 0; i < stationNum; i++){
			pw[i] = new PrintWriter(new FileWriter("E:\\climateById" + "\\" + "SURF_CLI_CHN_MUL_DAY-PRS_" + stationID[i] + "-1960-2012.txt"));
		}
		for(int i = 0; i < fileNames.length; i++){
			String curFileName = fileNames[i];
			String[] curFileSplits = curFileName.split("-");
			if(curFileSplits[curFileSplits.length - 1].compareTo("196001") < 0){
				continue;
			}
			File tempFile = new File(dir, fileNames[i]);
			BufferedReader reader =  new BufferedReader(new FileReader(tempFile));
			String lastMonth = "-1";
			int lastLoc = -1;
			String tempLine = null;
			while((tempLine = reader.readLine()) != null){
				String[] tempStrs = tempLine.split(" ");
				String[] newTempStrs = new String[17];
				int newNum = 0;
				for(int j = 0; j < tempStrs.length; j++){
					if(!tempStrs[j].equals("")){
						newTempStrs[newNum] = tempStrs[j];
						newNum++;
					}
				}
				String printLine = newTempStrs[0] + " " + newTempStrs[4] + " " + newTempStrs[5] + " " + newTempStrs[6]
						+ " " + newTempStrs[7] + " " + newTempStrs[8] + " " + newTempStrs[9];
				if(newTempStrs[0].equals(lastMonth)){
					
				}else{
					lastMonth = newTempStrs[0];
					int k = 0;
					for(k = 0; k < stationNum; k++){
						if(stationID[k].equals(newTempStrs[0])){
							break;
						}
					}
					lastLoc = k;
				}
				if(lastLoc != stationNum){
					pw[lastLoc].println(printLine);
					records[lastLoc]++;
				}
			}
			reader.close();
		}
		for(int i = 0; i < stationNum; i++){
			pw[i].close();
		}
		int recordNum = records[0];
		System.out.println("Based Record Number = " + recordNum);
		System.out.println("**************************************");
		for(int i = 1; i < stationNum; i++){
			if(recordNum != records[i]){
				System.out.println("I = " + i);
				System.out.println("RecordNum = " + records[i]);
				System.out.println("******************************");
			}
		}
		
		System.out.println("Finished!!!");
	}
}
