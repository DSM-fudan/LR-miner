package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class KMeansResultByCluster {
	
	public static void main(String[] args) throws IOException{
		String kMeansReuslt = "D:\\data\\KMeans_Result(k=20).txt";
		String kMeansCluster = "D:\\data\\KMeans_ResultByType(k=";
		int clusterNum = 20;
		kMeansCluster += clusterNum + ").txt";
		String[] lines = new String[clusterNum + 1];
		for(int i = 0; i <= clusterNum; i++){
			lines[i] = "";
		}
		BufferedReader brStation = new BufferedReader(new FileReader(kMeansReuslt));
		String tempLine = null;
		while((tempLine = brStation.readLine()) != null){
			String[] strs = tempLine.split("\t");
			int index = Integer.parseInt(strs[1]);
			lines[index] += strs[0] + "\t";
		}
		PrintWriter pw = new PrintWriter(new FileWriter(kMeansCluster));
		for(int i = 1; i <= clusterNum; i++){
			lines[i] = lines[i].substring(0, lines[i].length() - 1);
			pw.println(lines[i]);
		}
		brStation.close();
		pw.close();
		System.out.println("Finished!!!");
	}

}
