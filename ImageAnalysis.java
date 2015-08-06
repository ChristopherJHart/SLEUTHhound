import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;

public class ImageAnalysis {
	
	static PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));
	static PrintStream seedOut = null;
	static PrintStream seedIn = null;
	
	private static long totalAnalyzeImageRunTime;
	private static long totalOutputDataRunTime;
	private static long totalOpenImageRunTime;
	
	public void resetTotalAnalyzeTimer(){
		totalAnalyzeImageRunTime = 0;
	}
	
	public void resetTotalOutputDataTimer(){
		totalOutputDataRunTime = 0;
	}
	
	public static long getTotalAnalyzeTime(){
		return totalAnalyzeImageRunTime;
	}
	
	public static long getTotalOutputDataTime(){
		return totalOutputDataRunTime;
	}
	
	public static long getTotalOpenImageRunTime(){
		return totalOpenImageRunTime;
	}
	
	public static BufferedImage openImageFile(BufferedImage img, String outputDir, String endYear, String scenName){
		Timer openImageRunTime = new Timer(System.nanoTime());
		try{
			img = ImageIO.read(new File(outputDir + "\\" + scenName + "urban_" + endYear + ".gif"));
			totalOpenImageRunTime += openImageRunTime.time();
			return img;
		}
		catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static void analyzeImageFile(BufferedImage img, int iteration, String fullDataPath, String seedDataPath){
		Timer analyzeImageRunTime = new Timer(System.nanoTime());
		
		int[][][] pixelData = new int[img.getWidth()][img.getHeight()][3];
		
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				int[] rgbValues = getPixelData(img, x, y);
				
				for(int rgb = 0; rgb < rgbValues.length; rgb = rgb + 1){
					pixelData[x][y][rgb] = rgbValues[rgb];
				}
				int red = pixelData[x][y][0];
				int green = pixelData[x][y][1];
				int blue = pixelData[x][y][2];
				
				outputData(x, y, red, green, blue, iteration, seedDataPath);
			}
		}
		if(iteration == 0){
			seedOut.close();
		}
		totalAnalyzeImageRunTime += analyzeImageRunTime.time();
	}
	
	private static int[] getPixelData(BufferedImage img, int x, int y){		
		int argb = img.getRGB(x, y);
		
		int rgb[] = new int[]{
				(argb >> 16) & 0xff,
				(argb >> 8) & 0xff,
				(argb) & 0xff
		};
		return rgb;
	}
	
	public static void outputData(int x, int y, int red, int green, int blue, int iteration, String seedDataPath){
		Timer outputDataTimer = new Timer(System.nanoTime());
		
		String defaultOutput = (x + 1) + " " + (y + 1) + " " + (iteration + 1);
		if(green == red && green == blue){ // Filters out grey pixels
			return;
		}
		if(red == 20 && green == 52 && blue == 214){ // Filters out water pixels
			return;
		}
		if((green > 0 && red == 0 && blue == 0) || (red == 139 && blue == 0 && green == 0)){
			System.out.println(defaultOutput); // Freshly-urbanized grid
		}
		if(green == 209 && red == 249 && blue == 110){
			outputSeed(x, y, red, green, blue, iteration, seedDataPath);
			return;								// Seed grids (pre-urbanized)
		}
		totalOutputDataRunTime += outputDataTimer.time();
	}
	
	public static void outputSeed(int x, int y, int red, int green, int blue, int iteration, String seedDataPath){
		if(seedOut == null){
			initPrintStream(seedDataPath);
		}
		seedOut.println((x + 1) + " " + (y + 1) + " " + iteration);
	}
	
	public static void initPrintStream(String seedDataPath){
		try {
			seedOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(seedDataPath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
