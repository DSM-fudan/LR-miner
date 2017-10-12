package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import math.geom2d.Point2D;

import cn.edu.fudan.dsm.tslrm.data.ForexData;
import cn.edu.fudan.dsm.tslrm.data.Point2DUtils;

public class CurrencyPoint {
	
	//The file has two line which come from two currency files.
	public void combineCurPoints(String fileName1, String fileName2, String outFileName, int original) throws IOException{
		ForexData data1 = ForexData.readFromFile(new File(fileName1));
		ForexData data2 = ForexData.readFromFile(new File(fileName2));
		Point2D[] point2Ds = Point2DUtils.genFromForexData(data1, data2, original);
		System.out.println("Point Num:" + point2Ds.length);
		StringBuffer line1 = new StringBuffer();
		StringBuffer line2 = new StringBuffer();
		int length = point2Ds.length;
		for(int i = 0; i < length - 1; i++){
			line1.append(point2Ds[i].getX()).append(",");
			line2.append(point2Ds[i].getY()).append(",");
		}
		line1.append(point2Ds[length - 1].getX());
		line2.append(point2Ds[length - 1].getY());
		
		PrintWriter print = new PrintWriter(new FileWriter(new File(outFileName)));
		print.println(line1.toString());
		print.print(line2.toString());
		print.close();
	}
	
	public static void main(String[] args) throws IOException{
		String fileName1 = "5_USD_CAD.csv";
		String fileName2 = "5_USD_MXN.csv";
		int original = 1;
		
		String[] tempFile1 = fileName1.split("\\.");
		System.out.println(tempFile1.length);
		String[] tempFile2 = fileName2.split("\\.");
		String[] tempFile3 = tempFile2[0].split("_");
		String outFileName = "data/experiments/" + tempFile1[0] + "_" + tempFile3[2] + "_" + original + ".txt";
		fileName1 = "data/currency/" + fileName1;
		fileName2 = "data/currency/" + fileName2;
		System.out.println(new File(fileName1).getAbsolutePath());
		CurrencyPoint curPoint = new CurrencyPoint();
		curPoint.combineCurPoints(fileName1, fileName2, outFileName, original);
		
	}

}
