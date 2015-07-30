public class Timer {
	
	private long startTime;
	private long endTime;
	
	public Timer(){
		
	}
	public Timer(long sTime){
		startTime = sTime;
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	public void setStartTime(long sTime){
		startTime = sTime;
	}
	
	public long getEndTime(){
		return endTime;
	}
	
	public void setEndTime(long eTime){
		endTime = eTime;
	}
	
	public long time(){
		long endTime = System.nanoTime();
		long total = endTime - this.startTime;
		long totalInSeconds = total;
		return totalInSeconds;
	}
	
	public String outputTime(String str){
		String s = str + this.time();
		return s;
	}
}
