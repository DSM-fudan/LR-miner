package cn.edu.fudan.dsm.tslrm.preprocess;

public class ClimateInfo {
	
	private String climateName;
	private String startTime;
	private String endTime;
	private int num;
	
	public ClimateInfo(String climateName, String startTime, String endTime,
			int num) {
		this.climateName = climateName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.num = num;
	}

	public ClimateInfo(String climateName, String startTime, int num) {
		this.climateName = climateName;
		this.startTime = startTime;
		this.num = num;
	}
	
	public ClimateInfo(){}

	public String getClimateName() {
		return climateName;
	}

	public void setClimateName(String climateName) {
		this.climateName = climateName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	public String toString(){
		return climateName + " " + startTime + " " + endTime + " " + num;
	}

}
