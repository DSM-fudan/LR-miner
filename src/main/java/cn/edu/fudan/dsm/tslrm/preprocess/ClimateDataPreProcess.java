package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ClimateDataPreProcess {
	
	public static void main(String[] args) throws Exception{
		String dirName = "E:\\Chinaclimate1951-2012\\1951.1-2012.12";
		String outFile = "E:\\SURF_CLI_CHN_MUL_DAY-GST_1960_1_1";
		
		File climateDir = new File(dirName);
		String[] fileNames = climateDir.list(new ClimateFileNameFilter("SURF_CLI_CHN_MUL_DAY-GST"));
		
		String lastStation = null;
		List<ClimateInfo> stationInfos = new ArrayList<ClimateInfo>();
		for(int i = 0; i < fileNames.length; i++){
			File curFile = new File(climateDir, fileNames[i]);
			BufferedReader reader = new BufferedReader(new FileReader(curFile));
			String curLine = null;
			while((curLine = reader.readLine()) != null){
				String[] curStrs = curLine.split(" ");
				String[] newStrs = new String[17];
				int strNum = 0;
				for(int j = 0; j < curStrs.length; j++){
					if(!curStrs[j].equals("")){
						newStrs[strNum] = curStrs[j];
						strNum++;
					}
				}
				if(newStrs[4].compareTo("1960") < 0){
					continue;
				}
				
				if(newStrs[5].length() == 1){
					newStrs[5] = "0" + newStrs[5];
				}
				String recordTime = newStrs[4] + " " + newStrs[5] + " " + newStrs[6];
				
				if(lastStation == null){
					ClimateInfo info = new ClimateInfo(newStrs[0], recordTime, recordTime, 1);
					stationInfos.add(info);
					lastStation = newStrs[0];
				}else{
					if(lastStation.equals(newStrs[0])){
						// do nothing
					}else{
						lastStation = newStrs[0];
						boolean isAdd = true;
						for(int k = 0; k < stationInfos.size(); k++){
							ClimateInfo tempInfo = stationInfos.get(k);
							if(tempInfo.getClimateName().equals(newStrs[0])){
								tempInfo.setNum(tempInfo.getNum() + 1);
								if(tempInfo.getStartTime().compareTo(recordTime) > 0){
									tempInfo.setStartTime(recordTime);
								}
								if(tempInfo.getEndTime().compareTo(recordTime) < 0){
									tempInfo.setEndTime(recordTime);
								}
								isAdd = false;
							}
						}
						if(isAdd){
							ClimateInfo info = new ClimateInfo(newStrs[0], recordTime, recordTime, 1);
							stationInfos.add(info);
						}
					}
				}
			}
			reader.close();
		}
		//Sort by the Station occurred numbers
		Collections.sort(stationInfos, new Comparator<ClimateInfo>(){

			public int compare(ClimateInfo o1, ClimateInfo o2) {
				return o2.getNum() - o1.getNum();
			}
			
		});
		
		PrintWriter print = new PrintWriter(new FileWriter(outFile));
		print.println("SURF_CLI_CHN_MUL_DAY-GST");
		print.println("Station Num = " + stationInfos.size());
		for(int i = 0; i < stationInfos.size(); i++){
			print.println(stationInfos.get(i));
		}
		print.close();
		System.out.println("Finished!!!");
	}

}
