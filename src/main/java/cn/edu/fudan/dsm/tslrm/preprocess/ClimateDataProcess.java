package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

public class ClimateDataProcess {
	
	public static void main(String[] args) throws Exception{
		String dirName = "E:\\Chinaclimate1951-2012\\1951.1-2012.12";
		String testFileName = "SURF_CLI_CHN_MUL_DAY-TEM-12001-195101.TXT";
		File climateDir = new File(dirName);
		String[] fileNames = climateDir.list(new ClimateFileNameFilter("SURF_CLI_CHN_MUL_DAY-TEM"));
		
//		for(int i = 0; i < fileNames.length; i++){
//			System.out.println(fileNames[i]);
//		}
		
		File testFile = new File(climateDir, testFileName);
		BufferedReader reader = new BufferedReader(new FileReader(testFile));
		String line = reader.readLine();
		String[] strs = line.split(" ");
		System.out.println(line);
		for(int i = 0; i < strs.length; i++){
			if(!strs[i].equals("")){
				System.out.println("&" + strs[i] + "&");
			}
			
		}
		
		System.out.println("Finished!");
	}

}
