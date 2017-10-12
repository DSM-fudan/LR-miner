package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class KMeansPre {
	
	public static void main(String[] args) throws IOException{
		String stationFile = "D:\\data\\stationIDwithfullRecord_1960_1_1";
		String fileName = "D:\\data\\2012\\SURF_CLI_CHN_MUL_DAY-TEM-12001-201201.TXT";
		String printFileName = "D:\\data\\StationLocationInfo.txt";
		
		int stationNum = 703;
		BufferedReader brStation = new BufferedReader(new FileReader(stationFile));
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
		brStation.close();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		PrintWriter pw = new PrintWriter(new FileWriter(printFileName));
		
		String lineInfo = null;
		String lastId = "-1";
		while((lineInfo = br.readLine()) != null){
			String[] strs = lineInfo.split(" ");
			String[] newStrs = new String[13];
			int strNum = 0;
			for(int i = 0; i < strs.length; i++){
				if(!strs[i].equals("")){
					newStrs[strNum] = strs[i];
					strNum++;
				}
			}
			if(lastId.equals(newStrs[0])){
				continue;
			}
			
			boolean isIn = false;
			for(int i = 0; i < stationID.length; i ++){
				if(newStrs[0].equals(stationID[i])){
					isIn = true;
					break;
				}
			}
			if(isIn){
				String w1 = newStrs[1].substring(0, newStrs[1].length() - 2);
				String w2 = newStrs[1].substring(newStrs[1].length() - 2, newStrs[1].length());
				double wDu = Double.parseDouble(w1) +  Double.parseDouble(w2)/ 60;				
				String j1 = newStrs[2].substring(0, newStrs[2].length() - 2);
				String j2 = newStrs[2].substring(newStrs[2].length() - 2, newStrs[2].length());
				double jDu = Double.parseDouble(j1) +  Double.parseDouble(j2)/ 60;
				int gDu = Integer.parseInt(newStrs[3]) / 10;
				pw.println(newStrs[0] + "\t" + String.format("%.2f", wDu) + "\t" + String.format("%.2f", jDu) + "\t" + gDu);
			}
			lastId = newStrs[0];
		}
		br.close();
		pw.close();
		System.out.println("Finished!!!");
	}

}
