package cn.edu.fudan.dsm.expriment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;

import math.geom2d.Point2D;

public class TemMining {
	public static void main(String[] args) throws Exception{
//		int[] type = {2, 3, 8, 9, 11};
//		int baseType = 2;
		TemMining tm = new TemMining();
		double errorBound = 0.1;
		double error = 0.05;
		String baseStation = "50788";
		tm.miningAllWithPointBasedOptiLength(baseStation, errorBound, error, 365);
		tm.miningAllWithPointBasedOptiLength(baseStation, errorBound, error, 730);
		System.out.println("Finished!");
	}
	
	public void miningWithPointBasedOpti(int[] type, int baseType, double errorBound, double error) throws Exception{
		String sBaseType = Integer.toString(baseType);
		String[] sType = new String[type.length];
		for(int i = 0; i < sType.length; i++){
			sType[i] = Integer.toString(type[i]);
		}
		
		String typeFile = "D:\\data\\KMeans_ResultByType(k=20).txt";
		String stationinfoFile = "D:\\data\\climateByType\\experiment_GST_Equal_row_1960-2012.txt";
		String resultFile = "data/experiments/miningresult/GST_Equal_errorBound="+ errorBound + "error=" + error;
		String descFile = "data/experiments/miningresult/GST_Equal_errorBound="+ errorBound + "error=" + error + "_desc" + ".txt";
		PrintWriter descPW = new PrintWriter(new FileWriter(descFile));		
		Map<String, String[]> typeToStation = new HashMap<String, String[]>();
		BufferedReader typeBr = new BufferedReader(new FileReader(typeFile));
		
		String tempTypeInfo = null;
		int typeLineNum = 0;
		while((tempTypeInfo = typeBr.readLine()) != null){
			String key = "" + typeLineNum;
			typeToStation.put(key, tempTypeInfo.split("\t"));
			typeLineNum ++;
		}
		
		//find based station
		double[] basePointX = null;
		BufferedReader stationBr = new BufferedReader(new FileReader(stationinfoFile));
		String baseStation = typeToStation.get(sBaseType)[0];
		String stationInfoFile = null;
		while((stationInfoFile = stationBr.readLine()) != null){
			String[] sInfo = stationInfoFile.split("\t");
			if(sInfo[0].equals(baseStation)){
				basePointX = new double[sInfo.length - 1];
				for(int i = 0; i < basePointX.length; i++){
					basePointX[i] = Double.parseDouble(sInfo[i + 1]);
				}
			}
		}		
		descPW.println("BaseStationInfo:");
		descPW.println("StationID: " + baseStation + "typeID: " + baseType);
		descPW.println("***************************************");
		
		double[] corrPointY = null;
		int lineNum = 0;
		for(int i = 0; i < sType.length; i++){
			String corrStation1 = null;
			String corrStation2 = null;
			if(sType[i].equals(sBaseType)){
				corrStation1 = typeToStation.get(sType[i])[1];
				corrStation2 = typeToStation.get(sType[i])[2];
			}else{
				corrStation1 = typeToStation.get(sType[i])[3];
				corrStation2 = typeToStation.get(sType[i])[4];
			}
			stationInfoFile = null;
			stationBr.close();
			stationBr = new BufferedReader(new FileReader(stationinfoFile));
			while((stationInfoFile = stationBr.readLine()) != null){
				String[] sInfo = stationInfoFile.split("\t");
				if(sInfo[0].equals(corrStation1) || sInfo[0].equals(corrStation2)){
					corrPointY = new double[sInfo.length - 1];
					for(int j = 0; j < corrPointY.length; j++){
						corrPointY[j] = Double.parseDouble(sInfo[j + 1]);
					}
					if(corrPointY.length != basePointX.length){
						System.out.println("corrPointY doesn't equal basePointX" + "now Station is " + sInfo[0]);
					}
					Point2D[] point2Ds = new Point2D[corrPointY.length];
					for(int k = 0; k < point2Ds.length; k++){
						point2Ds[k] = new Point2D(basePointX[k], corrPointY[k]);
					}
					
					TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
			        miner.process();          
			        List<PLASegment> segs = miner.buildSpecificSegments(3);
			        
			        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
			        plaRegionSearch.errorBound = errorBound;
			        
			        for(int j = segs.size() - 1; j >= 0; j--){
			          	if(segs.get(j).getPolygonKB().getRings().size() > 1){
			          		segs.remove(j);
			          		System.out.println("Remove at " + j);
			          	}
			          }
			        
			        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
			        descPW.println("Correlate StationInfo:");
			        double k = point2Ds1.getX();
			        double b = point2Ds1.getY();
			        descPW.println("FileNum = " + lineNum + " StationID: " + sInfo[0] + " TypeID: " + sType[i] + " k = " + k + " b = " + b
			        		+ " RealLength = " + plaRegionSearch.finalLength + " Upbound = " + plaRegionSearch.maxUpBound);
			        descPW.println("*******************************");
			        lineNum++;
			        
			        // print the correlation information
			        String tempResultFile = resultFile + "_" + sInfo[0] + " FileNum=" + lineNum + ".txt";
			        PrintWriter resultPW = new PrintWriter(new FileWriter(tempResultFile));
			        int length = point2Ds.length;			        			       
			        for(int j = 0; j < length; j++){
			        	double estimateY = k * point2Ds[j].getX() + b;
						if(Math.abs(estimateY - point2Ds[j].getY()) < errorBound){
							resultPW.println("1");						
						}else{
							resultPW.println("0");
						}
			        }
			        resultPW.close();			        			        
			        System.out.println(lineNum + "correlation computes has been finished!");			        
				}
			}
		}
		descPW.close();		
		typeBr.close();
		stationBr.close();
	}
	
	public void miningAllWithPointBasedOpti(String baseStation, double errorBound, double error) throws Exception{
		String typeFile = "D:\\data\\KMeans_ResultByType(k=20).txt";
		String locationFile = "D:\\data\\StationLocationInfo.txt";
		String stationinfoFile = "D:\\data\\climateByType\\experiment_GST_Equal_row_1960-2012.txt";
		String resultFile = "D:\\data\\climatemining\\GST_Equal_errorBound="+ errorBound + "error=" + error;
		String descFile = "D:\\data\\climatemining\\GST_Equal_errorBound="+ errorBound + "error=" + error + "_desc" + ".txt";
		String baseDesc = "D:\\data\\climatemining\\GST_Equal_errorBound="+ errorBound + "error=" + error + "baseStation = " + baseStation + "_desc" + ".txt";
		
		PrintWriter descPW = new PrintWriter(new FileWriter(descFile));
		Map<String, String> stationToType = new HashMap<String, String>();
		Map<String, String> stationToWDu = new HashMap<String, String>();
		Map<String, String> stationToJDu = new HashMap<String, String>();
		Map<String, String> stationToGDu = new HashMap<String, String>();
		
		String descHead = "FileNum\t" +  "StationID\t" + "Type\t" + "latitude\t" + "longitude\t" + "High\t" + 
							"k\t" + "b\t" + "FinalLength\t" + "FinalLength";
		descPW.println(descHead);
		//get the map of Station to Type
		BufferedReader typeBr = new BufferedReader(new FileReader(typeFile));	
		String tempTypeInfo = null;
		int typeLineNum = 0;
		while((tempTypeInfo = typeBr.readLine()) != null){
			String[] lineStation = tempTypeInfo.split("\t");
			String type = "" + typeLineNum;
			for(int i = 0; i < lineStation.length; i++){
				stationToType.put(lineStation[i], type);
			}
			typeLineNum ++;
		}
		typeBr.close();
		
		//get location of each station
		BufferedReader locationBr = new BufferedReader(new FileReader(locationFile));
		String tempLoc = null;
		while((tempLoc = locationBr.readLine()) != null){
			String[] lineLoc = tempLoc.split("\t");
			stationToWDu.put(lineLoc[0], lineLoc[1]);
			stationToJDu.put(lineLoc[0], lineLoc[2]);
			stationToGDu.put(lineLoc[0], lineLoc[3]);
		}
		locationBr.close();
		
		//get the description information of base station
		PrintWriter baseDescPW = new PrintWriter(new FileWriter(baseDesc));
		String baseType = stationToType.get(baseStation);
		String baseWdu = stationToWDu.get(baseStation);
		String baseJdu = stationToJDu.get(baseStation);
		String baseGdu = stationToGDu.get(baseStation);
		baseDescPW.println("StationId = " + baseStation + " type = " + baseType + " latitude = " + baseWdu + 
				" longitude = " + baseJdu + " High = " + baseGdu);
		baseDescPW.close();
		double[] basePointX = null;
		BufferedReader stationBr = new BufferedReader(new FileReader(stationinfoFile));		
		String stationInfoFile = null;
		while((stationInfoFile = stationBr.readLine()) != null){
			String[] sInfo = stationInfoFile.split("\t");
			if(sInfo[0].equals(baseStation)){
				basePointX = new double[sInfo.length - 1];
				for(int i = 0; i < basePointX.length; i++){
					basePointX[i] = Double.parseDouble(sInfo[i + 1]);
				}
			}
		}
		stationBr.close();
		
		//compute the correlation for each station based on base station
		BufferedReader temBr = new BufferedReader(new FileReader(stationinfoFile));
		double[] corrPointY = null;
		String tempTem = null;
		int lineNum = 1;
		while((tempTem = temBr.readLine()) != null){
			if(lineNum > 24){
				lineNum ++;
				continue;
			}
			
			String[] sInfo = tempTem.split("\t");
			corrPointY = new double[sInfo.length - 1];
			for(int i = 0; i < corrPointY.length; i++){
				corrPointY[i] = Double.parseDouble(sInfo[i + 1]);
			}
			if(corrPointY.length != basePointX.length){
				System.out.println("corrPointY doesn't equal basePointX" + "now Station is " + sInfo[0]);
				continue;
			}
			
			Point2D[] point2Ds = new Point2D[corrPointY.length];
			for(int k = 0; k < point2Ds.length; k++){
				point2Ds[k] = new Point2D(basePointX[k], corrPointY[k]);
			}
			
			TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	        miner.process();          
	        List<PLASegment> segs = miner.buildSpecificSegments(3);
	        
	        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
	        plaRegionSearch.errorBound = errorBound;
	        
	        for(int j = segs.size() - 1; j >= 0; j--){
	          	if(segs.get(j).getPolygonKB().getRings().size() > 1){
	          		segs.remove(j);
	          		System.out.println("Remove at " + j);
	          	}
	          }
	        
	        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
	        double k = point2Ds1.getX();
	        double b = point2Ds1.getY();
	        String corrType = stationToType.get(sInfo[0]);
			String corrWDu = stationToWDu.get(sInfo[0]);
			String corrJDu = stationToJDu.get(sInfo[0]);
			String corrGDu = stationToGDu.get(sInfo[0]);
	        String corrInfo = "" + lineNum + "\t" + sInfo[0] + "\t" + corrType + "\t" + corrWDu + "\t" + corrJDu +
	        					"\t" + corrGDu + "\t" + k + "\t" + b + "\t" + plaRegionSearch.finalLength + "\t" + plaRegionSearch.maxUpBound;
	        descPW.println(corrInfo);
	        descPW.flush();
	        String tempResultFile = resultFile + "_" + sInfo[0] + " FileNum=" + lineNum + ".txt";
	        //print 0-1 file
	        printResultFile(tempResultFile, point2Ds, k, b, errorBound);
	        System.out.println(lineNum + " has been finished!");
	        lineNum ++;	        
		}
		temBr.close();
		descPW.close();
		
	}
	
	public void miningAllWithPointBasedOptiLength(String baseStation, double errorBound, double error, int length) throws Exception{
		String stationinfoFile = "D:\\data\\climatemining\\test\\experiment_GST_Equal_nomalized_purified_1960-2012.txt";
		String resultFile = "D:\\data\\climatemining\\" + length + "\\GST_Equal_errorBound="+ errorBound + "error=" + error;
		String descFile = "D:\\data\\climatemining\\" + length + "\\GST_Equal_errorBound="+ errorBound + "error=" + error + "_desc" + ".txt";
		String corrFile = "D:\\data\\climatemining\\test\\correlationwith_50788_" + length + ".txt";
		BufferedReader corrBr = new BufferedReader(new FileReader(corrFile));
		Map<String, String> stationToCorr = new HashMap<String, String>();
		String tempCorr = null;
		while((tempCorr = corrBr.readLine()) != null){
			String[] corrStrs = tempCorr.split("\t");
			stationToCorr.put(corrStrs[0], corrStrs[1]);
		}
		corrBr.close();
		PrintWriter descPW = new PrintWriter(new FileWriter(descFile));
		
		String descHead = "FileNum\t" +  "StationID\t" + "k\t" + "b\t" + "FinalLength\t" + "UB\t" + "Correlation";
		descPW.println(descHead);
		
		double[] basePointX = null;
		BufferedReader stationBr = new BufferedReader(new FileReader(stationinfoFile));		
		String stationInfoFile = null;
		while((stationInfoFile = stationBr.readLine()) != null){
			String[] sInfo = stationInfoFile.split("\t");
			if(sInfo[0].equals(baseStation)){
				basePointX = new double[length];
				for(int i = 0; i < basePointX.length; i++){
					basePointX[i] = Double.parseDouble(sInfo[i + 1]);
				}
			}
		}
		stationBr.close();
		
		//compute the correlation for each station based on base station
		BufferedReader temBr = new BufferedReader(new FileReader(stationinfoFile));
		double[] corrPointY = null;
		String tempTem = null;
		int lineNum = 1;
		while((tempTem = temBr.readLine()) != null){
			
			String[] sInfo = tempTem.split("\t");
			corrPointY = new double[length];
			for(int i = 0; i < corrPointY.length; i++){
				corrPointY[i] = Double.parseDouble(sInfo[i + 1]);
			}
			if(corrPointY.length != basePointX.length){
				System.out.println("corrPointY doesn't equal basePointX" + "now Station is " + sInfo[0]);
				continue;
			}
			
			Point2D[] point2Ds = new Point2D[corrPointY.length];
			for(int k = 0; k < point2Ds.length; k++){
				point2Ds[k] = new Point2D(basePointX[k], corrPointY[k]);
			}
			
			TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
	        miner.process();          
	        List<PLASegment> segs = miner.buildSpecificSegments(3);
	        
	        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
	        plaRegionSearch.errorBound = errorBound;
	        
	        for(int j = segs.size() - 1; j >= 0; j--){
	          	if(segs.get(j).getPolygonKB().getRings().size() > 1){
	          		segs.remove(j);
	          		System.out.println("Remove at " + j);
	          	}
	          }
	        
	        Point2D point2Ds1 = plaRegionSearch.searchByBox2DWithInside(segs, error);
	        double k = point2Ds1.getX();
	        double b = point2Ds1.getY();
	        String corrInfo = "" + lineNum + "\t" + sInfo[0] + "\t" + k + "\t" + b + "\t" + plaRegionSearch.finalLength + "\t" 
	        					+ plaRegionSearch.maxUpBound + "\t" + stationToCorr.get(sInfo[0]);
	        descPW.println(corrInfo);
	        descPW.flush();
//	        String tempResultFile = resultFile + "_" + sInfo[0] + " FileNum=" + lineNum + ".txt";
	        //print 0-1 file
//	        printResultFile(tempResultFile, point2Ds, k, b, errorBound);
	        System.out.println(lineNum + " has been finished!");
	        lineNum ++;	        
		}
		temBr.close();
		descPW.close();
		
	}
	
	public void printResultFile(String outFile, Point2D[] points, double k, double b, double errorBound) throws Exception{
		int consectiveNum = 0;
		PrintWriter pw = new PrintWriter(new FileWriter(outFile));
		for(int i = 0; i < points.length; i++){
			double x = points[i].getX();
			double y = points[i].getY();
			double estimateY = k * x + b;
			if(Math.abs(estimateY - y) < errorBound){
				consectiveNum ++;						
			}else{
				pw.println("0");
				if(consectiveNum >= 3){
					while(consectiveNum > 0){
						pw.println("1");
						consectiveNum --;
					}
				}else{
					while(consectiveNum > 0){
						pw.println("0");
						consectiveNum --;
					}
				}
			}
		}
		if(consectiveNum >= 3){
			while(consectiveNum > 0){
				pw.println("1");
				consectiveNum --;
			}
		}else{
			while(consectiveNum > 0){
				pw.println("0");
				consectiveNum --;
			}
		}
		pw.close();
	}
}
