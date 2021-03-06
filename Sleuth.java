import java.util.List;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.SwingWorker;

public class Sleuth extends SwingWorker<Void, String>{
	
	static PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));
	
	private long totalSleuthRunTime = 0;
	private String rootPath;
	private String scenPath;
	private String outputDir;
	private String endYear;
	private String scenName;
	private int iterations;
	private BufferedImage img;
	private String fullDataPath;
	private String seedDataPath;
	private long totalImgPixels;
	private long iterationsRemaining;
	private long time; 
	
	public Sleuth(String rtPath, String scnPath, String outDir, String eYear, String scnName, int i, BufferedImage image, String data, String seed){
		rootPath = rtPath;
		scenPath = scnPath;
		outputDir = outDir;
		endYear = eYear;
		scenName = scnName;
		iterations = i;
		img = image;
		fullDataPath = data;
		seedDataPath = seed;
		time = 0;
	}
	
	public void addTotalTime(long time){
		totalSleuthRunTime += time;
	}
	
	public void runOtherProcs(int i){
		img = ImageAnalysis.openImageFile(img, outputDir, endYear, scenName);
		ImageAnalysis.analyzeImageFile(img, i, fullDataPath, seedDataPath);
	}
	
	public long getTotalTime(){
		return totalSleuthRunTime;
	}
	
	public String getRootPath(){
		return this.rootPath;
	}
	
	public void setRootPath(String str){
		rootPath = str;
	}
	
	public String getScenPath(){
		return this.scenPath;
	}
	
	public void setScenPath(String str){
		scenPath = str;
	}
	
	public String getOutputDir(){
		return this.outputDir;
	}
	
	public void setOutputDir(String str){
		outputDir = str;
	}
	
	public String getEndYear(){
		return this.endYear;
	}
	
	public void setEndYear(String str){
		endYear = str;
	}
	
	public int getIterations(){
		return this.iterations;
	}
	
	public void setIterations(int i){
		iterations = i;
	}
	
	public BufferedImage getImage(){
		return this.img;
	}
	
	public void setImage(BufferedImage image){
		img = image;
	}
	
	public String getScenName(){
		return this.scenName;
	}
	
	public void setScenName(String str){
		scenName = str;
	}
	
	public String getDataPath(){
		return fullDataPath;
	}
	
	public String getSeedPath(){
		return seedDataPath;
	}

	@Override
	protected Void doInBackground() throws Exception {
		Timer sleuthRunTime = new Timer(System.nanoTime());
		iterationsRemaining = iterations;
		for(int i = 0; i < iterations; i++){
			try {
				String output = "Running SLEUTH, iteration " + (i + 1) + " of " + iterations;
				
				Process proc = new ProcessBuilder(rootPath + "\\grow.exe", "predict", scenPath).start();
				BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				while(input.readLine() != null){
					//console.println(input.readLine());
				}
				proc.waitFor();
				img = ImageAnalysis.openImageFile(img, outputDir, endYear, scenName);
				this.setTotalPixels(img.getHeight() * img.getWidth());
				publish(output);
				publish(""+this.getTotalTimeRemaining());
				publish(""+this.getTotalEstimatedTime());
				ImageAnalysis.analyzeImageFile(img, i, fullDataPath, seedDataPath);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			iterationsRemaining--;
		}
		this.addTotalTime(sleuthRunTime.time());
	return null;
	}
	
	@Override
	protected void process(List<String> chunks){
		Gui.updateStatusBar(chunks.get(chunks.size()-3));
		long time = Long.parseLong(chunks.get(chunks.size() - 2));
		Gui.updateTimeLabel(parseNanoToNormal(time));
		long estTime = Long.parseLong(chunks.get(chunks.size() - 1));
		Gui.updateTotalTimeLabel(parseNanoToNormal(estTime));
	}
	
	public static String parseNanoToNormal(long nano){
		long second;
		long minute;
		long hour;
		
		String str = "";
		
		second = (nano / 1000000000) % 60;
		minute = (nano / ((long)1000000000 * 60)) % 60;
		hour = (nano / ((long)1000000000 * 60 * 60)) % 24;
		
		if(hour > 0){
			str = hour + "h, " + minute + "m, " + second + "s";
		}
		else if(minute > 0){
			str = minute + "m, " + second + "s";
		}
		else if(second > 0){
			str = second + "s";
		}
		
		return str;
	}
	
	@Override
	protected void done(){
		Gui.progressBar.setIndeterminate(false);
		NumberFormat formatter = new DecimalFormat("#0.00000");
		Gui.progressBar.setString("Done! Total execution time is " + formatter.format(totalSleuthRunTime / 1000000000d) + " seconds.");
		Gui.updateTimeLabel("0");
		Main.outputTimers(this.getTotalTime(), this.totalImgPixels, this.getIterations());
	}
	
	public long getTotalPixels(){
		return totalImgPixels;
	}
	
	public void setTotalPixels(long x){
		totalImgPixels = x;
	}
	
	public long getIterationsRemaining(){
		return this.iterationsRemaining;
	}
	
	public long getTotalEstimatedTime(){
		long time = (this.getTotalPixels() * Main.getCurrentAvgTimePerPixel()) * (this.getIterations());
		return time;
	}
	
	public long getTotalTimeRemaining(){
		time = (this.getTotalPixels() * Main.getCurrentAvgTimePerPixel()) * (this.getIterationsRemaining());
		return time;
	}
}
