package cn.edu.fudan.dsm.tslrm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import math.geom2d.Point2D;
import cn.edu.fudan.dsm.tslrm.data.DataGenerator;
import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;

public class RealDatasetTest {
	
	public void dataBaseAppro() throws Exception{
		String dataSource = "data\\wind.dat";
		String medialFile = "data\\appropriate.txt";
		String result = "data\\result.txt";
		
		String line = null;
		int[] equalLength = new int[12];
		int lineNum = 0;
		
		for(int baseDataIdx = 0; baseDataIdx < 12; baseDataIdx++){
			for(int repDataIndex = baseDataIdx + 1; repDataIndex < 12; repDataIndex++){
				BufferedReader read = new BufferedReader(new FileReader(dataSource));
				PrintWriter print = new PrintWriter(new FileWriter(medialFile));
				String line1 = "", line2 = "";
				lineNum = 0;
				while((line = read.readLine()) != null){
					lineNum++;
					String[] strs = line.split(" ");
					String[] strs2 = new String[15];
					int k = 0;
					for(int i = 0; i < strs.length; i++){
						if(!strs[i].equals("")){
							strs2[k] = strs[i];
							k++;
						}				
					}
					if(lineNum == 1){
						line1 += strs2[baseDataIdx + 3];
						line2 += strs2[repDataIndex + 3];
					}else{					
						line1 += "," + strs2[baseDataIdx + 3];
						line2 += "," + strs2[repDataIndex + 3];
					}
				}
				print.println(line1);
				print.print(line2);
				read.close();
				print.close();
				
				Point2D[] point2Ds = DataGenerator.readPointsFromFile(new File(medialFile));

		        double errorBound = 1;

		        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(point2Ds, errorBound);
		        miner.process();

		        List<PLASegment> segmentList = miner.buildAllSegments();

		        PLARegionSearch plaRegionSearch = new PLARegionSearch(point2Ds);
		        plaRegionSearch.errorBound = errorBound;

		        List<PLASegment> segs = SegmentUtils.filter(segmentList, 3, 3);
		        System.out.println("SegmentSize = " + segs.size());
		        plaRegionSearch.searchByBox2D(segs, 0.05);
		        int maxLength =  plaRegionSearch.finalLength;
		        System.out.println("MaxLength = " + maxLength);
		        equalLength[baseDataIdx] += maxLength;
		        equalLength[repDataIndex] += maxLength;
			}
		}
		
		PrintWriter resultPrint = new PrintWriter(new FileWriter(result));
		resultPrint.println(new Date());
		for(int i = 0; i < equalLength.length; i++){
			equalLength[i] = equalLength[i] / 11;
			System.out.println("i = " + equalLength[i]);
			resultPrint.println("i = " + equalLength[i]);
		}
		resultPrint.close();
		System.out.println("finished!!!!!!!!!!!!!!!");
	}
	
	public static void main(String[] args){
		RealDatasetTest test = new RealDatasetTest();
		try {
			test.dataBaseAppro();
		} catch (Exception e) {
			System.out.println("Error detected !!!!!!!!!!!!!!");
			e.printStackTrace();
		}
	}

}
