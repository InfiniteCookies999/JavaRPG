package vork.server.game.scripts;

public class S2Logger {
	
	private String fileName;
	private int numErrors = 0;
	
	public S2Logger(String fileName) {
		this.fileName = fileName;
	}
	
	public void error(S2Location location, String message) {
		System.out.println(fileName + ":" + location.getLineNumber() + ": Error: " + message);
		++numErrors;
	}
	
	public boolean hasErrors() {
		return numErrors > 0;
	}
}
