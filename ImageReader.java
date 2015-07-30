import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.awt.image.BufferedImage;

public class ImageReader{
	
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
		
		String outputDataPath = dataPath + "\\SLEUTH RGB Output.txt";
		String seedDataPath = dataPath + "\\SLEUTH Seed Output.txt";
		
		int iterations = Integer.parseInt(numIterations);
		String[] returnVals = parseScenFile(scenPath);
		String outputDir = returnVals[0];
		String endYear = returnVals[1];
		String scenName = returnVals[2];
		
		initPrintStream(outputDataPath, seedDataPath);
		
		Sleuth thread1 = new Sleuth(rootPath, scenPath, outputDir, endYear, scenName, iterations, img, outputDataPath, seedDataPath);
		thread1.execute();
	}
	
	public static void outputTimers(long sleuthTime){
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
		String[] returnVals = new String[3];
		String outputDir = null;
		String endYear = null;
		boolean foundOutputDir = false;
		boolean foundEndYear = false;
		try{
			reader = new BufferedReader(new FileReader(file));
			String text;
			while((text = reader.readLine()) != null){
				if(foundOutputDir && foundEndYear){
					break;
				}
				if(!text.isEmpty()){
					if(text.charAt(0) != '#'){
						String substr = text.substring(0, text.indexOf('='));
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
		return returnVals;
	}
}