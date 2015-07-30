import java.awt.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.event.*;
import java.io.File;

public class Gui extends JFrame
{
	private static final int DEBUG = 2;
	
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 600;
	private static final int HEIGHT = 150;
	private static final int hGap = 5;
	private static final int vGap = 5;
	
	public static JProgressBar progressBar = new JProgressBar();
	
	private JLabel sleuthRootL, sleuthScenarioL, dataOutputL, numIterationsL;
	private JTextField sleuthRootTF, sleuthScenarioTF, dataOutputTF, numIterationsTF;
	private JButton sleuthRootB, sleuthScenarioB, dataOutputB, sleuthRunB;
	private JFileChooser sleuthRootFC, sleuthScenarioFC, dataOutputFC;
	
	//Button handlers:
	private sleuthRootButtonHandler sRBHandler;
	private sleuthScenarioButtonHandler sCBHandler;
	private dataOutputButtonHandler dOBHandler;
	private sleuthRunButtonHandler runHandler;
	
	public Gui()
	{	
		sleuthRootL = new JLabel("SLEUTH Root directory: ", SwingConstants.RIGHT);
		sleuthScenarioL = new JLabel("SLEUTH Scenario file location: ", SwingConstants.RIGHT);
		dataOutputL = new JLabel("SLEUTH Data output file location: ", SwingConstants.RIGHT);
		numIterationsL = new JLabel("Number of SLEUTH iterations: ", SwingConstants.RIGHT);
		
		sleuthRootFC = new JFileChooser();
		sleuthRootFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Dialog only needs to point to folders, not files.
		
		sleuthScenarioFC = new JFileChooser();
		
		dataOutputFC = new JFileChooser();
		dataOutputFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Dialog only needs to point to folders, not files.
		
		sleuthRootTF = new JTextField(1024);
		sleuthScenarioTF = new JTextField(1024);
		dataOutputTF = new JTextField(1024);
		numIterationsTF = new JTextField(10);
		
		//SPecify handlers for each button and add (register) ActionListeners to each button.
		sleuthRootB = new JButton("Browse");
		sleuthScenarioB = new JButton("Browse");
		dataOutputB = new JButton("Browse");
		sleuthRunB = new JButton("Run");
		
		sRBHandler = new sleuthRootButtonHandler();
		sCBHandler = new sleuthScenarioButtonHandler();
		dOBHandler = new dataOutputButtonHandler();
		runHandler = new sleuthRunButtonHandler();
		
		sleuthRootB.addActionListener(sRBHandler);
		sleuthScenarioB.addActionListener(sCBHandler);
		dataOutputB.addActionListener(dOBHandler);
		sleuthRunB.addActionListener(runHandler);
		
		JFrame primFrame = new JFrame();
		primFrame.setLayout(new BorderLayout());
		primFrame.setSize(WIDTH, HEIGHT);
		primFrame.setTitle("SLEUTHhound");
		primFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Container topPane =  getContentPane();
		topPane.setLayout(new GridLayout(5, 3, hGap, vGap));
		
		//Add things to the pane in the order you want them to appear (left to right, top to bottom)
		topPane.add(sleuthRootL);
		topPane.add(sleuthRootTF);
		topPane.add(sleuthRootB);
		
		topPane.add(sleuthScenarioL);
		topPane.add(sleuthScenarioTF);
		topPane.add(sleuthScenarioB);
		
		topPane.add(dataOutputL);
		topPane.add(dataOutputTF);
		topPane.add(dataOutputB);
		
		topPane.add(numIterationsL);
		topPane.add(numIterationsTF);
		topPane.add(new JLabel("")); // Empty spacer
		
		topPane.add(new JLabel("")); // Empty spacer
		topPane.add(sleuthRunB);
		
		JPanel statusBar = new JPanel();
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusBar.setLayout(new BorderLayout());
		statusBar.setSize(WIDTH, 16);
		statusBar.add(progressBar);
		
		primFrame.add(topPane);
		primFrame.add(statusBar, BorderLayout.SOUTH);	
		primFrame.setVisible(true);
		
		progressBar.setStringPainted(true);
		progressBar.setString("Awaiting user input...");
		//progressBar.setIndeterminate(true);
		
		if(DEBUG == 1){
			sleuthRootTF.setText("C:\\Users\\Chris\\Documents\\Sleuth\\SLEUTH3r");
			sleuthScenarioTF.setText("C:\\Users\\Chris\\Documents\\Sleuth\\SLEUTH3r\\scenario.SC060_1880_predict02");
			dataOutputTF.setText("C:\\Users\\Chris\\Documents");
			numIterationsTF.setText("1");
		}
		if(DEBUG == 2){
			sleuthRootTF.setText("C:\\Sleuth\\SLEUTH3r");
			sleuthScenarioTF.setText("C:\\Sleuth\\SLEUTH3r\\scenario.SC060_1880_predict02");
			dataOutputTF.setText("C:\\Sleuth");
			numIterationsTF.setText("1");
		}
	}
	
	private class sleuthRootButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int returnVal = sleuthRootFC.showOpenDialog(Gui.this);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File folder = sleuthRootFC.getSelectedFile();
				sleuthRootTF.setText(folder.getPath());
			}
		}
	}
	
	public class sleuthScenarioButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int returnVal = sleuthScenarioFC.showOpenDialog(Gui.this);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File file = sleuthScenarioFC.getSelectedFile();
				sleuthScenarioTF.setText(file.getPath());
			}
		}
	}
	
	public class dataOutputButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			int returnVal = dataOutputFC.showOpenDialog(Gui.this);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File folder = dataOutputFC.getSelectedFile();
				dataOutputTF.setText(folder.getPath());
			}
		}
	}
	
	public class sleuthRunButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String rootPath = sleuthRootTF.getText();
			String scenPath = sleuthScenarioTF.getText();
			String dataPath = dataOutputTF.getText();
			String nIterations = numIterationsTF.getText();
			
			String[] args = {rootPath, scenPath, dataPath, nIterations};
			
			updateStatusBar("Processing...");
			progressBar.setIndeterminate(true);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			ImageReader.main(args);
		}
	}
	
	public static void updateStatusBar(String str){
			progressBar.setString(str);
	}
	
	public static void setProgressBarDeterminate(boolean bool){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				progressBar.setIndeterminate(bool);
			}
		});
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				@SuppressWarnings("unused")
				Gui guiObj = new Gui();
			}
		});
	}
}
