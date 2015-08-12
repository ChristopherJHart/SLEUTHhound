import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.imageio.ImageIO;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.awt.image.BufferedImage;

public class Main{
	
	public static long totalOutputRunTime = 0;
	public static long totalAnalyzeRunTime = 0;
	public static long totalSleuthRunTime = 0;
	private static long totalOutputDataRunTime;
	static PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));
	static Timer programTimer;
	
	public static void main(String[] args){
		programTimer = new Timer(System.nanoTime());
		
		String rootPath = args[0];
		String scenPath = args[1];
		String dataPath = args[2];
		String numIterations = args[3];
		
		BufferedImage img = null;
		int totalPixels = 0;
		
		String outputDataPath = dataPath + "\\SLEUTH RGB Output.txt";
		String seedDataPath = dataPath + "\\SLEUTH Seed Output.txt";
		File settingsFile = getSettingsDirectory();
		
		saveSettings(settingsFile, rootPath, scenPath, dataPath, numIterations);
		
		int iterations = Integer.parseInt(numIterations);
		String[] returnVals = parseScenFile(scenPath);
		String outputDir = returnVals[0];
		String endYear = returnVals[1];
		String scenName = returnVals[2];
		String inputDir = returnVals[3];
		
		totalPixels = getTotalPixels(inputDir);
		
		try {
			ensureFreeDiskSpace(numIterations, dataPath, totalPixels);
		} catch (IOException e) {
			Gui.updateStatusBar("Not enough disk space!");
			Gui.setProgressBarDeterminate(false);
			e.printStackTrace();
			return;
		}
		
		ensureOutputDirExists(outputDir);
		initPrintStream(outputDataPath, seedDataPath);
		
		Sleuth thread1 = new Sleuth(rootPath, scenPath, outputDir, endYear, scenName, iterations, img, outputDataPath, seedDataPath);
		thread1.execute();
	}
	
	public static void outputTimers(long sleuthTime, long totalPixels, int iterations){
		totalOutputDataRunTime = ImageAnalysis.getTotalOutputDataTime();
		totalAnalyzeRunTime = ImageAnalysis.getTotalAnalyzeTime();
		totalSleuthRunTime = sleuthTime;
		long totalRunTime = programTimer.time();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		
		console.println("Total output run time: " + formatter.format(totalOutputDataRunTime / 1000000000d) + " seconds.");
		console.println("Total analyze run time: " +formatter.format(totalAnalyzeRunTime / 1000000000d) + " seconds.");
		console.println("Total SLEUTH run time: " + formatter.format(totalSleuthRunTime / 1000000000d) + " seconds.");
		console.println("Total program run time: " + formatter.format(totalRunTime / 1000000000d) + " seconds.");
		
		System.out.println("Total output run time: " + formatter.format(totalOutputDataRunTime / 1000000000d) + " seconds.");
		System.out.println("Total analyze run time: " +formatter.format(totalAnalyzeRunTime / 1000000000d) + " seconds.");
		System.out.println("Total SLEUTH run time: " + formatter.format(totalSleuthRunTime / 1000000000d) + " seconds.");
		System.out.println("Total program run time: " + formatter.format(totalRunTime / 1000000000d) + " seconds.");
		
		Gui.progressBar.setIndeterminate(false);
		Gui.progressBar.setString("Done! Total execution time is " + formatter.format(totalRunTime / 1000000000d) + " seconds.");
		saveRuntimeFile(totalRunTime, totalPixels, iterations);
		System.out.close();
	}
	
	public static void initPrintStream(String fullDataPath, String seedDataPath){
		PrintStream out = null;
		File seedOutput = new File("SLEUTH Seed Output.txt");
		File dataOutput = new File("SLEUTH RGB Output.txt");
		if(!dataOutput.exists()){
			try {
				dataOutput.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!seedOutput.exists()){
			try {
				seedOutput.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fullDataPath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.setOut(out);
	}
	
	public static String[] parseScenFile(String scenPath){
		File file = new File(scenPath);
		BufferedReader reader = null;
		String[] returnVals = new String[4];
		String outputDir = null;
		String endYear = null;
		String inputDir = null;
		boolean foundOutputDir = false;
		boolean foundEndYear = false;
		boolean foundInputDir = false;
		try{
			reader = new BufferedReader(new FileReader(file));
			String text;
			while((text = reader.readLine()) != null){
				if(foundOutputDir && foundEndYear && foundInputDir){
					break;
				}
				if(!text.isEmpty()){
					if(text.charAt(0) != '#'){
						String substr = text.substring(0, text.indexOf('='));
						if(substr.trim().equals("INPUT_DIR")){
							inputDir = text.substring(text.indexOf('=') + 1).trim();
							foundInputDir = true;
						}
						if(substr.trim().equals("OUTPUT_DIR")){
							outputDir = text.substring(text.indexOf('=') + 1).trim();
							foundOutputDir = true;
						}
						if(substr.trim().equals("PREDICTION_STOP_DATE")){
							endYear = text.substring(text.indexOf('=') + 1);
							foundEndYear = true;
						}
					}
				}
			}
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try{
				if(reader != null){
					reader.close();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		String scenName = null;
		String str = scenPath.substring(scenPath.indexOf('.') + 1);
		char[] substr = str.toCharArray();
		int index = 0;
		for(int i = str.length() - 1; i > 0; i--){
			if(substr[i] == '_'){
				index = i;
				break;
			}
		}
		scenName = str.substring(0, index + 1);
		
		returnVals[0] = outputDir;
		returnVals[1] = endYear;
		returnVals[2] = scenName;
		returnVals[3] = inputDir;
		return returnVals;
	}
	
	public static void ensureOutputDirExists(String outputDir){
		Path path = Paths.get(outputDir);
		if(!Files.exists(path)){
			console.println("SLEUTH output directory does not exist. Creating directory to prevent SLEUTH errors.");
			new File(outputDir).mkdir();
		}
	}
	
	public static File getSettingsDirectory(){
		File settings = null;
		String userHome = System.getProperty("user.home");
		if(userHome == null){
			throw new IllegalStateException("user.home == null");
		}
		String settingsFile = userHome + "\\Documents\\SLEUTHhound\\config.ini";
		settings = new File(settingsFile);
		if(!settings.exists()){
			console.println("Settings file/directory does not exist. Creating now.");
			settings.getParentFile().mkdirs();
			try {
				settings.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return settings;
	}
	
	public static void saveSettings(File settingsFile, String rootPath, String scenPath, String dataPath, String numIterations){
		try(PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(settingsFile)))){
			out.println("ROOT_DIRECTORY=" + rootPath);
			out.println("SCENARIO_DIRECTORY=" + scenPath);
			out.println("OUTPUT_DIRECTORY=" + dataPath);
			out.println("NUMBER_OF_ITERATIONS=" + numIterations);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static File getRuntimeDirectory(){
		File runtime = null;
		String userHome = System.getProperty("user.home");
		if(userHome == null){
			throw new IllegalStateException("user.home == null");
		}
		String runtimeFile = userHome + "\\Documents\\SLEUTHhound\\runtime.ini";
		runtime = new File(runtimeFile);
		if(!runtime.exists()){
			console.println("Runtime file/directory does not exist. Creating now.");
			runtime.getParentFile().mkdirs();
			try {
				runtime.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return runtime;
	}
	
	public static void saveRuntimeFile(long totalRuntime, long totalPixels, int iterations){
		File runtimeFile = getRuntimeDirectory();
		long avgTimePerPixel = calculateTimePerPixel(totalRuntime, totalPixels, iterations);
		try(PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(runtimeFile, true)))){
			out.println(avgTimePerPixel);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static long calculateTimePerPixel(long totalRuntime, long totalPixels, int iterations){
		long runtimePerIteration = totalRuntime / iterations;
		long timePerPixel = runtimePerIteration / totalPixels;
		return timePerPixel;
	}
	
	public static long getCurrentAvgTimePerPixel(){
		BufferedReader reader = null;
		long currentAverage = 0;
		File runtimeFile = getRuntimeDirectory();
		try{
			reader = new BufferedReader(new FileReader(runtimeFile));
			String text;
			int i = 1;
			long sum = 0;
			while((text = reader.readLine()) != null){
				sum += Long.parseLong(text);
				i++;
			}
			currentAverage = sum / i;
		} catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try{
				if(reader != null){
					reader.close();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		return currentAverage;
	}
	
	public static void ensureFreeDiskSpace(String iterations, String dataPath, int totalPixels) throws IOException{
		File file = new File(dataPath);
		long totalFreeSpace = file.getUsableSpace();
		long numIterations = Integer.parseInt(iterations);
		long estimatedFileSize = (long)(numIterations * 1.15 * totalPixels);
		if(estimatedFileSize > totalFreeSpace){
			throw new IOException();
		}
	}
	
	public static int getTotalPixels(String inputDir){
		int totalPixels = 0;
		BufferedImage img = null;
		File file = new File(inputDir);
		File[] images = file.listFiles();
		try {
			img = ImageIO.read(images[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalPixels = img.getHeight() * img.getWidth();
		return totalPixels;
	}
}