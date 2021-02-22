 import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.*;

/*
* @author Pascal Polchow, s0564053
* @version 1.0
*/

public class PuzzleApp {
	
	private static JFrame mainFrame;
	private static LinkedList<JTextField> pyramidTextFields; 
	private static LinkedList<JTextField> selectionTextFields;
	private static LinkedList<JTextField> difficultyTextFields;
	private static JLabel contentLabel;
	private static PlusPuzzle puzzle;
	private static int[] correctNumbers;
	private static int[] mixedNumbers;
	private static int pyramidPointer;
	private static int selectionPointer;
	private static int previousSelectionPointer;
	private static int difficultyPointer;
	private static String buttonHandlingIndicator; //zur Bestimmung der Schalterbelegung pro Screen
	private static String fieldLocationIndicator; //zur Bestimmung des Auswahlbereiches(pyramidFields, selectionsFields oder keines)

	public static void main(String[] args) {
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
		mainFrame.setTitle("PlusPuzzle");
		mainFrame.setUndecorated(true);
		mainFrame.setBounds(0, 0, 720, 480);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	private static void showStartScreen(){
		try {
			BufferedImage wonPicture = ImageIO.read(PuzzleApp.class.getResource("mayaStart.png"));
			contentLabel = new JLabel(new ImageIcon(wonPicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		difficultyTextFields = new LinkedList<JTextField>();
		contentLabel.setBounds(0,0, 720, 480);
		contentLabel.setVisible(true);
		mainFrame.getContentPane().add(contentLabel);
		generateDifficultyFields();
		difficultyPointer = 0;
		difficultyTextFields.getFirst().setBackground(new Color(22, 97, 218));
		buttonHandlingIndicator = "startScreen";
	}
	
	private static void generateDifficultyFields() {
		int xPos = 68;
		String text = "leicht";
		for (int i = 0; i < 3; i++) {
			if(i > 1) {
				text = "schwer";
			}
			JTextField difficultyField = new JTextField();
			difficultyField.setEditable(false);
			difficultyField.setBounds(xPos, 300, 150, 60);
			difficultyField.setHorizontalAlignment(JTextField.CENTER);
			difficultyField.setText(text);
			difficultyField.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
			difficultyField.setBackground(new Color(255,255,255));
			contentLabel.add(difficultyField);
			difficultyTextFields.add(difficultyField);
			xPos = xPos + 223;
			text = "mittel";
		}
	}
	
	private static int getDifficutlyFromPointer() {
		int difficulty = 5;
		switch (difficultyPointer) {
		case 1:
			difficulty = 50;
			break;
		case 2:
			difficulty = 500;
			break;
		}
		return difficulty;
	}
	
	private static void addGPIOButtonHandling() {
		final GpioController gpioCon = GpioFactory.getInstance();
		final GpioPinDigitalInput leftButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_24, PinPullResistance.PULL_DOWN); // wirinPi pin 24 ^= physischem pin #35
		final GpioPinDigitalInput rightButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_23, PinPullResistance.PULL_DOWN); // wirinPi pin 23 ^= physischem pin #33
		final GpioPinDigitalInput upButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_22, PinPullResistance.PULL_DOWN); // wirinPi pin 22 ^= physischem pin #31
		final GpioPinDigitalInput downButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_DOWN); // wirinPi pin 25 ^= physischem pin #37
		final GpioPinDigitalInput startButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_26, PinPullResistance.PULL_DOWN); // wirinPi pin 26 ^= physischem pin #32
		final GpioPinDigitalInput endButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_27, PinPullResistance.PULL_DOWN); // wirinPi pin 27 ^= physischem pin #36
		
		leftButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(buttonHandlingIndicator.equals("startScreen")) { 
						if(difficultyPointer > 0 ) {
							difficultyPointer = difficultyPointer - 1;
							setColorOfPointedField(difficultyTextFields.get(difficultyPointer), difficultyTextFields.get(difficultyPointer+1));							
						}
					}
					
					if(buttonHandlingIndicator.equals("gameScreen")) { 
						if(!fieldLocationIndicator.equals("selection")) {
							if(pyramidPointer-1 < 5) {
								pyramidPointer = pyramidTextFields.size() - 1;
								pyramidTextFields.get(4).setBackground(new Color(255,255,255));
							}
							else {
								pyramidPointer = pyramidPointer - 1;	
							}
							setColorOfPointedField(pyramidTextFields.get(pyramidPointer), pyramidTextFields.get(pyramidPointer+1));
						}
						else {							
							if(selectionPointer-5 < 0) {
								previousSelectionPointer = selectionPointer;
								selectionPointer = getNextEnabledSelectionFieldIndex(10);
								setColorOfPointedField(selectionTextFields.get(selectionPointer), selectionTextFields.get(previousSelectionPointer));
							}
							else {
								previousSelectionPointer = selectionPointer;
								selectionPointer = getNextEnabledSelectionFieldIndex(-5);
								setColorOfPointedField(selectionTextFields.get(selectionPointer), selectionTextFields.get(previousSelectionPointer));
							}
						}
					}
				}
			}
		});
		
		rightButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(buttonHandlingIndicator.equals("startScreen")) { 
						if(difficultyPointer < 2 ) { 
							difficultyPointer++;
							setColorOfPointedField(difficultyTextFields.get(difficultyPointer), difficultyTextFields.get(difficultyPointer-1));						
						}
					}
					if(buttonHandlingIndicator.equals("gameScreen")) { 
						if(!fieldLocationIndicator.equals("selection") ) {
							if(pyramidPointer+1 >= pyramidTextFields.size()) {
								pyramidPointer = 5;
								pyramidTextFields.getLast().setBackground(new Color(255,255,255));
							}
							else {
								pyramidPointer++;	
							}
							setColorOfPointedField(pyramidTextFields.get(pyramidPointer), pyramidTextFields.get(pyramidPointer-1));
						}
						else {
							if(selectionPointer+5 >= selectionTextFields.size()) {
								previousSelectionPointer = selectionPointer;
								selectionPointer = getNextEnabledSelectionFieldIndex(-10);
								setColorOfPointedField(selectionTextFields.get(selectionPointer), selectionTextFields.get(previousSelectionPointer));
							}
							else {
								previousSelectionPointer = selectionPointer;
								selectionPointer = getNextEnabledSelectionFieldIndex(5);
								setColorOfPointedField(selectionTextFields.get(selectionPointer), selectionTextFields.get(previousSelectionPointer));
							}
						}
					}
				}
			}
		});
		
		upButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(buttonHandlingIndicator.equals("gameScreen") && fieldLocationIndicator.equals("selection")) {		
						previousSelectionPointer = selectionPointer;
						selectionPointer = getNextEnabledSelectionFieldIndex(-1);
						setColorOfPointedField(selectionTextFields.get(selectionPointer), selectionTextFields.get(previousSelectionPointer));
					}
				}	
			}
		});
		
		downButton.addListener(new GpioPinListenerDigital() { 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(buttonHandlingIndicator.equals("gameScreen") && fieldLocationIndicator.equals("selection")) {	
						previousSelectionPointer = selectionPointer;
						selectionPointer = getNextEnabledSelectionFieldIndex(1);
						setColorOfPointedField(selectionTextFields.get(selectionPointer), selectionTextFields.get(previousSelectionPointer));
					}
				}
			}				
		});
		
		startButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(buttonHandlingIndicator.equals("startScreen")) {
						showOtherScreen("instructionScreen");						
					}
					if(buttonHandlingIndicator.equals("instructionScreen")) {
						if(!fieldLocationIndicator.equals("instruction")) { 
							showOtherScreen("gameScreen");
						}
						else {
							fieldLocationIndicator = "none";
						}
					}
					if(buttonHandlingIndicator.equals("gameScreen")) {
						if(fieldLocationIndicator.equals("selection")) {
							fieldLocationIndicator = "none";
							JTextField selected = selectionTextFields.get(selectionPointer);
							selected.setBackground(new Color(255,255,255));
							selected.setEnabled(false);
							int selectionValue = getIntFromString(selected.getText());
							if(pyramidTextFields.get(pyramidPointer).getText() != "") {
								enableDisabledSelectionField(pyramidTextFields.get(pyramidPointer).getText());
							}
							pyramidTextFields.get(pyramidPointer).setText("" + selectionValue);
						}
						if(fieldLocationIndicator.equals("pyramid")) {
							fieldLocationIndicator = "selection";
							selectionPointer = getFirstEnabledSelectionFieldIndex();
							JTextField selected = selectionTextFields.get(selectionPointer);
							selected.setBackground(new Color(22, 97, 218));
						}
						if(fieldLocationIndicator.equals("none")) {
							fieldLocationIndicator = "pyramid";
						}
						if(validatePyramidTextFields(puzzle) == true) {
							showOtherScreen("wonScreen");
						}
					}
					if(buttonHandlingIndicator.equals("wonScreen")) {
						if(!fieldLocationIndicator.equals("won")) { 						
							showOtherScreen("startScreen");
						}
						else {
							fieldLocationIndicator = "none";
						}
					}
				}
			}
		});
		
		endButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(buttonHandlingIndicator.equals("startScreen") || buttonHandlingIndicator.equals("wonScreen")) {
						String command = "java -jar GameInterface.jar"; 
						System.out.println("starte " + command);
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
					if(buttonHandlingIndicator.equals("gameScreen")) {
						showOtherScreen("startScreen");
					}
				}
			}
		});
	}
	
	private static void showInstructionScreen(){
		try {
			BufferedImage instructionPicture = ImageIO.read(PuzzleApp.class.getResource("mayaInstruction.png")); 
			contentLabel = new JLabel(new ImageIcon(instructionPicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		contentLabel.setBounds(0,0, 720, 480);
		contentLabel.setVisible(true);
		mainFrame.getContentPane().add(contentLabel);
		buttonHandlingIndicator = "instructionScreen";
		fieldLocationIndicator = "instruction";
	}
	
	private static void showGameScreen(int difficulty) {
		try {
			BufferedImage gamePicture = ImageIO.read(PuzzleApp.class.getResource("mayaGame.png"));
			contentLabel = new JLabel(new ImageIcon(gamePicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		puzzle = new PlusPuzzle(difficulty);
		fieldLocationIndicator = "none"; 
		int fontSize = getFontSizeFromPointer();
		correctNumbers = puzzle.getCorrectNumbers();
		mixedNumbers = puzzle.getMixedNumbers();
		contentLabel.setBounds(0,0, 720, 480);
		contentLabel.setVisible(true);
		generateSelectionFields(fontSize); //zur Anpassung der Schriftgröße an die Zahlenlänge
		generatePyramidFields(fontSize);
		fillPyramidBottomRow();
		pyramidPointer = 5;
		pyramidTextFields.get(pyramidPointer).setBackground(new Color(22, 97, 218));
		mainFrame.getContentPane().add(contentLabel);
		buttonHandlingIndicator = "gameScreen";
	}
	
	private static void showWonScreen() {
		try {
			BufferedImage wonPicture = ImageIO.read(PuzzleApp.class.getResource("mayaWon.png")); 
			contentLabel = new JLabel(new ImageIcon(wonPicture));
		} catch (IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		contentLabel.setBounds(0, 0, 720, 480);
		fieldLocationIndicator = "won";
		buttonHandlingIndicator = "wonScreen";
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
			int difficulty = getDifficutlyFromPointer();
			showGameScreen(difficulty);
			break;
		case "wonScreen":
			showWonScreen();
			break;			
		}
		mainFrame.revalidate();
		mainFrame.repaint();
	}
	
	private static void generatePyramidFields(int fontSize) {
		pyramidTextFields = new LinkedList<JTextField>();
		int xPosAddition = 30; 
		int yPosAddition = 300;
		for (int i = 0; i < correctNumbers.length; i++) {
			if(i == 5) {
				yPosAddition -= 68;
				xPosAddition = 68;
			}
			if(i == 9) {
				yPosAddition -= 68;
				xPosAddition = 105;
			}
			if(i == 12) {
				yPosAddition -=68;
				xPosAddition = 142;
			}
			if(i == 14) {
				yPosAddition -=68;
				xPosAddition = 180;
			}
			JTextField txtField = new JTextField();
			txtField.setFont(new Font("Comic Sans MS", Font.BOLD, fontSize));
			txtField.setEditable(false);
			txtField.setHorizontalAlignment(JTextField.CENTER);
			txtField.setBackground(new Color(255, 255, 255));
			txtField.setBounds(xPosAddition, yPosAddition, 60, 45);
			txtField.setHorizontalAlignment(JTextField.CENTER);
			txtField.setText("");
			contentLabel.add(txtField);
			pyramidTextFields.add(txtField);
			xPosAddition += 75;
		}
	}
	
	private static void fillPyramidBottomRow() {
		for (int i = 0; i < 5; i++) {
			pyramidTextFields.get(i).setText(" "+ correctNumbers[i]);
		}
	}
	
	private static void generateSelectionFields(int fontSize) {
		selectionTextFields = new LinkedList<JTextField>();
		int xPosAddition = 450;
		int yPosAddition = 30;
		for (int i = 0; i < mixedNumbers.length; i++) {
			if(i == 5 || i == 10) {
				xPosAddition += 90;
				yPosAddition = 30;
			}
			JTextField txtField = new JTextField();
			txtField.setFont(new Font("Comic Sans MS", Font.BOLD, fontSize));
			txtField.setEditable(false);
			txtField.setBackground(new Color(255, 255, 255));
			txtField.setBounds(xPosAddition, yPosAddition, 60, 45);
			txtField.setHorizontalAlignment(JTextField.CENTER);
			txtField.setText("" + mixedNumbers[i]);
			contentLabel.add(txtField);
			yPosAddition += 68;
			selectionTextFields.add(txtField);
		}
	}
	
	private static void setColorOfPointedField(JTextField currentField, JTextField previousField) {
		currentField.setBackground(new Color(22, 97, 218));
		previousField.setBackground(new Color(255,255,255));
	}

	private static int getIntFromString(String numberText) {
		int number = 0;
		char[] numberChars = numberText.toCharArray();
		for (int i = 0; i < numberChars.length ; i++) { 
			number += ((int)numberChars[i] - 48) * (int) Math.pow(10, numberChars.length-1 - i); //damit mehrstellige Zahlen richtig übertragen werden
		}
		return number;
	}
	
	private static int getFirstEnabledSelectionFieldIndex() {
		int index = 0;
		for (int i = 0; i < selectionTextFields.size(); i++) {
			if(selectionTextFields.get(i).isEnabled() == true) {
				return i;
			}
		}
		return index;
	}
	
	private static int getNextEnabledSelectionFieldIndex(int steps) { // so werden nur aktivierte selectionFields auswählbar + Randbehandlung
		int desiredPosition = selectionPointer + steps;
		int resultingPosition = desiredPosition;
		if(desiredPosition >= selectionTextFields.size()) {
			resultingPosition = desiredPosition - selectionTextFields.size(); 
		}
		if(desiredPosition < 0) {
			resultingPosition = selectionTextFields.size() - Math.abs(steps);
		}
		if(steps < 0) { 
			for (int i = resultingPosition; i >= 0; i--) {
				if(selectionTextFields.get(i).isEnabled()) {
					return i;
				}
				if(i == 0) { 
					i = selectionTextFields.size();
				}
			}
		}
		else {
			for (int i = resultingPosition; i < selectionTextFields.size(); i++) {
				if(selectionTextFields.get(i).isEnabled()) {
					return i;
				}
				if(i == selectionTextFields.size() - 1) { 
					i = -1;
				}
			}
		}
		return resultingPosition;
	}
	
	private static void enableDisabledSelectionField(String fieldText) {
		for (JTextField field : selectionTextFields) {
			if(field.getText().equals(fieldText) && field.isEnabled() == false) {
				field.setEnabled(true);
				break; 
			}
		}
	}
	
	private static boolean validatePyramidTextFields(PlusPuzzle puzzleObject) {
		int[] pyramidValues = new int[pyramidTextFields.size()];
		int numberCount = 0;
		for (int i = 0; i < pyramidTextFields.size(); i++) {
			String value = pyramidTextFields.get(i).getText().trim();
			if(value.equals("")) {
				break;
			}
			pyramidValues[i] = getIntFromString(value);
			numberCount++;
		}
		if(pyramidTextFields.size() == numberCount) { 
			boolean isRight = puzzleObject.compareNumbers(pyramidValues);
			if(isRight == true) {
				return true;
			}
		}
		return false;
	}
	
	private static int getFontSizeFromPointer() {
		int fontSize = 33;
		switch (difficultyPointer) {
		case 1:
			fontSize = 24;
			break;
		case 2:
			fontSize = 18;
			break;
		}
		return fontSize;
	}
}
