package vork.server;

public class Timing {
	
	public static int seconds(int time) {
		return Constants.TPS * time;
	}
	
	public static int minutes(int time) {
		return seconds(time) * 60;
	}
	
	public static int hours(int time) {
		return minutes(time) * 60;
	}
}
