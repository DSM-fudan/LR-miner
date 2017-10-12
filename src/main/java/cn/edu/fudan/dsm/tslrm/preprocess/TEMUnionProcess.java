package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class TEMUnionProcess {
	
	public static void main(String[] args) throws Exception{
		TEMUnionProcess temProcess = new TEMUnionProcess();
		temProcess.getStatisticsByType();
//		List<String> list = temProcess.checkFile();
//		System.out.println("List Size:" + list.size());
//		for(int i = list.size() - 1; i > 0; i--){
//			File tempFile = new File(list.get(i));
//			System.out.println(tempFile.delete());
//		}
//		System.out.println(temProcess.tempPre("-1"));
//		temProcess.unionAllTem();
		System.out.println("Finished!!!");
	}

	public void unionAllTem() throws IOException{
		String dirBase = "D:\\data\\climateById\\";
		String priBase = "D:\\data\\";
		String recordType = "SURF_CLI_CHN_MUL_DAY-TEM";
		String stationName = "D:\\data\\stationIDwithfullRecord_1960_1_1";
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
		brStation.close();
		BufferedReader[] br = new BufferedReader[stationNum];
		for(int i = 0; i < stationNum; i++){
			br[i] = new BufferedReader(new FileReader(dirBase + recordType + "_" + stationID[i] + "-1960-2012.txt"));
		}
		PrintWriter pw = new PrintWriter(new FileWriter(priBase + "TEM" + "-1960-2012.txt"));
//		String line1 = "Identifier";
		
		String tempInfo = null;
//		while((tempInfo = br[0].readLine()) != null){
//			String[] tempStrs = tempInfo.split(" ");
//			line1 += "\t" + tempStrs[1] + "-" + tempStrs[2] + "-" + tempStrs[3];
//		}
//		pw.println(line1);
//		br[0].close();
//		br[0] = new BufferedReader(new FileReader(dirBase + recordType + "_" + stationID[0] + "-1960-2012.txt"));
		
		for(int i = 0; i < br.length; i++){
			tempInfo = null;
			StringBuilder idf1 = new StringBuilder("");
			StringBuilder idf2 = new StringBuilder("");
			StringBuilder idf3 = new StringBuilder("");
			
			tempInfo = br[i].readLine();
			String[] tempStrs = null;
			tempStrs = tempInfo.split(" ");
			idf1.append(tempPre(tempStrs[4]));
			idf2.append(tempPre(tempStrs[5]));
			idf3.append(tempPre(tempStrs[6]));
			
			while((tempInfo = br[i].readLine()) != null){
				tempStrs = tempInfo.split(" ");
				idf1.append("\t" + tempPre(tempStrs[4]));
				idf2.append("\t" + tempPre(tempStrs[5]));
				idf3.append("\t" + tempPre(tempStrs[6]));
				
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
	
	public void unionSpecificLengthTEM(int type, int length) throws IOException{
		String fileName = "D:\\data\\TEM-1960-2012.txt";
		String filePrint = "D:\\data\\TEM-Record-1960-2012(Length = " + length + ").txt";
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		PrintWriter pw = new PrintWriter(new FileWriter(filePrint));
		for(int i = 0; i < type; i++){
			br.readLine();
		}
		
		for(int k = 0; k < 2; k++){
			boolean isStop = false;
			int recordNum = 1;
			String tempLine = null;
			StringBuilder tempBuilder = new StringBuilder("");
			while((tempLine = br.readLine()) != null){
				String[] tempRecords = tempLine.split("\t");
				for(int i = 0; i < tempRecords.length; i++){
					if(recordNum == length){
						tempBuilder.append(tempRecords[i]);
						isStop = true;
						break;
					}
					tempBuilder.append(tempRecords[i] + ",");
					recordNum ++;
				}
				br.readLine();
				br.readLine();
				if(isStop){
					break;
				}
			}
			pw.println(tempBuilder);
		}
		br.close();
		pw.close();
	}
	
	public void temPreAfterKMeans(int length) throws IOException{
		String kMeansTypeFile = "D:\\data\\KMeans_ResultByType(k=20).txt";
		String kMeansUnion = "D:\\data\\experiments\\Experiment_Tem_Record_1960-2012(length=" + length + ").txt";
		int recordNum = 0;
		BufferedReader br = new BufferedReader(new FileReader(kMeansTypeFile));
		boolean isStop = false;
		StringBuilder printLine1 = new StringBuilder("");
		StringBuilder printLine2 = new StringBuilder("");
		int fileNum = 0;
		
		for(int i = 0; i < 9; i++){
			br.readLine();
		}
		String tempKResult = br.readLine();
		String[] tempKStrs = tempKResult.split("\t");
		
		while(true){
			String file1 = null;					
			String file2 = null;
			String tempFileName = null;
			File tempFile = null;
			//to find a better combination which will have an appropriate maxLength
			int fileCount = 0;
			while(true){
				tempFileName = "D:\\data\\climateById\\SURF_CLI_CHN_MUL_DAY-TEM_" + tempKStrs[fileNum] + "-1960-2012.txt";
				tempFile = new File(tempFileName);
				if(tempFile.exists()){
					if(fileCount == 0){
						file1 = tempFileName;
						fileCount++;
					}else if(fileCount ==1){
						file2 = tempFileName;
						fileCount++;
						fileNum++;
						break;
					}
				}else{
					System.out.println("FileNotExist");
				}
				fileNum++;
			}
						
			BufferedReader br1 = new BufferedReader(new FileReader(file1));
			BufferedReader br2 = new BufferedReader(new FileReader(file2));
			String tempLine1 = null;
			String tempLine2 = null;
			while((tempLine1 = br1.readLine()) != null){
				tempLine2 = br2.readLine();
				recordNum ++;
				String[] lineStrs1 = tempLine1.split(" ");
				String[] lineStrs2 = tempLine2.split(" ");
				if(recordNum == length){
					printLine1.append(tempPre(lineStrs1[4]));
					printLine2.append(tempPre(lineStrs2[4]));
					isStop = true;
					break;
				}
				printLine1.append(tempPre(lineStrs1[4]) + ",");
				printLine2.append(tempPre(lineStrs2[4]) + ",");
			}
			br1.close();
			br2.close();
			if(isStop){
				break;
			}
		}
		PrintWriter pw = new PrintWriter(new FileWriter(kMeansUnion));
		pw.println(printLine1.toString());
		pw.println(printLine2.toString());
		pw.close();
		System.out.println("FileNum = " + fileNum);
		System.out.println("RecordNum = " + recordNum);
	}
	
	public List<String> checkFile() throws IOException{
		File directory = new File("D:\\data\\climateById");
		File[] files = directory.listFiles();
		List<String> fileList = new ArrayList<String>();
		for(int i = files.length - 1; i > 0; i--){
			boolean isDelete = false;
			BufferedReader br = new BufferedReader(new FileReader(files[i]));
			String tempStr = null;
			while((tempStr = br.readLine()) != null){
				String[] lineStrs = tempStr.split(" ");
				if((lineStrs.length > 5) && (lineStrs[4].equals("32766"))){
					isDelete = true;
//					System.out.println(files[i].getAbsolutePath());
					break;
				}
			}
			if(isDelete){
				fileList.add(files[i].getAbsolutePath());
			}
			if(i % 100 == 0){
				System.out.println("i = " + i);
			}
		}
		return fileList;
	}
	
	public void TemRhuUnion(int length) throws IOException{
		String kMeansTypeFile = "D:\\data\\KMeans_ResultByType(k=20).txt";
		String outFile = "D:\\data\\experiments\\Experiment_Tem+Rhu_Record_1960-2012(length=" + length + ").txt";
		int recordNum = 0;
		BufferedReader br = new BufferedReader(new FileReader(kMeansTypeFile));
		boolean isStop = false;
		StringBuilder printLine1 = new StringBuilder("");
		StringBuilder printLine2 = new StringBuilder("");
		int fileNum = -1;

		for(int i = 0; i < 8; i++){
			br.readLine();
		}
		String tempKResult = br.readLine();
		String[] tempKStrs = tempKResult.split("\t");
		
		while(true){
			fileNum++;
			String fileName1 = null;					
			String fileName2 = null;
			//to find a better combination which will have an appropriate maxLength
			fileName1 = "D:\\data\\climateById\\SURF_CLI_CHN_MUL_DAY-TEM_" + tempKStrs[fileNum] + "-1960-2012.txt";
			fileName2 = "D:\\data\\climateById\\SURF_CLI_CHN_MUL_DAY-RHU_" + tempKStrs[fileNum] + "-1960-2012.txt";
			File file1 = new File(fileName1);
			File file2 = new File(fileName2);
			if(!file1.exists() || !file2.exists()){
				System.out.println("Continue!!!");
				continue;
			}
			
			BufferedReader br1 = new BufferedReader(new FileReader(file1));
			BufferedReader br2 = new BufferedReader(new FileReader(file2));
			String tempLine1 = null;
			String tempLine2 = null;
			while((tempLine1 = br1.readLine()) != null){
				tempLine2 = br2.readLine();
				recordNum ++;
				String[] lineStrs1 = tempLine1.split(" ");
				String[] lineStrs2 = tempLine2.split(" ");
				if(recordNum == length){
					printLine1.append(tempPre(lineStrs1[4]));
					printLine2.append(lineStrs2[4]);
					isStop = true;
					break;
				}
				printLine1.append(tempPre(lineStrs1[4]) + ",");
				printLine2.append(lineStrs2[4] + ",");
			}
			br1.close();
			br2.close();
			if(isStop){
				break;
			}
		}
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		pw.println(printLine1.toString());
		pw.println(printLine2.toString());
		pw.close();
		System.out.println("RecordNum = " + recordNum);
		System.out.println("FileNum = " + fileNum);
	}
	
	public void TemGstUnion(int length) throws IOException{
		int[] stationIds = {59058, 58457, 58265, 57584, 57461, 56548, 54218, 54094, 53723, 51053};
		String baseDir = "D:\\data\\climateById\\";
		String outFile = "D:\\data\\experiments\\Experiment_Tem+GST_Record_1960-2012(length=" + length + ").txt";
		int fileNum = 0;
		int recordNum = 0;
		StringBuilder printLine1 = new StringBuilder("");
		StringBuilder printLine2 = new StringBuilder("");
		boolean isStop = false;
		while(true){
			String temFile = baseDir + "SURF_CLI_CHN_MUL_DAY-TEM_" + stationIds[fileNum] + "-1960-2012.txt";
			String gstFile = baseDir + "SURF_CLI_CHN_MUL_DAY-GST_" + stationIds[fileNum] + "-1960-2012.txt";
			BufferedReader br1 = new BufferedReader(new FileReader(temFile));
			BufferedReader br2 = new BufferedReader(new FileReader(gstFile));
			String tempLine1 = null;
			String tempLine2 = null;
			while((tempLine1 = br1.readLine()) != null){
				tempLine2 = br2.readLine();
				recordNum ++;
				String[] lineStrs1 = tempLine1.split(" ");
				String[] lineStrs2 = tempLine2.split(" ");
				if(recordNum == length){
					printLine1.append(tempPre(lineStrs1[4]));
					printLine2.append(tempPre(lineStrs2[4]));
					isStop = true;
					break;
				}
				printLine1.append(tempPre(lineStrs1[4]) + ",");
				printLine2.append(tempPre(lineStrs2[4]) + ",");
			}			
			br1.close();
			br2.close();
			if(isStop){
				break;
			}
			fileNum++;
		}
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		pw.println(printLine1.toString());
		pw.println(printLine2.toString());
		System.out.println("FileNum = " + fileNum);
		System.out.println("RecordNum = " + recordNum);
		pw.close();		
	}
	
	public String tempPre(String preTem){
		if(preTem.length() == 1){
			return "0." + preTem;
		}
		if((preTem.length() == 2) && (preTem.charAt(0) == '-')){
			return preTem.charAt(0) + "0." + preTem.charAt(1);
		}
		return preTem.substring(0, preTem.length() - 1) + "." +  preTem.substring(preTem.length() - 1, preTem.length());
	}
	
	//Nomalization
	public void normalizationClimateData() throws IOException{
		String inputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW-1960-2012.txt";
		String outputFile = "D:\\data\\climateByType\\experiment_GST_Equal_row_1960-2012.txt";
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		br.readLine();
		int processedFileNum = 0;
		String tempLine = null;
		while((tempLine = br.readLine()) != null){
			String[] parts = tempLine.split("	");
			if(parts[0].contains("equal")){
				processedFileNum++;
				DescriptiveStatistics ds = new DescriptiveStatistics();
				double[] equalTem = new double[parts.length - 1];
				for(int i = 1; i < parts.length; i++){
					equalTem[i - 1] = Double.parseDouble(tempPre(parts[i]));
					ds.addValue(equalTem[i - 1]);
				}
				StringBuilder pTempLine = new StringBuilder(parts[0].split("equal")[0]);
				for(int i = 0; i < equalTem.length; i++){
					double nValue = (equalTem[i] - ds.getMean()) / ds.getStandardDeviation();
					pTempLine.append("\t" + nValue);
				}
				pw.println(pTempLine);
				System.out.println(processedFileNum + "has processed!");
			}
		}
		br.close();
		pw.close();
	}
	
	public void normalizationClimateData2() throws IOException{
		String inputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW_Equal_Purified-1960-2012.txt";
		String outputFile = "D:\\data\\climatemining\\test\\experiment_GST_Equal_nomalized_purified_1960-2012.txt";
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		br.readLine();
		int processedFileNum = 0;
		String tempLine = null;
		while((tempLine = br.readLine()) != null){
			String[] parts = tempLine.split("	");
				processedFileNum++;
				DescriptiveStatistics ds = new DescriptiveStatistics();
				double[] equalTem = new double[parts.length - 1];
				for(int i = 1; i < parts.length; i++){
					equalTem[i - 1] = Double.parseDouble(tempPre(parts[i]));
					ds.addValue(equalTem[i - 1]);
				}
				StringBuilder pTempLine = new StringBuilder(parts[0]);
				for(int i = 0; i < equalTem.length; i++){
					double nValue = (equalTem[i] - ds.getMean()) / ds.getStandardDeviation();
					pTempLine.append("\t" + nValue);
				}
				pw.println(pTempLine);
				System.out.println(processedFileNum + "has processed!");
		}
		br.close();
		pw.close();
	}
	
	public void kMeansTypeInfo() throws IOException{
		String inputFile1 = "D:\\data\\StationInfoForKMeans.txt";
		String inputFile2 = "D:\\data\\KMeans_ResultByType(k=20).txt";
		String outputFile = "D:\\data\\longitudedimensionalityKMeans_ResultByType(k=20).txt";
		BufferedReader br1 = new BufferedReader(new FileReader(inputFile1));
		BufferedReader br2 = new BufferedReader(new FileReader(inputFile2));
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		Map<String, String> stationToL = new HashMap<String, String>();
		Map<String, String> stationToD = new HashMap<String, String>();
		
		String stationInfo = null;
		while((stationInfo = br1.readLine()) != null){
			String[] sInfos = stationInfo.split("\t");
			stationToL.put(sInfos[0], sInfos[1]);
			stationToD.put(sInfos[0], sInfos[2]);
		}
		
		double typeL = 0;
		double typeD = 0;
		String typeInfo = null;
		int typeNum = 0;
		while((typeInfo = br2.readLine()) != null){
			String[] tInfos = typeInfo.split("\t");
			typeL = 0;
			typeD = 0;
			for(int i = 0; i < tInfos.length; i++){
				typeL += Double.parseDouble(stationToL.get(tInfos[i]));
				typeD += Double.parseDouble(stationToD.get(tInfos[i]));
			}
			typeL = typeL / tInfos.length;
			typeD = typeD / tInfos.length;
			pw.println(typeNum + "\t" + typeL + "\t" + typeD);
			typeNum++;
		}
		br1.close();
		br2.close();
		pw.close();
	}
	
	public void nomalization() throws IOException{
		String inputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW_Equal_Purified-1960-2012.txt";
		String outputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW_Equal_Purified_Nomalized-1960-2012.txt";
		
		String linetemp = null;
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		int lineNum = 1;
		while((linetemp = br.readLine()) != null){
			String[] tempStrs = linetemp.split("\t");
			String stationId = tempStrs[0];
			DescriptiveStatistics ds = new DescriptiveStatistics();
			double[] infos = new double[tempStrs.length - 1];
			for(int i = 0; i < infos.length; i++){
				infos[i] = Double.parseDouble(tempStrs[i + 1]);
				ds.addValue(infos[i]);
			}
			StringBuilder outStr = new StringBuilder(stationId);
			for(int i = 0; i < infos.length; i++){
				double value = (infos[i]- ds.getMean()) / ds.getStandardDeviation();
				outStr.append("\t" + value);
			}
			pw.println(outStr);
			System.out.println(lineNum + " has been processed!");
			lineNum++;
		}
		br.close();
		pw.close();
	}
	
	//get the station information and the output format is "stationId\tlatitude\tlongitude\tHigh\tMean\tStandardDeviation"
	public void getMeansAndDev() throws IOException{
		String inputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW_Equal_Purified-1960-2012.txt";
		String outputFile = "D:\\data\\StationLocMeansStandardDev_equal_purified.txt";
		String locationFile = "D:\\data\\StationLocationInfo.txt";
		Map<String, String> stationToLoc = new HashMap<String, String>();
		
		BufferedReader locBr = new BufferedReader(new FileReader(locationFile));
		String tempLoc = null;
		while((tempLoc = locBr.readLine()) != null){
			int tabIndex = tempLoc.indexOf("\t");
			String stationId = tempLoc.substring(0, tabIndex);
			String locInfo = tempLoc.substring(tabIndex, tempLoc.length());
			stationToLoc.put(stationId, locInfo);
		}
		
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		br.readLine();
		int processedFileNum = 0;
		String tempLine = null;
		while((tempLine = br.readLine()) != null){
			String[] parts = tempLine.split("	");
				processedFileNum++;
				DescriptiveStatistics ds = new DescriptiveStatistics();
				double[] equalTem = new double[parts.length - 1];
				for(int i = 1; i < parts.length; i++){
					equalTem[i - 1] = Double.parseDouble(tempPre(parts[i]));
					ds.addValue(equalTem[i - 1]);
				}
				String stationId = parts[0];				
				StringBuilder pTempLine = new StringBuilder(stationId);
				pTempLine.append(stationToLoc.get(stationId));
				pTempLine.append("\t" + ds.getMean() + "\t" + ds.getStandardDeviation());
				pw.println(pTempLine);
				System.out.println(processedFileNum + "has processed!");
		}
		locBr.close();
		br.close();
		pw.close();
	}
	
	//add correlation information, kmeans cluster information of location, kmeans information of location and mean, deviation
	public void addAdditionMiningToTemMining() throws IOException{
		String kmeans10File = "D:\\data\\climatemining\\test\\typefilternorepair\\typeresult10.txt";
		String kmeans15File = "D:\\data\\climatemining\\test\\typefilternorepair\\typeresult15.txt";
		String kmeans20File = "D:\\data\\climatemining\\test\\typefilternorepair\\typeresult20.txt";
		String temMiningFile = "D:\\data\\climatemining\\test\\GST_Equal_errorBound=0.05error=0.05_full.txt";
		String temMiningResultFile = "D:\\data\\climatemining\\test\\GST_Equal_errorBound=0.05error=0.05_full_3.txt";
		
		Map<String, String> stationToKmeans10 = new HashMap<String, String>();
		Map<String, String> stationToKmeans15 = new HashMap<String, String>();
		Map<String, String> stationToKmeans20 = new HashMap<String, String>();
		
		BufferedReader br10 = new BufferedReader(new FileReader(kmeans10File));
		String tempKmeans10 = null;
		while((tempKmeans10 = br10.readLine()) != null){
			String[] kmeansStr10 = tempKmeans10.split("\t");
			stationToKmeans10.put(kmeansStr10[0], kmeansStr10[1]);
		}
		br10.close();
		
		BufferedReader br15 = new BufferedReader(new FileReader(kmeans15File));
		String tempKmeans15 = null;
		while((tempKmeans15 = br15.readLine()) != null){
			String[] kmeansStr15 = tempKmeans15.split("\t");
			stationToKmeans15.put(kmeansStr15[0], kmeansStr15[1]);
		}
		br15.close();
		
		BufferedReader br20 = new BufferedReader(new FileReader(kmeans20File));
		String tempKmeans20 = null;
		while((tempKmeans20 = br20.readLine()) != null){
			String[] kmeansStr20 = tempKmeans20.split("\t");
			stationToKmeans20.put(kmeansStr20[0], kmeansStr20[1]);
		}
		br20.close();
		
		BufferedReader miningBr = new BufferedReader(new FileReader(temMiningFile));
		PrintWriter pw = new PrintWriter(new FileWriter(temMiningResultFile));
		String tempMining = null;
		tempMining = miningBr.readLine();
		pw.println(tempMining + "\t" + "kmeansnorepair10" + "\t" + "kmeansnorepair15" + "\t" + "kmeansnorepair20");
		while((tempMining = miningBr.readLine()) != null){
			String stationId = tempMining.split("\t")[1];
			String kmeans10 = stationToKmeans10.get(stationId);
			String kmeans15 = stationToKmeans15.get(stationId);
			String kmeans20 = stationToKmeans20.get(stationId);
			if(kmeans10 != null){
				pw.println(tempMining + "\t" + kmeans10 + "\t" + kmeans15 + "\t" + kmeans20);
			}else{
				pw.println(tempMining + "\t" + -1 + "\t" + -1+ "\t" + -1);
			}
			
		}
		miningBr.close();
		pw.close();
	}
	
	public void getStatisticsByType() throws IOException{
		int classNum = 20;
		int typeClum = 12;
		int baseStationtype = 12;
		String inputFile = "D:\\data\\climatemining\\test\\GST_Equal_errorBound=0.05error=0.05_full.txt";
		String corrFile = "D:\\data\\climatemining\\test\\CorrByType.txt";
		String kbFile = "D:\\data\\climatemining\\test\\KBByType.txt";
		String lengthFile = "D:\\data\\climatemining\\test\\LenByType.txt";
		String baseOutput = "D:\\data\\climatemining\\test\\BaseStationTypeAfterMerge.txt";
		String outputFile = "D:\\data\\climatemining\\test\\GST_Equal_errorBound=0.05error=0.05_full.txt_4";
		
		List<int[]> mergeClass = new ArrayList<int[]>();
		int[] merge1 = {3, 7, 14, 15, 19};
		mergeClass.add(merge1);
		int[] merge2 = {5, 11};
		mergeClass.add(merge2);
		int[] merge3 = {6, 16, 20};
		mergeClass.add(merge3);
		
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		PrintWriter corrPW = new PrintWriter(new FileWriter(corrFile));
		PrintWriter kbPW = new PrintWriter(new FileWriter(kbFile));
		PrintWriter lenPW = new PrintWriter(new FileWriter(lengthFile));
		classNum ++;
		int[] typeNum = new int[classNum];
		double[] k = new double[classNum];
		double[] b = new double[classNum];
		double[] corr = new double[classNum];
		int[] len = new int[classNum];
		
		String tempLine = null;
		br.readLine();
		int filterNum = 0;
		while((tempLine = br.readLine()) != null){
			String[] strs = tempLine.split("\t");
			int type = Integer.parseInt(strs[typeClum]);
			if(type < 0){
				filterNum ++;
				continue;
			}
			double tempK = Double.parseDouble(strs[6]);
			double tempB = Double.parseDouble(strs[7]);
			int tempLen = Integer.parseInt(strs[8]);
			double tempCorr = Double.parseDouble(strs[10]);
			
			k[type] += tempK;
			b[type] += tempB;
			corr[type] += tempCorr;
			len[type] += tempLen;
			typeNum[type] += 1;
		}
		String kStr = "";
		String bStr = "";
		String corrStr = "";
		String lenStr = "";
		String typeStr = "";
		for(int i = 0; i < mergeClass.size(); i++){
			int[] typeMerge = mergeClass.get(i);
			for(int j = 1; j < typeMerge.length; j++){
				k[typeMerge[0]] += k[typeMerge[j]];
				k[typeMerge[j]] = 0;
				b[typeMerge[0]] += b[typeMerge[j]];
				b[typeMerge[j]] = 0;
				corr[typeMerge[0]] += corr[typeMerge[j]];
				corr[typeMerge[j]] = 0;
				len[typeMerge[0]] += len[typeMerge[j]];
				len[typeMerge[j]] = 0;
				typeNum[typeMerge[0]] += typeNum[typeMerge[j]];
				typeNum[typeMerge[j]] = 0;
			}
		}
		int newType = 0;
		int[] oldToNew = new int[21];
		PrintWriter baseStationDesc = new PrintWriter(new FileWriter(baseOutput));
		for(int i = 1; i < classNum; i++){
			if(typeNum[i] != 0){
				newType++;
				oldToNew[i] = newType;
				typeStr += newType + "\t";
				k[i] = k[i] / typeNum[i];
				b[i] = b[i] / typeNum[i];
				corr[i] = corr[i] / typeNum[i];
				len[i] = len[i] / typeNum[i];
				
				kStr += k[i] + "\t";
				bStr += b[i] + "\t";
				corrStr += corr[i] + "\t";
				lenStr += len[i] + "\t";
				if(i == baseStationtype){
					baseStationDesc.println("50788" + "\t type = " + newType);//程序运行正确的前提是baseStation所属的类没被合并
				}
								
			}			
		}
		for(int i = 0; i < mergeClass.size(); i++){
			int[] typeMerge = mergeClass.get(i);
			for(int j = 1; j < typeMerge.length; j++){
				oldToNew[typeMerge[j]] = typeMerge[0];
			}
		}
		br.close();
		br = new BufferedReader(new FileReader(inputFile));
		PrintWriter outPW = new PrintWriter(new FileWriter(outputFile));
		outPW.println(br.readLine() + "\t" + "kmeansAfterMerge");
		tempLine = null;
		while((tempLine = br.readLine()) != null){
			String[] strs = tempLine.split("\t");
			int oldType = Integer.parseInt(strs[12]);
			outPW.println(tempLine + "\t" + oldToNew[oldType]);
		}
		kbPW.println(typeStr);
		kbPW.println(kStr);
		kbPW.println(bStr);
		corrPW.println(typeStr);
		corrPW.println(corrStr);
		lenPW.println(typeStr);
		lenPW.println(lenStr);
		outPW.close();
		br.close();
		kbPW.close();
		corrPW.close();
		lenPW.close();
		baseStationDesc.close();
		System.out.println("filterNum = " + filterNum);
	}
	
	public void temp() throws IOException{
		String inputFile1 = "D:\\data\\StationLocMeansStandardDev_equal_purified.txt";
		String inputFile2 = "D:\\data\\StationLocMeansStandardDev_equal_nomalized.txt";
		String outputFile = "D:\\data\\StationLocMeansStandardDev_equal_nomalized_norepaired.txt";
		BufferedReader br1 = new BufferedReader(new FileReader(inputFile1));
		Map<String, Boolean> stationIn = new HashMap<String, Boolean>();
		String tempLine = null;
		while((tempLine = br1.readLine()) != null){
			String[] tempStrs = tempLine.split("\t");
			stationIn.put(tempStrs[0], true);
		}
		br1.close();
		BufferedReader br2 = new BufferedReader(new FileReader(inputFile2));
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		tempLine = null;
		while((tempLine = br2.readLine()) != null){
			String[] tempStrs = tempLine.split("\t");
			if(stationIn.get(tempStrs[0]) != null){
				pw.println(tempLine);
			}
		}
		br2.close();
		pw.close();
	}
}
