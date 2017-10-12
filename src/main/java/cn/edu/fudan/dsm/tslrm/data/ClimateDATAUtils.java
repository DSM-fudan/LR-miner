package cn.edu.fudan.dsm.tslrm.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import math.geom2d.Point2D;

public class ClimateDATAUtils {

	public static Point2D[] readPointsFromFile(File file, int baseLine, int line) throws Exception{
		List<Point2D> ret = new ArrayList<Point2D>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String lineInfo = null;
		br.readLine();
		while((lineInfo = br.readLine()) != null){
			String[] infos = lineInfo.split("\t");
			Point2D tempPoint = new Point2D(Double.parseDouble(infos[baseLine]), Double.parseDouble(infos[line]));
			ret.add(tempPoint);
		}
		br.close();
		Point2D[] points = new Point2D[ret.size()];
		ret.toArray(points);
		return points;
	}
	
	public static Point2D[] readPointsFromTem(File file, int baseLine, int line) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String lineInfo = null;
		int lineNum = 0;
		String line1 = null;
		String line2 = null;
		while((lineInfo = br.readLine()) != null){
			if(lineNum == baseLine){
				line1 = lineInfo;
			}
			if(lineNum == line){
				line2 = lineInfo;
			}
			lineNum ++;
			if((line1 != null) && (line2 != null)){
				break;
			}
		}
		br.close();
		String[] pointX = line1.split("\t");
		String[] pointY = line2.split("\t");
		Point2D[] points = new Point2D[pointX.length];
		for(int i = 0; i < pointX.length; i++){
			points[i] = new Point2D(Double.parseDouble(pointX[i]), Double.parseDouble(pointY[i]));
		}
		return points;
	}
	
	public static Point2D[] readPointsFromFile(File file, int length) throws IOException {
        String s = FileUtils.readFileToString(file);
        String[] split = s.split("\n");
        String[] xs = split[0].split(",");
        String[] ys = split[1].split(",");        
        Point2D[] ret = new Point2D[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new Point2D(Double.parseDouble(xs[i]), Double.parseDouble(ys[i]));
//            System.out.println(i + "****" + ret[i].getX() + "******" + ret[i].getY() + "***");
        }
        return ret;
    }
}
