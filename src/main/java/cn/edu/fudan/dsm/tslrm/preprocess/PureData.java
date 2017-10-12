package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PureData {
	public static void main(String[] args) throws IOException{
//		PureData pd = new PureData();
//		pd.removeAbnormalData();
//		System.out.println("finished!");
		
		String inputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW_Equal_Purified-1960-2012.txt";
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String tempLine = null;
		int lineNum = 0;
		while((tempLine = br.readLine()) != null){
			String[] str = tempLine.split("\t");
			lineNum ++;
			if(str.length != 19360){
				System.out.println("Bad Result");
				System.out.println("LineNum = " + lineNum);
			}
		}
	}
	
	public void removeAbnormalData() throws IOException{
		String inputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW-1960-2012.txt";
		String outputFile = "D:\\data\\climateByType\\SURF_CLI_CHN_MUL_DAY-GST_ROW_Equal_Purified-1960-2012.txt";
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		String tempLine = null;
		
		while((tempLine = br.readLine()) != null){
			int dirtyDataNum = 0;
			String[] gstStr = tempLine.split("\t");			
			if(!gstStr[0].contains("equal")){
				continue;
			}
			
			StringBuilder sb = new StringBuilder(gstStr[0].split("equal")[0]);
			int[] gstNum = new int[gstStr.length - 1];
			for(int i = 0; i < gstNum.length; i++){
				gstNum[i] = Integer.parseInt(gstStr[i + 1]);
				if((gstNum[i] > 1000) || (gstNum[i] < -1000)){
					dirtyDataNum ++;
				}
			}
			if(dirtyDataNum > 1000){
				continue;
			}
			int index1 = -1, index2 = -1;
			boolean isFirst = false, isSecond = false;
			for(int i = 0; i < gstNum.length; i++){
				if((gstNum[i] > 1000) || (gstNum[i] < -1000)){
					if(!isFirst){
						index1 = i - 1;
						isFirst = true;
					}
				}else if(isFirst){
					index2 = i;
					isSecond = true;
				}else{
					sb.append("\t" + gstStr[i + 1]);
				}
				
				if(isFirst && isSecond){
					int equal = -1;
					if(index1 < 0){
						equal = gstNum[index2];
					}else{
						equal = (gstNum[index1] + gstNum[index2]) / 2;
					}
					
					for(int j = index1 + 1; j < index2; j++){
						sb.append("\t" + equal);
					}
					sb.append("\t" + gstNum[index2]);
					isFirst = false;
					isSecond = false;
				}
			}
			if(isFirst && !isSecond){
				for(int j = index1 + 1; j < gstNum.length; j++){
					sb.append("\t" + gstNum[index1]);
				}
			}
			
			pw.println(sb);
		}
		br.close();
		pw.close();
	}

}
