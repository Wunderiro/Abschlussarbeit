import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.*;

/*
* @author Pascal Polchow, s0564053
* @version 1.0
*/

public class KnackApp {

	private static JFrame mainFrame;
	private static LinkedList<JTextField> textFields;
	private static LinkedList<JLabel> indicatorLabels;
	private static LinkedList<JTextField> difficultyTextFields;
	private static int difficultyPointer;

	private static JLabel contentLabel;
	private static String screenIndicator;
	private static boolean startButtonPressed;

	private static SchlossKnack schlossKnack;
	private static int[] enteredNumber;
	private static int textFieldPointer;
	private static Color pointerColor = new Color(248,24,148);
	private static Color white = new Color(255,255,255);
	
	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					setMainFrame();		
					showStartScreen();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
		});
		addGPIOButtonHandling();
	}
	
	private static void setMainFrame() {
		mainFrame = new JFrame();
		mainFrame.setTitle("SchlossKnacker");
		mainFrame.setUndecorated(true);
		mainFrame.setBounds(0, 0, 720, 480);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	private static void showStartScreen() {
		try {
			BufferedImage startPicture = ImageIO.read(KnackApp.class.getResource("knackStart.png"));
			contentLabel = new JLabel(new ImageIcon(startPicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		startButtonPressed = false; //um automatisches Auslösen beim Betreten des Screens mit startButton zu vermeiden
		screenIndicator = "startScreen";
		difficultyTextFields = new LinkedList<JTextField>();
		contentLabel.setBounds(0,0, 720, 480);
		generateDifficultyFields(contentLabel);
		difficultyPointer = 0;
		difficultyTextFields.getFirst().setBackground(pointerColor);		
		contentLabel.setVisible(true);
		mainFrame.getContentPane().add(contentLabel);
	}
	
	private static void generateDifficultyFields(JLabel contentLabel) {
		int xPos = 67;
		String text = "leicht";
		for (int i = 0; i < 3; i++) {
			if(i>1) text = "schwer";
			JTextField difficultyField = new JTextField();
			difficultyField.setEditable(false);
			difficultyField.setBounds(xPos, 285, 150, 60);
			difficultyField.setHorizontalAlignment(JTextField.CENTER);
			difficultyField.setText(text);
			difficultyField.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
			difficultyField.setBackground(new Color(255,255,255));
			contentLabel.add(difficultyField);
			difficultyTextFields.add(difficultyField);
			xPos = xPos + 217;
			text = "mittel";
		}
	}
	
	private static int getDifficultyFromPointer() {
		int difficulty = 4;
		switch (difficultyPointer) {
		case 1:
			difficulty = 6;
			break;
		case 2:
			difficulty = 9;
			break;
		}
		return difficulty;
	}
	
	private static void addGPIOButtonHandling() {
		final GpioController gpioCon = GpioFactory.getInstance();
		final GpioPinDigitalInput rightButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_23, PinPullResistance.PULL_DOWN); // wirinPi pin 23 ^= physischem pin #33
		final GpioPinDigitalInput leftButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_24, PinPullResistance.PULL_DOWN); // wirinPi pin 24 ^= physischem pin #35
		final GpioPinDigitalInput upButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_22, PinPullResistance.PULL_DOWN); // wirinPi pin 22 ^= physischem pin #31
		final GpioPinDigitalInput downButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_DOWN); // wirinPi pin 25 ^= physischem pin #37
		final GpioPinDigitalInput startButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_26, PinPullResistance.PULL_DOWN); // wirinPi pin 26 ^= physischem pin #32
		final GpioPinDigitalInput endButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_27, PinPullResistance.PULL_DOWN); // wirinPi pin 27 ^= physischem pin #36

		rightButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(screenIndicator.equals("startScreen") && difficultyPointer < 2){
						difficultyPointer++;
						setColorOfPointedField(difficultyPointer, difficultyTextFields);	
					}	
					if(screenIndicator.equals("gameScreen") && textFieldPointer < 3){
						textFieldPointer++;
						setColorOfPointedField(textFieldPointer, textFields);
					}
				}
			}
		});
		
		leftButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(screenIndicator.equals("startScreen") && difficultyPointer > 0){
						difficultyPointer = difficultyPointer - 1;
						setColorOfPointedField(difficultyPointer, difficultyTextFields);
					}
					if(screenIndicator.equals("gameScreen") && textFieldPointer > 0){
						textFieldPointer = textFieldPointer - 1;
						setColorOfPointedField(textFieldPointer, textFields);
					}
				}
			}
		});
		
		upButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(screenIndicator.equals("gameScreen")){
						setValueOfPointedField(textFieldPointer,1);
						enteredNumber[textFieldPointer]++;
					}
				}
			}
		});
		
		downButton.addListener(new GpioPinListenerDigital() { 
			@Override
			public void handleGpioPinDigitalStateChangeEvent (GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(screenIndicator.equals("gameScreen")){
						setValueOfPointedField(textFieldPointer,0);
						enteredNumber[textFieldPointer] = enteredNumber[textFieldPointer] -1;
					}
				}
			}
		});
	
		startButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(screenIndicator.equals("startScreen")){
						if(startButtonPressed == true) {
							showOtherScreen("instructionScreen");
						}
						else {
							startButtonPressed = true;
						}
					}
					if(screenIndicator.equals("instructionScreen")){
						if(startButtonPressed == true) {
							showOtherScreen("gameScreen");
						}
						else {
							startButtonPressed = true;
						}
					}				
					if(screenIndicator.equals("gameScreen")){
						if(startButtonPressed == true) {
							evaluateEnteredDigits();
						}
						else {
							startButtonPressed = true;
						}
					}
					if(screenIndicator.equals("wonScreen")){
						if(startButtonPressed == true) {
							showOtherScreen("startScreen");
						}
						else {
							startButtonPressed = true;
						}
					}
				}
			}
		});
	
		endButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent (GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(screenIndicator.equals("startScreen") || screenIndicator.equals("wonScreen")) {
						String command = "java -jar GameInterface.jar";
						try {
							Process runningGame = Runtime.getRuntime().exec(command);			
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						JButton endElement = new JButton();
						contentLabel.add(endElement);
						Container frame = endElement.getParent();
						do
							frame = frame.getParent();
						while (frame instanceof JFrame == false);
						((JFrame)frame).dispose();
						System.exit(0);
					}
					if(screenIndicator.equals("gameScreen")) {
						showOtherScreen("startScreen");
					}
				}
			}
		});
	}
	
	private static void showInstructionScreen() {
		try {
			BufferedImage instructPicture = ImageIO.read(KnackApp.class.getResource("knackInstruct.png")); 
			contentLabel = new JLabel(new ImageIcon(instructPicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		contentLabel.setBounds(0,0, 720, 480);
		startButtonPressed = false;
		screenIndicator = "instructionScreen";
		contentLabel.setVisible(true);
		mainFrame.getContentPane().add(contentLabel);
	}
	
	private static void showGameScreen(int difficulty) {
		try {
			BufferedImage gamePicture = ImageIO.read(KnackApp.class.getResource("knackGame.png"));
			contentLabel = new JLabel(new ImageIcon(gamePicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		startButtonPressed = false;
		screenIndicator = "gameScreen";
		contentLabel.setBounds(0,0, 720, 480);
		generateTextFields();
		generateLabelFields();
		
		enteredNumber = new int[4];
		schlossKnack = new SchlossKnack(difficulty);
		textFieldPointer = 0;
		setColorOfPointedField(textFieldPointer, textFields);		
		mainFrame.getContentPane().add(contentLabel);
	}
	
	private static void showWonScreen() {
		try {
			BufferedImage wonPicture = ImageIO.read(KnackApp.class.getResource("knackWon.png"));
			contentLabel = new JLabel(new ImageIcon(wonPicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		contentLabel.setBounds(0,0, 720, 480);
		startButtonPressed = false;
		screenIndicator = "wonScreen";		
		contentLabel.setVisible(true);
		mainFrame.getContentPane().add(contentLabel);
	}
	
	private static void showOtherScreen(String screenName) {
		contentLabel.setVisible(false);
		mainFrame.getContentPane().remove(contentLabel);
		switch(screenName) {
		case "startScreen":
			showStartScreen();
			break;
		case "instructionScreen":
			showInstructionScreen();
			break;
		case "gameScreen":
			int difficulty = getDifficultyFromPointer();
			showGameScreen(difficulty);
			break;
		case "wonScreen":
			showWonScreen();
			break;			
		}
		mainFrame.revalidate();
		mainFrame.repaint();
	}
	
	private static void generateTextFields() {
		textFields = new LinkedList<JTextField>();
		int xPos = 77;
		for (int i = 0; i < 4; i++) {
			JTextField textField = new JTextField();
			textField.setFont(new Font("Comic Sans MS", Font.BOLD, 42));
			textField.setEditable(false);
			textField.setHorizontalAlignment(JTextField.CENTER);
			textField.setBackground(new Color(255, 255, 255));
			textField.setBounds(xPos, 168, 45, 45);
			textField.setText("0");
			contentLabel.add(textField);
			textFields.add(textField);
			xPos += 98;
		}
	}
	
	private static void generateLabelFields() {
		indicatorLabels = new LinkedList<JLabel>();
		int xPos = 37;
		for (int i = 0; i < 4; i++) {
			JLabel indicatorLabel = new JLabel();
			indicatorLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
			indicatorLabel.setHorizontalAlignment(JTextField.CENTER);
			indicatorLabel.setBounds(xPos, 300, 135, 90);
			indicatorLabel.setVisible(true);
			indicatorLabels.add(indicatorLabel);
			contentLabel.add(indicatorLabel);
			xPos += 97;
		}
	}
	
	private static void evaluateEnteredDigits(){
		schlossKnack.enterNumber(enteredNumber);
		if(schlossKnack.compareNumbers() == true) {
			showOtherScreen("wonScreen");
		}
		else {
			String[] comparison = schlossKnack.getComparisonAsSymbols();
			for (int i = 0; i < indicatorLabels.size(); i++) {
				indicatorLabels.get(i).setText(comparison[i]);
			}
		}
	}
	
	private static void setValueOfPointedField(int pointer, int direction) { 
		for (int i = 0; i < textFields.size(); i++) {
			int value = 0;
			JTextField textField = textFields.get(i);
			if(i == pointer) {
		    	if(direction == 1) {
			    	value = limitValue((int)textField.getText().charAt(0) - 47); //ASCII werte -48 +-1, damit identisch zu Binärwert
		    		textField.setText(""+(value)); //
		    	}
		    	else { 
		    		value = limitValue((int)textField.getText().charAt(0) - 49); // ursprünglicher Wert -1
		    		textField.setText(""+(value));
		    	}
			}
		}
	}
	
	private static int limitValue(int value) {
		int newVal = value;
		int maxValue = getDifficultyFromPointer();
		if(value > maxValue) newVal = maxValue;
		if(value < 0) newVal = 0;
		return newVal;
	}
	
	private static void setColorOfPointedField(int pointer, LinkedList<JTextField> textFields) {
		for (int i = 0; i < textFields.size(); i++) {
			JTextField textField = textFields.get(i);
			if(pointer == i) {
				textField.setBackground(pointerColor);
			} 
			else {
				textField.setBackground(white);
			}
		}
	}
}
