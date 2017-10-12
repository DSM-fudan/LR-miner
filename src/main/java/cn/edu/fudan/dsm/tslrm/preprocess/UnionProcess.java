package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class UnionProcess {
	
	public static void main(String[] args) throws Exception{
		String dirBase = "E:\\Chinaclimate1951-2012\\1951.1-2012.12";
		String unionFileFilter = "SURF_CLI_CHN_MUL_DAY-PRS";
		String stationName = "E:\\SURF_CLI_CHN_MUL_DAY-PRS_1960_1_1";
		File dir = new File(dirBase);
		String[] fileNames = dir.list(new ClimateFileNameFilter(unionFileFilter));
		BufferedReader stationNameReader = new BufferedReader(new FileReader(stationName));
		stationNameReader.readLine();
		stationNameReader.readLine();
		String stationInfo = null;
		while((stationInfo = stationNameReader.readLine()) != null){
			String[] infos = stationInfo.split(" ");
			String stationId = infos[0];
			PrintWriter pw = new PrintWriter(new FileWriter("E:\\climateById" + "\\" + stationId + "-1960-2012"));
			for(int i = 0; i < fileNames.length; i++){
				String curFileName = fileNames[i];
				String[] curFileSplits = curFileName.split("-");
				if(curFileSplits[curFileSplits.length - 1].compareTo("196001") < 0){
					continue;
				}
				File tempFile = new File(dir, fileNames[i]);
				BufferedReader reader =  new BufferedReader(new FileReader(tempFile));
				String tempLine = null;
				while((tempLine = reader.readLine()) != null){
					String[] tempStrs = tempLine.split(" ");
					String[] newTempStrs = new String[13];
					int newNum = 0;
					for(int j = 0; j < tempStrs.length; j++){
						if(!tempStrs[j].equals("")){
							newTempStrs[newNum] = tempStrs[j];
							newNum++;
						}
					}
					if(newTempStrs[0].equals(stationId)){
						String printLine = newTempStrs[0] + " " + newTempStrs[4] + " " + newTempStrs[5] + " " + newTempStrs[6]
								+ " " + newTempStrs[7] + " " + newTempStrs[8] + " " + newTempStrs[9];
						pw.println(printLine);
					}
				}
				reader.close();
			}
			pw.close();
		}
		System.out.println("FINISHED!!!");
	}

}
