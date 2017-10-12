package cn.edu.fudan.dsm.tslrm.preprocess;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClimateFileNameFilter implements FilenameFilter{
	
	private String suffix;
	public ClimateFileNameFilter(String suffix){
		this.suffix = suffix;
	}
	
	public boolean accept(File dir, String name){
		return name.startsWith(suffix);
	}

}
